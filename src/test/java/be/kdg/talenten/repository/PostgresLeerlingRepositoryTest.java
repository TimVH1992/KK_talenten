package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.postgres.PostgresKlasRepository;
import be.kdg.talenten.repository.postgres.PostgresLeerlingRepository;
import be.kdg.talenten.repository.postgres.PostgresSchooljaarRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public class PostgresLeerlingRepositoryTest {

    private LeerlingRepository repository;
    private KlasRepository klasRepository;
    private Schooljaar schooljaar2026_2027;
    private Klas klas;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             Statement statement = connection.createStatement()) {

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

        PostgresSchooljaarRepository schooljaarRepository =
                new PostgresSchooljaarRepository();

        schooljaar2026_2027 = schooljaarRepository.save(
                new Schooljaar(
                        "2026-2027",
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2027, 6, 30),
                        true
                )
        );

        repository = new PostgresLeerlingRepository();
        klasRepository = new PostgresKlasRepository();

        klas = klasRepository.save(
                new Klas(
                        "2AA",
                        schooljaar2026_2027,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );
    }

    @Test
    void saveSlaatLeerlingOpEnGeeftDatabaseIdTerug() {
        // ARRANGE
        Leerling leerling =
                new Leerling("Test", "Leerling", klas);

        // ACT
        Leerling opgeslagenLeerling =
                repository.save(leerling);

        // ASSERT
        Assertions.assertNotNull(opgeslagenLeerling);
        Assertions.assertNotNull(opgeslagenLeerling.getId());
        Assertions.assertTrue(opgeslagenLeerling.getId() > 0);
    }

    @Test
    void zoekVoorKlasGeeftLijstLeerlingenTerug() {
        // ARRANGE
        Leerling tim =
                new Leerling("Tim", "VH", klas);

        Leerling eveline =
                new Leerling("Eveline", "VO", klas);

        repository.save(tim);
        repository.save(eveline);

        // ACT
        List<Leerling> resultaat =
                repository.zoekVoorKlas(klas);

        // ASSERT
        Assertions.assertEquals(2, resultaat.size());
        Assertions.assertEquals("Tim", resultaat.get(0).getVoornaam());
        Assertions.assertEquals("Eveline", resultaat.get(1).getVoornaam());
    }

    @Test
    void zoekLeerlingOpIdTest() {
        // ARRANGE
        Leerling test =
                new Leerling("Test", "Leerling", klas);

        Leerling opgeslagenLeerling =
                repository.save(test);

        // ACT
        Leerling gevondenLeerling =
                repository.zoekOpId(opgeslagenLeerling.getId());

        // ASSERT
        Assertions.assertNotNull(gevondenLeerling);
        Assertions.assertEquals(
                opgeslagenLeerling,
                gevondenLeerling
        );

        Assertions.assertEquals(
                opgeslagenLeerling.getId(),
                gevondenLeerling.getId()
        );

        Assertions.assertEquals(
                "Test",
                gevondenLeerling.getVoornaam()
        );

        Assertions.assertEquals(
                "Leerling",
                gevondenLeerling.getAchternaam()
        );

        Assertions.assertEquals(
                klas,
                gevondenLeerling.getKlas()
        );

        Assertions.assertEquals(
                schooljaar2026_2027,
                gevondenLeerling.getKlas().getSchooljaar()
        );
    }
}