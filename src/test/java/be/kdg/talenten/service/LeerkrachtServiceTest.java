package be.kdg.talenten.service;

import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.repository.LeerkrachtRepository;
import be.kdg.talenten.service.beheer.LeerkrachtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class LeerkrachtServiceTest {

    private TestLeerkrachtRepository repository;
    private LeerkrachtService service;

    @BeforeEach
    void setUp() {
        repository = new TestLeerkrachtRepository();
        service = new LeerkrachtService(repository);
    }

    @Test
    void geefAlleLeerkrachtenGeeftLeerkrachtenUitRepositoryTerug() {
        // ARRANGE
        repository.save(new Leerkracht("Tim", "Van Herreweghe"));
        repository.save(new Leerkracht("Jos", "Peeters"));

        // ACT
        List<Leerkracht> resultaat = service.geefAlleLeerkrachten();

        // ASSERT
        Assertions.assertEquals(2, resultaat.size());
        Assertions.assertEquals("Tim", resultaat.get(0).getVoornaam());
        Assertions.assertEquals("Van Herreweghe", resultaat.get(0).getAchternaam());
        Assertions.assertEquals("Jos", resultaat.get(1).getVoornaam());
        Assertions.assertEquals("Peeters", resultaat.get(1).getAchternaam());
    }

    @Test
    void maakLeerkrachtSlaatNieuweLeerkrachtOp() {
        // ACT
        Leerkracht resultaat = service.maakLeerkracht("Tim", "Van Herreweghe");

        // ASSERT
        Assertions.assertNotNull(resultaat);
        Assertions.assertNotNull(resultaat.getId());
        Assertions.assertEquals("Tim", resultaat.getVoornaam());
        Assertions.assertEquals("Van Herreweghe", resultaat.getAchternaam());

        List<Leerkracht> opgeslagenLeerkrachten = repository.zoekAlle();

        Assertions.assertEquals(1, opgeslagenLeerkrachten.size());
        Assertions.assertEquals(resultaat.getId(), opgeslagenLeerkrachten.getFirst().getId());
        Assertions.assertEquals("Tim", opgeslagenLeerkrachten.getFirst().getVoornaam());
    }

    @Test
    void wijzigLeerkrachtWijzigtGegevensEnUpdateRepository() {
        // ARRANGE
        Leerkracht opgeslagenLeerkracht = repository.save(new Leerkracht("Tim", "Van Herreweghe"));

        // ACT
        service.wijzigLeerkracht(opgeslagenLeerkracht, "Jos", "Van Herreweghe");

        // ASSERT
        Leerkracht aangepasteLeerkracht = repository.zoekOpId(opgeslagenLeerkracht.getId());

        Assertions.assertTrue(repository.isUpdateAangeroepen());
        Assertions.assertEquals("Jos", aangepasteLeerkracht.getVoornaam());
        Assertions.assertEquals("Van Herreweghe", aangepasteLeerkracht.getAchternaam());
    }

    @Test
    void wijzigLeerkrachtMetNullGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigLeerkracht(null, "Tim", "Van Herreweghe")
        );
    }

    @Test
    void constructorMetNullRepositoryGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LeerkrachtService(null)
        );
    }

    private static class TestLeerkrachtRepository implements LeerkrachtRepository {

        private final List<Leerkracht> leerkrachten = new ArrayList<>();
        private long volgendId = 1;
        private boolean updateAangeroepen;

        @Override
        public Leerkracht save(Leerkracht leerkracht) {
            long id = volgendId++;

            Leerkracht opgeslagenLeerkracht = new Leerkracht(
                    id,
                    leerkracht.getVoornaam(),
                    leerkracht.getAchternaam()
            );

            leerkrachten.add(opgeslagenLeerkracht);

            return new Leerkracht(
                    id,
                    opgeslagenLeerkracht.getVoornaam(),
                    opgeslagenLeerkracht.getAchternaam()
            );
        }

        @Override
        public List<Leerkracht> zoekAlle() {
            List<Leerkracht> resultaat = new ArrayList<>();

            for (Leerkracht leerkracht : leerkrachten) {
                resultaat.add(kopieVan(leerkracht));
            }

            return resultaat;
        }

        @Override
        public Leerkracht zoekOpId(long id) {
            for (Leerkracht leerkracht : leerkrachten) {
                if (leerkracht.getId() == id) {
                    return kopieVan(leerkracht);
                }
            }

            throw new IllegalStateException("Leerkracht niet gevonden");
        }

        @Override
        public void update(Leerkracht leerkracht) {
            updateAangeroepen = true;

            for (int i = 0; i < leerkrachten.size(); i++) {
                if (leerkrachten.get(i).getId().equals(leerkracht.getId())) {
                    leerkrachten.set(i, kopieVan(leerkracht));
                    return;
                }
            }

            throw new IllegalStateException("Leerkracht niet gevonden");
        }

        public boolean isUpdateAangeroepen() {
            return updateAangeroepen;
        }

        private Leerkracht kopieVan(Leerkracht leerkracht) {
            return new Leerkracht(
                    leerkracht.getId(),
                    leerkracht.getVoornaam(),
                    leerkracht.getAchternaam()
            );
        }
    }
}