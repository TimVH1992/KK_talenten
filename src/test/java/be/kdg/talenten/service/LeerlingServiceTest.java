package be.kdg.talenten.service;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.service.beheer.LeerlingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LeerlingServiceTest {

    private TestLeerlingRepository repository;
    private LeerlingService service;
    private Schooljaar schooljaar;
    private Klas klas2A;
    private Klas klas2B;

    @BeforeEach
    void setUp() {
        repository = new TestLeerlingRepository();
        service = new LeerlingService(repository);

        schooljaar = new Schooljaar(
                1L,
                "2026-2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 6, 30),
                true
        );

        klas2A = new Klas(
                1L,
                "2A",
                schooljaar,
                2,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        klas2B = new Klas(
                2L,
                "2B",
                schooljaar,
                2,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
    }

    @Test
    void geefLeerlingenVoorSchooljaarGeeftLeerlingenUitRepositoryTerug() {
        // ARRANGE
        repository.save(new Leerling("Jan", "Peeters", klas2A));
        repository.save(new Leerling("Sofie", "Janssens", klas2B));

        // ACT
        List<Leerling> resultaat = service.geefLeerlingenVoorSchooljaar(schooljaar);

        // ASSERT
        Assertions.assertEquals(2, resultaat.size());
        Assertions.assertEquals("Jan", resultaat.get(0).getVoornaam());
        Assertions.assertEquals("Peeters", resultaat.get(0).getAchternaam());
        Assertions.assertEquals("Sofie", resultaat.get(1).getVoornaam());
        Assertions.assertEquals("Janssens", resultaat.get(1).getAchternaam());
    }

    @Test
    void maakLeerlingSlaatNieuweLeerlingOp() {
        // ACT
        Leerling resultaat = service.maakLeerling(
                "Jan",
                "Peeters",
                klas2A
        );

        // ASSERT
        Assertions.assertNotNull(resultaat);
        Assertions.assertNotNull(resultaat.getId());
        Assertions.assertEquals("Jan", resultaat.getVoornaam());
        Assertions.assertEquals("Peeters", resultaat.getAchternaam());
        Assertions.assertEquals(klas2A, resultaat.getKlas());

        Assertions.assertTrue(repository.isSaveAangeroepen());
        Assertions.assertEquals(1, repository.zoekVoorSchooljaar(schooljaar).size());
    }

    @Test
    void wijzigLeerlingWijzigtGegevensEnKlasEnUpdateRepository() {
        // ARRANGE
        Leerling opgeslagenLeerling = repository.save(
                new Leerling("Jan", "Peeters", klas2A)
        );

        // ACT
        service.wijzigLeerling(
                opgeslagenLeerling,
                "Janne",
                "Peeters",
                klas2B
        );

        // ASSERT
        Leerling aangepasteLeerling = repository.zoekOpId(
                opgeslagenLeerling.getId()
        );

        Assertions.assertTrue(repository.isUpdateAangeroepen());
        Assertions.assertEquals("Janne", aangepasteLeerling.getVoornaam());
        Assertions.assertEquals("Peeters", aangepasteLeerling.getAchternaam());
        Assertions.assertEquals(klas2B.getId(), aangepasteLeerling.getKlas().getId());
        Assertions.assertEquals("2B", aangepasteLeerling.getKlas().getNaam());
    }

    @Test
    void wijzigLeerlingMetNullGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigLeerling(
                        null,
                        "Jan",
                        "Peeters",
                        klas2A
                )
        );
    }

    @Test
    void constructorMetNullRepositoryGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LeerlingService(null)
        );
    }

    private static class TestLeerlingRepository implements LeerlingRepository {

        private final List<Leerling> leerlingen = new ArrayList<>();
        private long volgendId = 1;
        private boolean saveAangeroepen;
        private boolean updateAangeroepen;

        @Override
        public List<Leerling> zoekVoorKlas(Klas klas) {
            List<Leerling> resultaat = new ArrayList<>();

            for (Leerling leerling : leerlingen) {
                if (leerling.getKlas().equals(klas)) {
                    resultaat.add(kopieVan(leerling));
                }
            }

            return resultaat;
        }

        @Override
        public Leerling save(Leerling leerling) {
            saveAangeroepen = true;

            Leerling opgeslagenLeerling = new Leerling(
                    volgendId++,
                    leerling.getVoornaam(),
                    leerling.getAchternaam(),
                    leerling.getKlas()
            );

            leerlingen.add(opgeslagenLeerling);

            return kopieVan(opgeslagenLeerling);
        }

        @Override
        public Leerling zoekOpId(long id) {
            for (Leerling leerling : leerlingen) {
                if (leerling.getId() != null && leerling.getId() == id) {
                    return kopieVan(leerling);
                }
            }

            throw new IllegalStateException(
                    "Geen leerling gevonden met id: " + id
            );
        }

        @Override
        public List<Leerling> zoekVoorSchooljaar(Schooljaar schooljaar) {
            List<Leerling> resultaat = new ArrayList<>();

            for (Leerling leerling : leerlingen) {
                if (leerling.getKlas().getSchooljaar().equals(schooljaar)) {
                    resultaat.add(kopieVan(leerling));
                }
            }

            return resultaat;
        }

        @Override
        public void update(Leerling leerling) {
            updateAangeroepen = true;

            for (int i = 0; i < leerlingen.size(); i++) {
                Leerling opgeslagenLeerling = leerlingen.get(i);

                if (opgeslagenLeerling.getId().equals(leerling.getId())) {
                    leerlingen.set(i, kopieVan(leerling));
                    return;
                }
            }

            throw new IllegalStateException(
                    "Geen leerling gevonden met id: " + leerling.getId()
            );
        }

        public boolean isSaveAangeroepen() {
            return saveAangeroepen;
        }

        public boolean isUpdateAangeroepen() {
            return updateAangeroepen;
        }

        private Leerling kopieVan(Leerling leerling) {
            return new Leerling(
                    leerling.getId(),
                    leerling.getVoornaam(),
                    leerling.getAchternaam(),
                    leerling.getKlas()
            );
        }
    }
}