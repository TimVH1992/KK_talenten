package be.kdg.talenten.service;

import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.repository.TalentRepository;
import be.kdg.talenten.service.beheer.TalentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TalentServiceTest {

    private TestTalentRepository repository;
    private TalentService service;

    @BeforeEach
    void setUp() {
        repository = new TestTalentRepository();
        service = new TalentService(repository);
    }

    @Test
    void geefAlleTalentenGeeftTalentenUitRepositoryTerug() {
        // ARRANGE
        repository.save(new Talent("Schaken", "Strategisch denkspel"));
        repository.save(new Talent("Dansen", "Bewegen op muziek"));

        // ACT
        List<Talent> resultaat = service.geefAlleTalenten();

        // ASSERT
        Assertions.assertEquals(2, resultaat.size());
        Assertions.assertEquals("Schaken", resultaat.get(0).getNaam());
        Assertions.assertEquals("Strategisch denkspel", resultaat.get(0).getBeschrijving());
        Assertions.assertEquals("Dansen", resultaat.get(1).getNaam());
        Assertions.assertEquals("Bewegen op muziek", resultaat.get(1).getBeschrijving());
    }

    @Test
    void maakTalentSlaatNieuwTalentOp() {
        // ACT
        Talent resultaat = service.maakTalent("Schaken", "Strategisch denkspel");

        // ASSERT
        Assertions.assertNotNull(resultaat);
        Assertions.assertNotNull(resultaat.getId());
        Assertions.assertEquals("Schaken", resultaat.getNaam());
        Assertions.assertEquals("Strategisch denkspel", resultaat.getBeschrijving());

        List<Talent> opgeslagenTalenten = repository.zoekAlle();

        Assertions.assertEquals(1, opgeslagenTalenten.size());
        Assertions.assertEquals(resultaat.getId(), opgeslagenTalenten.getFirst().getId());
        Assertions.assertEquals("Schaken", opgeslagenTalenten.getFirst().getNaam());
        Assertions.assertEquals("Strategisch denkspel", opgeslagenTalenten.getFirst().getBeschrijving());
    }

    @Test
    void wijzigTalentWijzigtGegevensEnUpdateRepository() {
        // ARRANGE
        Talent opgeslagenTalent = repository.save(new Talent("Schaken", "Strategisch denkspel"));

        // ACT
        service.wijzigTalent(opgeslagenTalent, "Schaken gevorderd", "Strategisch denkspel voor gevorderden");

        // ASSERT
        Talent aangepastTalent = repository.zoekOpId(opgeslagenTalent.getId());

        Assertions.assertTrue(repository.isUpdateAangeroepen());
        Assertions.assertEquals("Schaken gevorderd", aangepastTalent.getNaam());
        Assertions.assertEquals("Strategisch denkspel voor gevorderden", aangepastTalent.getBeschrijving());
    }

    @Test
    void wijzigTalentMetNullGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigTalent(null, "Schaken", "Strategisch denkspel")
        );
    }

    @Test
    void constructorMetNullRepositoryGeeftException() {
        // ACT & ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new TalentService(null)
        );
    }

    private static class TestTalentRepository implements TalentRepository {

        private final List<Talent> talenten = new ArrayList<>();
        private long volgendId = 1;
        private boolean updateAangeroepen;

        @Override
        public Talent save(Talent talent) {
            long id = volgendId++;

            Talent opgeslagenTalent = new Talent(
                    id,
                    talent.getNaam(),
                    talent.getBeschrijving()
            );

            talenten.add(opgeslagenTalent);

            return kopieVan(opgeslagenTalent);
        }

        @Override
        public List<Talent> zoekAlle() {
            List<Talent> resultaat = new ArrayList<>();

            for (Talent talent : talenten) {
                resultaat.add(kopieVan(talent));
            }

            return resultaat;
        }

        @Override
        public Talent zoekOpId(long id) {
            for (Talent talent : talenten) {
                if (talent.getId() == id) {
                    return kopieVan(talent);
                }
            }

            throw new IllegalStateException("Talent niet gevonden");
        }

        @Override
        public void update(Talent talent) {
            updateAangeroepen = true;

            for (int i = 0; i < talenten.size(); i++) {
                if (talenten.get(i).getId().equals(talent.getId())) {
                    talenten.set(i, kopieVan(talent));
                    return;
                }
            }

            throw new IllegalStateException("Talent niet gevonden");
        }

        public boolean isUpdateAangeroepen() {
            return updateAangeroepen;
        }

        private Talent kopieVan(Talent talent) {
            return new Talent(
                    talent.getId(),
                    talent.getNaam(),
                    talent.getBeschrijving()
            );
        }
    }
}