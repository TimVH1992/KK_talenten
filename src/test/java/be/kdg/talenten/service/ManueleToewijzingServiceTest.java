package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryToewijzingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryVoorkeurRepository;
import be.kdg.talenten.service.verdeling.ManueleToewijzingService;
import be.kdg.talenten.testutil.TestDataFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManueleToewijzingServiceTest {

    private static final Doelgroep OBSERVATIE =
            Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

    private static final Doelgroep KWALIFICATIE =
            Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB;

    private Leerkracht testLeerkracht;
    private Schooljaar schooljaar2099_2100;

    @BeforeEach
    void setUp() {
        testLeerkracht =
                new Leerkracht(
                        "Test",
                        "Leerkracht"
                );

        schooljaar2099_2100 =
                TestDataFactory.schooljaarVoorPeriode(
                        LocalDate.of(2099, 9, 1),
                        LocalDate.of(2100, 1, 21)
                );
    }

    @Test
    void manueleToewijzingNaarAnderTalentWijzigtTalentEnType() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent voetbal =
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        10
                );

        IngerichtTalent voetbalHerfst =
                richtTalentIn(
                        voetbal,
                        herfst,
                        10
                );

        InMemoryToewijzingRepository repository =
                maakLeegToewijzingRepository();

        repository.save(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        ManueleToewijzingService service =
                maakService(
                        repository
                );

        // ACT
        service.wijzigToewijzing(
                herfst,
                jan,
                voetbalHerfst
        );

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                herfst
                        );

        assertNotNull(
                gewijzigdeToewijzing
        );

        assertSame(
                voetbalHerfst,
                gewijzigdeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.MANUEEL,
                gewijzigdeToewijzing.getToewijzingsType()
        );
    }

    @Test
    void manueleToewijzingNaarZelfdeTalentVerandertTypeNaarManueel() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        1
                );

        InMemoryToewijzingRepository repository =
                maakLeegToewijzingRepository();

        repository.save(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        ManueleToewijzingService service =
                maakService(
                        repository
                );

        // ACT
        service.wijzigToewijzing(
                herfst,
                jan,
                schakenHerfst
        );

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                herfst
                        );

        assertNotNull(
                gewijzigdeToewijzing
        );

        assertSame(
                schakenHerfst,
                gewijzigdeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.MANUEEL,
                gewijzigdeToewijzing.getToewijzingsType()
        );
    }

    @Test
    void manueleToewijzingNegeertHistoriek() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        TalentenPeriode kerst =
                new TalentenPeriode(
                        "Kerst",
                        LocalDate.of(2099, 12, 22),
                        LocalDate.of(2100, 1, 21),
                        schooljaar2099_2100
                );

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent voetbal =
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                );

        IngerichtTalent voetbalHerfst =
                richtTalentIn(
                        voetbal,
                        herfst,
                        10
                );

        IngerichtTalent schakenKerst =
                richtTalentIn(
                        schaken,
                        kerst,
                        10
                );

        IngerichtTalent voetbalKerst =
                richtTalentIn(
                        voetbal,
                        kerst,
                        10
                );

        Toewijzing historischeToewijzing =
                new Toewijzing(
                        jan,
                        voetbalHerfst,
                        ToewijzingsType.AUTOMATISCH
                );

        InMemoryToewijzingRepository repository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>(
                                List.of(
                                        historischeToewijzing
                                )
                        )
                );

        repository.save(
                new Toewijzing(
                        jan,
                        schakenKerst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        ManueleToewijzingService service =
                maakService(
                        repository
                );

        // ACT
        service.wijzigToewijzing(
                kerst,
                jan,
                voetbalKerst
        );

        // ASSERT
        Toewijzing huidigeToewijzing =
                repository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                kerst
                        );

        assertNotNull(
                huidigeToewijzing
        );

        assertSame(
                voetbalKerst,
                huidigeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.MANUEEL,
                huidigeToewijzing.getToewijzingsType()
        );

        List<Toewijzing> historiek =
                repository
                        .zoekHistorischeToewijzingenVoorSchooljaar(
                                schooljaar2099_2100
                        );

        assertEquals(
                1,
                historiek.size()
        );

        assertSame(
                voetbalHerfst,
                historiek
                        .getFirst()
                        .getIngerichtTalent()
        );
    }

    @Test
    void manueleToewijzingNaarTalentUitAnderePeriodeWordtGeweigerd() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        TalentenPeriode kerst =
                new TalentenPeriode(
                        "Kerst",
                        LocalDate.of(2099, 12, 22),
                        LocalDate.of(2100, 1, 21),
                        schooljaar2099_2100
                );

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent voetbal =
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        10
                );

        IngerichtTalent voetbalKerst =
                richtTalentIn(
                        voetbal,
                        kerst,
                        10
                );

        InMemoryToewijzingRepository repository =
                maakLeegToewijzingRepository();

        repository.save(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        ManueleToewijzingService service =
                maakService(
                        repository
                );

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(
                        herfst,
                        jan,
                        voetbalKerst
                )
        );

        Toewijzing oorspronkelijkeToewijzing =
                repository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                herfst
                        );

        assertNotNull(
                oorspronkelijkeToewijzing
        );

        assertSame(
                schakenHerfst,
                oorspronkelijkeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.AUTOMATISCH,
                oorspronkelijkeToewijzing.getToewijzingsType()
        );
    }

    @Test
    void manueleToewijzingZonderBestaandeToewijzingMaaktNieuweToewijzing() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent voetbal =
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                );

        IngerichtTalent voetbalHerfst =
                richtTalentIn(
                        voetbal,
                        herfst,
                        10
                );

        InMemoryToewijzingRepository repository =
                maakLeegToewijzingRepository();

        ManueleToewijzingService service =
                maakService(
                        repository
                );

        assertNull(
                repository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                herfst
                        )
        );

        // ACT
        service.wijzigToewijzing(
                herfst,
                jan,
                voetbalHerfst
        );

        // ASSERT
        Toewijzing nieuweToewijzing =
                repository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                herfst
                        );

        assertNotNull(
                nieuweToewijzing
        );

        assertSame(
                voetbalHerfst,
                nieuweToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.MANUEEL,
                nieuweToewijzing.getToewijzingsType()
        );
    }

    @Test
    void manueleToewijzingMetNullArgumentWordtGeweigerd() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent voetbal =
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                );

        IngerichtTalent voetbalHerfst =
                richtTalentIn(
                        voetbal,
                        herfst,
                        10
                );

        InMemoryToewijzingRepository repository =
                maakLeegToewijzingRepository();

        ManueleToewijzingService service =
                maakService(
                        repository
                );

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(
                        null,
                        jan,
                        voetbalHerfst
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(
                        herfst,
                        null,
                        voetbalHerfst
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(
                        herfst,
                        jan,
                        null
                )
        );
    }

    @Test
    void manueleToewijzingMagKlaslimietOverschrijden() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        Leerling tim =
                new Leerling(
                        "Tim",
                        "Janssens",
                        klas1AA
                );

        Leerling sara =
                new Leerling(
                        "Sara",
                        "Mertens",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent voetbal =
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        10
                );

        IngerichtTalent voetbalHerfst =
                richtTalentIn(
                        voetbal,
                        herfst,
                        10
                );

        InMemoryToewijzingRepository repository =
                maakLeegToewijzingRepository();

        repository.save(
                new Toewijzing(
                        tim,
                        voetbalHerfst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        repository.save(
                new Toewijzing(
                        sara,
                        voetbalHerfst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        repository.save(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        ManueleToewijzingService service =
                maakService(
                        repository
                );

        // ACT
        service.wijzigToewijzing(
                herfst,
                jan,
                voetbalHerfst
        );

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                herfst
                        );

        assertNotNull(
                gewijzigdeToewijzing
        );

        assertSame(
                voetbalHerfst,
                gewijzigdeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.MANUEEL,
                gewijzigdeToewijzing.getToewijzingsType()
        );

        assertEquals(
                3,
                repository.telToewijzingenVoorIngerichtTalent(
                        voetbalHerfst
                )
        );
    }

    @Test
    void manueelVanTweedeNaarEersteVoorkeurGeeftVoorkeurNummer1() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent voetbal =
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                );

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent koken =
                new Talent(
                        "Koken",
                        "Leren koken"
                );

        IngerichtTalent voetbalHerfst =
                richtTalentIn(
                        voetbal,
                        herfst,
                        10
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        10
                );

        IngerichtTalent kokenHerfst =
                richtTalentIn(
                        koken,
                        herfst,
                        10
                );

        InMemoryToewijzingRepository toewijzingRepository =
                maakLeegToewijzingRepository();

        toewijzingRepository.save(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        2
                )
        );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(
                        List.of(
                                new Voorkeur(
                                        jan,
                                        herfst,
                                        voetbalHerfst,
                                        1
                                ),
                                new Voorkeur(
                                        jan,
                                        herfst,
                                        schakenHerfst,
                                        2
                                ),
                                new Voorkeur(
                                        jan,
                                        herfst,
                                        kokenHerfst,
                                        3
                                )
                        )
                );

        ManueleToewijzingService service =
                new ManueleToewijzingService(
                        toewijzingRepository,
                        voorkeurRepository
                );

        // ACT
        service.wijzigToewijzing(
                herfst,
                jan,
                voetbalHerfst
        );

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                toewijzingRepository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                herfst
                        );

        assertNotNull(
                gewijzigdeToewijzing
        );

        assertSame(
                voetbalHerfst,
                gewijzigdeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.MANUEEL,
                gewijzigdeToewijzing.getToewijzingsType()
        );

        assertEquals(
                1,
                gewijzigdeToewijzing.getVoorkeurNummer()
        );
    }

    @Test
    void manueelVanEersteNaarDerdeVoorkeurGeeftVoorkeurNummer3() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent voetbal =
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                );

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent koken =
                new Talent(
                        "Koken",
                        "Leren koken"
                );

        IngerichtTalent voetbalHerfst =
                richtTalentIn(
                        voetbal,
                        herfst,
                        10
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        10
                );

        IngerichtTalent kokenHerfst =
                richtTalentIn(
                        koken,
                        herfst,
                        10
                );

        InMemoryToewijzingRepository toewijzingRepository =
                maakLeegToewijzingRepository();

        toewijzingRepository.save(
                new Toewijzing(
                        jan,
                        voetbalHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        1
                )
        );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(
                        List.of(
                                new Voorkeur(
                                        jan,
                                        herfst,
                                        voetbalHerfst,
                                        1
                                ),
                                new Voorkeur(
                                        jan,
                                        herfst,
                                        schakenHerfst,
                                        2
                                ),
                                new Voorkeur(
                                        jan,
                                        herfst,
                                        kokenHerfst,
                                        3
                                )
                        )
                );

        ManueleToewijzingService service =
                new ManueleToewijzingService(
                        toewijzingRepository,
                        voorkeurRepository
                );

        // ACT
        service.wijzigToewijzing(
                herfst,
                jan,
                kokenHerfst
        );

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                toewijzingRepository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                herfst
                        );

        assertNotNull(
                gewijzigdeToewijzing
        );

        assertSame(
                kokenHerfst,
                gewijzigdeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.MANUEEL,
                gewijzigdeToewijzing.getToewijzingsType()
        );

        assertEquals(
                3,
                gewijzigdeToewijzing.getVoorkeurNummer()
        );
    }

    @Test
    void manueelNaarTalentDatGeenVoorkeurIsGeeftNull() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent voetbal =
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                );

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent koken =
                new Talent(
                        "Koken",
                        "Leren koken"
                );

        Talent dans =
                new Talent(
                        "Dans",
                        "Leren dansen"
                );

        IngerichtTalent voetbalHerfst =
                richtTalentIn(
                        voetbal,
                        herfst,
                        10
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        10
                );

        IngerichtTalent kokenHerfst =
                richtTalentIn(
                        koken,
                        herfst,
                        10
                );

        IngerichtTalent dansHerfst =
                richtTalentIn(
                        dans,
                        herfst,
                        10
                );

        InMemoryToewijzingRepository toewijzingRepository =
                maakLeegToewijzingRepository();

        toewijzingRepository.save(
                new Toewijzing(
                        jan,
                        voetbalHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        1
                )
        );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(
                        List.of(
                                new Voorkeur(
                                        jan,
                                        herfst,
                                        voetbalHerfst,
                                        1
                                ),
                                new Voorkeur(
                                        jan,
                                        herfst,
                                        schakenHerfst,
                                        2
                                ),
                                new Voorkeur(
                                        jan,
                                        herfst,
                                        kokenHerfst,
                                        3
                                )
                        )
                );

        ManueleToewijzingService service =
                new ManueleToewijzingService(
                        toewijzingRepository,
                        voorkeurRepository
                );

        // ACT
        service.wijzigToewijzing(
                herfst,
                jan,
                dansHerfst
        );

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                toewijzingRepository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                herfst
                        );

        assertNotNull(
                gewijzigdeToewijzing
        );

        assertSame(
                dansHerfst,
                gewijzigdeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.MANUEEL,
                gewijzigdeToewijzing.getToewijzingsType()
        );

        assertNull(
                gewijzigdeToewijzing.getVoorkeurNummer()
        );
    }

    @Test
    void manueleToewijzingNaarVerkeerdeDoelgroepWordtGeweigerd() {
        // ARRANGE
        Klas observatieKlas =
                maakObservatieKlas(
                        "2AA",
                        2,
                        schooljaar2099_2100
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        observatieKlas
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent voetbal =
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                );

        IngerichtTalent schakenObservatie =
                richtTalentIn(
                        schaken,
                        herfst,
                        10,
                        OBSERVATIE
                );

        IngerichtTalent voetbalKwalificatie =
                richtTalentIn(
                        voetbal,
                        herfst,
                        10,
                        KWALIFICATIE
                );

        InMemoryToewijzingRepository repository =
                maakLeegToewijzingRepository();

        repository.save(
                new Toewijzing(
                        jan,
                        schakenObservatie,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        ManueleToewijzingService service =
                maakService(
                        repository
                );

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(
                        herfst,
                        jan,
                        voetbalKwalificatie
                )
        );

        Toewijzing oorspronkelijkeToewijzing =
                repository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                jan,
                                herfst
                        );

        assertNotNull(
                oorspronkelijkeToewijzing
        );

        assertSame(
                schakenObservatie,
                oorspronkelijkeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.AUTOMATISCH,
                oorspronkelijkeToewijzing.getToewijzingsType()
        );
    }

    @Test
    void manueleToewijzingVoorAfgelopenPeriodeWordtGeweigerd() {
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
                        "1AA",
                        1,
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

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent koken =
                new Talent(
                        "Koken",
                        "Leren koken"
                );

        IngerichtTalent schakenLente =
                richtTalentIn(
                        schaken,
                        lente,
                        10
                );

        IngerichtTalent kokenLente =
                richtTalentIn(
                        koken,
                        lente,
                        10
                );

        InMemoryToewijzingRepository repository =
                maakLeegToewijzingRepository();

        repository.save(
                new Toewijzing(
                        alice,
                        schakenLente,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        ManueleToewijzingService service =
                maakService(
                        repository
                );

        // ACT + ASSERT
        assertThrows(
                IllegalStateException.class,
                () -> service.wijzigToewijzing(
                        lente,
                        alice,
                        kokenLente
                )
        );

        Toewijzing oorspronkelijkeToewijzing =
                repository
                        .zoekToewijzingVoorLeerlingEnPeriode(
                                alice,
                                lente
                        );

        assertNotNull(
                oorspronkelijkeToewijzing
        );

        assertSame(
                schakenLente,
                oorspronkelijkeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.AUTOMATISCH,
                oorspronkelijkeToewijzing.getToewijzingsType()
        );

        assertEquals(
                1,
                repository
                        .zoekVoorPeriode(
                                lente
                        )
                        .size()
        );
    }

    @Test
    void manueleToewijzingMagMaximumCapaciteitOverschrijden() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.now()
                        .plusMonths(2);

        LocalDate eindDatum =
                LocalDate.now()
                        .plusMonths(4);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1,
                        schooljaar
                );

        Leerling alice =
                new Leerling(
                        "Alice",
                        "Janssens",
                        klas1AA
                );

        Leerling jos =
                new Leerling(
                        "Jos",
                        "Janssens",
                        klas1AA
                );

        Leerling tim =
                new Leerling(
                        "Tim",
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

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        IngerichtTalent schakenLente =
                richtTalentIn(
                        schaken,
                        lente,
                        2
                );

        InMemoryToewijzingRepository repository =
                maakLeegToewijzingRepository();

        repository.save(
                new Toewijzing(
                        alice,
                        schakenLente,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        repository.save(
                new Toewijzing(
                        jos,
                        schakenLente,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        ManueleToewijzingService service =
                maakService(
                        repository
                );

        // ACT
        Toewijzing result =
                service.wijzigToewijzing(
                        lente,
                        tim,
                        schakenLente
                );

        // ASSERT
        Assertions.assertNotNull(
                result
        );

        Assertions.assertEquals(
                schakenLente,
                result.getIngerichtTalent()
        );

        Assertions.assertEquals(
                ToewijzingsType.MANUEEL,
                result.getToewijzingsType()
        );
    }

    private Klas maakObservatieKlas(
            String naam,
            int leerjaar,
            Schooljaar schooljaar
    ) {
        return new Klas(
                naam,
                schooljaar,
                leerjaar,
                OBSERVATIE
        );
    }

    private TalentenPeriode maakHerfstPeriode() {
        return new TalentenPeriode(
                "Herfst",
                LocalDate.of(2099, 9, 1),
                LocalDate.of(2099, 10, 31),
                schooljaar2099_2100
        );
    }

    private IngerichtTalent richtTalentIn(
            Talent talent,
            TalentenPeriode periode,
            int maximumCapaciteit
    ) {
        return richtTalentIn(
                talent,
                periode,
                maximumCapaciteit,
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

    private InMemoryToewijzingRepository maakLeegToewijzingRepository() {
        return new InMemoryToewijzingRepository(
                new ArrayList<>()
        );
    }

    private ManueleToewijzingService maakService(
            InMemoryToewijzingRepository toewijzingRepository
    ) {
        return new ManueleToewijzingService(
                toewijzingRepository,
                new InMemoryVoorkeurRepository(
                        new ArrayList<>()
                )
        );
    }
}