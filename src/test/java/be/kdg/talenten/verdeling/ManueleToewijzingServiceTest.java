package be.kdg.talenten.verdeling;

import be.kdg.talenten.testutil.TestDataFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryToewijzingRepository;
import be.kdg.talenten.service.ManueleToewijzingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManueleToewijzingServiceTest {

    private Leerkracht testLeerkracht;
    private Schooljaar schooljaar2026_2027;

    @BeforeEach
    void setUp() {
        testLeerkracht = new Leerkracht("Test", "Leerkracht");

        schooljaar2026_2027 = TestDataFactory.schooljaarVoorPeriode(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 1, 21)
        );
    }

    @Test
    void manueleToewijzingNaarAnderTalentWijzigtTalentEnType() {
        // ARRANGE
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar2026_2027);
        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);

        TalentenPeriode herfst = maakHerfstPeriode();

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent schakenHerfst = richtTalentIn(schaken, herfst, 10);
        IngerichtTalent voetbalHerfst = richtTalentIn(voetbal, herfst, 10);

        InMemoryToewijzingRepository repository = maakLeegToewijzingRepository();

        repository.save(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH));

        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(herfst, jan, voetbalHerfst);

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(gewijzigdeToewijzing);
        assertSame(voetbalHerfst, gewijzigdeToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.MANUEEL, gewijzigdeToewijzing.getToewijzingsType());
    }

    @Test
    void manueleToewijzingNaarZelfdeTalentVerandertTypeNaarManueel() {
        // ARRANGE
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar2026_2027);
        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);

        TalentenPeriode herfst = maakHerfstPeriode();

        Talent schaken = new Talent("Schaken", "Leren schaken");
        IngerichtTalent schakenHerfst = richtTalentIn(schaken, herfst, 1);

        InMemoryToewijzingRepository repository = maakLeegToewijzingRepository();

        repository.save(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH));

        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(herfst, jan, schakenHerfst);

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(gewijzigdeToewijzing);
        assertSame(schakenHerfst, gewijzigdeToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.MANUEEL, gewijzigdeToewijzing.getToewijzingsType());
    }

    @Test
    void manueleToewijzingNegeertHistoriek() {
        // ARRANGE
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar2026_2027);
        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);

        TalentenPeriode herfst = maakHerfstPeriode();

        TalentenPeriode kerst = new TalentenPeriode(
                "Kerst",
                LocalDate.of(2026, 12, 22),
                LocalDate.of(2027, 1, 21),
                schooljaar2026_2027
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent voetbalHerfst = richtTalentIn(voetbal, herfst, 10);
        IngerichtTalent schakenKerst = richtTalentIn(schaken, kerst, 10);
        IngerichtTalent voetbalKerst = richtTalentIn(voetbal, kerst, 10);

        Toewijzing historischeToewijzing =
                new Toewijzing(jan, voetbalHerfst, ToewijzingsType.AUTOMATISCH);

        InMemoryToewijzingRepository repository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>(List.of(historischeToewijzing))
                );

        repository.save(new Toewijzing(jan, schakenKerst, ToewijzingsType.AUTOMATISCH));

        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(kerst, jan, voetbalKerst);

        // ASSERT
        Toewijzing huidigeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, kerst);

        assertNotNull(huidigeToewijzing);
        assertSame(voetbalKerst, huidigeToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.MANUEEL, huidigeToewijzing.getToewijzingsType());

        List<Toewijzing> historiek =
                repository.zoekHistorischeToewijzingenVoorSchooljaar(schooljaar2026_2027);

        assertEquals(1, historiek.size());
        assertSame(voetbalHerfst, historiek.getFirst().getIngerichtTalent());
    }

    @Test
    void manueleToewijzingNaarTalentUitAnderePeriodeWordtGeweigerd() {
        // ARRANGE
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar2026_2027);
        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);

        TalentenPeriode herfst = maakHerfstPeriode();

        TalentenPeriode kerst = new TalentenPeriode(
                "Kerst",
                LocalDate.of(2026, 12, 22),
                LocalDate.of(2027, 1, 21),
                schooljaar2026_2027
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent schakenHerfst = richtTalentIn(schaken, herfst, 10);
        IngerichtTalent voetbalKerst = richtTalentIn(voetbal, kerst, 10);

        InMemoryToewijzingRepository repository = maakLeegToewijzingRepository();

        repository.save(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH));

        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(herfst, jan, voetbalKerst)
        );

        Toewijzing oorspronkelijkeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(oorspronkelijkeToewijzing);
        assertSame(schakenHerfst, oorspronkelijkeToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.AUTOMATISCH, oorspronkelijkeToewijzing.getToewijzingsType());
    }

    @Test
    void manueleToewijzingZonderBestaandeToewijzingMaaktNieuweToewijzing() {
        // ARRANGE
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar2026_2027);
        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);

        TalentenPeriode herfst = maakHerfstPeriode();

        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        IngerichtTalent voetbalHerfst = richtTalentIn(voetbal, herfst, 10);

        InMemoryToewijzingRepository repository = maakLeegToewijzingRepository();
        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        assertNull(repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst));

        // ACT
        service.wijzigToewijzing(herfst, jan, voetbalHerfst);

        // ASSERT
        Toewijzing nieuweToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(nieuweToewijzing);
        assertSame(voetbalHerfst, nieuweToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.MANUEEL, nieuweToewijzing.getToewijzingsType());
    }

    @Test
    void manueleToewijzingMetNullArgumentWordtGeweigerd() {
        // ARRANGE
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar2026_2027);
        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);

        TalentenPeriode herfst = maakHerfstPeriode();

        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        IngerichtTalent voetbalHerfst = richtTalentIn(voetbal, herfst, 10);

        InMemoryToewijzingRepository repository = maakLeegToewijzingRepository();
        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(null, jan, voetbalHerfst)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(herfst, null, voetbalHerfst)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(herfst, jan, null)
        );
    }

    @Test
    void manueleToewijzingMagKlaslimietOverschrijden() {
        // ARRANGE
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar2026_2027);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling tim = new Leerling("Tim", "Janssens", klas1AA);
        Leerling sara = new Leerling("Sara", "Mertens", klas1AA);

        TalentenPeriode herfst = maakHerfstPeriode();

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent schakenHerfst = richtTalentIn(schaken, herfst, 10);
        IngerichtTalent voetbalHerfst = richtTalentIn(voetbal, herfst, 10);

        InMemoryToewijzingRepository repository = maakLeegToewijzingRepository();

        repository.save(new Toewijzing(tim, voetbalHerfst, ToewijzingsType.AUTOMATISCH));
        repository.save(new Toewijzing(sara, voetbalHerfst, ToewijzingsType.AUTOMATISCH));
        repository.save(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH));

        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(herfst, jan, voetbalHerfst);

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(gewijzigdeToewijzing);
        assertSame(voetbalHerfst, gewijzigdeToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.MANUEEL, gewijzigdeToewijzing.getToewijzingsType());
        assertEquals(3, repository.telToewijzingenVoorIngerichtTalent(voetbalHerfst));
    }

    @Test
    void manueleToewijzingHeeftGeenVoorkeurNummer() {
        // ARRANGE
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar2026_2027);
        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);

        TalentenPeriode herfst = maakHerfstPeriode();

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent schakenHerfst = richtTalentIn(schaken, herfst, 10);
        IngerichtTalent voetbalHerfst = richtTalentIn(voetbal, herfst, 10);

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(new Voorkeur(jan, herfst, schakenHerfst, 1));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);
        VerdelingsResultaat resultaat = verdeler.verdeel();

        Toewijzing automatischeToewijzing = resultaat.getToewijzingen().getFirst();

        assertEquals(1, automatischeToewijzing.getVoorkeurNummer());

        InMemoryToewijzingRepository repository = maakLeegToewijzingRepository();
        repository.save(automatischeToewijzing);

        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(herfst, jan, voetbalHerfst);

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(gewijzigdeToewijzing);
        assertSame(voetbalHerfst, gewijzigdeToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.MANUEEL, gewijzigdeToewijzing.getToewijzingsType());
        assertNull(gewijzigdeToewijzing.getVoorkeurNummer());
    }

    @Test
    void manueleToewijzingNaarVerkeerdeDoelgroepWordtGeweigerd() {
        // ARRANGE
        Klas observatieKlas = maakObservatieKlas("2AA", 2, schooljaar2026_2027);
        Leerling jan = new Leerling("Jan", "Peeters", observatieKlas);

        TalentenPeriode herfst = maakHerfstPeriode();

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent schakenObservatie = richtTalentIn(
                schaken,
                herfst,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        IngerichtTalent voetbalKwalificatie = richtTalentIn(
                voetbal,
                herfst,
                10,
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB
        );

        InMemoryToewijzingRepository repository = maakLeegToewijzingRepository();

        repository.save(new Toewijzing(jan, schakenObservatie, ToewijzingsType.AUTOMATISCH));

        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(herfst, jan, voetbalKwalificatie)
        );

        Toewijzing oorspronkelijkeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(oorspronkelijkeToewijzing);
        assertSame(schakenObservatie, oorspronkelijkeToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.AUTOMATISCH, oorspronkelijkeToewijzing.getToewijzingsType());
    }

    @Test
    void manueleToewijzingVoorAfgelopenPeriodeWordtGeweigerd() {
        // ARRANGE
        LocalDate startDatum = LocalDate.now().minusMonths(4);
        LocalDate eindDatum = LocalDate.now().minusMonths(2);

        Schooljaar schooljaar = TestDataFactory.schooljaarVoorPeriode(startDatum, eindDatum);
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar);

        Leerling alice = new Leerling("Alice", "Janssens", klas1AA);

        TalentenPeriode lente =
                new TalentenPeriode("Lente", startDatum, eindDatum, schooljaar);

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenLente = richtTalentIn(schaken, lente, 10);
        IngerichtTalent kokenLente = richtTalentIn(koken, lente, 10);

        InMemoryToewijzingRepository repository = maakLeegToewijzingRepository();

        repository.save(
                new Toewijzing(alice, schakenLente, ToewijzingsType.AUTOMATISCH)
        );

        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        // ACT + ASSERT
        assertThrows(
                IllegalStateException.class,
                () -> service.wijzigToewijzing(lente, alice, kokenLente)
        );

        Toewijzing oorspronkelijkeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(alice, lente);

        assertNotNull(oorspronkelijkeToewijzing);
        assertSame(schakenLente, oorspronkelijkeToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.AUTOMATISCH, oorspronkelijkeToewijzing.getToewijzingsType());
        assertEquals(1, repository.zoekVoorPeriode(lente).size());
    }

    @Test
    void manueleToewijzingMagMaximumCapaciteitOverschrijden() {
        // ARRANGE
        LocalDate startDatum = LocalDate.now().plusMonths(2);
        LocalDate eindDatum = LocalDate.now().plusMonths(4);

        Schooljaar schooljaar = TestDataFactory.schooljaarVoorPeriode(startDatum, eindDatum);
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar);

        Leerling alice = new Leerling("Alice", "Janssens", klas1AA);
        Leerling jos = new Leerling("Jos", "Janssens", klas1AA);
        Leerling tim = new Leerling("Tim", "Janssens", klas1AA);

        TalentenPeriode lente =
                new TalentenPeriode("Lente", startDatum, eindDatum, schooljaar);

        Talent schaken = new Talent("Schaken", "Leren schaken");

        IngerichtTalent schakenLente = richtTalentIn(schaken, lente, 2);

        InMemoryToewijzingRepository repository = maakLeegToewijzingRepository();

        repository.save(new Toewijzing(alice, schakenLente, ToewijzingsType.AUTOMATISCH));
        repository.save(new Toewijzing(jos, schakenLente, ToewijzingsType.AUTOMATISCH));

        ManueleToewijzingService service = new ManueleToewijzingService(repository);

        // ACT
        Toewijzing result = service.wijzigToewijzing(lente, tim, schakenLente);

        // ASSERT
        Assertions.assertNotNull(result);
        Assertions.assertEquals(schakenLente, result.getIngerichtTalent());
        Assertions.assertEquals(ToewijzingsType.MANUEEL, result.getToewijzingsType());
    }

    private Klas maakObservatieKlas(String naam, int leerjaar, Schooljaar schooljaar) {
        return new Klas(
                naam,
                schooljaar,
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

    private IngerichtTalent richtTalentIn(Talent talent, TalentenPeriode periode, int maximumCapaciteit) {
        return richtTalentIn(
                talent,
                periode,
                maximumCapaciteit,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
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
                maximumCapaciteit,
                doelgroep,
                List.of(testLeerkracht)
        );
    }

    private InMemoryToewijzingRepository maakLeegToewijzingRepository() {
        return new InMemoryToewijzingRepository(new ArrayList<>());
    }
}