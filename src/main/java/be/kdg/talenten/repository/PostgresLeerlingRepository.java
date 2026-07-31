package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PostgresLeerlingRepository implements LeerlingRepository {
    @Override
    public List<Leerling> zoekVoorKlas(Klas klas) {
        return List.of();
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
