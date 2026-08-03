package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.repository.postgres.PostgresKlasRepository;
import be.kdg.talenten.repository.postgres.PostgresLeerlingRepository;
import be.kdg.talenten.repository.postgres.PostgresTalentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

public class PostgresTalentRepositoryTest {
    private TalentRepository repository;

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

        repository = new PostgresTalentRepository();
    }

    @Test
    public void saveSlaatTalentOpEnGeeftDatabaseIdTerug() throws SQLException {
//        ARRANGE & ACT
        Talent opgeslagenTalent = repository.save(new Talent("Schaken", "Strategisch denkspel"));
//        ASSERT
        Assertions.assertNotNull(opgeslagenTalent);
        Assertions.assertTrue(opgeslagenTalent.getId() > 0);
        Assertions.assertEquals("Schaken", opgeslagenTalent.getNaam());

        String sql = """
                SELECT naam, beschrijving
                FROM talenten
                WHERE talent_id = ?
                """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, opgeslagenTalent.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                Assertions.assertTrue(resultSet.next());
                Assertions.assertEquals(
                        "Schaken",
                        resultSet.getString("naam")
                );
                Assertions.assertEquals(
                        "Strategisch denkspel",
                        resultSet.getString("beschrijving")
                );
            }
        }
    }
    @Test
    public void zoekAlleTalentenGeeftLijstTerug(){
//        Arrange
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent dansen = new Talent("Dansen", "Bewegen op het ritme van de muziek");
        Talent voetbal = new Talent("Voetbal", "Balsport");
//        ACT

        Talent opgeslagenSchaken = repository.save(schaken);
        Talent opgeslagenDansen = repository.save(dansen);
        Talent opgeslagenVoetbal = repository.save(voetbal);

        List<Talent> resultaat = List.of(opgeslagenDansen, opgeslagenSchaken,opgeslagenVoetbal);
//        ASSERT
        Assertions.assertNotNull(resultaat);
        Assertions.assertEquals(resultaat, repository.zoekAlle());
    }
}
