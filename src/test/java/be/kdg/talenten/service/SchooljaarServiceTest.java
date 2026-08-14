package be.kdg.talenten.service;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.SchooljaarRepository;
import be.kdg.talenten.service.beheer.SchooljaarService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class SchooljaarServiceTest {

    private TestSchooljaarRepository repository;
    private SchooljaarService service;

    @BeforeEach
    void setUp() {
        repository = new TestSchooljaarRepository();
        service = new SchooljaarService(repository);
    }

    @Test
    void zoekAlleSchooljarenGeeftAlleSchooljarenUitRepositoryTerug() {
        // ARRANGE
        repository.save(new Schooljaar(
                "2025-2026",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 6, 30),
                false
        ));

        repository.save(new Schooljaar(
                "2026-2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 6, 30),
                true
        ));

        // ACT
        List<Schooljaar> resultaat = service.zoekAlleSchooljaren();

        // ASSERT
        Assertions.assertEquals(2, resultaat.size());
    }

    @Test
    void voegVolgendSchooljaarToeMaaktVolgendSchooljaarVanafEenSeptember() {
        // ARRANGE
        repository.save(new Schooljaar(
                "2026-2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 6, 30),
                true
        ));

        // ACT
        Schooljaar resultaat = service.voegVolgendSchooljaarToe();

        // ASSERT
        Assertions.assertNotNull(resultaat.getId());
        Assertions.assertEquals("2027-2028", resultaat.getNaam());
        Assertions.assertEquals(LocalDate.of(2027, 9, 1), resultaat.getStartDatum());
        Assertions.assertEquals(LocalDate.of(2028, 6, 30), resultaat.getEindDatum());
        Assertions.assertFalse(resultaat.isActief());
    }

    @Test
    void tweeKeerVolgendSchooljaarToevoegenMaaktOpeenvolgendeSchooljaren() {
        // ARRANGE
        repository.save(new Schooljaar(
                "2026-2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 6, 30),
                true
        ));

        // ACT
        Schooljaar schooljaar2027_2028 = service.voegVolgendSchooljaarToe();
        Schooljaar schooljaar2028_2029 = service.voegVolgendSchooljaarToe();

        // ASSERT
        Assertions.assertEquals("2027-2028", schooljaar2027_2028.getNaam());
        Assertions.assertEquals("2028-2029", schooljaar2028_2029.getNaam());

        Assertions.assertEquals(
                LocalDate.of(2028, 9, 1),
                schooljaar2028_2029.getStartDatum()
        );

        Assertions.assertEquals(
                LocalDate.of(2029, 6, 30),
                schooljaar2028_2029.getEindDatum()
        );
    }

    @Test
    void voegVolgendSchooljaarToeZonderBestaandSchooljaarGeeftException() {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.voegVolgendSchooljaarToe()
        );
    }

    @Test
    void maakActiefMaaktGekozenSchooljaarActief() {
        // ARRANGE
        Schooljaar schooljaar2026_2027 = repository.save(new Schooljaar(
                "2026-2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 6, 30),
                true
        ));

        Schooljaar schooljaar2027_2028 = repository.save(new Schooljaar(
                "2027-2028",
                LocalDate.of(2027, 9, 1),
                LocalDate.of(2028, 6, 30),
                false
        ));

        // ACT
        service.maakActief(schooljaar2027_2028);

        // ASSERT
        Schooljaar actief = repository.zoekActiefSchooljaar().orElseThrow();

        Assertions.assertEquals(
                schooljaar2027_2028.getId(),
                actief.getId()
        );

        Assertions.assertEquals(
                "2027-2028",
                actief.getNaam()
        );

        Assertions.assertNotEquals(
                schooljaar2026_2027.getId(),
                actief.getId()
        );
    }

    @Test
    void maakActiefMetNullGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.maakActief(null)
        );
    }

    @Test
    void constructorMetNullRepositoryGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new SchooljaarService(null)
        );
    }

    private static class TestSchooljaarRepository implements SchooljaarRepository {

        private final List<Schooljaar> schooljaren = new ArrayList<>();
        private long volgendId = 1;

        @Override
        public Schooljaar save(Schooljaar schooljaar) {
            Schooljaar opgeslagen = new Schooljaar(
                    volgendId++,
                    schooljaar.getNaam(),
                    schooljaar.getStartDatum(),
                    schooljaar.getEindDatum(),
                    schooljaar.isActief()
            );

            if (opgeslagen.isActief()) {
                maakAlleInactief();
            }

            schooljaren.add(opgeslagen);

            return opgeslagen;
        }

        @Override
        public List<Schooljaar> zoekAlle() {
            return new ArrayList<>(schooljaren);
        }

        @Override
        public List<Schooljaar> zoekSelecteerbareSchooljaren() {
            return new ArrayList<>(schooljaren);
        }

        @Override
        public Optional<Schooljaar> zoekActiefSchooljaar() {
            return schooljaren.stream()
                    .filter(Schooljaar::isActief)
                    .findFirst();
        }

        @Override
        public void maakActief(Schooljaar schooljaar) {
            List<Schooljaar> nieuweLijst = new ArrayList<>();

            for (Schooljaar bestaand : schooljaren) {
                boolean actief = bestaand.getId().equals(schooljaar.getId());

                nieuweLijst.add(new Schooljaar(
                        bestaand.getId(),
                        bestaand.getNaam(),
                        bestaand.getStartDatum(),
                        bestaand.getEindDatum(),
                        actief
                ));
            }

            schooljaren.clear();
            schooljaren.addAll(nieuweLijst);
        }

        private void maakAlleInactief() {
            List<Schooljaar> nieuweLijst = new ArrayList<>();

            for (Schooljaar bestaand : schooljaren) {
                nieuweLijst.add(new Schooljaar(
                        bestaand.getId(),
                        bestaand.getNaam(),
                        bestaand.getStartDatum(),
                        bestaand.getEindDatum(),
                        false
                ));
            }

            schooljaren.clear();
            schooljaren.addAll(nieuweLijst);
        }
    }
}