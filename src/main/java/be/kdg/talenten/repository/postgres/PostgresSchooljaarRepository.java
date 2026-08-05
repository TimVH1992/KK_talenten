package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.SchooljaarRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresSchooljaarRepository implements SchooljaarRepository {
    @Override
    public Schooljaar save(Schooljaar schooljaar) {
        if (schooljaar == null) {
            throw new IllegalArgumentException("Schooljaar mag niet null zijn");
        }

        String sql = """
                INSERT INTO schooljaren (naam, startdatum, einddatum, actief)
                VALUES (?, ?, ?, ?)
                RETURNING schooljaar_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding()) {
            connection.setAutoCommit(false);

            try {
                if (schooljaar.isActief()) {
                    zetAlleSchooljarenInactief(connection);
                }

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, schooljaar.getNaam());
                    statement.setDate(2, java.sql.Date.valueOf(schooljaar.getStartDatum()));
                    statement.setDate(3, java.sql.Date.valueOf(schooljaar.getEindDatum()));
                    statement.setBoolean(4, schooljaar.isActief());

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new IllegalStateException("PostgreSQL gaf geen schooljaar_id terug");
                        }

                        Schooljaar opgeslagen = new Schooljaar(resultSet.getLong("schooljaar_id"), schooljaar.getNaam(), schooljaar.getStartDatum(), schooljaar.getEindDatum(), schooljaar.isActief());
                        connection.commit();
                        return opgeslagen;
                    }
                }
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw new IllegalStateException("Het schooljaar kon niet opgeslagen worden", sqlException);
                }
                throw (RuntimeException) exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Er kon geen databankverbinding gemaakt worden", exception);
        }
    }

    @Override
    public List<Schooljaar> zoekAlle() {
        String sql = """
                SELECT schooljaar_id, naam, startdatum, einddatum, actief
                FROM schooljaren
                ORDER BY startdatum DESC
                """;
        return voerLijstQueryUit(sql);
    }

    @Override
    public List<Schooljaar> zoekSelecteerbareSchooljaren() {
        String sql = """
                SELECT schooljaar_id, naam, startdatum, einddatum, actief
                FROM schooljaren
                WHERE einddatum >= CURRENT_DATE OR actief = TRUE
                ORDER BY startdatum DESC
                """;
        return voerLijstQueryUit(sql);
    }

    @Override
    public Optional<Schooljaar> zoekActiefSchooljaar() {
        String sql = """
                SELECT schooljaar_id, naam, startdatum, einddatum, actief
                FROM schooljaren
                WHERE actief = TRUE
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) return Optional.empty();
            return Optional.of(mapSchooljaar(resultSet));
        } catch (SQLException exception) {
            throw new IllegalStateException("Het actieve schooljaar kon niet opgehaald worden", exception);
        }
    }

    @Override
    public void maakActief(Schooljaar schooljaar) {
        if (schooljaar == null) {
            throw new IllegalArgumentException("Schooljaar mag niet null zijn");
        }
        if (schooljaar.getId() == null) {
            throw new IllegalArgumentException("Het schooljaar moet eerst opgeslagen zijn");
        }

        String sql = "UPDATE schooljaren SET actief = TRUE WHERE schooljaar_id = ?";

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding()) {
            connection.setAutoCommit(false);

            try {
                zetAlleSchooljarenInactief(connection);

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, schooljaar.getId());
                    if (statement.executeUpdate() != 1) {
                        throw new IllegalStateException("Het gekozen schooljaar werd niet gevonden");
                    }
                }

                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw new IllegalStateException("Het actieve schooljaar kon niet gewijzigd worden", sqlException);
                }
                throw (RuntimeException) exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Er kon geen databankverbinding gemaakt worden", exception);
        }
    }

    private List<Schooljaar> voerLijstQueryUit(String sql) {
        List<Schooljaar> schooljaren = new ArrayList<>();

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                schooljaren.add(mapSchooljaar(resultSet));
            }
            return schooljaren;
        } catch (SQLException exception) {
            throw new IllegalStateException("De schooljaren konden niet opgehaald worden", exception);
        }
    }

    private void zetAlleSchooljarenInactief(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE schooljaren SET actief = FALSE WHERE actief = TRUE")) {
            statement.executeUpdate();
        }
    }

    private Schooljaar mapSchooljaar(ResultSet resultSet) throws SQLException {
        return new Schooljaar(resultSet.getLong("schooljaar_id"), resultSet.getString("naam"), resultSet.getDate("startdatum").toLocalDate(), resultSet.getDate("einddatum").toLocalDate(), resultSet.getBoolean("actief"));
    }
}
