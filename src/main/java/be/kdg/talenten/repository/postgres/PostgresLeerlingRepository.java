package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.repository.LeerlingRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresLeerlingRepository implements LeerlingRepository {
    @Override
    public List<Leerling> zoekVoorKlas(Klas klas) {
        if (klas == null) {
            throw new IllegalArgumentException("Klas mag niet null zijn");
        }

        String sql = """
                SELECT leerling_id, voornaam, achternaam
                FROM leerlingen
                WHERE klas_id = ?
                ORDER BY leerling_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setLong(1, klas.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Leerling> leerlingenPerKlas = new ArrayList<>();

                while (resultSet.next()) {
                    Leerling leerling = new Leerling(
                            resultSet.getLong("leerling_id"),
                            resultSet.getString("voornaam"),
                            resultSet.getString("achternaam"),
                            klas
                    );

                    leerlingenPerKlas.add(leerling);
                }

                return leerlingenPerKlas;
            }
        }
        catch (SQLException e) {
            throw new IllegalStateException(
                    "De leerlingen voor deze klas konden niet opgehaald worden.",
                    e
            );
        }
    }


    @Override
    public Leerling save(Leerling leerling) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }

        String sql = """
                INSERT INTO leerlingen (
                    voornaam,
                    achternaam,
                    klas_id
                )
                VALUES (?, ?, ?)
                RETURNING leerling_id
                """;

        try (
                Connection connection =
                        DatabaseConnectionFactory.maakVerbinding();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, leerling.getVoornaam());
            statement.setString(2, leerling.getAchternaam());
            statement.setLong(3, leerling.getKlas().getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException(
                            "PostgreSQL gaf geen leerling_id terug."
                    );
                }

                long gegenereerdId =
                        resultSet.getLong("leerling_id");

                return new Leerling(
                        gegenereerdId,
                        leerling.getVoornaam(),
                        leerling.getAchternaam(),
                        leerling.getKlas()
                );
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De leerling kon niet opgeslagen worden.",
                    e
            );
        }
    }
}
