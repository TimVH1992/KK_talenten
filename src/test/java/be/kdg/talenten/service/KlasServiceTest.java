package be.kdg.talenten.service;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.KlasRepository;
import be.kdg.talenten.service.beheer.KlasService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class KlasServiceTest {

    private TestKlasRepository repository;
    private KlasService service;
    private Schooljaar schooljaar;

    @BeforeEach
    void setUp() {
        repository = new TestKlasRepository();
        service = new KlasService(repository);

        schooljaar = new Schooljaar(
                1L,
                "2026-2027",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2027, 6, 30),
                true
        );
    }

    @Test
    void geefAlleKlassenGeeftKlassenUitRepositoryTerug() {
        // ARRANGE
        repository.save(
                new Klas(
                        "1AA",
                        schooljaar,
                        1,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        repository.save(
                new Klas(
                        "5AA",
                        schooljaar,
                        5,
                        Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB
                )
        );

        // ACT
        List<Klas> resultaat = service.geefAlleKlassen();

        // ASSERT
        Assertions.assertEquals(2, resultaat.size());

        Assertions.assertEquals(
                "1AA",
                resultaat.get(0).getNaam()
        );

        Assertions.assertEquals(
                "5AA",
                resultaat.get(1).getNaam()
        );
    }

    @Test
    void maakKlasSlaatNieuweKlasOp() {
        // ACT
        Klas resultaat = service.maakKlas(
                "2AA",
                schooljaar,
                2,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        // ASSERT
        Assertions.assertNotNull(resultaat);
        Assertions.assertNotNull(resultaat.getId());

        Assertions.assertEquals(
                "2AA",
                resultaat.getNaam()
        );

        Assertions.assertEquals(
                2,
                resultaat.getLeerjaar()
        );

        Assertions.assertEquals(
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                resultaat.getDoelgroep()
        );

        Assertions.assertEquals(
                schooljaar,
                resultaat.getSchooljaar()
        );

        Assertions.assertTrue(
                repository.isSaveAangeroepen()
        );

        Assertions.assertEquals(
                1,
                repository.zoekAlle().size()
        );
    }

    @Test
    void wijzigKlasWijzigtGegevensEnUpdateRepository() {
        // ARRANGE
        Klas opgeslagenKlas = repository.save(
                new Klas(
                        "2AA",
                        schooljaar,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        // ACT
        service.wijzigKlas(
                opgeslagenKlas,
                "3AB",
                3,
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB
        );

        // ASSERT
        Klas aangepasteKlas = repository.zoekOpId(
                opgeslagenKlas.getId()
        );

        Assertions.assertTrue(
                repository.isUpdateAangeroepen()
        );

        Assertions.assertEquals(
                "3AB",
                aangepasteKlas.getNaam()
        );

        Assertions.assertEquals(
                3,
                aangepasteKlas.getLeerjaar()
        );

        Assertions.assertEquals(
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB,
                aangepasteKlas.getDoelgroep()
        );

        Assertions.assertEquals(
                schooljaar,
                aangepasteKlas.getSchooljaar()
        );
    }

    @Test
    void verwijderKlasRoeptDeleteVanRepositoryAan() {
        // ARRANGE
        Klas opgeslagenKlas = repository.save(
                new Klas(
                        "2AA",
                        schooljaar,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        // ACT
        service.verwijderKlas(opgeslagenKlas);

        // ASSERT
        Assertions.assertTrue(
                repository.isDeleteAangeroepen()
        );

        Assertions.assertTrue(
                repository.zoekAlle().isEmpty()
        );
    }

    @Test
    void wijzigKlasMetNullGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigKlas(
                        null,
                        "2AA",
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );
    }

    @Test
    void verwijderKlasMetNullGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.verwijderKlas(null)
        );
    }

    @Test
    void constructorMetNullRepositoryGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new KlasService(null)
        );
    }

    private static class TestKlasRepository implements KlasRepository {

        private final List<Klas> klassen = new ArrayList<>();
        private long volgendId = 1;

        private boolean saveAangeroepen;
        private boolean updateAangeroepen;
        private boolean deleteAangeroepen;

        @Override
        public Klas save(Klas klas) {
            saveAangeroepen = true;

            Klas opgeslagenKlas = new Klas(
                    volgendId++,
                    klas.getNaam(),
                    klas.getSchooljaar(),
                    klas.getLeerjaar(),
                    klas.getDoelgroep()
            );

            klassen.add(opgeslagenKlas);

            return kopieVan(opgeslagenKlas);
        }

        @Override
        public List<Klas> zoekAlle() {
            List<Klas> resultaat = new ArrayList<>();

            for (Klas klas : klassen) {
                resultaat.add(kopieVan(klas));
            }

            return resultaat;
        }

        @Override
        public Klas zoekOpId(long id) {
            for (Klas klas : klassen) {
                if (klas.getId() != null && klas.getId() == id) {
                    return kopieVan(klas);
                }
            }

            throw new IllegalStateException(
                    "Geen klas gevonden met id: " + id
            );
        }

        @Override
        public void update(Klas klas) {
            updateAangeroepen = true;

            for (int i = 0; i < klassen.size(); i++) {
                if (klassen.get(i).getId().equals(klas.getId())) {
                    klassen.set(
                            i,
                            kopieVan(klas)
                    );
                    return;
                }
            }

            throw new IllegalStateException(
                    "Geen klas gevonden met id: " + klas.getId()
            );
        }

        @Override
        public void delete(Klas klas) {
            deleteAangeroepen = true;

            boolean verwijderd = klassen.removeIf(
                    opgeslagenKlas ->
                            opgeslagenKlas.getId().equals(klas.getId())
            );

            if (!verwijderd) {
                throw new IllegalStateException(
                        "Geen klas gevonden met id: " + klas.getId()
                );
            }
        }

        public boolean isSaveAangeroepen() {
            return saveAangeroepen;
        }

        public boolean isUpdateAangeroepen() {
            return updateAangeroepen;
        }

        public boolean isDeleteAangeroepen() {
            return deleteAangeroepen;
        }

        private Klas kopieVan(Klas klas) {
            return new Klas(
                    klas.getId(),
                    klas.getNaam(),
                    klas.getSchooljaar(),
                    klas.getLeerjaar(),
                    klas.getDoelgroep()
            );
        }
    }
}