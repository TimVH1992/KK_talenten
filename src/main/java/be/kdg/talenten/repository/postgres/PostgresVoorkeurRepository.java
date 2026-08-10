package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.VoorkeurRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgresVoorkeurRepository implements VoorkeurRepository {

    private final LeerlingRepository leerlingRepository;
    private final IngerichtTalentRepository ingerichtTalentRepository;

    public PostgresVoorkeurRepository() {
        this(
                new PostgresLeerlingRepository(),
                new PostgresIngerichtTalentRepository()
        );
    }

    public PostgresVoorkeurRepository(
            LeerlingRepository leerlingRepository,
            IngerichtTalentRepository ingerichtTalentRepository
    ) {
        if (leerlingRepository == null || ingerichtTalentRepository == null) {
            throw new IllegalArgumentException(
                    "Repositories mogen niet null zijn"
            );
        }

        this.leerlingRepository = leerlingRepository;
        this.ingerichtTalentRepository = ingerichtTalentRepository;
    }

    @Override
    public Voorkeur save(Voorkeur voorkeur) {
        valideerVoorkeurVoorOpslag(voorkeur);

        String sql = """
                INSERT INTO voorkeuren (
                    voorkeur_nummer,
                    leerling_id,
                    talenten_periode_id,
                    ingericht_talent_id
                )
                VALUES (?, ?, ?, ?)
                RETURNING voorkeur_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, voorkeur.getVoorkeurNummer());
            statement.setLong(2, voorkeur.getLeerling().getId());
            statement.setLong(3, voorkeur.getTalentenPeriode().getId());
            statement.setLong(4, voorkeur.getIngerichtTalent().getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException(
                            "PostgreSQL gaf geen voorkeur_id terug"
                    );
                }

                return new Voorkeur(
                        resultSet.getLong("voorkeur_id"),
                        voorkeur.getLeerling(),
                        voorkeur.getTalentenPeriode(),
                        voorkeur.getIngerichtTalent(),
                        voorkeur.getVoorkeurNummer()
                );
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De voorkeur kon niet opgeslagen worden",
                    e
            );
        }
    }

    @Override
    public List<Voorkeur> zoekVoorPeriode(TalentenPeriode periode) {
        valideerOpgeslagenPeriode(periode);

        String sql = """
                SELECT
                    v.voorkeur_id,
                    v.voorkeur_nummer,

                    l.leerling_id,
                    l.voornaam,
                    l.achternaam,

                    k.klas_id,
                    k.klas_naam,
                    k.leerjaar,
                    k.doelgroep AS klas_doelgroep,

                    it.ingericht_talent_id,
                    it.naam AS ingericht_talent_naam,
                    it.omschrijving AS ingericht_talent_omschrijving,
                    it.maximum_capaciteit,
                    it.doelgroep AS talent_doelgroep,
                    it.actief AS ingericht_talent_actief,

                    t.talent_id,
                    t.naam AS talent_naam,
                    t.beschrijving

                FROM voorkeuren v
                JOIN leerlingen l
                    ON l.leerling_id = v.leerling_id
                JOIN klassen k
                    ON k.klas_id = l.klas_id
                JOIN ingerichte_talenten it
                    ON it.ingericht_talent_id = v.ingericht_talent_id
                JOIN talenten t
                    ON t.talent_id = it.talent_id
                WHERE v.talenten_periode_id = ?
                ORDER BY l.leerling_id, v.voorkeur_nummer
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding()) {

            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                    zoekLeerkrachtenVoorPeriode(
                            connection,
                            periode.getId()
                    );

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, periode.getId());

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Voorkeur> voorkeuren = new ArrayList<>();

                    while (resultSet.next()) {
                        voorkeuren.add(
                                maakVoorkeurVoorPeriode(
                                        resultSet,
                                        periode,
                                        leerkrachtenPerIngerichtTalent
                                )
                        );
                    }

                    return voorkeuren;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De voorkeuren konden niet opgehaald worden",
                    e
            );
        }
    }

    @Override
    public List<Voorkeur> zoekVoorLeerlingEnPeriode(
            Leerling leerling,
            TalentenPeriode periode
    ) {
        valideerOpgeslagenLeerling(leerling);
        valideerOpgeslagenPeriode(periode);

        String sql = """
                SELECT
                    v.voorkeur_id,
                    v.voorkeur_nummer,

                    it.ingericht_talent_id,
                    it.naam AS ingericht_talent_naam,
                    it.omschrijving AS ingericht_talent_omschrijving,
                    it.maximum_capaciteit,
                    it.doelgroep AS talent_doelgroep,
                    it.actief AS ingericht_talent_actief,

                    t.talent_id,
                    t.naam AS talent_naam,
                    t.beschrijving

                FROM voorkeuren v
                JOIN ingerichte_talenten it
                    ON it.ingericht_talent_id = v.ingericht_talent_id
                JOIN talenten t
                    ON t.talent_id = it.talent_id
                WHERE v.leerling_id = ?
                  AND v.talenten_periode_id = ?
                ORDER BY v.voorkeur_nummer
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding()) {

            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                    zoekLeerkrachtenVoorLeerlingEnPeriode(
                            connection,
                            leerling.getId(),
                            periode.getId()
                    );

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, leerling.getId());
                statement.setLong(2, periode.getId());

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Voorkeur> voorkeuren = new ArrayList<>();

                    while (resultSet.next()) {
                        voorkeuren.add(
                                maakVoorkeurVoorLeerlingEnPeriode(
                                        resultSet,
                                        leerling,
                                        periode,
                                        leerkrachtenPerIngerichtTalent
                                )
                        );
                    }

                    return voorkeuren;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De voorkeuren van de leerling konden niet opgehaald worden",
                    e
            );
        }
    }

    private Voorkeur maakVoorkeurVoorPeriode(
            ResultSet resultSet,
            TalentenPeriode periode,
            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent
    ) throws SQLException {

        Klas klas = new Klas(
                resultSet.getLong("klas_id"),
                resultSet.getString("klas_naam"),
                periode.getSchooljaar(),
                resultSet.getInt("leerjaar"),
                Doelgroep.valueOf(
                        resultSet.getString("klas_doelgroep")
                )
        );

        Leerling leerling = new Leerling(
                resultSet.getLong("leerling_id"),
                resultSet.getString("voornaam"),
                resultSet.getString("achternaam"),
                klas
        );

        Talent talent = maakTalent(resultSet);

        IngerichtTalent ingerichtTalent =
                maakIngerichtTalent(
                        resultSet,
                        talent,
                        periode,
                        leerkrachtenPerIngerichtTalent
                );

        return new Voorkeur(
                resultSet.getLong("voorkeur_id"),
                leerling,
                periode,
                ingerichtTalent,
                resultSet.getInt("voorkeur_nummer")
        );
    }

    private Voorkeur maakVoorkeurVoorLeerlingEnPeriode(
            ResultSet resultSet,
            Leerling leerling,
            TalentenPeriode periode,
            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent
    ) throws SQLException {

        Talent talent = maakTalent(resultSet);

        IngerichtTalent ingerichtTalent =
                maakIngerichtTalent(
                        resultSet,
                        talent,
                        periode,
                        leerkrachtenPerIngerichtTalent
                );

        return new Voorkeur(
                resultSet.getLong("voorkeur_id"),
                leerling,
                periode,
                ingerichtTalent,
                resultSet.getInt("voorkeur_nummer")
        );
    }

    private Talent maakTalent(ResultSet resultSet)
            throws SQLException {

        return new Talent(
                resultSet.getLong("talent_id"),
                resultSet.getString("talent_naam"),
                resultSet.getString("beschrijving")
        );
    }

    private IngerichtTalent maakIngerichtTalent(
            ResultSet resultSet,
            Talent talent,
            TalentenPeriode periode,
            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent
    ) throws SQLException {

        long ingerichtTalentId =
                resultSet.getLong("ingericht_talent_id");

        List<Leerkracht> leerkrachten =
                zoekLeerkrachtenInMap(
                        leerkrachtenPerIngerichtTalent,
                        ingerichtTalentId
                );

        return new IngerichtTalent(
                ingerichtTalentId,
                talent,
                periode,
                resultSet.getString("ingericht_talent_naam"),
                resultSet.getString("ingericht_talent_omschrijving"),
                resultSet.getInt("maximum_capaciteit"),
                Doelgroep.valueOf(
                        resultSet.getString("talent_doelgroep")
                ),
                leerkrachten,
                resultSet.getBoolean("ingericht_talent_actief")
        );
    }

    private Map<Long, List<Leerkracht>> zoekLeerkrachtenVoorPeriode(
            Connection connection,
            long periodeId
    ) throws SQLException {

        String sql = """
                SELECT
                    itl.ingericht_talent_id,
                    l.leerkracht_id,
                    l.voornaam,
                    l.achternaam
                FROM ingericht_talent_leerkrachten itl
                JOIN ingerichte_talenten it
                    ON it.ingericht_talent_id = itl.ingericht_talent_id
                JOIN leerkrachten l
                    ON l.leerkracht_id = itl.leerkracht_id
                WHERE it.talenten_periode_id = ?
                ORDER BY
                    itl.ingericht_talent_id,
                    l.leerkracht_id
                """;

        Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, periodeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long ingerichtTalentId =
                            resultSet.getLong("ingericht_talent_id");

                    Leerkracht leerkracht =
                            maakLeerkracht(resultSet);

                    leerkrachtenPerIngerichtTalent
                            .computeIfAbsent(
                                    ingerichtTalentId,
                                    id -> new ArrayList<>()
                            )
                            .add(leerkracht);
                }
            }
        }

        return leerkrachtenPerIngerichtTalent;
    }

    private Map<Long, List<Leerkracht>> zoekLeerkrachtenVoorLeerlingEnPeriode(
            Connection connection,
            long leerlingId,
            long periodeId
    ) throws SQLException {

        String sql = """
                SELECT DISTINCT
                    itl.ingericht_talent_id,
                    l.leerkracht_id,
                    l.voornaam,
                    l.achternaam
                FROM voorkeuren v
                JOIN ingericht_talent_leerkrachten itl
                    ON itl.ingericht_talent_id = v.ingericht_talent_id
                JOIN leerkrachten l
                    ON l.leerkracht_id = itl.leerkracht_id
                WHERE v.leerling_id = ?
                  AND v.talenten_periode_id = ?
                ORDER BY
                    itl.ingericht_talent_id,
                    l.leerkracht_id
                """;

        Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, leerlingId);
            statement.setLong(2, periodeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long ingerichtTalentId =
                            resultSet.getLong("ingericht_talent_id");

                    Leerkracht leerkracht =
                            maakLeerkracht(resultSet);

                    leerkrachtenPerIngerichtTalent
                            .computeIfAbsent(
                                    ingerichtTalentId,
                                    id -> new ArrayList<>()
                            )
                            .add(leerkracht);
                }
            }
        }

        return leerkrachtenPerIngerichtTalent;
    }

    private Leerkracht maakLeerkracht(ResultSet resultSet)
            throws SQLException {

        return new Leerkracht(
                resultSet.getLong("leerkracht_id"),
                resultSet.getString("voornaam"),
                resultSet.getString("achternaam")
        );
    }

    private List<Leerkracht> zoekLeerkrachtenInMap(
            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent,
            long ingerichtTalentId
    ) {
        List<Leerkracht> leerkrachten =
                leerkrachtenPerIngerichtTalent.get(
                        ingerichtTalentId
                );

        if (leerkrachten == null || leerkrachten.isEmpty()) {
            throw new IllegalStateException(
                    "Ingericht talent "
                            + ingerichtTalentId
                            + " heeft geen leerkracht in de databank"
            );
        }

        return leerkrachten;
    }

    private void valideerVoorkeurVoorOpslag(Voorkeur voorkeur) {
        if (voorkeur == null) {
            throw new IllegalArgumentException(
                    "Voorkeur mag niet null zijn"
            );
        }

        if (voorkeur.getLeerling().getId() == null) {
            throw new IllegalArgumentException(
                    "De leerling moet eerst opgeslagen zijn"
            );
        }

        if (voorkeur.getTalentenPeriode().getId() == null) {
            throw new IllegalArgumentException(
                    "De talentenperiode moet eerst opgeslagen zijn"
            );
        }

        if (voorkeur.getIngerichtTalent().getId() == null) {
            throw new IllegalArgumentException(
                    "Het ingerichte talent moet eerst opgeslagen zijn"
            );
        }
    }

    private void valideerOpgeslagenLeerling(Leerling leerling) {
        if (leerling == null) {
            throw new IllegalArgumentException(
                    "De leerling mag niet null zijn"
            );
        }

        if (leerling.getId() == null) {
            throw new IllegalArgumentException(
                    "De leerling moet eerst opgeslagen zijn"
            );
        }
    }

    private void valideerOpgeslagenPeriode(TalentenPeriode periode) {
        if (periode == null) {
            throw new IllegalArgumentException(
                    "De periode mag niet null zijn"
            );
        }

        if (periode.getId() == null) {
            throw new IllegalArgumentException(
                    "De periode moet eerst opgeslagen zijn"
            );
        }
    }
}