package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.repository.postgres.PostgresKlasRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresKlasRepositoryTest {
    private KlasRepository repository;

    @BeforeEach
    public void setup() throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate("""
                    TRUNCATE TABLE
                        ingericht_talent_leerkrachten,
                        voorkeuren,
                        toewijzingen,
                        leerlingen,
                        ingerichte_talenten,
                        leerkrachten,
                        talenten,
                        talenten_periodes,
                        klassen
                    RESTART IDENTITY CASCADE
                    """);
        }

        repository = new PostgresKlasRepository();
    }

    @Test
    void saveKlasSlaatKlasOpInDatabank() throws SQLException {
        // Arrange
        Klas klas1AA = new Klas("1AA", "2026-2027", 1);

        // Act
        Klas opgeslagenKlas = repository.save(klas1AA);

        // Assert op het teruggegeven object
        Assertions.assertNotNull(opgeslagenKlas);
        Assertions.assertTrue(opgeslagenKlas.getId() > 0);

        String sql = """
            SELECT klas_naam, schooljaar, leerjaar
            FROM klassen
            WHERE klas_id = ?
            """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, opgeslagenKlas.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                Assertions.assertTrue(
                        resultSet.next(),
                        "De klas werd niet teruggevonden in de databank"
                );

                Assertions.assertEquals(
                        "1AA",
                        resultSet.getString("klas_naam")
                );
                Assertions.assertEquals(
                        "2026-2027",
                        resultSet.getString("schooljaar")
                );
                Assertions.assertEquals(
                        1,
                        resultSet.getInt("leerjaar")
                );

                Assertions.assertFalse(
                        resultSet.next(),
                        "Er werden meerdere klassen met hetzelfde ID gevonden"
                );
            }
        }
    }
    @Test
    void zoekAlleKlassen(){
        // Arrange
        Klas klas1AA = new Klas("1AA", "2026-2027", 1);
        Klas klas2AA = new Klas("2AA", "2026-2027", 2);
        Klas klas3AA = new Klas("3AA", "2026-2027", 3);

        // Act
        Klas opgeslagenKlas1AA = repository.save(klas1AA);
        Klas opgeslagenKlas2AA = repository.save(klas2AA);
        Klas opgeslagenKlas3AA = repository.save(klas3AA);

        List<Klas> resultaat = new ArrayList<>();
        resultaat.add(opgeslagenKlas1AA);
        resultaat.add(opgeslagenKlas2AA);
        resultaat.add(opgeslagenKlas3AA);

        // Assert op het teruggegeven object
        Assertions.assertNotNull(opgeslagenKlas1AA);
        Assertions.assertNotNull(opgeslagenKlas2AA);
        Assertions.assertNotNull(opgeslagenKlas3AA);
        Assertions.assertEquals(1, opgeslagenKlas1AA.getId());
        Assertions.assertEquals(2, opgeslagenKlas2AA.getId());
        Assertions.assertEquals(3, opgeslagenKlas3AA.getId());

        Assertions.assertEquals(resultaat, repository.zoekAlle());


    }
}

