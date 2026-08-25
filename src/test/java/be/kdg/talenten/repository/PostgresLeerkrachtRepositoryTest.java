package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.repository.postgres.PostgresLeerkrachtRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

class PostgresLeerkrachtRepositoryTest {
    private LeerkrachtRepository repository;

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
                        schooljaren,
                        klassen
                    RESTART IDENTITY CASCADE
                    """);
        }

        repository = new PostgresLeerkrachtRepository();
    }

    @Test
    void saveSlaatLeerkrachtOpEnGeeftDatabaseIdTerug()
            throws SQLException {

        // Arrange
        Leerkracht leerkracht = new Leerkracht("Tim", "Van Herreweghe");

        // Act
        Leerkracht opgeslagenLeerkracht = repository.save(leerkracht);

        // Assert op teruggegeven object
        Assertions.assertNotNull(opgeslagenLeerkracht);
        Assertions.assertNotNull(opgeslagenLeerkracht.getId());
        Assertions.assertTrue(opgeslagenLeerkracht.getId() > 0);

        Assertions.assertEquals("Tim", opgeslagenLeerkracht.getVoornaam());
        Assertions.assertEquals("Van Herreweghe", opgeslagenLeerkracht.getAchternaam());

        // Rechtstreeks controleren in PostgreSQL
        String sql = """
                SELECT voornaam, achternaam
                FROM leerkrachten
                WHERE leerkracht_id = ?
                """;

        try (
                Connection connection = DatabaseConnectionFactory.maakVerbinding();

                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, opgeslagenLeerkracht.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                Assertions.assertTrue(resultSet.next(), "De leerkracht werd niet teruggevonden in de databank");
                Assertions.assertEquals("Tim", resultSet.getString("voornaam"));
                Assertions.assertEquals("Van Herreweghe", resultSet.getString("achternaam"));
                Assertions.assertFalse(resultSet.next(), "Er werden meerdere leerkrachten met hetzelfde ID gevonden");
            }
        }
    }

    @Test
    void zoekAlleGeeftLeerkrachtenAlfabetischTerug() {
        // Arrange: bewust in een andere volgorde opslaan
        repository.save(
                new Leerkracht("Tim", "Peeters")
        );

        repository.save(
                new Leerkracht("Bram", "Janssens")
        );

        repository.save(
                new Leerkracht("Anna", "Janssens")
        );

        // Act
        List<Leerkracht> resultaat =
                repository.zoekAlle();

        // Assert
        Assertions.assertEquals(3, resultaat.size());

        Assertions.assertEquals(
                "Anna",
                resultaat.get(0).getVoornaam()
        );
        Assertions.assertEquals(
                "Janssens",
                resultaat.get(0).getAchternaam()
        );

        Assertions.assertEquals(
                "Bram",
                resultaat.get(1).getVoornaam()
        );
        Assertions.assertEquals(
                "Janssens",
                resultaat.get(1).getAchternaam()
        );

        Assertions.assertEquals(
                "Tim",
                resultaat.get(2).getVoornaam()
        );
        Assertions.assertEquals(
                "Peeters",
                resultaat.get(2).getAchternaam()
        );
    }

    @Test
    void zoekAlleGeeftLegeLijstWanneerErGeenLeerkrachtenZijn() {
        // Act
        List<Leerkracht> resultaat =
                repository.zoekAlle();

        // Assert
        Assertions.assertNotNull(resultaat);
        Assertions.assertTrue(resultaat.isEmpty());
    }

    @Test
    void saveMetNullLeerkrachtWordtGeweigerd() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(null)
        );
    }

    @Test
    void zoekOpIdGeeftLeerkrachtTerug(){
//        ARRANGE
        Leerkracht opgeslagenLeerkracht = repository.save(new Leerkracht("Tim", "Van Herreweghe"));

//        ACT
        Leerkracht resultaatLeerkracht = repository.zoekOpId(opgeslagenLeerkracht.getId());

//        ASSERT
        Assertions.assertNotNull(resultaatLeerkracht);
        Assertions.assertEquals(opgeslagenLeerkracht, resultaatLeerkracht);
        Assertions.assertEquals(opgeslagenLeerkracht.getId(), resultaatLeerkracht.getId());
        Assertions.assertEquals("Tim", resultaatLeerkracht.getVoornaam());
        Assertions.assertEquals("Van Herreweghe", resultaatLeerkracht.getAchternaam());
    }

    @Test
    public void zoekOpIdMetOngeldigIdGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.zoekOpId(0)
        );
    }

    @Test
    public void updateWijzigtNaamEnBeschrijvingVanLeerkracht() {
        // ARRANGE
        Leerkracht opgeslagenLeerkracht = repository.save(new Leerkracht("Tim", "Van Herreweghe"));
        opgeslagenLeerkracht.wijzigGegevens("Jos", "Van Herreweghe");

//        ACT
        repository.update(opgeslagenLeerkracht);

//        ASSERT
        Leerkracht aangepasteLeerkracht = repository.zoekOpId(opgeslagenLeerkracht.getId());

        Assertions.assertEquals(opgeslagenLeerkracht.getId(), aangepasteLeerkracht.getId());
        Assertions.assertEquals("Jos", aangepasteLeerkracht.getVoornaam());
        Assertions.assertEquals("Van Herreweghe", aangepasteLeerkracht.getAchternaam());
        }

    @Test
    void updateWijzigtActieveStatusVanLeerkracht() {
        // ARRANGE
        Leerkracht opgeslagenLeerkracht =
                repository.save(new Leerkracht("Tim", "Van Herreweghe"));

        opgeslagenLeerkracht.deactiveer();

        // ACT
        repository.update(opgeslagenLeerkracht);

        // ASSERT
        Leerkracht opgehaaldeLeerkracht =
                repository.zoekOpId(opgeslagenLeerkracht.getId());

        Assertions.assertFalse(opgehaaldeLeerkracht.isActief());
    }
    @Test
    void zoekAlleBehoudtInactieveStatusVanLeerkracht() throws SQLException {
        // ARRANGE
        Leerkracht opgeslagenLeerkracht =
                repository.save(new Leerkracht("Tim", "Van Herreweghe"));

        String sql = """
            UPDATE leerkrachten
            SET actief = FALSE
            WHERE leerkracht_id = ?
            """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, opgeslagenLeerkracht.getId());
            statement.executeUpdate();
        }

        // ACT
        List<Leerkracht> resultaat = repository.zoekAlle();

        // ASSERT
        Assertions.assertEquals(1, resultaat.size());
        Assertions.assertFalse(resultaat.getFirst().isActief());
    }
    }
