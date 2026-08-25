package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.overzicht.KlasOverzicht;
import be.kdg.talenten.overzicht.LeerlingToewijzingOverzicht;
import be.kdg.talenten.overzicht.NietToegewezenLeerlingOverzicht;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.LeerlingKlasHistoriekRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryIngerichtTalentRepository;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryToewijzingRepository;
import be.kdg.talenten.service.verdeling.VerdelingBekijkenService;
import be.kdg.talenten.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VerdelingBekijkenServiceTest {

    private Leerkracht testLeerkracht;
    private Schooljaar schooljaar2026_2027;

    @BeforeEach
    void setUp() {
        testLeerkracht =
                new Leerkracht(
                        "Test",
                        "Leerkracht"
                );

        schooljaar2026_2027 =
                TestDataFactory.schooljaarVoorPeriode(
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2027, 1, 21)
                );
    }

    @Test
    void bekijkPerIngerichtTalentGroepeertToewijzingenCorrect() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1
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

        Leerling eveline =
                new Leerling(
                        "Eveline",
                        "Van Oevelen",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent dansen =
                new Talent(
                        "Dansen",
                        "Shake shake shake"
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        10
                );

        IngerichtTalent dansenHerfst =
                richtTalentIn(
                        dansen,
                        herfst,
                        6
                );

        List<IngerichtTalent> ingerichteTalenten =
                List.of(
                        schakenHerfst,
                        dansenHerfst
                );

        List<Toewijzing> schakenToewijzingen =
                List.of(
                        new Toewijzing(
                                jan,
                                schakenHerfst,
                                ToewijzingsType.AUTOMATISCH
                        ),
                        new Toewijzing(
                                julie,
                                schakenHerfst,
                                ToewijzingsType.MANUEEL
                        )
                );

        List<Toewijzing> dansenToewijzingen =
                List.of(
                        new Toewijzing(
                                eveline,
                                dansenHerfst,
                                ToewijzingsType.AUTOMATISCH
                        )
                );

        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        ingerichteTalenten
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        toewijzingRepository.saveAll(
                schakenToewijzingen
        );

        toewijzingRepository.saveAll(
                dansenToewijzingen
        );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        new ArrayList<>()
                );

        LeerlingKlasHistoriekRepository historiekRepository =
                new TestLeerlingKlasHistoriekRepository();

        VerdelingBekijkenService service =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        historiekRepository
                );

        // ACT
        List<IngerichtTalentOverzicht> overzichten =
                service.bekijkPerIngerichtTalent(
                        herfst
                );

        // ASSERT
        assertEquals(
                new IngerichtTalentOverzicht(
                        schakenHerfst,
                        2,
                        8,
                        schakenToewijzingen
                ),
                overzichten.getFirst()
        );

        assertEquals(
                new IngerichtTalentOverzicht(
                        dansenHerfst,
                        1,
                        5,
                        dansenToewijzingen
                ),
                overzichten.get(1)
        );
    }

    @Test
    void bekijkPerIngerichtTalentToontOokTalentZonderToewijzingen() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1
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

        Leerling eveline =
                new Leerling(
                        "Eveline",
                        "Van Oevelen",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent dansen =
                new Talent(
                        "Dansen",
                        "Shake shake shake"
                );

        Talent koken =
                new Talent(
                        "Koken",
                        "Een warme keuken is vaak lekker"
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        10
                );

        IngerichtTalent dansenHerfst =
                richtTalentIn(
                        dansen,
                        herfst,
                        6
                );

        IngerichtTalent kokenHerfst =
                richtTalentIn(
                        koken,
                        herfst,
                        5
                );

        List<IngerichtTalent> ingerichteTalenten =
                List.of(
                        schakenHerfst,
                        dansenHerfst,
                        kokenHerfst
                );

        List<Toewijzing> schakenToewijzingen =
                List.of(
                        new Toewijzing(
                                jan,
                                schakenHerfst,
                                ToewijzingsType.AUTOMATISCH
                        ),
                        new Toewijzing(
                                julie,
                                schakenHerfst,
                                ToewijzingsType.MANUEEL
                        )
                );

        List<Toewijzing> dansenToewijzingen =
                List.of(
                        new Toewijzing(
                                eveline,
                                dansenHerfst,
                                ToewijzingsType.AUTOMATISCH
                        )
                );

        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        ingerichteTalenten
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        toewijzingRepository.saveAll(
                schakenToewijzingen
        );

        toewijzingRepository.saveAll(
                dansenToewijzingen
        );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        new ArrayList<>()
                );

        LeerlingKlasHistoriekRepository historiekRepository =
                new TestLeerlingKlasHistoriekRepository();

        VerdelingBekijkenService service =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        historiekRepository
                );

        // ACT
        List<IngerichtTalentOverzicht> overzichten =
                service.bekijkPerIngerichtTalent(
                        herfst
                );

        // ASSERT
        assertEquals(
                3,
                overzichten.size()
        );

        assertEquals(
                new IngerichtTalentOverzicht(
                        kokenHerfst,
                        0,
                        5,
                        List.of()
                ),
                overzichten.get(2)
        );
    }

    @Test
    void bekijkPerIngerichtTalentToontAlleenGevraagdePeriode() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1
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

        Leerling eveline =
                new Leerling(
                        "Eveline",
                        "Van Oevelen",
                        klas1AA
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        TalentenPeriode kerst =
                new TalentenPeriode(
                        "Kerst",
                        LocalDate.of(2026, 12, 22),
                        LocalDate.of(2027, 1, 21),
                        schooljaar2026_2027
                );

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent dansen =
                new Talent(
                        "Dansen",
                        "Shake shake shake"
                );

        Talent koken =
                new Talent(
                        "Koken",
                        "Een warme keuken is vaak lekker"
                );

        IngerichtTalent schakenHerfst =
                richtTalentIn(
                        schaken,
                        herfst,
                        10
                );

        IngerichtTalent dansenHerfst =
                richtTalentIn(
                        dansen,
                        herfst,
                        6
                );

        IngerichtTalent kokenHerfst =
                richtTalentIn(
                        koken,
                        herfst,
                        5
                );

        IngerichtTalent schakenKerst =
                richtTalentIn(
                        schaken,
                        kerst,
                        10
                );

        IngerichtTalent dansenKerst =
                richtTalentIn(
                        dansen,
                        kerst,
                        2
                );

        List<IngerichtTalent> ingerichteTalenten =
                List.of(
                        schakenHerfst,
                        dansenHerfst,
                        kokenHerfst,
                        schakenKerst,
                        dansenKerst
                );

        List<Toewijzing> schakenToewijzingenHerfst =
                List.of(
                        new Toewijzing(
                                jan,
                                schakenHerfst,
                                ToewijzingsType.AUTOMATISCH
                        ),
                        new Toewijzing(
                                julie,
                                schakenHerfst,
                                ToewijzingsType.MANUEEL
                        )
                );

        List<Toewijzing> dansenToewijzingenHerfst =
                List.of(
                        new Toewijzing(
                                eveline,
                                dansenHerfst,
                                ToewijzingsType.AUTOMATISCH
                        )
                );

        List<Toewijzing> kerstToewijzingen =
                List.of(
                        new Toewijzing(
                                jan,
                                schakenKerst,
                                ToewijzingsType.MANUEEL
                        ),
                        new Toewijzing(
                                eveline,
                                dansenKerst,
                                ToewijzingsType.AUTOMATISCH
                        )
                );

        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        ingerichteTalenten
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        toewijzingRepository.saveAll(
                schakenToewijzingenHerfst
        );

        toewijzingRepository.saveAll(
                dansenToewijzingenHerfst
        );

        toewijzingRepository.saveAll(
                kerstToewijzingen
        );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        new ArrayList<>()
                );

        LeerlingKlasHistoriekRepository historiekRepository =
                new TestLeerlingKlasHistoriekRepository();

        VerdelingBekijkenService service =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        historiekRepository
                );

        List<IngerichtTalentOverzicht> verwacht =
                List.of(
                        new IngerichtTalentOverzicht(
                                schakenHerfst,
                                2,
                                8,
                                schakenToewijzingenHerfst
                        ),
                        new IngerichtTalentOverzicht(
                                dansenHerfst,
                                1,
                                5,
                                dansenToewijzingenHerfst
                        ),
                        new IngerichtTalentOverzicht(
                                kokenHerfst,
                                0,
                                5,
                                List.of()
                        )
                );

        // ACT
        List<IngerichtTalentOverzicht> werkelijk =
                service.bekijkPerIngerichtTalent(
                        herfst
                );

        // ASSERT
        assertEquals(
                verwacht,
                werkelijk
        );
    }

    @Test
    void bekijkPerIngerichtTalentMetNullPeriodeWordtGeweigerd() {
        // ARRANGE
        VerdelingBekijkenService service =
                maakLegeVerdelingBekijkenService();

        // ACT & ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.bekijkPerIngerichtTalent(
                        null
                )
        );
    }

    @Test
    void bekijkVoorKlasToontElkeLeerlingMetEventueleToewijzing() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1
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

        Leerling eveline =
                new Leerling(
                        "Eveline",
                        "Van Oevelen",
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
                        10
                );

        Toewijzing toewijzingJan =
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                );

        Toewijzing toewijzingJulie =
                new Toewijzing(
                        julie,
                        schakenHerfst,
                        ToewijzingsType.MANUEEL
                );

        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        List.of(schakenHerfst)
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        toewijzingRepository.saveAll(
                List.of(
                        toewijzingJan,
                        toewijzingJulie
                )
        );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan,
                                eveline,
                                julie
                        )
                );

        TestLeerlingKlasHistoriekRepository historiekRepository =
                new TestLeerlingKlasHistoriekRepository();

        historiekRepository.startHistoriek(
                jan,
                klas1AA,
                schooljaar2026_2027.getStartDatum()
        );

        historiekRepository.startHistoriek(
                eveline,
                klas1AA,
                schooljaar2026_2027.getStartDatum()
        );

        historiekRepository.startHistoriek(
                julie,
                klas1AA,
                schooljaar2026_2027.getStartDatum()
        );

        VerdelingBekijkenService service =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        historiekRepository
                );

        // ACT
        KlasOverzicht werkelijk =
                service.bekijkVoorKlas(
                        herfst,
                        klas1AA
                );

        // ASSERT
        List<LeerlingToewijzingOverzicht> verwachteOverzichten =
                List.of(
                        new LeerlingToewijzingOverzicht(
                                jan,
                                toewijzingJan
                        ),
                        new LeerlingToewijzingOverzicht(
                                eveline,
                                null
                        ),
                        new LeerlingToewijzingOverzicht(
                                julie,
                                toewijzingJulie
                        )
                );

        KlasOverzicht verwacht =
                new KlasOverzicht(
                        klas1AA,
                        herfst,
                        verwachteOverzichten
                );

        assertEquals(
                verwacht,
                werkelijk
        );
    }

    @Test
    void bekijkVoorKlasMetNullPeriodeWordtGeweigerd() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1
                );

        VerdelingBekijkenService service =
                maakLegeVerdelingBekijkenService();

        // ACT & ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.bekijkVoorKlas(
                        null,
                        klas1AA
                )
        );
    }

    @Test
    void bekijkVoorKlasMetNullKlasWordtGeweigerd() {
        // ARRANGE
        TalentenPeriode herfst =
                maakHerfstPeriode();

        VerdelingBekijkenService service =
                maakLegeVerdelingBekijkenService();

        // ACT & ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.bekijkVoorKlas(
                        herfst,
                        null
                )
        );
    }

    @Test
    void bekijkVoorKlasToontAlleenLeerlingenVanGevraagdeKlas() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1
                );

        Klas klas1AB =
                maakObservatieKlas(
                        "1AB",
                        1
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

        Leerling eveline =
                new Leerling(
                        "Eveline",
                        "Van Oevelen",
                        klas1AB
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
                        10
                );

        Toewijzing toewijzingJan =
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                );

        Toewijzing toewijzingJulie =
                new Toewijzing(
                        julie,
                        schakenHerfst,
                        ToewijzingsType.MANUEEL
                );

        Toewijzing toewijzingEveline =
                new Toewijzing(
                        eveline,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                );

        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        List.of(schakenHerfst)
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        toewijzingRepository.saveAll(
                List.of(
                        toewijzingJan,
                        toewijzingJulie,
                        toewijzingEveline
                )
        );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan,
                                julie,
                                eveline
                        )
                );

        TestLeerlingKlasHistoriekRepository historiekRepository =
                new TestLeerlingKlasHistoriekRepository();

        historiekRepository.startHistoriek(
                jan,
                klas1AA,
                schooljaar2026_2027.getStartDatum()
        );

        historiekRepository.startHistoriek(
                julie,
                klas1AA,
                schooljaar2026_2027.getStartDatum()
        );

        historiekRepository.startHistoriek(
                eveline,
                klas1AB,
                schooljaar2026_2027.getStartDatum()
        );

        VerdelingBekijkenService service =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        historiekRepository
                );

        // ACT
        KlasOverzicht werkelijk =
                service.bekijkVoorKlas(
                        herfst,
                        klas1AA
                );

        // ASSERT
        KlasOverzicht verwacht =
                new KlasOverzicht(
                        klas1AA,
                        herfst,
                        List.of(
                                new LeerlingToewijzingOverzicht(
                                        jan,
                                        toewijzingJan
                                ),
                                new LeerlingToewijzingOverzicht(
                                        julie,
                                        toewijzingJulie
                                )
                        )
                );

        assertEquals(
                verwacht,
                werkelijk
        );
    }

    @Test
    void bekijkVoorKlasGebruiktKlasVanStartTalentenPeriode() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1
                );

        Klas klas1AB =
                maakObservatieKlas(
                        "1AB",
                        1
                );

        TalentenPeriode herfst =
                maakHerfstPeriode();

        Leerling sofie =
                new Leerling(
                        "Sofie",
                        "Janssens",
                        klas1AB
                );

        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        new ArrayList<>()
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(sofie)
                );

        TestLeerlingKlasHistoriekRepository historiekRepository =
                new TestLeerlingKlasHistoriekRepository();

        LocalDate wisseldatum =
                LocalDate.of(
                        2026,
                        10,
                        15
                );

        historiekRepository.startHistoriek(
                sofie,
                klas1AA,
                schooljaar2026_2027.getStartDatum()
        );

        historiekRepository.sluitHuidigeHistoriekAf(
                sofie,
                wisseldatum
        );

        historiekRepository.startHistoriek(
                sofie,
                klas1AB,
                wisseldatum
        );

        VerdelingBekijkenService service =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        historiekRepository
                );

        // ACT
        KlasOverzicht werkelijk1AA =
                service.bekijkVoorKlas(
                        herfst,
                        klas1AA
                );

        KlasOverzicht werkelijk1AB =
                service.bekijkVoorKlas(
                        herfst,
                        klas1AB
                );

        // ASSERT
        KlasOverzicht verwacht1AA =
                new KlasOverzicht(
                        klas1AA,
                        herfst,
                        List.of(
                                new LeerlingToewijzingOverzicht(
                                        sofie,
                                        null
                                )
                        )
                );

        KlasOverzicht verwacht1AB =
                new KlasOverzicht(
                        klas1AB,
                        herfst,
                        List.of()
                );

        assertEquals(
                verwacht1AA,
                werkelijk1AA
        );

        assertEquals(
                verwacht1AB,
                werkelijk1AB
        );
    }

    @Test
    void bekijkNietToegewezenLeerlingenToontLeerlingenZonderToewijzing() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1
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
                        10
                );

        Toewijzing toewijzingJan =
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                );

        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        List.of(schakenHerfst)
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        toewijzingRepository.saveAll(
                List.of(toewijzingJan)
        );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan,
                                sofie
                        )
                );

        LeerlingKlasHistoriekRepository historiekRepository =
                new TestLeerlingKlasHistoriekRepository();

        VerdelingBekijkenService service =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        historiekRepository
                );

        // ACT
        List<NietToegewezenLeerlingOverzicht> resultaat =
                service.bekijkNietToegewezenLeerlingen(
                        herfst
                );

        // ASSERT
        assertEquals(
                1,
                resultaat.size()
        );

        assertEquals(
                sofie,
                resultaat.getFirst().leerling()
        );
    }

    @Test
    void bekijkNietToegewezenLeerlingenNeemtLeerlingDieNietDeelneemtNietOp() {
        // ARRANGE
        Klas klas1AA =
                maakObservatieKlas(
                        "1AA",
                        1
                );

        Leerling sofie =
                new Leerling(
                        "Sofie",
                        "Janssens",
                        klas1AA
                );

        sofie.deactiveer();

        TalentenPeriode herfst =
                maakHerfstPeriode();

        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        new ArrayList<>()
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(sofie)
                );

        LeerlingKlasHistoriekRepository historiekRepository =
                new TestLeerlingKlasHistoriekRepository();

        VerdelingBekijkenService service =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        historiekRepository
                );

        // ACT
        List<NietToegewezenLeerlingOverzicht> resultaat =
                service.bekijkNietToegewezenLeerlingen(
                        herfst
                );

        // ASSERT
        assertTrue(
                resultaat.isEmpty()
        );
    }

    @Test
    void bekijkNietToegewezenLeerlingenMetNullPeriodeWordtGeweigerd() {
        // ARRANGE
        VerdelingBekijkenService service =
                maakLegeVerdelingBekijkenService();

        // ACT & ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.bekijkNietToegewezenLeerlingen(
                        null
                )
        );
    }

    private Klas maakObservatieKlas(
            String naam,
            int leerjaar
    ) {
        return new Klas(
                naam,
                schooljaar2026_2027,
                leerjaar,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
    }

    private TalentenPeriode maakHerfstPeriode() {
        return new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31),
                schooljaar2026_2027
        );
    }

    private IngerichtTalent richtTalentIn(
            Talent talent,
            TalentenPeriode periode,
            int maximumCapaciteit
    ) {
        return new IngerichtTalent(
                talent,
                periode,
                talent.getNaam(),
                talent.getBeschrijving(),
                maximumCapaciteit,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                List.of(testLeerkracht)
        );
    }

    private VerdelingBekijkenService maakLegeVerdelingBekijkenService() {
        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        new ArrayList<>()
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        new ArrayList<>()
                );

        LeerlingKlasHistoriekRepository historiekRepository =
                new TestLeerlingKlasHistoriekRepository();

        return new VerdelingBekijkenService(
                ingerichtTalentRepository,
                toewijzingRepository,
                leerlingRepository,
                historiekRepository
        );
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

                if (registratie.getLeerling().equals(leerling)
                        && registratie.isHuidig()) {

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
                                    && !registratie
                                    .getVanaf()
                                    .isAfter(datum)
                                    && (
                                    registratie.getTot() == null
                                            || registratie
                                            .getTot()
                                            .isAfter(datum)
                            )
                    )
                    .toList();
        }
    }
}