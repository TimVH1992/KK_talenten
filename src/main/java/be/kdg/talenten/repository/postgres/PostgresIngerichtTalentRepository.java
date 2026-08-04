package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                    maximum_capaciteit,
                    doelgroep,
                    talent_id,
                    talenten_periode_id
                )
                VALUES (?, ?, ?, ?)
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
                    statement.setInt(1, ingerichtTalent.getMaxCapaciteit());
                    statement.setString(2, ingerichtTalent.getDoelgroep().name());
                    statement.setLong(3, ingerichtTalent.getTalent().getId());
                    statement.setLong(4, ingerichtTalent.getTalentenPeriode().getId());

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new IllegalStateException("PostgreSQL gaf geen ingericht_talent_id terug");
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
                        ingerichtTalent.getMaxCapaciteit(),
                        ingerichtTalent.getDoelgroep(),
                        ingerichtTalent.getLeerkrachten()
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
                    it.maximum_capaciteit,
                    it.doelgroep,
                    t.talent_id,
                    t.naam AS talent_naam,
                    t.beschrijving
                FROM ingerichte_talenten it
                JOIN talenten t
                    ON t.talent_id = it.talent_id
                WHERE it.talenten_periode_id = ?
                ORDER BY t.naam
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, periode.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<IngerichtTalent> ingerichteTalenten = new ArrayList<>();

                while (resultSet.next()) {
                    long ingerichtTalentId = resultSet.getLong("ingericht_talent_id");

                    Talent talent = new Talent(
                            resultSet.getLong("talent_id"),
                            resultSet.getString("talent_naam"),
                            resultSet.getString("beschrijving")
                    );

                    List<Leerkracht> leerkrachten =
                            zoekLeerkrachtenVoorIngerichtTalent(connection, ingerichtTalentId);

                    IngerichtTalent ingerichtTalent = new IngerichtTalent(
                            ingerichtTalentId,
                            talent,
                            periode,
                            resultSet.getInt("maximum_capaciteit"),
                            Doelgroep.valueOf(resultSet.getString("doelgroep")),
                            leerkrachten
                    );

                    ingerichteTalenten.add(ingerichtTalent);
                }

                return ingerichteTalenten;
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
                    it.maximum_capaciteit,
                    it.doelgroep,
                    t.talent_id,
                    t.naam AS talent_naam,
                    t.beschrijving,
                    tp.talenten_periode_id,
                    tp.naam AS periode_naam,
                    tp.startdatum,
                    tp.einddatum
                FROM ingerichte_talenten it
                JOIN talenten t ON (t.talent_id = it.talent_id)
                JOIN talenten_periodes tp ON tp.talenten_periode_id = it.talenten_periode_id
                WHERE it.ingericht_talent_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1,id);

            try (ResultSet resultSet = statement.executeQuery()){
                if (!resultSet.next()){
                    throw new IllegalStateException("PostgreSQL kon geen ingerichtTalent vinden met id: " + id);
                }
                Talent talent = new Talent(resultSet.getLong("talent_id"), resultSet.getString("talent_naam"), resultSet.getString("beschrijving"));

                TalentenPeriode periode = new TalentenPeriode(
                        resultSet.getLong("talenten_periode_id"),
                        resultSet.getString("periode_naam"),
                        resultSet.getDate("startdatum").toLocalDate(),
                        resultSet.getDate("einddatum").toLocalDate()
                );

                List<Leerkracht> leerkrachten = zoekLeerkrachtenVoorIngerichtTalent(connection, id);


                IngerichtTalent ingerichtTalent = new IngerichtTalent(
                        id,
                        talent,
                        periode,
                        resultSet.getInt("maximum_capaciteit"),
                        Doelgroep.valueOf(resultSet.getString("doelgroep")),
                        leerkrachten);

                return ingerichtTalent;
            }
        } catch (SQLException e){
            throw new IllegalStateException("PostgreSQL kong geen Ingericht talent vinden op id met id : " + id, e);
        }

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
                    Leerkracht leerkracht = new Leerkracht(
                            resultSet.getLong("leerkracht_id"),
                            resultSet.getString("voornaam"),
                            resultSet.getString("achternaam")
                    );

                    leerkrachten.add(leerkracht);
                }

                return leerkrachten;
            }
        }
    }


}
