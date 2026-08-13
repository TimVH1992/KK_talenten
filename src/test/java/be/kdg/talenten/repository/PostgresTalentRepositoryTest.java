package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.repository.postgres.PostgresTalentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class PostgresTalentRepositoryTest {

    private TalentRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    TRUNCATE TABLE
                        voorkeur_import_problemen,
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

        repository = new PostgresTalentRepository();
    }

    @Test
    public void saveSlaatTalentOpEnGeeftDatabaseIdTerug() throws SQLException {
        // ARRANGE & ACT
        Talent opgeslagenTalent = repository.save(new Talent("Schaken", "Strategisch denkspel"));

        // ASSERT
        Assertions.assertNotNull(opgeslagenTalent);
        Assertions.assertTrue(opgeslagenTalent.getId() > 0);
        Assertions.assertEquals("Schaken", opgeslagenTalent.getNaam());

        String sql = """
                SELECT naam, beschrijving
                FROM talenten
                WHERE talent_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, opgeslagenTalent.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                Assertions.assertTrue(resultSet.next());
                Assertions.assertEquals("Schaken", resultSet.getString("naam"));
                Assertions.assertEquals("Strategisch denkspel", resultSet.getString("beschrijving"));
            }
        }
    }

    @Test
    public void zoekAlleTalentenGeeftLijstTerug() {
        // ARRANGE
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent dansen = new Talent("Dansen", "Bewegen op het ritme van de muziek");
        Talent voetbal = new Talent("Voetbal", "Balsport");

        Talent opgeslagenSchaken = repository.save(schaken);
        Talent opgeslagenDansen = repository.save(dansen);
        Talent opgeslagenVoetbal = repository.save(voetbal);

        List<Talent> verwacht = List.of(opgeslagenDansen, opgeslagenSchaken, opgeslagenVoetbal);

        // ACT
        List<Talent> resultaat = repository.zoekAlle();

        // ASSERT
        Assertions.assertNotNull(resultaat);
        Assertions.assertEquals(verwacht, resultaat);
    }

    @Test
    public void zoekOpIdGeeftJuisteTalentTerug() {
        // ARRANGE
        Talent opgeslagenTalent = repository.save(new Talent("Schaken", "Strategisch denkspel"));

        // ACT
        Talent gevondenTalent = repository.zoekOpId(opgeslagenTalent.getId());

        // ASSERT
        Assertions.assertNotNull(gevondenTalent);
        Assertions.assertEquals(opgeslagenTalent.getId(), gevondenTalent.getId());
        Assertions.assertEquals("Schaken", gevondenTalent.getNaam());
        Assertions.assertEquals("Strategisch denkspel", gevondenTalent.getBeschrijving());
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
    public void updateWijzigtNaamEnBeschrijvingVanTalent() {
        // ARRANGE
        Talent opgeslagenTalent = repository.save(new Talent("Schaken", "Strategisch denkspel"));

        opgeslagenTalent.wijzigGegevens(
                "Schaken gevorderd",
                "Strategisch denkspel voor gevorderden"
        );

        // ACT
        repository.update(opgeslagenTalent);

        // ASSERT
        Talent aangepastTalent = repository.zoekOpId(opgeslagenTalent.getId());

        Assertions.assertEquals(opgeslagenTalent.getId(), aangepastTalent.getId());
        Assertions.assertEquals("Schaken gevorderd", aangepastTalent.getNaam());
        Assertions.assertEquals("Strategisch denkspel voor gevorderden", aangepastTalent.getBeschrijving());
    }

    @Test
    public void updateMetNullTalentGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.update(null)
        );
    }

    @Test
    public void updateVanNietOpgeslagenTalentGeeftException() {
        // ARRANGE
        Talent talent = new Talent("Dansen", "Bewegen op muziek");

        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> repository.update(talent)
        );
    }
}