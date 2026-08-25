package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryToewijzingRepository;
import be.kdg.talenten.service.beheer.IngerichtTalentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IngerichtTalentServiceTest {

    private TestIngerichtTalentRepository repository;
    private ToewijzingRepository toewijzingRepository;
    private IngerichtTalentService service;

    private Schooljaar schooljaar;
    private TalentenPeriode periode;
    private Talent talent;
    private Leerkracht tim;
    private Leerkracht sara;

    @BeforeEach
    void setUp() {
        repository =
                new TestIngerichtTalentRepository();

        toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        service =
                new IngerichtTalentService(
                        repository,
                        toewijzingRepository
                );

        schooljaar = new Schooljaar(
                1L,
                "2026-2027",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2027, 6, 30),
                true
        );

        periode = new TalentenPeriode(
                1L,
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31),
                schooljaar
        );

        talent = new Talent(
                1L,
                "Schaken",
                "Strategisch denkspel"
        );

        tim = new Leerkracht(
                1L,
                "Tim",
                "Van Herreweghe"
        );

        sara = new Leerkracht(
                2L,
                "Sara",
                "Janssens"
        );
    }

    @Test
    void geefIngerichteTalentenVoorPeriodeGeeftResultaatUitRepositoryTerug() {
        // ARRANGE
        repository.save(
                maakIngerichtTalent(
                        "Schaken beginners",
                        List.of(tim)
                )
        );

        repository.save(
                maakIngerichtTalent(
                        "Schaken gevorderd",
                        List.of(sara)
                )
        );

        // ACT
        List<IngerichtTalent> resultaat =
                service.geefIngerichteTalentenVoorPeriode(
                        periode
                );

        // ASSERT
        Assertions.assertEquals(
                2,
                resultaat.size()
        );

        Assertions.assertEquals(
                "Schaken beginners",
                resultaat.get(0).getNaam()
        );

        Assertions.assertEquals(
                "Schaken gevorderd",
                resultaat.get(1).getNaam()
        );
    }

    @Test
    void maakIngerichtTalentSlaatNieuwIngerichtTalentOp() {
        // ACT
        IngerichtTalent resultaat =
                service.maakIngerichtTalent(
                        talent,
                        periode,
                        "Schaken beginners",
                        "Leren schaken",
                        10,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of()
                );

        // ASSERT
        Assertions.assertNotNull(
                resultaat
        );

        Assertions.assertNotNull(
                resultaat.getId()
        );

        Assertions.assertEquals(
                "Schaken beginners",
                resultaat.getNaam()
        );

        Assertions.assertEquals(
                "Leren schaken",
                resultaat.getOmschrijving()
        );

        Assertions.assertEquals(
                10,
                resultaat.getMaxCapaciteit()
        );

        Assertions.assertTrue(
                resultaat.getLeerkrachten().isEmpty()
        );

        Assertions.assertTrue(
                repository.isSaveAangeroepen()
        );
    }

    @Test
    void wijzigIngerichtTalentWijzigtGegevensEnUpdateRepository() {
        // ARRANGE
        IngerichtTalent opgeslagen =
                repository.save(
                        maakIngerichtTalent(
                                "Schaken beginners",
                                List.of(tim)
                        )
                );

        // ACT
        service.wijzigIngerichtTalent(
                opgeslagen,
                "Schaken gevorderd",
                "Schaken voor gevorderden",
                15
        );

        // ASSERT
        IngerichtTalent resultaat =
                repository.zoekOpId(
                        opgeslagen.getId()
                );

        Assertions.assertTrue(
                repository.isUpdateAangeroepen()
        );

        Assertions.assertEquals(
                "Schaken gevorderd",
                resultaat.getNaam()
        );

        Assertions.assertEquals(
                "Schaken voor gevorderden",
                resultaat.getOmschrijving()
        );

        Assertions.assertEquals(
                15,
                resultaat.getMaxCapaciteit()
        );
    }

    @Test
    void voegLeerkrachtToeVoegtLeerkrachtToeEnUpdateRepository() {
        // ARRANGE
        IngerichtTalent opgeslagen =
                repository.save(
                        maakIngerichtTalent(
                                "Schaken beginners",
                                List.of(tim)
                        )
                );

        // ACT
        service.voegLeerkrachtToe(
                opgeslagen,
                sara
        );

        // ASSERT
        IngerichtTalent resultaat =
                repository.zoekOpId(
                        opgeslagen.getId()
                );

        Assertions.assertTrue(
                repository.isUpdateAangeroepen()
        );

        Assertions.assertEquals(
                2,
                resultaat.getLeerkrachten().size()
        );

        Assertions.assertTrue(
                resultaat.getLeerkrachten().contains(tim)
        );

        Assertions.assertTrue(
                resultaat.getLeerkrachten().contains(sara)
        );
    }

    @Test
    void verwijderLeerkrachtKanLaatsteLeerkrachtVerwijderen() {
        // ARRANGE
        IngerichtTalent opgeslagen =
                repository.save(
                        maakIngerichtTalent(
                                "Schaken beginners",
                                List.of(tim)
                        )
                );

        // ACT
        service.verwijderLeerkracht(
                opgeslagen,
                tim
        );

        // ASSERT
        IngerichtTalent resultaat =
                repository.zoekOpId(
                        opgeslagen.getId()
                );

        Assertions.assertTrue(
                repository.isUpdateAangeroepen()
        );

        Assertions.assertTrue(
                resultaat.getLeerkrachten().isEmpty()
        );
    }

    @Test
    void deactiveerMaaktIngerichtTalentInactief() {
        // ARRANGE
        IngerichtTalent opgeslagen =
                repository.save(
                        maakIngerichtTalent(
                                "Schaken beginners",
                                List.of()
                        )
                );

        // ACT
        service.deactiveer(
                opgeslagen
        );

        // ASSERT
        IngerichtTalent resultaat =
                repository.zoekOpId(
                        opgeslagen.getId()
                );

        Assertions.assertFalse(
                resultaat.isActief()
        );

        Assertions.assertTrue(
                repository.isUpdateAangeroepen()
        );
    }

    @Test
    void deactiveerVerwijdertLeerkrachtenEnToewijzingen() {
        // ARRANGE
        IngerichtTalent opgeslagen =
                repository.save(
                        maakIngerichtTalent(
                                "Schaken beginners",
                                List.of(
                                        tim,
                                        sara
                                )
                        )
                );

        Klas klas = new Klas(
                1L,
                "1AA",
                schooljaar,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        Leerling jan = new Leerling(
                1L,
                "Jan",
                "Peeters",
                klas
        );

        Leerling sofie = new Leerling(
                2L,
                "Sofie",
                "Janssens",
                klas
        );

        toewijzingRepository.save(
                new Toewijzing(
                        jan,
                        opgeslagen,
                        ToewijzingsType.AUTOMATISCH,
                        1
                )
        );

        toewijzingRepository.save(
                new Toewijzing(
                        sofie,
                        opgeslagen,
                        ToewijzingsType.AUTOMATISCH,
                        2
                )
        );

        Assertions.assertEquals(
                2,
                toewijzingRepository
                        .telToewijzingenVoorIngerichtTalent(
                                opgeslagen
                        )
        );

        Assertions.assertEquals(
                2,
                opgeslagen.getLeerkrachten().size()
        );

        // ACT
        service.deactiveer(
                opgeslagen
        );

        // ASSERT
        IngerichtTalent resultaat =
                repository.zoekOpId(
                        opgeslagen.getId()
                );

        Assertions.assertFalse(
                resultaat.isActief()
        );

        Assertions.assertTrue(
                resultaat.getLeerkrachten().isEmpty()
        );

        Assertions.assertEquals(
                0,
                toewijzingRepository
                        .telToewijzingenVoorIngerichtTalent(
                                opgeslagen
                        )
        );

        Assertions.assertNull(
                toewijzingRepository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                periode
                        )
        );

        Assertions.assertNull(
                toewijzingRepository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                sofie,
                                periode
                        )
        );

        Assertions.assertTrue(
                repository.isUpdateAangeroepen()
        );
    }

    @Test
    void activeerMaaktIngerichtTalentActiefEnUpdateRepository() {
        // ARRANGE
        IngerichtTalent opgeslagen =
                repository.save(
                        maakIngerichtTalent(
                                "Schaken beginners",
                                List.of()
                        )
                );

        opgeslagen.deactiveer();
        repository.update(
                opgeslagen
        );

        repository.resetUpdateAangeroepen();

        // ACT
        service.activeer(
                opgeslagen
        );

        // ASSERT
        IngerichtTalent resultaat =
                repository.zoekOpId(
                        opgeslagen.getId()
                );

        Assertions.assertTrue(
                resultaat.isActief()
        );

        Assertions.assertTrue(
                repository.isUpdateAangeroepen()
        );
    }

    @Test
    void wijzigIngerichtTalentMetNullGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.wijzigIngerichtTalent(
                                null,
                                "Schaken",
                                "Beschrijving",
                                10
                        )
        );
    }

    @Test
    void voegLeerkrachtToeMetNullIngerichtTalentGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.voegLeerkrachtToe(
                                null,
                                tim
                        )
        );
    }

    @Test
    void verwijderLeerkrachtMetNullIngerichtTalentGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.verwijderLeerkracht(
                                null,
                                tim
                        )
        );
    }

    @Test
    void constructorMetNullIngerichtTalentRepositoryGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        new IngerichtTalentService(
                                null,
                                toewijzingRepository
                        )
        );
    }

    @Test
    void constructorMetNullToewijzingRepositoryGeeftException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        new IngerichtTalentService(
                                repository,
                                null
                        )
        );
    }

    @Test
    void maakIngerichtTalentMetLegeOmschrijvingGebruiktBeschrijvingVanTalent() {
        // ACT
        IngerichtTalent resultaat =
                service.maakIngerichtTalent(
                        talent,
                        periode,
                        "Schaken beginners",
                        "",
                        10,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of()
                );

        // ASSERT
        Assertions.assertEquals(
                talent.getBeschrijving(),
                resultaat.getOmschrijving()
        );
    }

    private IngerichtTalent maakIngerichtTalent(
            String naam,
            List<Leerkracht> leerkrachten
    ) {
        return new IngerichtTalent(
                talent,
                periode,
                naam,
                "Leren schaken",
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                leerkrachten
        );
    }

    private static class TestIngerichtTalentRepository
            implements IngerichtTalentRepository {

        private final List<IngerichtTalent> ingerichteTalenten =
                new ArrayList<>();

        private long volgendId = 1;

        private boolean saveAangeroepen;
        private boolean updateAangeroepen;

        @Override
        public IngerichtTalent save(
                IngerichtTalent ingerichtTalent
        ) {
            saveAangeroepen =
                    true;

            IngerichtTalent opgeslagen =
                    kopieMetId(
                            ingerichtTalent,
                            volgendId++
                    );

            ingerichteTalenten.add(
                    opgeslagen
            );

            return kopieVan(
                    opgeslagen
            );
        }

        @Override
        public List<IngerichtTalent> zoekVoorPeriode(
                TalentenPeriode periode
        ) {
            List<IngerichtTalent> resultaat =
                    new ArrayList<>();

            for (IngerichtTalent ingerichtTalent :
                    ingerichteTalenten) {

                if (ingerichtTalent
                        .getTalentenPeriode()
                        .equals(periode)) {

                    resultaat.add(
                            kopieVan(
                                    ingerichtTalent
                            )
                    );
                }
            }

            return resultaat;
        }

        @Override
        public List<IngerichtTalent>
        zoekActieveVoorPeriodeEnDoelgroep(
                TalentenPeriode periode,
                Doelgroep doelgroep
        ) {
            List<IngerichtTalent> resultaat =
                    new ArrayList<>();

            for (IngerichtTalent ingerichtTalent :
                    ingerichteTalenten) {

                if (ingerichtTalent
                        .getTalentenPeriode()
                        .equals(periode)
                        && ingerichtTalent.getDoelgroep()
                        == doelgroep
                        && ingerichtTalent.isActief()) {

                    resultaat.add(
                            kopieVan(
                                    ingerichtTalent
                            )
                    );
                }
            }

            return resultaat;
        }

        @Override
        public IngerichtTalent zoekOpId(
                long id
        ) {
            for (IngerichtTalent ingerichtTalent :
                    ingerichteTalenten) {

                if (ingerichtTalent.getId() != null
                        && ingerichtTalent.getId() == id) {

                    return kopieVan(
                            ingerichtTalent
                    );
                }
            }

            throw new IllegalStateException(
                    "Geen ingericht talent gevonden met id: "
                            + id
            );
        }

        @Override
        public void update(
                IngerichtTalent ingerichtTalent
        ) {
            updateAangeroepen =
                    true;

            for (int i = 0;
                 i < ingerichteTalenten.size();
                 i++) {

                IngerichtTalent opgeslagen =
                        ingerichteTalenten.get(i);

                if (opgeslagen.getId() != null
                        && opgeslagen
                        .getId()
                        .equals(
                                ingerichtTalent.getId()
                        )) {

                    ingerichteTalenten.set(
                            i,
                            kopieVan(
                                    ingerichtTalent
                            )
                    );

                    return;
                }
            }

            throw new IllegalStateException(
                    "Geen ingericht talent gevonden met id: "
                            + ingerichtTalent.getId()
            );
        }

        public boolean isSaveAangeroepen() {
            return saveAangeroepen;
        }

        public boolean isUpdateAangeroepen() {
            return updateAangeroepen;
        }

        public void resetUpdateAangeroepen() {
            updateAangeroepen =
                    false;
        }

        private IngerichtTalent kopieMetId(
                IngerichtTalent ingerichtTalent,
                long id
        ) {
            return new IngerichtTalent(
                    id,
                    ingerichtTalent.getTalent(),
                    ingerichtTalent.getTalentenPeriode(),
                    ingerichtTalent.getNaam(),
                    ingerichtTalent.getOmschrijving(),
                    ingerichtTalent.getMaxCapaciteit(),
                    ingerichtTalent.getDoelgroep(),
                    ingerichtTalent.getLeerkrachten(),
                    ingerichtTalent.isActief()
            );
        }

        private IngerichtTalent kopieVan(
                IngerichtTalent ingerichtTalent
        ) {
            return new IngerichtTalent(
                    ingerichtTalent.getId(),
                    ingerichtTalent.getTalent(),
                    ingerichtTalent.getTalentenPeriode(),
                    ingerichtTalent.getNaam(),
                    ingerichtTalent.getOmschrijving(),
                    ingerichtTalent.getMaxCapaciteit(),
                    ingerichtTalent.getDoelgroep(),
                    ingerichtTalent.getLeerkrachten(),
                    ingerichtTalent.isActief()
            );
        }
    }
}