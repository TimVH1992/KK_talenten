package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.domain.ToewijzingsType;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.ToewijzingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresToewijzingRepository implements ToewijzingRepository {
    private final LeerlingRepository leerlingRepository;
    private final IngerichtTalentRepository ingerichtTalentRepository;

    public PostgresToewijzingRepository() {
        this(new PostgresLeerlingRepository(), new PostgresIngerichtTalentRepository());
    }

    public PostgresToewijzingRepository(LeerlingRepository leerlingRepository, IngerichtTalentRepository ingerichtTalentRepository) {
        if (leerlingRepository == null || ingerichtTalentRepository == null) {
            throw new IllegalArgumentException("Repositories mogen niet null zijn");
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
                    throw new IllegalStateException("PostgreSQL gaf geen toewijzing_id terug");
                }

                Timestamp gewijzigdOp = resultSet.getTimestamp("gewijzigd_op");

                return new Toewijzing(
                        resultSet.getLong("toewijzing_id"),
                        toewijzing.getLeerling(),
                        toewijzing.getIngerichtTalent(),
                        toewijzing.getToewijzingsType(),
                        resultSet.getTimestamp("toegewezen_op").toLocalDateTime(),
                        gewijzigdOp == null ? null : gewijzigdOp.toLocalDateTime(),
                        toewijzing.getVoorkeurNummer()
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException("De toewijzing kon niet opgeslagen worden", e);
        }
    }

    @Override
    public List<Toewijzing> zoekVoorPeriode(TalentenPeriode periode) {
        valideerOpgeslagenPeriode(periode);

        String sql = """
                SELECT
                    toewijzing_id,
                    toewijzings_type,
                    voorkeur_nummer,
                    leerling_id,
                    ingericht_talent_id,
                    toegewezen_op,
                    gewijzigd_op
                FROM toewijzingen
                WHERE talenten_periode_id = ?
                ORDER BY leerling_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, periode.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Toewijzing> toewijzingen = new ArrayList<>();

                while (resultSet.next()) {
                    toewijzingen.add(maakToewijzing(resultSet));
                }

                return toewijzingen;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("De toewijzingen konden niet opgehaald worden voor periode: " + periode.getId(), e);
        }
    }

    @Override
    public Toewijzing zoekToewijzingVoorLeerlingEnPeriode(Leerling leerling, TalentenPeriode talentenPeriode) {
        if (leerling == null) {
            throw new IllegalArgumentException("De leerling mag niet null zijn");
        }
        if (leerling.getId() == null) {
            throw new IllegalArgumentException("De leerling moet eerst opgeslagen zijn");
        }

        valideerOpgeslagenPeriode(talentenPeriode);

        String sql = """
                SELECT
                    toewijzing_id,
                    toewijzings_type,
                    voorkeur_nummer,
                    leerling_id,
                    ingericht_talent_id,
                    toegewezen_op,
                    gewijzigd_op
                FROM toewijzingen
                WHERE leerling_id = ?
                  AND talenten_periode_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, leerling.getId());
            statement.setLong(2, talentenPeriode.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                Toewijzing toewijzing = maakToewijzing(resultSet);

                if (resultSet.next()) {
                    throw new IllegalStateException("Er bestaan meerdere toewijzingen voor dezelfde leerling en periode");
                }

                return toewijzing;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("De toewijzing kon niet opgehaald worden voor leerling: " + leerling.getId(), e);
        }
    }

    @Override
    public int telToewijzingenVoorIngerichtTalent(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Het ingerichte talent mag niet null zijn");
        }
        if (ingerichtTalent.getId() == null) {
            throw new IllegalArgumentException("Het ingerichte talent moet eerst opgeslagen zijn");
        }

        String sql = """
                SELECT COUNT(*) AS aantal
                FROM toewijzingen
                WHERE ingericht_talent_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ingerichtTalent.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("PostgreSQL gaf geen aantal toewijzingen terug");
                }

                return resultSet.getInt("aantal");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Het aantal toewijzingen kon niet opgehaald worden", e);
        }
    }

    @Override
    public Toewijzing update(Toewijzing toewijzing) {
        valideerToewijzingVoorOpslag(toewijzing);

        if (toewijzing.getId() == null) {
            throw new IllegalArgumentException("De toewijzing moet eerst opgeslagen zijn");
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

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, toewijzing.getToewijzingsType().name());
            zetVoorkeurNummer(statement, 2, toewijzing.getVoorkeurNummer());
            statement.setLong(3, toewijzing.getIngerichtTalent().getTalentenPeriode().getId());
            statement.setLong(4, toewijzing.getIngerichtTalent().getId());

            if (toewijzing.getGewijzigdOp() == null) {
                statement.setNull(5, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(5, Timestamp.valueOf(toewijzing.getGewijzigdOp()));
            }

            statement.setLong(6, toewijzing.getId());

            int aantalGewijzigdeRijen = statement.executeUpdate();

            if (aantalGewijzigdeRijen == 0) {
                throw new IllegalStateException("Geen toewijzing gevonden met id: " + toewijzing.getId());
            }
            if (aantalGewijzigdeRijen > 1) {
                throw new IllegalStateException("Er werden meerdere toewijzingen gewijzigd voor id: " + toewijzing.getId());
            }

            return toewijzing;
        } catch (SQLException e) {
            throw new IllegalStateException("De toewijzing kon niet gewijzigd worden", e);
        }
    }

    @Override
    public void saveAll(List<Toewijzing> toewijzingen) {
        if (toewijzingen == null) {
            throw new IllegalArgumentException("De lijst met toewijzingen mag niet null zijn");
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

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Toewijzing toewijzing : toewijzingen) {
                    vulInsertStatement(statement, toewijzing);
                    statement.addBatch();
                }

                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("De toewijzingen konden niet gezamenlijk opgeslagen worden", e);
        }
    }

    @Override
    public void vervangAutomatischeToewijzingenVoorPeriode(TalentenPeriode periode, List<Toewijzing> nieuweToewijzingen) {
        valideerOpgeslagenPeriode(periode);

        if (nieuweToewijzingen == null) {
            throw new IllegalArgumentException("De nieuwe toewijzingen mogen niet null zijn");
        }

        for (Toewijzing toewijzing : nieuweToewijzingen) {
            valideerToewijzingVoorOpslag(toewijzing);

            if (toewijzing.getToewijzingsType() != ToewijzingsType.AUTOMATISCH) {
                throw new IllegalArgumentException("Alle nieuwe toewijzingen moeten automatisch zijn");
            }
            if (!periode.getId().equals(toewijzing.getIngerichtTalent().getTalentenPeriode().getId())) {
                throw new IllegalArgumentException("Alle nieuwe toewijzingen moeten tot de gekozen periode behoren");
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

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement verwijderStatement = connection.prepareStatement(verwijderSql)) {
                    verwijderStatement.setLong(1, periode.getId());
                    verwijderStatement.executeUpdate();
                }

                if (!nieuweToewijzingen.isEmpty()) {
                    try (PreparedStatement invoegStatement = connection.prepareStatement(invoegSql)) {
                        for (Toewijzing toewijzing : nieuweToewijzingen) {
                            vulInsertStatement(invoegStatement, toewijzing);
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
            throw new IllegalStateException("De automatische toewijzingen konden niet veilig herberekend worden", e);
        }
    }

    @Override
    public List<Toewijzing> zoekHistorischeToewijzingen() {
        String sql = """
                SELECT
                    tw.toewijzing_id,
                    tw.toewijzings_type,
                    tw.voorkeur_nummer,
                    tw.leerling_id,
                    tw.ingericht_talent_id,
                    tw.toegewezen_op,
                    tw.gewijzigd_op
                FROM toewijzingen tw
                JOIN talenten_periodes tp ON tp.talenten_periode_id = tw.talenten_periode_id
                WHERE tp.einddatum < CURRENT_DATE
                ORDER BY tp.einddatum, tw.leerling_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<Toewijzing> historischeToewijzingen = new ArrayList<>();

            while (resultSet.next()) {
                historischeToewijzingen.add(maakToewijzing(resultSet));
            }

            return historischeToewijzingen;
        } catch (SQLException e) {
            throw new IllegalStateException("De historische toewijzingen konden niet opgehaald worden", e);
        }
    }

    private Toewijzing maakToewijzing(ResultSet resultSet) throws SQLException {
        Leerling leerling = leerlingRepository.zoekOpId(resultSet.getLong("leerling_id"));
        IngerichtTalent ingerichtTalent = ingerichtTalentRepository.zoekOpId(resultSet.getLong("ingericht_talent_id"));
        ToewijzingsType toewijzingsType = ToewijzingsType.valueOf(resultSet.getString("toewijzings_type"));

        int voorkeurWaarde = resultSet.getInt("voorkeur_nummer");
        Integer voorkeurNummer = resultSet.wasNull() ? null : voorkeurWaarde;

        Timestamp gewijzigdOp = resultSet.getTimestamp("gewijzigd_op");

        return new Toewijzing(
                resultSet.getLong("toewijzing_id"),
                leerling,
                ingerichtTalent,
                toewijzingsType,
                resultSet.getTimestamp("toegewezen_op").toLocalDateTime(),
                gewijzigdOp == null ? null : gewijzigdOp.toLocalDateTime(),
                voorkeurNummer
        );
    }

    private void vulInsertStatement(PreparedStatement statement, Toewijzing toewijzing) throws SQLException {
        statement.setString(1, toewijzing.getToewijzingsType().name());
        zetVoorkeurNummer(statement, 2, toewijzing.getVoorkeurNummer());
        statement.setLong(3, toewijzing.getLeerling().getId());
        statement.setLong(4, toewijzing.getIngerichtTalent().getTalentenPeriode().getId());
        statement.setLong(5, toewijzing.getIngerichtTalent().getId());
        statement.setTimestamp(6, Timestamp.valueOf(toewijzing.getToegewezenOp()));

        if (toewijzing.getGewijzigdOp() == null) {
            statement.setNull(7, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(7, Timestamp.valueOf(toewijzing.getGewijzigdOp()));
        }
    }

    private void zetVoorkeurNummer(PreparedStatement statement, int parameterNummer, Integer voorkeurNummer) throws SQLException {
        if (voorkeurNummer == null) {
            statement.setNull(parameterNummer, Types.SMALLINT);
        } else {
            statement.setInt(parameterNummer, voorkeurNummer);
        }
    }

    private void valideerToewijzingVoorOpslag(Toewijzing toewijzing) {
        if (toewijzing == null) {
            throw new IllegalArgumentException("De toewijzing mag niet null zijn");
        }
        if (toewijzing.getLeerling().getId() == null) {
            throw new IllegalArgumentException("De leerling moet eerst opgeslagen zijn");
        }
        if (toewijzing.getIngerichtTalent().getId() == null) {
            throw new IllegalArgumentException("Het ingerichte talent moet eerst opgeslagen zijn");
        }
        if (toewijzing.getIngerichtTalent().getTalentenPeriode().getId() == null) {
            throw new IllegalArgumentException("De talentenperiode moet eerst opgeslagen zijn");
        }
    }

    private void valideerOpgeslagenPeriode(TalentenPeriode periode) {
        if (periode == null) {
            throw new IllegalArgumentException("De talentenperiode mag niet null zijn");
        }
        if (periode.getId() == null) {
            throw new IllegalArgumentException("De talentenperiode moet eerst opgeslagen zijn");
        }
    }
}