package be.kdg.talenten.service;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.KlasRepository;
import be.kdg.talenten.repository.LeerlingKlasHistoriekRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.postgres.PostgresKlasRepository;
import be.kdg.talenten.repository.postgres.PostgresLeerlingKlasHistoriekRepository;
import be.kdg.talenten.repository.postgres.PostgresLeerlingRepository;
import be.kdg.talenten.repository.postgres.PostgresSchooljaarRepository;
import be.kdg.talenten.service.beheer.LeerlingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public class LeerlingKlasServiceTest {

    private LeerlingRepository leerlingRepository;
    private KlasRepository klasRepository;
    private LeerlingKlasHistoriekRepository leerlingKlasHistoriekRepository;
    private LeerlingService leerlingService;

    private Schooljaar schooljaar2026_2027;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    TRUNCATE TABLE
                        leerling_klas_historiek,
                        ingericht_talent_leerkrachten,
                        voorkeuren,
                        toewijzingen,
                        leerlingen,
                        ingerichte_talenten,
                        leerkrachten,
                        talenten,
                        talenten_periodes,
                        klassen,
                        schooljaren
                    RESTART IDENTITY CASCADE
                    """);
        }

        PostgresSchooljaarRepository schooljaarRepository =
                new PostgresSchooljaarRepository();

        klasRepository =
                new PostgresKlasRepository();

        leerlingRepository =
                new PostgresLeerlingRepository();

        leerlingKlasHistoriekRepository =
                new PostgresLeerlingKlasHistoriekRepository();

        schooljaar2026_2027 = schooljaarRepository.save(
                new Schooljaar(
                        "2026-2027",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2027, 6, 30),
                        true
                )
        );

        leerlingService = new LeerlingService(
                leerlingRepository,
                leerlingKlasHistoriekRepository
        );
    }

    @Test
    void wijzigKlasBehoudtVorigeKlasInHistoriek() {
        // ARRANGE
        Klas klas2A = klasRepository.save(
                new Klas(
                        "2A",
                        schooljaar2026_2027,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        Klas klas2B = klasRepository.save(
                new Klas(
                        "2B",
                        schooljaar2026_2027,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        Leerling leerling = leerlingRepository.save(
                new Leerling("Jan", "Peeters", klas2A)
        );

        LocalDate startdatum =
                LocalDate.of(2026, 9, 1);

        LocalDate wisseldatum =
                LocalDate.of(2027, 1, 15);

        leerlingKlasHistoriekRepository.startHistoriek(
                leerling,
                klas2A,
                startdatum
        );

        // ACT
        leerlingService.wijzigKlas(
                leerling,
                klas2B,
                wisseldatum
        );

        // ASSERT
        Leerling resultaat =
                leerlingRepository.zoekOpId(leerling.getId());

        Assertions.assertEquals(
                klas2B.getId(),
                resultaat.getKlas().getId()
        );

        List<LeerlingKlasHistoriek> historiek =
                leerlingKlasHistoriekRepository.zoekVoorLeerling(leerling);

        Assertions.assertEquals(2, historiek.size());

        LeerlingKlasHistoriek oudeKlas =
                historiek.get(0);

        Assertions.assertEquals(
                klas2A.getId(),
                oudeKlas.getKlas().getId()
        );

        Assertions.assertEquals(
                startdatum,
                oudeKlas.getVanaf()
        );

        Assertions.assertEquals(
                wisseldatum,
                oudeKlas.getTot()
        );

        LeerlingKlasHistoriek nieuweKlas =
                historiek.get(1);

        Assertions.assertEquals(
                klas2B.getId(),
                nieuweKlas.getKlas().getId()
        );

        Assertions.assertEquals(
                wisseldatum,
                nieuweKlas.getVanaf()
        );

        Assertions.assertNull(
                nieuweKlas.getTot()
        );

        Assertions.assertTrue(
                nieuweKlas.isHuidig()
        );
    }
}