package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgresIngerichtTalentRepository implements IngerichtTalentRepository {

    @Override
    public IngerichtTalent save(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn");
        }

        if (ingerichtTalent.getTalent().getId() == null) {
            throw new IllegalArgumentException("Het talent moet eerst opgeslagen zijn");
        }

        if (ingerichtTalent.getTalentenPeriode().getId() == null) {
            throw new IllegalArgumentException("De talentenperiode moet eerst opgeslagen zijn");
        }

        for (Leerkracht leerkracht : ingerichtTalent.getLeerkrachten()) {
            if (leerkracht.getId() == null) {
                throw new IllegalArgumentException("Alle leerkrachten moeten eerst opgeslagen zijn");
            }
        }

        String ingerichtTalentSql = """
                INSERT INTO ingerichte_talenten (
                    naam,
                    omschrijving,
                    maximum_capaciteit,
                    doelgroep,
                    actief,
                    talent_id,
                    talenten_periode_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING ingericht_talent_id
                """;

        String leerkrachtKoppelingSql = """
                INSERT INTO ingericht_talent_leerkrachten (
                    ingericht_talent_id,
                    leerkracht_id
                )
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding()) {
            connection.setAutoCommit(false);

            try {
                long gegenereerdId;

                try (PreparedStatement statement = connection.prepareStatement(ingerichtTalentSql)) {
                    statement.setString(1, ingerichtTalent.getNaam());
                    statement.setString(2, ingerichtTalent.getOmschrijving());
                    statement.setInt(3, ingerichtTalent.getMaxCapaciteit());
                    statement.setString(4, ingerichtTalent.getDoelgroep().name());
                    statement.setBoolean(5, ingerichtTalent.isActief());
                    statement.setLong(6, ingerichtTalent.getTalent().getId());
                    statement.setLong(7, ingerichtTalent.getTalentenPeriode().getId());

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new IllegalStateException(
                                    "PostgreSQL gaf geen ingericht_talent_id terug"
                            );
                        }

                        gegenereerdId = resultSet.getLong("ingericht_talent_id");
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(leerkrachtKoppelingSql)) {
                    for (Leerkracht leerkracht : ingerichtTalent.getLeerkrachten()) {
                        statement.setLong(1, gegenereerdId);
                        statement.setLong(2, leerkracht.getId());
                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                connection.commit();

                return new IngerichtTalent(
                        gegenereerdId,
                        ingerichtTalent.getTalent(),
                        ingerichtTalent.getTalentenPeriode(),
                        ingerichtTalent.getNaam(),
                        ingerichtTalent.getOmschrijving(),
                        ingerichtTalent.getMaxCapaciteit(),
                        ingerichtTalent.getDoelgroep(),
                        ingerichtTalent.getLeerkrachten(),
                        ingerichtTalent.isActief()
                );

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                if (e instanceof SQLException sqlException) {
                    throw new IllegalStateException(
                            "Het ingerichte talent kon niet opgeslagen worden",
                            sqlException
                    );
                }

                throw (RuntimeException) e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Er kon geen databankverbinding gemaakt worden",
                    e
            );
        }
    }

    @Override
    public List<IngerichtTalent> zoekVoorPeriode(TalentenPeriode periode) {
        if (periode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn");
        }

        if (periode.getId() == null) {
            throw new IllegalArgumentException("De talentenperiode moet eerst opgeslagen zijn");
        }

        String sql = """
                SELECT
                    it.ingericht_talent_id,
                    it.naam AS ingericht_talent_naam,
                    it.omschrijving AS ingericht_talent_omschrijving,
                    it.maximum_capaciteit,
                    it.doelgroep,
                    it.actief,
                    t.talent_id,
                    t.naam AS talent_naam,
                    t.beschrijving
                FROM ingerichte_talenten it
                JOIN talenten t
                    ON t.talent_id = it.talent_id
                WHERE it.talenten_periode_id = ?
                ORDER BY it.naam
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding()) {

            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                    zoekLeerkrachtenVoorPeriode(connection, periode.getId());

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, periode.getId());

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<IngerichtTalent> ingerichteTalenten = new ArrayList<>();

                    while (resultSet.next()) {
                        long ingerichtTalentId =
                                resultSet.getLong("ingericht_talent_id");

                        Talent talent = new Talent(
                                resultSet.getLong("talent_id"),
                                resultSet.getString("talent_naam"),
                                resultSet.getString("beschrijving")
                        );

                        List<Leerkracht> leerkrachten =
                                zoekLeerkrachtenInMap(
                                        leerkrachtenPerIngerichtTalent,
                                        ingerichtTalentId
                                );

                        IngerichtTalent ingerichtTalent = new IngerichtTalent(
                                ingerichtTalentId,
                                talent,
                                periode,
                                resultSet.getString("ingericht_talent_naam"),
                                resultSet.getString("ingericht_talent_omschrijving"),
                                resultSet.getInt("maximum_capaciteit"),
                                Doelgroep.valueOf(resultSet.getString("doelgroep")),
                                leerkrachten,
                                resultSet.getBoolean("actief")
                        );

                        ingerichteTalenten.add(ingerichtTalent);
                    }

                    return ingerichteTalenten;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De ingerichte talenten konden niet opgehaald worden",
                    e
            );
        }
    }

    @Override
    public List<IngerichtTalent> zoekActieveVoorPeriodeEnDoelgroep(TalentenPeriode periode, Doelgroep doelgroep) {
        if (periode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn");
        }

        if (periode.getId() == null) {
            throw new IllegalArgumentException("De talentenperiode moet eerst opgeslagen zijn");
        }
        if (doelgroep == null) {
            throw new IllegalArgumentException("Doelgroep mag niet null zijn");
        }

        String sql = """
                SELECT
                    it.ingericht_talent_id,
                    it.naam AS ingericht_talent_naam,
                    it.omschrijving AS ingericht_talent_omschrijving,
                    it.maximum_capaciteit,
                    it.doelgroep,
                    it.actief,
                    t.talent_id,
                    t.naam AS talent_naam,
                    t.beschrijving
                FROM ingerichte_talenten it
                JOIN talenten t
                    ON t.talent_id = it.talent_id
                WHERE it.talenten_periode_id = ?
                    AND it.doelgroep = ?
                    AND it.actief = TRUE
                ORDER BY it.naam
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding()) {

            Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent =
                    zoekLeerkrachtenVoorPeriode(connection, periode.getId());

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, periode.getId());
                statement.setString(2,doelgroep.name());

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<IngerichtTalent> ingerichteTalenten = new ArrayList<>();

                    while (resultSet.next()) {
                        long ingerichtTalentId =
                                resultSet.getLong("ingericht_talent_id");

                        Talent talent = new Talent(
                                resultSet.getLong("talent_id"),
                                resultSet.getString("talent_naam"),
                                resultSet.getString("beschrijving")
                        );

                        List<Leerkracht> leerkrachten =
                                zoekLeerkrachtenInMap(
                                        leerkrachtenPerIngerichtTalent,
                                        ingerichtTalentId
                                );

                        IngerichtTalent ingerichtTalent = new IngerichtTalent(
                                ingerichtTalentId,
                                talent,
                                periode,
                                resultSet.getString("ingericht_talent_naam"),
                                resultSet.getString("ingericht_talent_omschrijving"),
                                resultSet.getInt("maximum_capaciteit"),
                                Doelgroep.valueOf(resultSet.getString("doelgroep")),
                                leerkrachten,
                                resultSet.getBoolean("actief")
                        );

                        ingerichteTalenten.add(ingerichtTalent);
                    }

                    return ingerichteTalenten;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De ingerichte talenten konden niet opgehaald worden",
                    e
            );
        }
    }

    @Override
    public IngerichtTalent zoekOpId(long id) {
        if (id < 1) {
            throw new IllegalArgumentException("Id mag niet kleiner zijn dan 1");
        }

        String sql = """
                SELECT
                    it.ingericht_talent_id,
                    it.naam AS ingericht_talent_naam,
                    it.omschrijving AS ingericht_talent_omschrijving,
                    it.maximum_capaciteit,
                    it.doelgroep,
                    it.actief,

                    t.talent_id,
                    t.naam AS talent_naam,
                    t.beschrijving,

                    tp.talenten_periode_id,
                    tp.naam AS periode_naam,
                    tp.startdatum,
                    tp.einddatum,

                    sj.schooljaar_id,
                    sj.naam AS schooljaar_naam,
                    sj.startdatum AS schooljaar_startdatum,
                    sj.einddatum AS schooljaar_einddatum,
                    sj.actief AS schooljaar_actief

                FROM ingerichte_talenten it
                JOIN talenten t
                    ON t.talent_id = it.talent_id
                JOIN talenten_periodes tp
                    ON tp.talenten_periode_id = it.talenten_periode_id
                JOIN schooljaren sj
                    ON sj.schooljaar_id = tp.schooljaar_id
                WHERE it.ingericht_talent_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException(
                            "PostgreSQL kon geen ingericht talent vinden met id: " + id
                    );
                }

                Talent talent = new Talent(
                        resultSet.getLong("talent_id"),
                        resultSet.getString("talent_naam"),
                        resultSet.getString("beschrijving")
                );

                Schooljaar schooljaar = new Schooljaar(
                        resultSet.getLong("schooljaar_id"),
                        resultSet.getString("schooljaar_naam"),
                        resultSet.getDate("schooljaar_startdatum").toLocalDate(),
                        resultSet.getDate("schooljaar_einddatum").toLocalDate(),
                        resultSet.getBoolean("schooljaar_actief")
                );

                TalentenPeriode periode = new TalentenPeriode(
                        resultSet.getLong("talenten_periode_id"),
                        resultSet.getString("periode_naam"),
                        resultSet.getDate("startdatum").toLocalDate(),
                        resultSet.getDate("einddatum").toLocalDate(),
                        schooljaar
                );

                List<Leerkracht> leerkrachten =
                        zoekLeerkrachtenVoorIngerichtTalent(connection, id);

                return new IngerichtTalent(
                        id,
                        talent,
                        periode,
                        resultSet.getString("ingericht_talent_naam"),
                        resultSet.getString("ingericht_talent_omschrijving"),
                        resultSet.getInt("maximum_capaciteit"),
                        Doelgroep.valueOf(resultSet.getString("doelgroep")),
                        leerkrachten,
                        resultSet.getBoolean("actief")
                );
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "PostgreSQL kon geen ingericht talent vinden met id: " + id,
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
                    l.achternaam,
                    l.voornaam
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
                                    key -> new ArrayList<>()
                            )
                            .add(leerkracht);
                }
            }
        }

        return leerkrachtenPerIngerichtTalent;
    }

    private List<Leerkracht> zoekLeerkrachtenVoorIngerichtTalent(
            Connection connection,
            long ingerichtTalentId
    ) throws SQLException {

        String sql = """
                SELECT
                    l.leerkracht_id,
                    l.voornaam,
                    l.achternaam
                FROM ingericht_talent_leerkrachten itl
                JOIN leerkrachten l
                    ON l.leerkracht_id = itl.leerkracht_id
                WHERE itl.ingericht_talent_id = ?
                ORDER BY l.achternaam, l.voornaam
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ingerichtTalentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Leerkracht> leerkrachten = new ArrayList<>();

                while (resultSet.next()) {
                    leerkrachten.add(
                            maakLeerkracht(resultSet)
                    );
                }

                return leerkrachten;
            }
        }
    }

    @Override
    public void update(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn");
        }
        if (ingerichtTalent.getId() == null || ingerichtTalent.getId() < 1) {
            throw new IllegalStateException("Het ingerichte talent heeft geen bestaand id");
        }

        for (Leerkracht leerkracht : ingerichtTalent.getLeerkrachten()) {
            if (leerkracht.getId() == null || leerkracht.getId() < 1) {
                throw new IllegalStateException("Alle leerkrachten moeten eerst opgeslagen zijn");
            }
        }

        String updateSql = """
            UPDATE ingerichte_talenten
            SET naam = ?,
                omschrijving = ?,
                maximum_capaciteit = ?,
                actief = ?
            WHERE ingericht_talent_id = ?
            """;

        String verwijderLeerkrachtenSql = """
            DELETE FROM ingericht_talent_leerkrachten
            WHERE ingericht_talent_id = ?
            """;

        String voegLeerkrachtToeSql = """
            INSERT INTO ingericht_talent_leerkrachten (
                ingericht_talent_id,
                leerkracht_id
            )
            VALUES (?, ?)
            """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setString(1, ingerichtTalent.getNaam());
                    statement.setString(2, ingerichtTalent.getOmschrijving());
                    statement.setInt(3, ingerichtTalent.getMaxCapaciteit());
                    statement.setBoolean(4, ingerichtTalent.isActief());
                    statement.setLong(5, ingerichtTalent.getId());

                    int aantalAangepasteRijen = statement.executeUpdate();

                    if (aantalAangepasteRijen == 0) {
                        throw new IllegalStateException(
                                "Geen ingericht talent gevonden met id: " + ingerichtTalent.getId()
                        );
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(verwijderLeerkrachtenSql)) {
                    statement.setLong(1, ingerichtTalent.getId());
                    statement.executeUpdate();
                }

                if (!ingerichtTalent.getLeerkrachten().isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement(voegLeerkrachtToeSql)) {
                        for (Leerkracht leerkracht : ingerichtTalent.getLeerkrachten()) {
                            statement.setLong(1, ingerichtTalent.getId());
                            statement.setLong(2, leerkracht.getId());
                            statement.addBatch();
                        }

                        statement.executeBatch();
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                if (e instanceof SQLException sqlException) {
                    throw new IllegalStateException(
                            "Het ingerichte talent kon niet aangepast worden",
                            sqlException
                    );
                }

                throw (RuntimeException) e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Er kon geen databankverbinding gemaakt worden",
                    e
            );
        }
    }

    private Leerkracht maakLeerkracht(ResultSet resultSet)
            throws SQLException {

        return new Leerkracht(
                resultSet.getLong("leerkracht_id"),
                resultSet.getString("voornaam"),
                resultSet.getString("achternaam")
        );
    }

    private List<Leerkracht> zoekLeerkrachtenInMap(Map<Long, List<Leerkracht>> leerkrachtenPerIngerichtTalent, long ingerichtTalentId) {
        return leerkrachtenPerIngerichtTalent.getOrDefault(ingerichtTalentId, List.of());
    }
}