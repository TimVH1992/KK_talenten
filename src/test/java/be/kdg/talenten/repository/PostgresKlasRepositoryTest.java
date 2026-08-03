package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.repository.postgres.PostgresKlasRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

class PostgresKlasRepositoryTest {

    private KlasRepository repository;

    @BeforeEach
    void setup() throws SQLException {
        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding();
             Statement statement =
                     connection.createStatement()) {

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
        Klas klas1AA = new Klas(
                "1AA",
                "2026-2027",
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        // Act
        Klas opgeslagenKlas = repository.save(klas1AA);

        // Assert op het teruggegeven object
        Assertions.assertNotNull(opgeslagenKlas);
        Assertions.assertNotNull(opgeslagenKlas.getId());
        Assertions.assertTrue(opgeslagenKlas.getId() > 0);

        Assertions.assertEquals(
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                opgeslagenKlas.getDoelgroep()
        );

        String sql = """
                SELECT
                    klas_naam,
                    schooljaar,
                    leerjaar,
                    doelgroep
                FROM klassen
                WHERE klas_id = ?
                """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    opgeslagenKlas.getId()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

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

                Assertions.assertEquals(
                        "OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB",
                        resultSet.getString("doelgroep")
                );

                Assertions.assertFalse(
                        resultSet.next(),
                        "Er werden meerdere klassen met hetzelfde ID gevonden"
                );
            }
        }
    }

    @Test
    void zoekAlleKlassenGeeftAlleKlassenGesorteerdTerug() {
        // Arrange
        Klas klas1AA = new Klas(
                "1AA",
                "2026-2027",
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        Klas klas2AA = new Klas(
                "2AA",
                "2026-2027",
                2,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        Klas klas3AA = new Klas(
                "3AA",
                "2026-2027",
                3,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        Klas klas5AA = new Klas(
                "5AA",
                "2026-2027",
                5,
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB
        );

        Klas opgeslagenKlas1AA = repository.save(klas1AA);
        Klas opgeslagenKlas2AA = repository.save(klas2AA);
        Klas opgeslagenKlas3AA = repository.save(klas3AA);
        Klas opgeslagenKlas5AA = repository.save(klas5AA);

        List<Klas> verwacht = List.of(
                opgeslagenKlas1AA,
                opgeslagenKlas2AA,
                opgeslagenKlas3AA,
                opgeslagenKlas5AA
        );

        // Act
        List<Klas> werkelijk = repository.zoekAlle();

        // Assert
        Assertions.assertEquals(
                1L,
                opgeslagenKlas1AA.getId()
        );

        Assertions.assertEquals(
                2L,
                opgeslagenKlas2AA.getId()
        );

        Assertions.assertEquals(
                3L,
                opgeslagenKlas3AA.getId()
        );

        Assertions.assertEquals(
                4L,
                opgeslagenKlas5AA.getId()
        );

        Assertions.assertEquals(
                verwacht,
                werkelijk
        );

        Assertions.assertEquals(
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                werkelijk.getFirst().getDoelgroep()
        );

        Assertions.assertEquals(
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB,
                werkelijk.get(3).getDoelgroep()
        );
    }
}