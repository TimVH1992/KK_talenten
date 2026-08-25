package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.LeerlingKlasHistoriekRepository;
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
    private TestLeerlingKlasHistoriekRepository historiekRepository;
    private LeerlingService service;

    private Schooljaar schooljaar;
    private Klas klas2A;
    private Klas klas2B;

    @BeforeEach
    void setUp() {
        repository = new TestLeerlingRepository();
        historiekRepository = new TestLeerlingKlasHistoriekRepository();

        service = new LeerlingService(
                repository,
                historiekRepository
        );

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
        repository.save(
                new Leerling("Jan", "Peeters", klas2A)
        );

        repository.save(
                new Leerling("Sofie", "Janssens", klas2B)
        );

        // ACT
        List<Leerling> resultaat =
                service.geefLeerlingenVoorSchooljaar(schooljaar);

        // ASSERT
        Assertions.assertEquals(2, resultaat.size());

        Assertions.assertEquals(
                "Jan",
                resultaat.get(0).getVoornaam()
        );

        Assertions.assertEquals(
                "Peeters",
                resultaat.get(0).getAchternaam()
        );

        Assertions.assertEquals(
                "Sofie",
                resultaat.get(1).getVoornaam()
        );

        Assertions.assertEquals(
                "Janssens",
                resultaat.get(1).getAchternaam()
        );
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

        Assertions.assertEquals(
                "Jan",
                resultaat.getVoornaam()
        );

        Assertions.assertEquals(
                "Peeters",
                resultaat.getAchternaam()
        );

        Assertions.assertEquals(
                klas2A,
                resultaat.getKlas()
        );

        Assertions.assertTrue(
                repository.isSaveAangeroepen()
        );

        Assertions.assertEquals(
                1,
                repository.zoekVoorSchooljaar(schooljaar).size()
        );
    }

    @Test
    void maakLeerlingStartAutomatischKlasHistoriek() {
        // ACT
        Leerling leerling = service.maakLeerling(
                "Jan",
                "Peeters",
                klas2A
        );

        // ASSERT
        List<LeerlingKlasHistoriek> historiek =
                historiekRepository.zoekVoorLeerling(leerling);

        Assertions.assertEquals(1, historiek.size());

        LeerlingKlasHistoriek registratie =
                historiek.getFirst();

        Assertions.assertEquals(
                leerling.getId(),
                registratie.getLeerling().getId()
        );

        Assertions.assertEquals(
                klas2A.getId(),
                registratie.getKlas().getId()
        );

        Assertions.assertEquals(
                schooljaar.getStartDatum(),
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
    void wijzigLeerlingWijzigtNaamEnUpdateRepository() {
        // ARRANGE
        Leerling opgeslagenLeerling = service.maakLeerling(
                "Jan",
                "Peeters",
                klas2A
        );

        // ACT
        service.wijzigLeerling(
                opgeslagenLeerling,
                "Janne",
                "Peeters"
        );

        // ASSERT
        Leerling aangepasteLeerling =
                repository.zoekOpId(
                        opgeslagenLeerling.getId()
                );

        Assertions.assertTrue(
                repository.isUpdateAangeroepen()
        );

        Assertions.assertEquals(
                "Janne",
                aangepasteLeerling.getVoornaam()
        );

        Assertions.assertEquals(
                "Peeters",
                aangepasteLeerling.getAchternaam()
        );

        Assertions.assertEquals(
                klas2A,
                aangepasteLeerling.getKlas()
        );
    }

    @Test
    void wijzigKlasBehoudtVorigeKlasInHistoriek() {
        // ARRANGE
        Leerling leerling = service.maakLeerling(
                "Jan",
                "Peeters",
                klas2A
        );

        LocalDate wisseldatum =
                LocalDate.of(2027, 1, 15);

        // ACT
        service.wijzigKlas(
                leerling,
                klas2B,
                wisseldatum
        );

        // ASSERT
        Leerling resultaat =
                repository.zoekOpId(leerling.getId());

        Assertions.assertEquals(
                klas2B.getId(),
                resultaat.getKlas().getId()
        );

        List<LeerlingKlasHistoriek> historiek =
                historiekRepository.zoekVoorLeerling(leerling);

        Assertions.assertEquals(
                2,
                historiek.size()
        );

        LeerlingKlasHistoriek oudeKlas =
                historiek.get(0);

        Assertions.assertEquals(
                klas2A.getId(),
                oudeKlas.getKlas().getId()
        );

        Assertions.assertEquals(
                schooljaar.getStartDatum(),
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

    @Test
    void wijzigLeerlingMetNullGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigLeerling(
                        null,
                        "Jan",
                        "Peeters"
                )
        );
    }

    @Test
    void wijzigKlasNaarDezelfdeKlasGeeftException() {
        // ARRANGE
        Leerling leerling = service.maakLeerling(
                "Jan",
                "Peeters",
                klas2A
        );

        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigKlas(
                        leerling,
                        klas2A,
                        LocalDate.of(2027, 1, 15)
                )
        );
    }

    @Test
    void constructorMetNullLeerlingRepositoryGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LeerlingService(
                        null,
                        historiekRepository
                )
        );
    }

    @Test
    void constructorMetNullHistoriekRepositoryGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LeerlingService(
                        repository,
                        null
                )
        );
    }

    private static class TestLeerlingRepository
            implements LeerlingRepository {

        private final List<Leerling> leerlingen =
                new ArrayList<>();

        private long volgendId = 1;

        private boolean saveAangeroepen;
        private boolean updateAangeroepen;

        @Override
        public List<Leerling> zoekVoorKlas(Klas klas) {
            List<Leerling> resultaat =
                    new ArrayList<>();

            for (Leerling leerling : leerlingen) {
                if (leerling.getKlas().equals(klas)) {
                    resultaat.add(
                            kopieVan(leerling)
                    );
                }
            }

            return resultaat;
        }

        @Override
        public Leerling save(Leerling leerling) {
            saveAangeroepen = true;

            Leerling opgeslagenLeerling =
                    new Leerling(
                            volgendId++,
                            leerling.getVoornaam(),
                            leerling.getAchternaam(),
                            leerling.getKlas(),
                            leerling.isActief()
                    );

            leerlingen.add(
                    opgeslagenLeerling
            );

            return kopieVan(
                    opgeslagenLeerling
            );
        }

        @Override
        public Leerling zoekOpId(long id) {
            for (Leerling leerling : leerlingen) {
                if (
                        leerling.getId() != null
                                && leerling.getId() == id
                ) {
                    return kopieVan(leerling);
                }
            }

            throw new IllegalStateException(
                    "Geen leerling gevonden met id: " + id
            );
        }

        @Override
        public List<Leerling> zoekVoorSchooljaar(
                Schooljaar schooljaar
        ) {
            List<Leerling> resultaat =
                    new ArrayList<>();

            for (Leerling leerling : leerlingen) {
                if (
                        leerling.getKlas()
                                .getSchooljaar()
                                .equals(schooljaar)
                ) {
                    resultaat.add(
                            kopieVan(leerling)
                    );
                }
            }

            return resultaat;
        }

        @Override
        public void update(Leerling leerling) {
            updateAangeroepen = true;

            for (int i = 0; i < leerlingen.size(); i++) {
                Leerling opgeslagenLeerling =
                        leerlingen.get(i);

                if (
                        opgeslagenLeerling
                                .getId()
                                .equals(leerling.getId())
                ) {
                    leerlingen.set(
                            i,
                            kopieVan(leerling)
                    );

                    return;
                }
            }

            throw new IllegalStateException(
                    "Geen leerling gevonden met id: "
                            + leerling.getId()
            );
        }

        public boolean isSaveAangeroepen() {
            return saveAangeroepen;
        }

        public boolean isUpdateAangeroepen() {
            return updateAangeroepen;
        }

        private Leerling kopieVan(
                Leerling leerling
        ) {
            return new Leerling(
                    leerling.getId(),
                    leerling.getVoornaam(),
                    leerling.getAchternaam(),
                    leerling.getKlas(),
                    leerling.isActief()
            );
        }
    }

    private static class TestLeerlingKlasHistoriekRepository
            implements LeerlingKlasHistoriekRepository {

        private final List<LeerlingKlasHistoriek> historiek =
                new ArrayList<>();

        private long volgendId = 1;

        @Override
        public void startHistoriek(
                Leerling leerling,
                Klas klas,
                LocalDate vanaf
        ) {
            historiek.add(
                    new LeerlingKlasHistoriek(
                            volgendId++,
                            leerling,
                            klas,
                            vanaf,
                            null
                    )
            );
        }

        @Override
        public void sluitHuidigeHistoriekAf(
                Leerling leerling,
                LocalDate tot
        ) {
            for (int i = 0; i < historiek.size(); i++) {
                LeerlingKlasHistoriek registratie =
                        historiek.get(i);

                if (
                        registratie
                                .getLeerling()
                                .equals(leerling)
                                && registratie.isHuidig()
                ) {
                    historiek.set(
                            i,
                            new LeerlingKlasHistoriek(
                                    registratie.getId(),
                                    registratie.getLeerling(),
                                    registratie.getKlas(),
                                    registratie.getVanaf(),
                                    tot
                            )
                    );

                    return;
                }
            }

            throw new IllegalStateException(
                    "Geen huidige klashistoriek gevonden."
            );
        }

        @Override
        public List<LeerlingKlasHistoriek> zoekVoorLeerling(
                Leerling leerling
        ) {
            return historiek.stream()
                    .filter(registratie ->
                            registratie
                                    .getLeerling()
                                    .equals(leerling)
                    )
                    .toList();
        }
        @Override
        public List<LeerlingKlasHistoriek> zoekVoorKlasOpDatum(
                Klas klas,
                LocalDate datum
        ) {
            return historiek.stream()
                    .filter(registratie ->
                            registratie.getKlas().equals(klas)
                                    && !registratie.getVanaf().isAfter(datum)
                                    && (
                                    registratie.getTot() == null
                                            || registratie.getTot().isAfter(datum)
                            )
                    )
                    .toList();
        }
    }
    @Test
    void deactiveerLeerlingZetLeerlingOpNietActiefEnUpdateRepository() {
        // ARRANGE
        Leerling leerling = service.maakLeerling(
                "Jan",
                "Peeters",
                klas2A
        );

        Assertions.assertTrue(leerling.isActief());

        // ACT
        service.deactiveerLeerling(leerling);

        // ASSERT
        Leerling resultaat = repository.zoekOpId(
                leerling.getId()
        );

        Assertions.assertFalse(
                resultaat.isActief()
        );

        Assertions.assertTrue(
                repository.isUpdateAangeroepen()
        );
    }

    @Test
    void activeerLeerlingZetLeerlingTerugOpActief() {
        // ARRANGE
        Leerling leerling = service.maakLeerling(
                "Jan",
                "Peeters",
                klas2A
        );

        service.deactiveerLeerling(leerling);

        Assertions.assertFalse(
                repository.zoekOpId(leerling.getId()).isActief()
        );

        // ACT
        service.activeerLeerling(leerling);

        // ASSERT
        Leerling resultaat = repository.zoekOpId(
                leerling.getId()
        );

        Assertions.assertTrue(
                resultaat.isActief()
        );
    }
    @Test
    void geefActieveLeerlingenVoorSchooljaarGeeftEnkelActieveLeerlingenTerug() {
        // ARRANGE
        Leerling jan = service.maakLeerling(
                "Jan",
                "Peeters",
                klas2A
        );

        Leerling sofie = service.maakLeerling(
                "Sofie",
                "Janssens",
                klas2B
        );

        service.deactiveerLeerling(jan);

        // ACT
        List<Leerling> resultaat =
                service.geefActieveLeerlingenVoorSchooljaar(
                        schooljaar
                );

        // ASSERT
        Assertions.assertEquals(
                1,
                resultaat.size()
        );

        Assertions.assertEquals(
                sofie.getId(),
                resultaat.getFirst().getId()
        );

        Assertions.assertTrue(
                resultaat.getFirst().isActief()
        );
    }
    @Test
    void deactiveerNullLeerlingGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.deactiveerLeerling(null)
        );
    }
    @Test
    void activeerNullLeerlingGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.activeerLeerling(null)
        );
    }
}