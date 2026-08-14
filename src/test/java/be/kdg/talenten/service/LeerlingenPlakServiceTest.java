package be.kdg.talenten.service;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.service.beheer.LeerlingService;
import be.kdg.talenten.service.leerling.LeerlingenPlakResultaat;
import be.kdg.talenten.service.leerling.LeerlingenPlakService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class LeerlingenPlakServiceTest {

    private TestLeerlingRepository repository;
    private LeerlingenPlakService plakService;
    private Klas klas;

    @BeforeEach
    void setUp() {
        repository = new TestLeerlingRepository();

        LeerlingService leerlingService = new LeerlingService(repository);
        plakService = new LeerlingenPlakService(leerlingService);

        Schooljaar schooljaar = new Schooljaar(
                1L,
                "2026-2027",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2027, 6, 30),
                true
        );

        klas = new Klas(
                1L,
                "1AA",
                schooljaar,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
    }

    @Test
    void analyseerMetGeldigeLeerlingenGeeftGeldigeRegelsTerug() {
        // ARRANGE
        String tekst = """
                Jan\tPeeters
                Sofie\tJanssens
                Mohamed\tEl Amrani
                """;

        // ACT
        LeerlingenPlakResultaat resultaat = plakService.analyseer(tekst);

        // ASSERT
        Assertions.assertFalse(resultaat.heeftProblemen());
        Assertions.assertEquals(3, resultaat.getAantalGeldigeLeerlingen());

        Assertions.assertEquals("Jan", resultaat.getGeldigeRegels().get(0).voornaam());
        Assertions.assertEquals("Peeters", resultaat.getGeldigeRegels().get(0).achternaam());

        Assertions.assertEquals("Sofie", resultaat.getGeldigeRegels().get(1).voornaam());
        Assertions.assertEquals("Janssens", resultaat.getGeldigeRegels().get(1).achternaam());

        Assertions.assertEquals("Mohamed", resultaat.getGeldigeRegels().get(2).voornaam());
        Assertions.assertEquals("El Amrani", resultaat.getGeldigeRegels().get(2).achternaam());
    }

    @Test
    void analyseerSlaatHoofdingOver() {
        // ARRANGE
        String tekst = """
                Voornaam\tAchternaam
                Jan\tPeeters
                Sofie\tJanssens
                """;

        // ACT
        LeerlingenPlakResultaat resultaat = plakService.analyseer(tekst);

        // ASSERT
        Assertions.assertFalse(resultaat.heeftProblemen());
        Assertions.assertEquals(2, resultaat.getAantalGeldigeLeerlingen());

        Assertions.assertEquals("Jan", resultaat.getGeldigeRegels().getFirst().voornaam());
        Assertions.assertEquals("Peeters", resultaat.getGeldigeRegels().getFirst().achternaam());
    }

    @Test
    void analyseerSlaatLegeRijenOver() {
        // ARRANGE
        String tekst = "Jan\tPeeters\n\nSofie\tJanssens";

        // ACT
        LeerlingenPlakResultaat resultaat = plakService.analyseer(tekst);

        // ASSERT
        Assertions.assertFalse(resultaat.heeftProblemen());
        Assertions.assertEquals(2, resultaat.getAantalGeldigeLeerlingen());
    }

    @Test
    void analyseerMetOntbrekendeAchternaamGeeftProbleem() {
        // ARRANGE
        String tekst = "Jan\tPeeters\nSofie\t";

        // ACT
        LeerlingenPlakResultaat resultaat = plakService.analyseer(tekst);

        // ASSERT
        Assertions.assertTrue(resultaat.heeftProblemen());
        Assertions.assertEquals(1, resultaat.getAantalGeldigeLeerlingen());
        Assertions.assertEquals(1, resultaat.getProblemen().size());

        Assertions.assertEquals(
                "Regel 2: achternaam ontbreekt",
                resultaat.getProblemen().getFirst()
        );
    }

    @Test
    void analyseerMetMeerDanTweeKolommenGeeftProbleem() {
        // ARRANGE
        String tekst = """
                Jan\tPeeters\tExtra
                Sofie\tJanssens
                """;

        // ACT
        LeerlingenPlakResultaat resultaat = plakService.analyseer(tekst);

        // ASSERT
        Assertions.assertTrue(resultaat.heeftProblemen());
        Assertions.assertEquals(1, resultaat.getAantalGeldigeLeerlingen());

        Assertions.assertEquals(
                "Regel 1 moet exact twee kolommen bevatten: voornaam en achternaam",
                resultaat.getProblemen().getFirst()
        );
    }

    @Test
    void slaLeerlingenOpSlaatAlleGeldigeLeerlingenOp() {
        // ARRANGE
        String tekst = """
                Jan\tPeeters
                Sofie\tJanssens
                Mohamed\tEl Amrani
                """;

        LeerlingenPlakResultaat analyseResultaat = plakService.analyseer(tekst);

        // ACT
        List<Leerling> opgeslagenLeerlingen = plakService.slaLeerlingenOp(
                klas,
                analyseResultaat
        );

        // ASSERT
        Assertions.assertEquals(3, opgeslagenLeerlingen.size());
        Assertions.assertEquals(3, repository.zoekVoorKlas(klas).size());

        Assertions.assertEquals("Jan", opgeslagenLeerlingen.get(0).getVoornaam());
        Assertions.assertEquals("Peeters", opgeslagenLeerlingen.get(0).getAchternaam());
        Assertions.assertEquals(klas, opgeslagenLeerlingen.get(0).getKlas());

        Assertions.assertNotNull(opgeslagenLeerlingen.get(0).getId());
        Assertions.assertNotNull(opgeslagenLeerlingen.get(1).getId());
        Assertions.assertNotNull(opgeslagenLeerlingen.get(2).getId());
    }

    @Test
    void slaLeerlingenOpMetProblemenGeeftExceptionEnSlaatNietsOp() {
        // ARRANGE
        String tekst = """
                Jan\tPeeters
                Sofie\t
                """;

        LeerlingenPlakResultaat analyseResultaat = plakService.analyseer(tekst);

        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> plakService.slaLeerlingenOp(klas, analyseResultaat)
        );

        Assertions.assertTrue(repository.zoekVoorKlas(klas).isEmpty());
    }

    private static class TestLeerlingRepository implements LeerlingRepository {

        private final List<Leerling> leerlingen = new ArrayList<>();
        private long volgendId = 1;

        @Override
        public List<Leerling> zoekVoorKlas(Klas klas) {
            return leerlingen.stream()
                    .filter(leerling -> leerling.getKlas().equals(klas))
                    .toList();
        }

        @Override
        public Leerling save(Leerling leerling) {
            Leerling opgeslagenLeerling = new Leerling(
                    volgendId++,
                    leerling.getVoornaam(),
                    leerling.getAchternaam(),
                    leerling.getKlas()
            );

            leerlingen.add(opgeslagenLeerling);

            return opgeslagenLeerling;
        }

        @Override
        public Leerling zoekOpId(long id) {
            return leerlingen.stream()
                    .filter(leerling -> leerling.getId() != null && leerling.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Leerling niet gevonden"));
        }

        @Override
        public List<Leerling> zoekVoorSchooljaar(Schooljaar schooljaar) {
            return leerlingen.stream()
                    .filter(leerling -> leerling.getKlas().getSchooljaar().equals(schooljaar))
                    .toList();
        }

        @Override
        public void update(Leerling leerling) {
            for (int i = 0; i < leerlingen.size(); i++) {
                if (leerlingen.get(i).getId().equals(leerling.getId())) {
                    leerlingen.set(i, leerling);
                    return;
                }
            }

            throw new IllegalStateException("Leerling niet gevonden");
        }
    }
}