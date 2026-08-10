package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.ToewijzingRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgresToewijzingRepository implements ToewijzingRepository {

    private final LeerlingRepository leerlingRepository;
    private final IngerichtTalentRepository ingerichtTalentRepository;

    public PostgresToewijzingRepository() {
        this(
                new PostgresLeerlingRepository(),
                new PostgresIngerichtTalentRepository()
        );
    }

    public PostgresToewijzingRepository(
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
    public Toewijzing save(Toewijzing toewijzing) {
        valideerToewijzingVoorOpslag(toewijzing);

        String sql = """
                INSERT INTO toewijzingen (
                    toewijzings_type,
                    voorkeur_nummer,
                    leerling_id,
                    talenten_periode_id,
                    ingericht_talent_id,
                    toegewezen_op,
                    gewijzigd_op
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING toewijzing_id, toegewezen_op, gewijzigd_op
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            vulInsertStatement(statement, toewijzing);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException(
                            "PostgreSQL gaf geen toewijzing_id terug"
                    );
                }

                Timestamp gewijzigdOp =
                        resultSet.getTimestamp("gewijzigd_op");

                return new Toewijzing(
                        resultSet.getLong("toewijzing_id"),
                        toewijzing.getLeerling(),
                        toewijzing.getIngerichtTalent(),
                        toewijzing.getToewijzingsType(),
                        resultSet.getTimestamp("toegewezen_op").toLocalDateTime(),
                        gewijzigdOp == null
                                ? null
                                : gewijzigdOp.toLocalDateTime(),
                        toewijzing.getVoorkeurNummer()
                );
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De toewijzing kon niet opgeslagen worden",
                    e
            );
        }
    }

    @Override
    public List<Toewijzing> zoekVoorPeriode(TalentenPeriode periode) {
        valideerOpgeslagenPeriode(periode);

        String sql = """
                SELECT
                    tw.toewijzing_id,
                    tw.toewijzings_type,
                    tw.voorkeur_nummer,
                    tw.leerling_id,
                    tw.toegewezen_op,
                    tw.gewijzigd_op,

                    l.voornaam,
                    l.achternaam,
                    l.klas_id,

                    k.klas_naam,
                    k.leerjaar,
                    k.doelgroep AS klas_doelgroep,

                    it.ingericht_talent_id,
                    it.naam AS ingericht_talent_naam,
                    it.omschrijving AS ingericht_talent_omschrijving,
                    it.maximum_capaciteit,
                    it.doelgroep AS talent_doelgroep,
                    it.actief AS ingericht_talent_actief,
                    it.talent_id,

                    t.naam AS talent_naam,
                    t.beschrijving
                FROM toewijzingen tw
                JOIN leerlingen l
                    ON l.leerling_id = tw.leerling_id
                JOIN klassen k
                    ON k.klas_id = l.klas_id
                JOIN ingerichte_talenten it
                    ON it.ingericht_talent_id = tw.ingericht_talent_id
                JOIN talenten t
                    ON t.talent_id = it.talent_id
                WHERE tw.talenten_periode_id = ?
                ORDER BY tw.leerling_id
                """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding()) {

            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                    zoekLeerkrachtenVoorPeriode(
                            connection,
                            periode.getId()
                    );

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setLong(1, periode.getId());

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    List<Toewijzing> toewijzingen =
                            new ArrayList<>();

                    while (resultSet.next()) {
                        toewijzingen.add(
                                maakToewijzingVoorPeriode(
                                        resultSet,
                                        periode,
                                        leerkrachtenPerIngerichtTalent
                                )
                        );
                    }

                    return toewijzingen;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De toewijzingen konden niet opgehaald worden voor periode: "
                            + periode.getId(),
                    e
            );
        }
    }

    @Override
    public Toewijzing zoekToewijzingVoorLeerlingEnPeriode(
            Leerling leerling,
            TalentenPeriode periode
    ) {
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

        valideerOpgeslagenPeriode(periode);

        String sql = """
                SELECT
                    tw.toewijzing_id,
                    tw.toewijzings_type,
                    tw.voorkeur_nummer,
                    tw.toegewezen_op,
                    tw.gewijzigd_op,

                    it.ingericht_talent_id,
                    it.naam AS ingericht_talent_naam,
                    it.omschrijving AS ingericht_talent_omschrijving,
                    it.maximum_capaciteit,
                    it.doelgroep AS talent_doelgroep,
                    it.actief AS ingericht_talent_actief,
                    it.talent_id,

                    t.naam AS talent_naam,
                    t.beschrijving
                FROM toewijzingen tw
                JOIN ingerichte_talenten it
                    ON it.ingericht_talent_id = tw.ingericht_talent_id
                JOIN talenten t
                    ON t.talent_id = it.talent_id
                WHERE tw.leerling_id = ?
                  AND tw.talenten_periode_id = ?
                """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding()) {

            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                    zoekLeerkrachtenVoorPeriode(
                            connection,
                            periode.getId()
                    );

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setLong(1, leerling.getId());
                statement.setLong(2, periode.getId());

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    if (!resultSet.next()) {
                        return null;
                    }

                    Toewijzing toewijzing =
                            maakToewijzingVoorLeerlingEnPeriode(
                                    resultSet,
                                    leerling,
                                    periode,
                                    leerkrachtenPerIngerichtTalent
                            );

                    if (resultSet.next()) {
                        throw new IllegalStateException(
                                "Er bestaan meerdere toewijzingen voor dezelfde leerling en periode"
                        );
                    }

                    return toewijzing;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De toewijzing kon niet opgehaald worden voor leerling: "
                            + leerling.getId(),
                    e
            );
        }
    }

    @Override
    public int telToewijzingenVoorIngerichtTalent(
            IngerichtTalent ingerichtTalent
    ) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException(
                    "Het ingerichte talent mag niet null zijn"
            );
        }

        if (ingerichtTalent.getId() == null) {
            throw new IllegalArgumentException(
                    "Het ingerichte talent moet eerst opgeslagen zijn"
            );
        }

        String sql = """
                SELECT COUNT(*) AS aantal
                FROM toewijzingen
                WHERE ingericht_talent_id = ?
                """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, ingerichtTalent.getId());

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new IllegalStateException(
                            "PostgreSQL gaf geen aantal toewijzingen terug"
                    );
                }

                return resultSet.getInt("aantal");
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Het aantal toewijzingen kon niet opgehaald worden",
                    e
            );
        }
    }

    @Override
    public Toewijzing update(Toewijzing toewijzing) {
        valideerToewijzingVoorOpslag(toewijzing);

        if (toewijzing.getId() == null) {
            throw new IllegalArgumentException(
                    "De toewijzing moet eerst opgeslagen zijn"
            );
        }

        String sql = """
                UPDATE toewijzingen
                SET toewijzings_type = ?,
                    voorkeur_nummer = ?,
                    talenten_periode_id = ?,
                    ingericht_talent_id = ?,
                    gewijzigd_op = ?
                WHERE toewijzing_id = ?
                """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    toewijzing.getToewijzingsType().name()
            );

            zetVoorkeurNummer(
                    statement,
                    2,
                    toewijzing.getVoorkeurNummer()
            );

            statement.setLong(
                    3,
                    toewijzing.getIngerichtTalent()
                            .getTalentenPeriode()
                            .getId()
            );

            statement.setLong(
                    4,
                    toewijzing.getIngerichtTalent().getId()
            );

            if (toewijzing.getGewijzigdOp() == null) {
                statement.setNull(
                        5,
                        Types.TIMESTAMP
                );
            } else {
                statement.setTimestamp(
                        5,
                        Timestamp.valueOf(
                                toewijzing.getGewijzigdOp()
                        )
                );
            }

            statement.setLong(
                    6,
                    toewijzing.getId()
            );

            int aantalGewijzigdeRijen =
                    statement.executeUpdate();

            if (aantalGewijzigdeRijen == 0) {
                throw new IllegalStateException(
                        "Geen toewijzing gevonden met id: "
                                + toewijzing.getId()
                );
            }

            if (aantalGewijzigdeRijen > 1) {
                throw new IllegalStateException(
                        "Er werden meerdere toewijzingen gewijzigd voor id: "
                                + toewijzing.getId()
                );
            }

            return toewijzing;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De toewijzing kon niet gewijzigd worden",
                    e
            );
        }
    }

    @Override
    public void saveAll(List<Toewijzing> toewijzingen) {
        if (toewijzingen == null) {
            throw new IllegalArgumentException(
                    "De lijst met toewijzingen mag niet null zijn"
            );
        }

        if (toewijzingen.isEmpty()) {
            return;
        }

        for (Toewijzing toewijzing : toewijzingen) {
            valideerToewijzingVoorOpslag(toewijzing);
        }

        String sql = """
                INSERT INTO toewijzingen (
                    toewijzings_type,
                    voorkeur_nummer,
                    leerling_id,
                    talenten_periode_id,
                    ingericht_talent_id,
                    toegewezen_op,
                    gewijzigd_op
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding()) {

            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                for (Toewijzing toewijzing : toewijzingen) {
                    vulInsertStatement(
                            statement,
                            toewijzing
                    );

                    statement.addBatch();
                }

                statement.executeBatch();
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De toewijzingen konden niet gezamenlijk opgeslagen worden",
                    e
            );
        }
    }

    @Override
    public void vervangAutomatischeToewijzingenVoorPeriode(
            TalentenPeriode periode,
            List<Toewijzing> nieuweToewijzingen
    ) {
        valideerOpgeslagenPeriode(periode);

        if (nieuweToewijzingen == null) {
            throw new IllegalArgumentException(
                    "De nieuwe toewijzingen mogen niet null zijn"
            );
        }

        for (Toewijzing toewijzing : nieuweToewijzingen) {
            valideerToewijzingVoorOpslag(toewijzing);

            if (toewijzing.getToewijzingsType()
                    != ToewijzingsType.AUTOMATISCH) {

                throw new IllegalArgumentException(
                        "Alle nieuwe toewijzingen moeten automatisch zijn"
                );
            }

            if (!periode.getId().equals(
                    toewijzing.getIngerichtTalent()
                            .getTalentenPeriode()
                            .getId())) {

                throw new IllegalArgumentException(
                        "Alle nieuwe toewijzingen moeten tot de gekozen periode behoren"
                );
            }
        }

        String verwijderSql = """
                DELETE FROM toewijzingen
                WHERE talenten_periode_id = ?
                  AND toewijzings_type = 'AUTOMATISCH'
                """;

        String invoegSql = """
                INSERT INTO toewijzingen (
                    toewijzings_type,
                    voorkeur_nummer,
                    leerling_id,
                    talenten_periode_id,
                    ingericht_talent_id,
                    toegewezen_op,
                    gewijzigd_op
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding()) {

            connection.setAutoCommit(false);

            try {
                try (PreparedStatement verwijderStatement =
                             connection.prepareStatement(
                                     verwijderSql
                             )) {

                    verwijderStatement.setLong(
                            1,
                            periode.getId()
                    );

                    verwijderStatement.executeUpdate();
                }

                if (!nieuweToewijzingen.isEmpty()) {
                    try (PreparedStatement invoegStatement =
                                 connection.prepareStatement(
                                         invoegSql
                                 )) {

                        for (Toewijzing toewijzing :
                                nieuweToewijzingen) {

                            vulInsertStatement(
                                    invoegStatement,
                                    toewijzing
                            );

                            invoegStatement.addBatch();
                        }

                        invoegStatement.executeBatch();
                    }
                }

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De automatische toewijzingen konden niet veilig herberekend worden",
                    e
            );
        }
    }

    @Override
    public List<Toewijzing> zoekHistorischeToewijzingenVoorSchooljaar(
            Schooljaar schooljaar
    ) {
        valideerOpgeslagenSchooljaar(schooljaar);

        String sql = """
                SELECT
                    tw.toewijzing_id,
                    tw.toewijzings_type,
                    tw.voorkeur_nummer,
                    tw.leerling_id,
                    tw.toegewezen_op,
                    tw.gewijzigd_op,

                    l.voornaam,
                    l.achternaam,
                    l.klas_id,

                    k.klas_naam,
                    k.leerjaar,
                    k.doelgroep AS klas_doelgroep,

                    it.ingericht_talent_id,
                    it.naam AS ingericht_talent_naam,
                    it.omschrijving AS ingericht_talent_omschrijving,
                    it.maximum_capaciteit,
                    it.doelgroep AS talent_doelgroep,
                    it.actief AS ingericht_talent_actief,
                    it.talent_id,

                    t.naam AS talent_naam,
                    t.beschrijving,

                    tp.talenten_periode_id,
                    tp.naam AS periode_naam,
                    tp.startdatum AS periode_startdatum,
                    tp.einddatum AS periode_einddatum
                FROM toewijzingen tw
                JOIN leerlingen l
                    ON l.leerling_id = tw.leerling_id
                JOIN klassen k
                    ON k.klas_id = l.klas_id
                JOIN talenten_periodes tp
                    ON tp.talenten_periode_id = tw.talenten_periode_id
                JOIN ingerichte_talenten it
                    ON it.ingericht_talent_id = tw.ingericht_talent_id
                JOIN talenten t
                    ON t.talent_id = it.talent_id
                WHERE tp.schooljaar_id = ?
                  AND tp.einddatum < CURRENT_DATE
                  AND k.schooljaar = ?
                ORDER BY tp.einddatum DESC, tw.leerling_id
                """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding()) {

            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                    zoekLeerkrachtenVoorSchooljaar(
                            connection,
                            schooljaar.getId()
                    );

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setLong(1, schooljaar.getId());
                statement.setString(2, schooljaar.getNaam());

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    List<Toewijzing> historischeToewijzingen =
                            new ArrayList<>();

                    while (resultSet.next()) {
                        historischeToewijzingen.add(
                                maakHistorischeToewijzingVoorSchooljaar(
                                        resultSet,
                                        schooljaar,
                                        leerkrachtenPerIngerichtTalent
                                )
                        );
                    }

                    return historischeToewijzingen;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De historische toewijzingen konden niet opgehaald worden",
                    e
            );
        }
    }

    @Override
    public List<Toewijzing> zoekHistorischeToewijzingenVoorLeerlingEnSchooljaar(
            Leerling leerling,
            Schooljaar schooljaar
    ) {
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

        valideerOpgeslagenSchooljaar(schooljaar);

        String sql = """
                SELECT
                    tw.toewijzing_id,
                    tw.toewijzings_type,
                    tw.voorkeur_nummer,
                    tw.toegewezen_op,
                    tw.gewijzigd_op,

                    it.ingericht_talent_id,
                    it.naam AS ingericht_talent_naam,
                    it.omschrijving AS ingericht_talent_omschrijving,
                    it.maximum_capaciteit,
                    it.doelgroep AS talent_doelgroep,
                    it.actief AS ingericht_talent_actief,
                    it.talent_id,

                    t.naam AS talent_naam,
                    t.beschrijving,

                    tp.talenten_periode_id,
                    tp.naam AS periode_naam,
                    tp.startdatum AS periode_startdatum,
                    tp.einddatum AS periode_einddatum
                FROM toewijzingen tw
                JOIN talenten_periodes tp
                    ON tp.talenten_periode_id = tw.talenten_periode_id
                JOIN ingerichte_talenten it
                    ON it.ingericht_talent_id = tw.ingericht_talent_id
                JOIN talenten t
                    ON t.talent_id = it.talent_id
                WHERE tw.leerling_id = ?
                  AND tp.schooljaar_id = ?
                  AND tp.einddatum < CURRENT_DATE
                ORDER BY tp.einddatum DESC
                """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding()) {

            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                    zoekLeerkrachtenVoorSchooljaar(
                            connection,
                            schooljaar.getId()
                    );

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setLong(1, leerling.getId());
                statement.setLong(2, schooljaar.getId());

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    List<Toewijzing> toewijzingen =
                            new ArrayList<>();

                    while (resultSet.next()) {
                        toewijzingen.add(
                                maakHistorischeToewijzingVoorLeerling(
                                        resultSet,
                                        leerling,
                                        schooljaar,
                                        leerkrachtenPerIngerichtTalent
                                )
                        );
                    }

                    return toewijzingen;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De historische toewijzingen van de leerling konden niet opgehaald worden",
                    e
            );
        }
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

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, periodeId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    long ingerichtTalentId =
                            resultSet.getLong(
                                    "ingericht_talent_id"
                            );

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

    private Map<Long, List<Leerkracht>> zoekLeerkrachtenVoorSchooljaar(
            Connection connection,
            long schooljaarId
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
                JOIN talenten_periodes tp
                    ON tp.talenten_periode_id = it.talenten_periode_id
                JOIN leerkrachten l
                    ON l.leerkracht_id = itl.leerkracht_id
                WHERE tp.schooljaar_id = ?
                  AND tp.einddatum < CURRENT_DATE
                ORDER BY
                    itl.ingericht_talent_id,
                    l.leerkracht_id
                """;

        Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                new HashMap<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, schooljaarId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    long ingerichtTalentId =
                            resultSet.getLong(
                                    "ingericht_talent_id"
                            );

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

    private Leerkracht maakLeerkracht(
            ResultSet resultSet
    ) throws SQLException {

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

    private Toewijzing maakToewijzingVoorPeriode(
            ResultSet resultSet,
            TalentenPeriode periode,
            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent
    ) throws SQLException {

        Doelgroep klasDoelgroep =
                Doelgroep.valueOf(
                        resultSet.getString(
                                "klas_doelgroep"
                        )
                );

        Klas klas = new Klas(
                resultSet.getLong("klas_id"),
                resultSet.getString("klas_naam"),
                periode.getSchooljaar(),
                resultSet.getInt("leerjaar"),
                klasDoelgroep
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

        return maakToewijzing(
                resultSet,
                leerling,
                ingerichtTalent
        );
    }

    private Toewijzing maakToewijzingVoorLeerlingEnPeriode(
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

        return maakToewijzing(
                resultSet,
                leerling,
                ingerichtTalent
        );
    }

    private Toewijzing maakHistorischeToewijzingVoorSchooljaar(
            ResultSet resultSet,
            Schooljaar schooljaar,
            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent
    ) throws SQLException {

        Klas klas = new Klas(
                resultSet.getLong("klas_id"),
                resultSet.getString("klas_naam"),
                schooljaar,
                resultSet.getInt("leerjaar"),
                Doelgroep.valueOf(
                        resultSet.getString(
                                "klas_doelgroep"
                        )
                )
        );

        Leerling leerling = new Leerling(
                resultSet.getLong("leerling_id"),
                resultSet.getString("voornaam"),
                resultSet.getString("achternaam"),
                klas
        );

        Talent talent = maakTalent(resultSet);

        TalentenPeriode periode =
                new TalentenPeriode(
                        resultSet.getLong(
                                "talenten_periode_id"
                        ),
                        resultSet.getString(
                                "periode_naam"
                        ),
                        resultSet.getDate(
                                "periode_startdatum"
                        ).toLocalDate(),
                        resultSet.getDate(
                                "periode_einddatum"
                        ).toLocalDate(),
                        schooljaar
                );

        IngerichtTalent ingerichtTalent =
                maakIngerichtTalent(
                        resultSet,
                        talent,
                        periode,
                        leerkrachtenPerIngerichtTalent
                );

        return maakToewijzing(
                resultSet,
                leerling,
                ingerichtTalent
        );
    }

    private Toewijzing maakHistorischeToewijzingVoorLeerling(
            ResultSet resultSet,
            Leerling leerling,
            Schooljaar schooljaar,
            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent
    ) throws SQLException {

        Talent talent = maakTalent(resultSet);

        TalentenPeriode periode =
                new TalentenPeriode(
                        resultSet.getLong(
                                "talenten_periode_id"
                        ),
                        resultSet.getString(
                                "periode_naam"
                        ),
                        resultSet.getDate(
                                "periode_startdatum"
                        ).toLocalDate(),
                        resultSet.getDate(
                                "periode_einddatum"
                        ).toLocalDate(),
                        schooljaar
                );

        IngerichtTalent ingerichtTalent =
                maakIngerichtTalent(
                        resultSet,
                        talent,
                        periode,
                        leerkrachtenPerIngerichtTalent
                );

        return maakToewijzing(
                resultSet,
                leerling,
                ingerichtTalent
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
                resultSet.getLong(
                        "ingericht_talent_id"
                );

        List<Leerkracht> leerkrachten =
                zoekLeerkrachtenInMap(
                        leerkrachtenPerIngerichtTalent,
                        ingerichtTalentId
                );

        return new IngerichtTalent(
                ingerichtTalentId,
                talent,
                periode,
                resultSet.getString(
                        "ingericht_talent_naam"
                ),
                resultSet.getString(
                        "ingericht_talent_omschrijving"
                ),
                resultSet.getInt(
                        "maximum_capaciteit"
                ),
                Doelgroep.valueOf(
                        resultSet.getString(
                                "talent_doelgroep"
                        )
                ),
                leerkrachten,
                resultSet.getBoolean(
                        "ingericht_talent_actief"
                )
        );
    }

    private Toewijzing maakToewijzing(
            ResultSet resultSet,
            Leerling leerling,
            IngerichtTalent ingerichtTalent
    ) throws SQLException {

        ToewijzingsType toewijzingsType =
                ToewijzingsType.valueOf(
                        resultSet.getString(
                                "toewijzings_type"
                        )
                );

        int voorkeurWaarde =
                resultSet.getInt(
                        "voorkeur_nummer"
                );

        Integer voorkeurNummer =
                resultSet.wasNull()
                        ? null
                        : voorkeurWaarde;

        Timestamp toegewezenOp =
                resultSet.getTimestamp(
                        "toegewezen_op"
                );

        Timestamp gewijzigdOp =
                resultSet.getTimestamp(
                        "gewijzigd_op"
                );

        LocalDateTime gewijzigdOpDatum =
                gewijzigdOp == null
                        ? null
                        : gewijzigdOp.toLocalDateTime();

        return new Toewijzing(
                resultSet.getLong(
                        "toewijzing_id"
                ),
                leerling,
                ingerichtTalent,
                toewijzingsType,
                toegewezenOp.toLocalDateTime(),
                gewijzigdOpDatum,
                voorkeurNummer
        );
    }

    private void vulInsertStatement(
            PreparedStatement statement,
            Toewijzing toewijzing
    ) throws SQLException {

        statement.setString(
                1,
                toewijzing.getToewijzingsType().name()
        );

        zetVoorkeurNummer(
                statement,
                2,
                toewijzing.getVoorkeurNummer()
        );

        statement.setLong(
                3,
                toewijzing.getLeerling().getId()
        );

        statement.setLong(
                4,
                toewijzing.getIngerichtTalent()
                        .getTalentenPeriode()
                        .getId()
        );

        statement.setLong(
                5,
                toewijzing.getIngerichtTalent().getId()
        );

        statement.setTimestamp(
                6,
                Timestamp.valueOf(
                        toewijzing.getToegewezenOp()
                )
        );

        if (toewijzing.getGewijzigdOp() == null) {
            statement.setNull(
                    7,
                    Types.TIMESTAMP
            );
        } else {
            statement.setTimestamp(
                    7,
                    Timestamp.valueOf(
                            toewijzing.getGewijzigdOp()
                    )
            );
        }
    }

    private void zetVoorkeurNummer(
            PreparedStatement statement,
            int parameterNummer,
            Integer voorkeurNummer
    ) throws SQLException {

        if (voorkeurNummer == null) {
            statement.setNull(
                    parameterNummer,
                    Types.SMALLINT
            );
        } else {
            statement.setInt(
                    parameterNummer,
                    voorkeurNummer
            );
        }
    }

    private void valideerToewijzingVoorOpslag(
            Toewijzing toewijzing
    ) {
        if (toewijzing == null) {
            throw new IllegalArgumentException(
                    "De toewijzing mag niet null zijn"
            );
        }

        if (toewijzing.getLeerling().getId() == null) {
            throw new IllegalArgumentException(
                    "De leerling moet eerst opgeslagen zijn"
            );
        }

        if (toewijzing.getIngerichtTalent().getId() == null) {
            throw new IllegalArgumentException(
                    "Het ingerichte talent moet eerst opgeslagen zijn"
            );
        }

        if (toewijzing.getIngerichtTalent()
                .getTalentenPeriode()
                .getId() == null) {

            throw new IllegalArgumentException(
                    "De talentenperiode moet eerst opgeslagen zijn"
            );
        }
    }

    private void valideerOpgeslagenPeriode(
            TalentenPeriode periode
    ) {
        if (periode == null) {
            throw new IllegalArgumentException(
                    "De talentenperiode mag niet null zijn"
            );
        }

        if (periode.getId() == null) {
            throw new IllegalArgumentException(
                    "De talentenperiode moet eerst opgeslagen zijn"
            );
        }
    }

    private void valideerOpgeslagenSchooljaar(
            Schooljaar schooljaar
    ) {
        if (schooljaar == null) {
            throw new IllegalArgumentException(
                    "Het schooljaar mag niet null zijn"
            );
        }

        if (schooljaar.getId() == null) {
            throw new IllegalArgumentException(
                    "Het schooljaar moet eerst opgeslagen zijn"
            );
        }
    }
}