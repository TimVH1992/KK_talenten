package be.kdg.talenten.verdeling;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryToewijzingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryVoorkeurImportProbleemRepository;
import be.kdg.talenten.repository.inmemory.InMemoryVoorkeurRepository;
import be.kdg.talenten.service.verdeling.AutomatischeVerdelingService;
import be.kdg.talenten.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AutomatischeVerdelingServiceTest {

    private static final Doelgroep OBSERVATIE =
            Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

    private static final Doelgroep KWALIFICATIE =
            Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB;

    private Leerkracht testLeerkracht;

    @BeforeEach
    void setUp() {
        testLeerkracht =
                new Leerkracht(
                        "Test",
                        "Leerkracht"
                );
    }

    @Test
    void serviceVoertAutomatischeVerdelingUitEnSlaatToewijzingenOp() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.of(2099, 11, 22);

        LocalDate eindDatum =
                LocalDate.of(2100, 2, 21);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas klas1AA =
                maakObservatieKlas(
                        schooljaar
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode winter =
                new TalentenPeriode(
                        "Winter",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        IngerichtTalent schakenWinter =
                richtTalentIn(
                        new Talent(
                                "Schaken",
                                "Leren schaken"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent voetbalWinter =
                richtTalentIn(
                        new Talent(
                                "Voetbal",
                                "Voetbaltraining"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent kokenWinter =
                richtTalentIn(
                        new Talent(
                                "Koken",
                                "Leren koken"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        List<Voorkeur> voorkeuren =
                List.of(
                        new Voorkeur(
                                jan,
                                winter,
                                schakenWinter,
                                1
                        ),
                        new Voorkeur(
                                jan,
                                winter,
                                voetbalWinter,
                                2
                        ),
                        new Voorkeur(
                                jan,
                                winter,
                                kokenWinter,
                                3
                        )
                );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(
                        voorkeuren
                );

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        InMemoryLeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan
                        )
                );

        InMemoryVoorkeurImportProbleemRepository probleemRepository =
                new InMemoryVoorkeurImportProbleemRepository(
                        new ArrayList<>()
                );

        AutomatischeVerdelingService service =
                maakService(
                        voorkeurRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        probleemRepository
                );

        // ACT
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(
                        winter,
                        OBSERVATIE
                );

        // ASSERT
        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );

        assertEquals(
                1,
                toewijzingRepository
                        .getOpgeslagenToewijzingen()
                        .size()
        );

        Toewijzing opgeslagenToewijzing =
                toewijzingRepository
                        .getOpgeslagenToewijzingen()
                        .getFirst();

        assertSame(
                jan,
                opgeslagenToewijzing.getLeerling()
        );

        assertSame(
                schakenWinter,
                opgeslagenToewijzing.getIngerichtTalent()
        );

        assertEquals(
                1,
                opgeslagenToewijzing.getVoorkeurNummer()
        );
    }

    @Test
    void serviceGebruiktHistorischeToewijzingenBijAutomatischeVerdeling() {
        // ARRANGE
        LocalDate startHerfst =
                LocalDate.of(2099, 9, 1);

        LocalDate eindeHerfst =
                LocalDate.of(2099, 11, 21);

        LocalDate startWinter =
                LocalDate.of(2099, 11, 22);

        LocalDate eindeWinter =
                LocalDate.of(2100, 2, 21);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startHerfst,
                        eindeWinter
                );

        Klas klas1AA =
                maakObservatieKlas(
                        schooljaar
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                new TalentenPeriode(
                        "Herfst",
                        startHerfst,
                        eindeHerfst,
                        schooljaar
                );

        TalentenPeriode winter =
                new TalentenPeriode(
                        "Winter",
                        startWinter,
                        eindeWinter,
                        schooljaar
                );

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent schakenWinter =
                richtTalentIn(
                        schaken,
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent voetbalWinter =
                richtTalentIn(
                        new Talent(
                                "Voetbal",
                                "Voetbaltraining"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent kokenWinter =
                richtTalentIn(
                        new Talent(
                                "Koken",
                                "Leren koken"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        Toewijzing historischeToewijzing =
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                );

        List<Voorkeur> voorkeuren =
                List.of(
                        new Voorkeur(
                                jan,
                                winter,
                                schakenWinter,
                                1
                        ),
                        new Voorkeur(
                                jan,
                                winter,
                                voetbalWinter,
                                2
                        ),
                        new Voorkeur(
                                jan,
                                winter,
                                kokenWinter,
                                3
                        )
                );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(
                        voorkeuren
                );

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        List.of(
                                historischeToewijzing
                        )
                );

        InMemoryLeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan
                        )
                );

        InMemoryVoorkeurImportProbleemRepository probleemRepository =
                new InMemoryVoorkeurImportProbleemRepository(
                        new ArrayList<>()
                );

        AutomatischeVerdelingService service =
                maakService(
                        voorkeurRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        probleemRepository
                );

        // ACT
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(
                        winter,
                        OBSERVATIE
                );

        // ASSERT
        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );

        Toewijzing nieuweToewijzing =
                resultaat
                        .getToewijzingen()
                        .getFirst();

        assertSame(
                jan,
                nieuweToewijzing.getLeerling()
        );

        assertSame(
                voetbalWinter,
                nieuweToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.AUTOMATISCH,
                nieuweToewijzing.getToewijzingsType()
        );

        assertEquals(
                2,
                nieuweToewijzing.getVoorkeurNummer()
        );

        assertTrue(
                toewijzingRepository
                        .getOpgeslagenToewijzingen()
                        .contains(
                                nieuweToewijzing
                        )
        );
    }

    @Test
    void serviceBehoudtManueleToewijzingenEnVervangtAlleenAutomatischeToewijzingenVanDoelgroep() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.of(2099, 11, 1);

        LocalDate eindDatum =
                LocalDate.of(2099, 12, 20);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas klas1AA =
                maakObservatieKlas(
                        schooljaar
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        Leerling julie =
                new Leerling(
                        "Julie",
                        "Martens",
                        klas1AA
                );

        TalentenPeriode winter =
                new TalentenPeriode(
                        "Winter",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        IngerichtTalent schakenWinter =
                richtTalentIn(
                        new Talent(
                                "Schaken",
                                "Leren schaken"
                        ),
                        winter,
                        1,
                        OBSERVATIE
                );

        IngerichtTalent voetbalWinter =
                richtTalentIn(
                        new Talent(
                                "Voetbal",
                                "Voetbaltraining"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent kokenWinter =
                richtTalentIn(
                        new Talent(
                                "Koken",
                                "Leren koken"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        List<Voorkeur> voorkeuren =
                List.of(
                        new Voorkeur(
                                jan,
                                winter,
                                schakenWinter,
                                1
                        ),
                        new Voorkeur(
                                jan,
                                winter,
                                voetbalWinter,
                                2
                        ),
                        new Voorkeur(
                                jan,
                                winter,
                                kokenWinter,
                                3
                        ),
                        new Voorkeur(
                                julie,
                                winter,
                                schakenWinter,
                                1
                        ),
                        new Voorkeur(
                                julie,
                                winter,
                                voetbalWinter,
                                2
                        ),
                        new Voorkeur(
                                julie,
                                winter,
                                kokenWinter,
                                3
                        )
                );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(
                        voorkeuren
                );

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        InMemoryLeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan,
                                julie
                        )
                );

        Toewijzing manueleToewijzing =
                toewijzingRepository.save(
                        new Toewijzing(
                                jan,
                                schakenWinter,
                                ToewijzingsType.MANUEEL
                        )
                );

        Toewijzing oudeAutomatischeToewijzing =
                toewijzingRepository.save(
                        new Toewijzing(
                                julie,
                                kokenWinter,
                                ToewijzingsType.AUTOMATISCH,
                                3
                        )
                );

        InMemoryVoorkeurImportProbleemRepository probleemRepository =
                new InMemoryVoorkeurImportProbleemRepository(
                        new ArrayList<>()
                );

        AutomatischeVerdelingService service =
                maakService(
                        voorkeurRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        probleemRepository
                );

        // ACT
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(
                        winter,
                        OBSERVATIE
                );

        // ASSERT
        List<Toewijzing> opgeslagenToewijzingen =
                toewijzingRepository
                        .getOpgeslagenToewijzingen();

        assertEquals(
                2,
                opgeslagenToewijzingen.size()
        );

        assertTrue(
                opgeslagenToewijzingen.contains(
                        manueleToewijzing
                )
        );

        assertFalse(
                opgeslagenToewijzingen.contains(
                        oudeAutomatischeToewijzing
                )
        );

        Toewijzing nieuweAutomatischeToewijzing =
                opgeslagenToewijzingen
                        .stream()
                        .filter(
                                toewijzing ->
                                        toewijzing.getToewijzingsType()
                                                == ToewijzingsType.AUTOMATISCH
                        )
                        .findFirst()
                        .orElseThrow();

        assertSame(
                julie,
                nieuweAutomatischeToewijzing.getLeerling()
        );

        assertSame(
                voetbalWinter,
                nieuweAutomatischeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                2,
                nieuweAutomatischeToewijzing.getVoorkeurNummer()
        );

        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );
    }

    @Test
    void herverdelingWijzigtToewijzingenVanAnderePeriodeNiet() {
        // ARRANGE
        LocalDate startHerfst =
                LocalDate.of(2099, 9, 1);

        LocalDate eindeHerfst =
                LocalDate.of(2099, 10, 31);

        LocalDate startWinter =
                LocalDate.of(2099, 11, 1);

        LocalDate eindeWinter =
                LocalDate.of(2099, 12, 20);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startHerfst,
                        eindeWinter
                );

        Klas klas1AA =
                maakObservatieKlas(
                        schooljaar
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        Leerling julie =
                new Leerling(
                        "Julie",
                        "Martens",
                        klas1AA
                );

        TalentenPeriode herfst =
                new TalentenPeriode(
                        "Herfst",
                        startHerfst,
                        eindeHerfst,
                        schooljaar
                );

        TalentenPeriode winter =
                new TalentenPeriode(
                        "Winter",
                        startWinter,
                        eindeWinter,
                        schooljaar
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        new Talent(
                                "Schaken",
                                "Leren schaken"
                        ),
                        herfst,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent schakenWinter =
                richtTalentIn(
                        new Talent(
                                "Schaken",
                                "Leren schaken"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent voetbalWinter =
                richtTalentIn(
                        new Talent(
                                "Voetbal",
                                "Voetbaltraining"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent kokenWinter =
                richtTalentIn(
                        new Talent(
                                "Koken",
                                "Leren koken"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        List<Voorkeur> voorkeuren =
                List.of(
                        new Voorkeur(
                                julie,
                                winter,
                                voetbalWinter,
                                1
                        ),
                        new Voorkeur(
                                julie,
                                winter,
                                kokenWinter,
                                2
                        ),
                        new Voorkeur(
                                julie,
                                winter,
                                schakenWinter,
                                3
                        )
                );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(
                        voorkeuren
                );

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        InMemoryLeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan,
                                julie
                        )
                );

        Toewijzing herfstToewijzing =
                toewijzingRepository.save(
                        new Toewijzing(
                                jan,
                                schakenHerfst,
                                ToewijzingsType.AUTOMATISCH,
                                1
                        )
                );

        Toewijzing oudeWinterToewijzing =
                toewijzingRepository.save(
                        new Toewijzing(
                                julie,
                                schakenWinter,
                                ToewijzingsType.AUTOMATISCH,
                                3
                        )
                );

        InMemoryVoorkeurImportProbleemRepository probleemRepository =
                new InMemoryVoorkeurImportProbleemRepository(
                        new ArrayList<>()
                );

        AutomatischeVerdelingService service =
                maakService(
                        voorkeurRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        probleemRepository
                );

        // ACT
        service.voerAutomatischeVerdelingUit(
                winter,
                OBSERVATIE
        );

        // ASSERT
        List<Toewijzing> opgeslagenToewijzingen =
                toewijzingRepository
                        .getOpgeslagenToewijzingen();

        assertEquals(
                2,
                opgeslagenToewijzingen.size()
        );

        assertTrue(
                opgeslagenToewijzingen.contains(
                        herfstToewijzing
                )
        );

        assertFalse(
                opgeslagenToewijzingen.contains(
                        oudeWinterToewijzing
                )
        );

        List<Toewijzing> herfstToewijzingen =
                toewijzingRepository
                        .zoekVoorPeriode(
                                herfst
                        );

        assertEquals(
                1,
                herfstToewijzingen.size()
        );

        assertSame(
                herfstToewijzing,
                herfstToewijzingen.getFirst()
        );

        List<Toewijzing> winterToewijzingen =
                toewijzingRepository
                        .zoekVoorPeriode(
                                winter
                        );

        assertEquals(
                1,
                winterToewijzingen.size()
        );

        Toewijzing nieuweWinterToewijzing =
                winterToewijzingen.getFirst();

        assertSame(
                julie,
                nieuweWinterToewijzing.getLeerling()
        );

        assertSame(
                voetbalWinter,
                nieuweWinterToewijzing.getIngerichtTalent()
        );

        assertEquals(
                1,
                nieuweWinterToewijzing.getVoorkeurNummer()
        );
    }

    @Test
    void automatischeVerdelingVoorObservatieLaatKwalificatieToewijzingenOngemoeid() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.of(2099, 9, 1);

        LocalDate eindDatum =
                LocalDate.of(2099, 10, 31);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas observatieKlas =
                new Klas(
                        "1AA",
                        schooljaar,
                        1,
                        OBSERVATIE
                );

        Klas kwalificatieKlas =
                new Klas(
                        "3KA",
                        schooljaar,
                        3,
                        KWALIFICATIE
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        observatieKlas
                );

        Leerling sofie =
                new Leerling(
                        "Sofie",
                        "Janssens",
                        kwalificatieKlas
                );

        TalentenPeriode herfst =
                new TalentenPeriode(
                        "Herfst",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        IngerichtTalent schakenObservatie =
                richtTalentIn(
                        new Talent(
                                "Schaken",
                                "Leren schaken"
                        ),
                        herfst,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent voetbalObservatie =
                richtTalentIn(
                        new Talent(
                                "Voetbal",
                                "Voetbaltraining"
                        ),
                        herfst,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent kokenObservatie =
                richtTalentIn(
                        new Talent(
                                "Koken",
                                "Leren koken"
                        ),
                        herfst,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent houtKwalificatie =
                richtTalentIn(
                        new Talent(
                                "Hout",
                                "Werken met hout"
                        ),
                        herfst,
                        10,
                        KWALIFICATIE
                );

        List<Voorkeur> voorkeuren =
                List.of(
                        new Voorkeur(
                                jan,
                                herfst,
                                schakenObservatie,
                                1
                        ),
                        new Voorkeur(
                                jan,
                                herfst,
                                voetbalObservatie,
                                2
                        ),
                        new Voorkeur(
                                jan,
                                herfst,
                                kokenObservatie,
                                3
                        )
                );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(
                        voorkeuren
                );

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        InMemoryLeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan,
                                sofie
                        )
                );

        Toewijzing oudeObservatieToewijzing =
                toewijzingRepository.save(
                        new Toewijzing(
                                jan,
                                kokenObservatie,
                                ToewijzingsType.AUTOMATISCH,
                                3
                        )
                );

        Toewijzing kwalificatieToewijzing =
                toewijzingRepository.save(
                        new Toewijzing(
                                sofie,
                                houtKwalificatie,
                                ToewijzingsType.AUTOMATISCH,
                                1
                        )
                );

        InMemoryVoorkeurImportProbleemRepository probleemRepository =
                new InMemoryVoorkeurImportProbleemRepository(
                        new ArrayList<>()
                );

        AutomatischeVerdelingService service =
                maakService(
                        voorkeurRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        probleemRepository
                );

        // ACT
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(
                        herfst,
                        OBSERVATIE
                );

        // ASSERT
        List<Toewijzing> opgeslagenToewijzingen =
                toewijzingRepository
                        .getOpgeslagenToewijzingen();

        assertEquals(
                2,
                opgeslagenToewijzingen.size()
        );

        assertFalse(
                opgeslagenToewijzingen.contains(
                        oudeObservatieToewijzing
                )
        );

        assertTrue(
                opgeslagenToewijzingen.contains(
                        kwalificatieToewijzing
                )
        );

        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );

        Toewijzing nieuweObservatieToewijzing =
                resultaat
                        .getToewijzingen()
                        .getFirst();

        assertSame(
                jan,
                nieuweObservatieToewijzing.getLeerling()
        );

        assertSame(
                schakenObservatie,
                nieuweObservatieToewijzing.getIngerichtTalent()
        );

        assertTrue(
                opgeslagenToewijzingen.contains(
                        nieuweObservatieToewijzing
                )
        );

        assertSame(
                sofie,
                kwalificatieToewijzing.getLeerling()
        );

        assertSame(
                houtKwalificatie,
                kwalificatieToewijzing.getIngerichtTalent()
        );
    }

    @Test
    void automatischeVerdelingVerdeeltGeenLeerlingenUitAndereDoelgroep() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.of(2099, 9, 1);

        LocalDate eindDatum =
                LocalDate.of(2099, 10, 31);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas observatieKlas =
                new Klas(
                        "1AA",
                        schooljaar,
                        1,
                        OBSERVATIE
                );

        Klas kwalificatieKlas =
                new Klas(
                        "3KA",
                        schooljaar,
                        3,
                        KWALIFICATIE
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        observatieKlas
                );

        Leerling sofie =
                new Leerling(
                        "Sofie",
                        "Janssens",
                        kwalificatieKlas
                );

        TalentenPeriode herfst =
                new TalentenPeriode(
                        "Herfst",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        IngerichtTalent schaken =
                richtTalentIn(
                        new Talent(
                                "Schaken",
                                "Leren schaken"
                        ),
                        herfst,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent voetbal =
                richtTalentIn(
                        new Talent(
                                "Voetbal",
                                "Voetbaltraining"
                        ),
                        herfst,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent koken =
                richtTalentIn(
                        new Talent(
                                "Koken",
                                "Leren koken"
                        ),
                        herfst,
                        10,
                        OBSERVATIE
                );

        List<Voorkeur> voorkeuren =
                List.of(
                        new Voorkeur(
                                jan,
                                herfst,
                                schaken,
                                1
                        ),
                        new Voorkeur(
                                jan,
                                herfst,
                                voetbal,
                                2
                        ),
                        new Voorkeur(
                                jan,
                                herfst,
                                koken,
                                3
                        )
                );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(
                        voorkeuren
                );

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        InMemoryLeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan,
                                sofie
                        )
                );

        InMemoryVoorkeurImportProbleemRepository probleemRepository =
                new InMemoryVoorkeurImportProbleemRepository(
                        new ArrayList<>()
                );

        AutomatischeVerdelingService service =
                maakService(
                        voorkeurRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        probleemRepository
                );

        // ACT
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(
                        herfst,
                        OBSERVATIE
                );

        // ASSERT
        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );

        assertSame(
                jan,
                resultaat
                        .getToewijzingen()
                        .getFirst()
                        .getLeerling()
        );

        assertTrue(
                resultaat
                        .getToewijzingen()
                        .stream()
                        .noneMatch(
                                toewijzing ->
                                        toewijzing.getLeerling()
                                                == sofie
                        )
        );

        assertFalse(
                resultaat
                        .getNietToegewezenLeerlingen()
                        .contains(
                                sofie
                        )
        );
    }

    @Test
    void automatischeVerdelingMetNullDoelgroepWordtGeweigerd() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.of(2099, 9, 1);

        LocalDate eindDatum =
                LocalDate.of(2099, 10, 31);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        TalentenPeriode herfst =
                new TalentenPeriode(
                        "Herfst",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        AutomatischeVerdelingService service =
                maakLegeService();

        // ACT & ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.voerAutomatischeVerdelingUit(
                                herfst,
                                null
                        )
        );
    }

    @Test
    void heeftBestaandeToewijzingenKijktAlleenNaarGekozenDoelgroep() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.of(2099, 9, 1);

        LocalDate eindDatum =
                LocalDate.of(2099, 10, 31);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas kwalificatieKlas =
                new Klas(
                        "3KA",
                        schooljaar,
                        3,
                        KWALIFICATIE
                );

        Leerling sofie =
                new Leerling(
                        "Sofie",
                        "Janssens",
                        kwalificatieKlas
                );

        TalentenPeriode herfst =
                new TalentenPeriode(
                        "Herfst",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        IngerichtTalent hout =
                richtTalentIn(
                        new Talent(
                                "Hout",
                                "Werken met hout"
                        ),
                        herfst,
                        10,
                        KWALIFICATIE
                );

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        toewijzingRepository.save(
                new Toewijzing(
                        sofie,
                        hout,
                        ToewijzingsType.AUTOMATISCH,
                        1
                )
        );

        AutomatischeVerdelingService service =
                maakService(
                        new InMemoryVoorkeurRepository(
                                new ArrayList<>()
                        ),
                        toewijzingRepository,
                        new InMemoryLeerlingRepository(
                                List.of(
                                        sofie
                                )
                        ),
                        new InMemoryVoorkeurImportProbleemRepository(
                                new ArrayList<>()
                        )
                );

        // ACT + ASSERT
        assertFalse(
                service.heeftBestaandeToewijzingen(
                        herfst,
                        OBSERVATIE
                )
        );

        assertTrue(
                service.heeftBestaandeToewijzingen(
                        herfst,
                        KWALIFICATIE
                )
        );
    }

    @Test
    void automatischeVerdelingVoorAfgelopenPeriodeWordtGeweigerd() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.now()
                        .minusMonths(4);

        LocalDate eindDatum =
                LocalDate.now()
                        .minusMonths(2);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas klas1AA =
                maakObservatieKlas(
                        schooljaar
                );

        Leerling alice =
                new Leerling(
                        "Alice",
                        "Janssens",
                        klas1AA
                );

        TalentenPeriode lente =
                new TalentenPeriode(
                        "Lente",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        IngerichtTalent schakenLente =
                richtTalentIn(
                        new Talent(
                                "Schaken",
                                "Leren schaken"
                        ),
                        lente,
                        10,
                        OBSERVATIE
                );

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        Toewijzing bestaandeToewijzing =
                toewijzingRepository.save(
                        new Toewijzing(
                                alice,
                                schakenLente,
                                ToewijzingsType.AUTOMATISCH,
                                3
                        )
                );

        AutomatischeVerdelingService service =
                maakService(
                        new InMemoryVoorkeurRepository(
                                new ArrayList<>()
                        ),
                        toewijzingRepository,
                        new InMemoryLeerlingRepository(
                                List.of(
                                        alice
                                )
                        ),
                        new InMemoryVoorkeurImportProbleemRepository(
                                new ArrayList<>()
                        )
                );

        // ACT + ASSERT
        assertThrows(
                IllegalStateException.class,
                () ->
                        service.voerAutomatischeVerdelingUit(
                                lente,
                                OBSERVATIE
                        )
        );

        List<Toewijzing> lenteToewijzingen =
                toewijzingRepository
                        .zoekVoorPeriode(
                                lente
                        );

        assertEquals(
                1,
                lenteToewijzingen.size()
        );

        assertSame(
                bestaandeToewijzing,
                lenteToewijzingen.getFirst()
        );
    }

    @Test
    void leerlingZonderVoorkeurenWordtNietToegewezen() {
        // ARRANGE
        Schooljaar schooljaar =
                new Schooljaar(
                        "2099-2100",
                        LocalDate.of(2099, 9, 1),
                        LocalDate.of(2100, 6, 30)
                );

        TalentenPeriode periode =
                new TalentenPeriode(
                        "Herfst",
                        LocalDate.of(2099, 9, 1),
                        LocalDate.of(2099, 12, 21),
                        schooljaar
                );

        Klas klas =
                new Klas(
                        "1AA",
                        schooljaar,
                        1,
                        OBSERVATIE
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Mertens",
                        klas
                );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(
                        List.of(
                                jan
                        ),
                        List.of(),
                        List.of(),
                        List.of()
                );

        // ACT
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // ASSERT
        assertEquals(
                0,
                resultaat
                        .getToewijzingen()
                        .size()
        );

        assertEquals(
                1,
                resultaat
                        .getNietToegewezenLeerlingen()
                        .size()
        );

        assertSame(
                jan,
                resultaat
                        .getNietToegewezenLeerlingen()
                        .getFirst()
        );
    }

    @Test
    void leerlingMetOnvolledigeVoorkeurenKrijgtImportprobleemMeeInVerdelingsResultaat() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.of(2099, 9, 1);

        LocalDate eindDatum =
                LocalDate.of(2099, 10, 31);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas klas1AA =
                maakObservatieKlas(
                        schooljaar
                );

        Leerling sofie =
                new Leerling(
                        "Sofie",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                new TalentenPeriode(
                        "Herfst",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        new Talent(
                                "Schaken",
                                "Leren schaken"
                        ),
                        herfst,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent kokenHerfst =
                richtTalentIn(
                        new Talent(
                                "Koken",
                                "Leren koken"
                        ),
                        herfst,
                        10,
                        OBSERVATIE
                );

        List<Voorkeur> voorkeuren =
                List.of(
                        new Voorkeur(
                                sofie,
                                herfst,
                                schakenHerfst,
                                1
                        ),
                        new Voorkeur(
                                sofie,
                                herfst,
                                kokenHerfst,
                                2
                        )
                );

        VoorkeurImportProbleem importProbleem =
                new VoorkeurImportProbleem(
                        sofie,
                        herfst,
                        "Keuze 3 is niet ingevuld"
                );

        InMemoryVoorkeurImportProbleemRepository probleemRepository =
                new InMemoryVoorkeurImportProbleemRepository(
                        new ArrayList<>()
                );

        probleemRepository.save(
                importProbleem
        );

        AutomatischeVerdelingService service =
                maakService(
                        new InMemoryVoorkeurRepository(
                                voorkeuren
                        ),
                        new InMemoryToewijzingRepository(
                                new ArrayList<>()
                        ),
                        new InMemoryLeerlingRepository(
                                List.of(
                                        sofie
                                )
                        ),
                        probleemRepository
                );

        // ACT
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(
                        herfst,
                        OBSERVATIE
                );

        // ASSERT
        assertEquals(
                0,
                resultaat.getAantalToewijzingen()
        );

        assertEquals(
                1,
                resultaat
                        .getNietToegewezenLeerlingen()
                        .size()
        );

        assertSame(
                sofie,
                resultaat
                        .getNietToegewezenLeerlingen()
                        .getFirst()
        );

        assertEquals(
                1,
                resultaat
                        .getImportProblemen()
                        .size()
        );

        VoorkeurImportProbleem probleemUitResultaat =
                resultaat
                        .getImportProblemen()
                        .getFirst();

        assertSame(
                sofie,
                probleemUitResultaat.getLeerling()
        );

        assertSame(
                herfst,
                probleemUitResultaat.getPeriode()
        );

        assertEquals(
                "Keuze 3 is niet ingevuld",
                probleemUitResultaat.getReden()
        );
    }

    @Test
    void leerlingDieNietDeelneemtWordtNietAutomatischToegewezen() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.of(2099, 11, 22);

        LocalDate eindDatum =
                LocalDate.of(2100, 2, 21);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas klas1AA =
                maakObservatieKlas(
                        schooljaar
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        Leerling sofie =
                new Leerling(
                        "Sofie",
                        "Janssens",
                        klas1AA
                );

        sofie.deactiveer();

        TalentenPeriode winter =
                new TalentenPeriode(
                        "Winter",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        IngerichtTalent schakenWinter =
                richtTalentIn(
                        new Talent(
                                "Schaken",
                                "Leren schaken"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent voetbalWinter =
                richtTalentIn(
                        new Talent(
                                "Voetbal",
                                "Voetbaltraining"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent kokenWinter =
                richtTalentIn(
                        new Talent(
                                "Koken",
                                "Leren koken"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        List<Voorkeur> voorkeuren =
                List.of(
                        new Voorkeur(
                                jan,
                                winter,
                                schakenWinter,
                                1
                        ),
                        new Voorkeur(
                                jan,
                                winter,
                                voetbalWinter,
                                2
                        ),
                        new Voorkeur(
                                jan,
                                winter,
                                kokenWinter,
                                3
                        ),
                        new Voorkeur(
                                sofie,
                                winter,
                                schakenWinter,
                                1
                        ),
                        new Voorkeur(
                                sofie,
                                winter,
                                voetbalWinter,
                                2
                        ),
                        new Voorkeur(
                                sofie,
                                winter,
                                kokenWinter,
                                3
                        )
                );

        AutomatischeVerdelingService service =
                maakService(
                        new InMemoryVoorkeurRepository(
                                voorkeuren
                        ),
                        new InMemoryToewijzingRepository(
                                new ArrayList<>()
                        ),
                        new InMemoryLeerlingRepository(
                                List.of(
                                        jan,
                                        sofie
                                )
                        ),
                        new InMemoryVoorkeurImportProbleemRepository(
                                new ArrayList<>()
                        )
                );

        // ACT
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(
                        winter,
                        OBSERVATIE
                );

        // ASSERT
        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );

        assertSame(
                jan,
                resultaat
                        .getToewijzingen()
                        .getFirst()
                        .getLeerling()
        );

        assertTrue(
                resultaat
                        .getToewijzingen()
                        .stream()
                        .noneMatch(
                                toewijzing ->
                                        toewijzing
                                                .getLeerling()
                                                .equals(
                                                        sofie
                                                )
                        )
        );

        assertFalse(
                resultaat
                        .getNietToegewezenLeerlingen()
                        .contains(
                                sofie
                        )
        );
    }

    @Test
    void automatischeVerdelingNegeertVoorkeurVoorInactiefIngerichtTalent() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.of(2099, 11, 22);

        LocalDate eindDatum =
                LocalDate.of(2100, 2, 21);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas klas1AA =
                maakObservatieKlas(
                        schooljaar
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode winter =
                new TalentenPeriode(
                        "Winter",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        IngerichtTalent digitaleMediaWinter =
                richtTalentIn(
                        new Talent(
                                "Digitale Media",
                                "Foto en video"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent voetbalWinter =
                richtTalentIn(
                        new Talent(
                                "Voetbal",
                                "Voetbaltraining"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent kokenWinter =
                richtTalentIn(
                        new Talent(
                                "Koken",
                                "Leren koken"
                        ),
                        winter,
                        10,
                        OBSERVATIE
                );

        digitaleMediaWinter.deactiveer();

        List<Voorkeur> voorkeuren =
                List.of(
                        new Voorkeur(
                                jan,
                                winter,
                                digitaleMediaWinter,
                                1
                        ),
                        new Voorkeur(
                                jan,
                                winter,
                                voetbalWinter,
                                2
                        ),
                        new Voorkeur(
                                jan,
                                winter,
                                kokenWinter,
                                3
                        )
                );

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        AutomatischeVerdelingService service =
                maakService(
                        new InMemoryVoorkeurRepository(
                                voorkeuren
                        ),
                        toewijzingRepository,
                        new InMemoryLeerlingRepository(
                                List.of(
                                        jan
                                )
                        ),
                        new InMemoryVoorkeurImportProbleemRepository(
                                new ArrayList<>()
                        )
                );

        // ACT
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(
                        winter,
                        OBSERVATIE
                );

        // ASSERT
        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );

        assertTrue(
                resultaat
                        .getNietToegewezenLeerlingen()
                        .isEmpty()
        );

        Toewijzing toewijzing =
                resultaat
                        .getToewijzingen()
                        .getFirst();

        assertSame(
                jan,
                toewijzing.getLeerling()
        );

        assertSame(
                voetbalWinter,
                toewijzing.getIngerichtTalent()
        );

        assertEquals(
                2,
                toewijzing.getVoorkeurNummer()
        );

        assertEquals(
                1,
                toewijzingRepository
                        .getOpgeslagenToewijzingen()
                        .size()
        );
    }

    private AutomatischeVerdelingService maakService(
            InMemoryVoorkeurRepository voorkeurRepository,
            InMemoryToewijzingRepository toewijzingRepository,
            InMemoryLeerlingRepository leerlingRepository,
            InMemoryVoorkeurImportProbleemRepository probleemRepository
    ) {
        return new AutomatischeVerdelingService(
                voorkeurRepository,
                toewijzingRepository,
                leerlingRepository,
                probleemRepository
        );
    }

    private AutomatischeVerdelingService maakLegeService() {
        return maakService(
                new InMemoryVoorkeurRepository(
                        new ArrayList<>()
                ),
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                ),
                new InMemoryLeerlingRepository(
                        new ArrayList<>()
                ),
                new InMemoryVoorkeurImportProbleemRepository(
                        new ArrayList<>()
                )
        );
    }

    private Klas maakObservatieKlas(
            Schooljaar schooljaar
    ) {
        return new Klas(
                "1AA",
                schooljaar,
                1,
                OBSERVATIE
        );
    }

    private IngerichtTalent richtTalentIn(
            Talent talent,
            TalentenPeriode periode,
            int maximumCapaciteit,
            Doelgroep doelgroep
    ) {
        return new IngerichtTalent(
                talent,
                periode,
                talent.getNaam(),
                talent.getBeschrijving(),
                maximumCapaciteit,
                doelgroep,
                List.of(
                        testLeerkracht
                )
        );
    }
}