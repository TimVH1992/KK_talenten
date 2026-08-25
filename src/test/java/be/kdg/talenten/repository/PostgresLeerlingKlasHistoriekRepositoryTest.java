package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.LeerlingKlasHistoriek;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.postgres.PostgresKlasRepository;
import be.kdg.talenten.repository.postgres.PostgresLeerlingKlasHistoriekRepository;
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

public class PostgresLeerlingKlasHistoriekRepositoryTest {

    private LeerlingKlasHistoriekRepository historiekRepository;
    private LeerlingRepository leerlingRepository;
    private KlasRepository klasRepository;

    private Schooljaar schooljaar;
    private Klas klas2A;
    private Leerling leerling;

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

        klasRepository = new PostgresKlasRepository();
        leerlingRepository = new PostgresLeerlingRepository();
        historiekRepository =
                new PostgresLeerlingKlasHistoriekRepository();

        schooljaar = schooljaarRepository.save(
                new Schooljaar(
                        "2026-2027",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2027, 6, 30),
                        true
                )
        );

        klas2A = klasRepository.save(
                new Klas(
                        "2A",
                        schooljaar,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        leerling = leerlingRepository.save(
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas2A
                )
        );
    }

    @Test
    void startHistoriekSlaatHuidigeKlasOp() {
        // ARRANGE
        LocalDate vanaf =
                LocalDate.of(2026, 9, 1);

        // ACT
        historiekRepository.startHistoriek(
                leerling,
                klas2A,
                vanaf
        );

        // ASSERT
        List<LeerlingKlasHistoriek> resultaat =
                historiekRepository.zoekVoorLeerling(leerling);

        Assertions.assertEquals(1, resultaat.size());

        LeerlingKlasHistoriek registratie =
                resultaat.getFirst();

        Assertions.assertEquals(
                leerling.getId(),
                registratie.getLeerling().getId()
        );

        Assertions.assertEquals(
                klas2A.getId(),
                registratie.getKlas().getId()
        );

        Assertions.assertEquals(
                vanaf,
                registratie.getVanaf()
        );

        Assertions.assertNull(
                registratie.getTot()
        );

        Assertions.assertTrue(
                registratie.isHuidig()
        );
    }

    @Test
    void sluitHuidigeHistoriekAfVultTotDatumIn() {
        // ARRANGE
        LocalDate vanaf =
                LocalDate.of(2026, 9, 1);

        LocalDate tot =
                LocalDate.of(2027, 1, 15);

        historiekRepository.startHistoriek(
                leerling,
                klas2A,
                vanaf
        );

        // ACT
        historiekRepository.sluitHuidigeHistoriekAf(
                leerling,
                tot
        );

        // ASSERT
        List<LeerlingKlasHistoriek> resultaat =
                historiekRepository.zoekVoorLeerling(leerling);

        Assertions.assertEquals(1, resultaat.size());

        LeerlingKlasHistoriek registratie =
                resultaat.getFirst();

        Assertions.assertEquals(
                vanaf,
                registratie.getVanaf()
        );

        Assertions.assertEquals(
                tot,
                registratie.getTot()
        );

        Assertions.assertFalse(
                registratie.isHuidig()
        );
    }

    @Test
    void zoekVoorLeerlingGeeftVolledigeHistoriekInVolgordeTerug() {
        // ARRANGE
        Klas klas2B = klasRepository.save(
                new Klas(
                        "2B",
                        schooljaar,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        LocalDate startdatum =
                LocalDate.of(2026, 9, 1);

        LocalDate wisseldatum =
                LocalDate.of(2027, 1, 15);

        historiekRepository.startHistoriek(
                leerling,
                klas2A,
                startdatum
        );

        historiekRepository.sluitHuidigeHistoriekAf(
                leerling,
                wisseldatum
        );

        historiekRepository.startHistoriek(
                leerling,
                klas2B,
                wisseldatum
        );

        // ACT
        List<LeerlingKlasHistoriek> resultaat =
                historiekRepository.zoekVoorLeerling(leerling);

        // ASSERT
        Assertions.assertEquals(2, resultaat.size());

        LeerlingKlasHistoriek eersteRegistratie =
                resultaat.get(0);

        Assertions.assertEquals(
                klas2A.getId(),
                eersteRegistratie.getKlas().getId()
        );

        Assertions.assertEquals(
                startdatum,
                eersteRegistratie.getVanaf()
        );

        Assertions.assertEquals(
                wisseldatum,
                eersteRegistratie.getTot()
        );

        LeerlingKlasHistoriek tweedeRegistratie =
                resultaat.get(1);

        Assertions.assertEquals(
                klas2B.getId(),
                tweedeRegistratie.getKlas().getId()
        );

        Assertions.assertEquals(
                wisseldatum,
                tweedeRegistratie.getVanaf()
        );

        Assertions.assertNull(
                tweedeRegistratie.getTot()
        );

        Assertions.assertTrue(
                tweedeRegistratie.isHuidig()
        );
    }
    @Test
    void tweedeHuidigeHistoriekVoorZelfdeLeerlingGeeftException() {
        // ARRANGE
        Klas klas2B = klasRepository.save(
                new Klas(
                        "2B",
                        schooljaar,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        historiekRepository.startHistoriek(
                leerling,
                klas2A,
                LocalDate.of(2026, 9, 1)
        );

        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> historiekRepository.startHistoriek(
                        leerling,
                        klas2B,
                        LocalDate.of(2027, 1, 15)
                )
        );
    }
    @Test
    void zoekVoorKlasOpDatumGeeftLeerlingDieOpDieDatumInKlasZat() {
        // ARRANGE
        historiekRepository.startHistoriek(
                leerling,
                klas2A,
                LocalDate.of(2026, 9, 1)
        );

        // ACT
        List<LeerlingKlasHistoriek> resultaat =
                historiekRepository.zoekVoorKlasOpDatum(
                        klas2A,
                        LocalDate.of(2026, 10, 15)
                );

        // ASSERT
        Assertions.assertEquals(
                1,
                resultaat.size()
        );

        Assertions.assertEquals(
                leerling.getId(),
                resultaat.getFirst()
                        .getLeerling()
                        .getId()
        );

        Assertions.assertEquals(
                klas2A.getId(),
                resultaat.getFirst()
                        .getKlas()
                        .getId()
        );
    }
    @Test
    void zoekVoorKlasOpDatumNeemtOudeKlasNietMeerMeeVanafWisseldatum() {
        // ARRANGE
        Klas klas2B = klasRepository.save(
                new Klas(
                        "2B",
                        schooljaar,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        LocalDate startdatum =
                LocalDate.of(2026, 9, 1);

        LocalDate wisseldatum =
                LocalDate.of(2027, 1, 15);

        historiekRepository.startHistoriek(
                leerling,
                klas2A,
                startdatum
        );

        historiekRepository.sluitHuidigeHistoriekAf(
                leerling,
                wisseldatum
        );

        historiekRepository.startHistoriek(
                leerling,
                klas2B,
                wisseldatum
        );

        // ACT
        List<LeerlingKlasHistoriek> oudeKlas =
                historiekRepository.zoekVoorKlasOpDatum(
                        klas2A,
                        wisseldatum
                );

        List<LeerlingKlasHistoriek> nieuweKlas =
                historiekRepository.zoekVoorKlasOpDatum(
                        klas2B,
                        wisseldatum
                );

        // ASSERT
        Assertions.assertTrue(
                oudeKlas.isEmpty()
        );

        Assertions.assertEquals(
                1,
                nieuweKlas.size()
        );

        Assertions.assertEquals(
                leerling.getId(),
                nieuweKlas.getFirst()
                        .getLeerling()
                        .getId()
        );
    }
    @Test
    void zoekVoorKlasOpDatumKanVorigeKlasHistorischTerugvinden() {
        // ARRANGE
        Klas klas2B = klasRepository.save(
                new Klas(
                        "2B",
                        schooljaar,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        LocalDate startdatum =
                LocalDate.of(2026, 9, 1);

        LocalDate wisseldatum =
                LocalDate.of(2027, 1, 15);

        historiekRepository.startHistoriek(
                leerling,
                klas2A,
                startdatum
        );

        historiekRepository.sluitHuidigeHistoriekAf(
                leerling,
                wisseldatum
        );

        historiekRepository.startHistoriek(
                leerling,
                klas2B,
                wisseldatum
        );

        // ACT
        List<LeerlingKlasHistoriek> resultaat =
                historiekRepository.zoekVoorKlasOpDatum(
                        klas2A,
                        LocalDate.of(2026, 11, 10)
                );

        // ASSERT
        Assertions.assertEquals(
                1,
                resultaat.size()
        );

        Assertions.assertEquals(
                leerling.getId(),
                resultaat.getFirst()
                        .getLeerling()
                        .getId()
        );

        Assertions.assertEquals(
                klas2A.getId(),
                resultaat.getFirst()
                        .getKlas()
                        .getId()
        );

        Assertions.assertEquals(
                startdatum,
                resultaat.getFirst()
                        .getVanaf()
        );

        Assertions.assertEquals(
                wisseldatum,
                resultaat.getFirst()
                        .getTot()
        );
    }
    @Test
    void zoekVoorKlasOpDatumMetNullKlasGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> historiekRepository.zoekVoorKlasOpDatum(
                        null,
                        LocalDate.of(2026, 10, 15)
                )
        );
    }

    @Test
    void zoekVoorKlasOpDatumMetNullDatumGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> historiekRepository.zoekVoorKlasOpDatum(
                        klas2A,
                        null
                )
        );
    }

}