package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.postgres.PostgresTalentenPeriodeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

class PostgresTalentenPeriodeRepositoryTest {

    private TalentenPeriodeRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        try (
                Connection connection =
                        DatabaseConnectionFactory.maakVerbinding();

                Statement statement =
                        connection.createStatement()
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

        repository = new PostgresTalentenPeriodeRepository();
    }

    @Test
    void saveSlaatTalentenPeriodeOpEnGeeftDatabaseIdTerug()
            throws SQLException {

        // Arrange
        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 21),
                LocalDate.of(2026, 12, 21)
        );

        // Act
        TalentenPeriode opgeslagenPeriode =
                repository.save(herfst);

        // Assert op teruggegeven object
        Assertions.assertNotNull(opgeslagenPeriode);
        Assertions.assertNotNull(opgeslagenPeriode.getId());
        Assertions.assertTrue(opgeslagenPeriode.getId() > 0);

        Assertions.assertEquals(
                "Herfst",
                opgeslagenPeriode.getNaam()
        );

        Assertions.assertEquals(
                LocalDate.of(2026, 9, 21),
                opgeslagenPeriode.getStartDatum()
        );

        Assertions.assertEquals(
                LocalDate.of(2026, 12, 21),
                opgeslagenPeriode.getEindDatum()
        );

        // Rechtstreeks controleren in PostgreSQL
        String sql = """
                SELECT naam, startdatum, einddatum
                FROM talenten_periodes
                WHERE talenten_periode_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnectionFactory.maakVerbinding();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(
                    1,
                    opgeslagenPeriode.getId()
            );

            try (ResultSet resultSet = statement.executeQuery()) {
                Assertions.assertTrue(
                        resultSet.next(),
                        "De talentenperiode werd niet teruggevonden"
                );

                Assertions.assertEquals(
                        "Herfst",
                        resultSet.getString("naam")
                );

                Assertions.assertEquals(
                        LocalDate.of(2026, 9, 21),
                        resultSet.getDate("startdatum").toLocalDate()
                );

                Assertions.assertEquals(
                        LocalDate.of(2026, 12, 21),
                        resultSet.getDate("einddatum").toLocalDate()
                );

                Assertions.assertFalse(
                        resultSet.next(),
                        "Er werden meerdere periodes met hetzelfde ID gevonden"
                );
            }
        }
    }

    @Test
    void zoekAlleGeeftPeriodesGesorteerdOpStartdatumTerug() {
        // Arrange: bewust niet chronologisch opslaan
        TalentenPeriode winter = repository.save(
                new TalentenPeriode(
                        "Winter",
                        LocalDate.of(2027, 1, 11),
                        LocalDate.of(2027, 3, 26)
                )
        );

        TalentenPeriode herfst = repository.save(
                new TalentenPeriode(
                        "Herfst",
                        LocalDate.of(2026, 9, 21),
                        LocalDate.of(2026, 12, 21)
                )
        );

        TalentenPeriode lente = repository.save(
                new TalentenPeriode(
                        "Lente",
                        LocalDate.of(2027, 4, 19),
                        LocalDate.of(2027, 6, 25)
                )
        );

        // Act
        List<TalentenPeriode> resultaat =
                repository.zoekAlle();

        // Assert: volgorde uit ORDER BY startdatum
        Assertions.assertEquals(
                List.of(herfst, winter, lente),
                resultaat
        );
    }

    @Test
    void zoekAlleGeeftLegeLijstWanneerErGeenPeriodesZijn() {
        // Act
        List<TalentenPeriode> resultaat =
                repository.zoekAlle();

        // Assert
        Assertions.assertNotNull(resultaat);
        Assertions.assertTrue(resultaat.isEmpty());
    }
}