package be.kdg.talenten;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.InMemoryToewijzingRepository;
import be.kdg.talenten.service.ManueleToewijzingService;
import be.kdg.talenten.verdeling.AutomatischeVerdeler;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManueleToewijzingServiceTest {

    /*
     * 1. Een bestaande toewijzing kan naar een ander talent
     *    gewijzigd worden.
     */
    @Test
    void manueleToewijzingNaarAnderTalentWijzigtTalentEnType() {
        // ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent schakenHerfst =
                new IngerichtTalent(schaken, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        IngerichtTalent voetbalHerfst =
                new IngerichtTalent(voetbal, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        InMemoryToewijzingRepository repository = new InMemoryToewijzingRepository(new ArrayList<>());

        repository.save(new Toewijzing(
                jan,
                schakenHerfst,
                ToewijzingsType.AUTOMATISCH
        ));

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(herfst, jan, voetbalHerfst);

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(gewijzigdeToewijzing);
        assertSame(
                voetbalHerfst,
                gewijzigdeToewijzing.getIngerichtTalent()
        );
        assertEquals(
                ToewijzingsType.MANUEEL,
                gewijzigdeToewijzing.getToewijzingsType()
        );
    }

    /*
     * 2. Wanneer dezelfde toewijzing manueel bevestigd wordt,
     *    blijft het talent hetzelfde maar wordt het type MANUEEL.
     */
    @Test
    void manueleToewijzingNaarZelfdeTalentVerandertTypeNaarManueel() {
        // ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");

        /*
         * Capaciteit 1 is hier bewust gekozen.
         * Jan bezet zelf die ene plaats en moet toch manueel
         * naar hetzelfde talent gewijzigd kunnen worden.
         */
        IngerichtTalent schakenHerfst =
                new IngerichtTalent(schaken, herfst, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        InMemoryToewijzingRepository repository = new InMemoryToewijzingRepository(new ArrayList<>());

        repository.save(new Toewijzing(
                jan,
                schakenHerfst,
                ToewijzingsType.AUTOMATISCH
        ));

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(herfst, jan, schakenHerfst);

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(gewijzigdeToewijzing);
        assertSame(
                schakenHerfst,
                gewijzigdeToewijzing.getIngerichtTalent()
        );
        assertEquals(
                ToewijzingsType.MANUEEL,
                gewijzigdeToewijzing.getToewijzingsType()
        );
    }

    /*
     * 3. Historische deelname aan een talent verhindert
     *    een nieuwe manuele toewijzing niet.
     */
    @Test
    void manueleToewijzingNegeertHistoriek() {
        // ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        TalentenPeriode kerst = new TalentenPeriode(
                "Kerst",
                LocalDate.of(2026, 12, 22),
                LocalDate.of(2027, 1, 21)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent voetbalHerfst =
                new IngerichtTalent(voetbal, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        IngerichtTalent schakenKerst =
                new IngerichtTalent(schaken, kerst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        IngerichtTalent voetbalKerst =
                new IngerichtTalent(voetbal, kerst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        Toewijzing historischeToewijzing = new Toewijzing(
                jan,
                voetbalHerfst,
                ToewijzingsType.AUTOMATISCH
        );

        InMemoryToewijzingRepository repository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>(List.of(historischeToewijzing))
                );

        repository.save(new Toewijzing(
                jan,
                schakenKerst,
                ToewijzingsType.AUTOMATISCH
        ));

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(kerst, jan, voetbalKerst);

        // ASSERT
        Toewijzing huidigeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, kerst);

        assertNotNull(huidigeToewijzing);
        assertSame(
                voetbalKerst,
                huidigeToewijzing.getIngerichtTalent()
        );
        assertEquals(
                ToewijzingsType.MANUEEL,
                huidigeToewijzing.getToewijzingsType()
        );

        List<Toewijzing> historiek =
                repository.zoekHistorischeToewijzingen();

        assertEquals(1, historiek.size());
        assertSame(
                voetbalHerfst,
                historiek.getFirst().getIngerichtTalent()
        );
    }

    /*
     * 4. Een leerling mag niet naar een vol talent
     *    verplaatst worden.
     */
    @Test
    void manueleToewijzingNaarVolTalentWordtGeweigerd() {
        // ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));
        Leerling tim = new Leerling("Tim", "Janssens", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent schakenHerfst =
                new IngerichtTalent(schaken, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        IngerichtTalent voetbalHerfst =
                new IngerichtTalent(voetbal, herfst, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        InMemoryToewijzingRepository repository = new InMemoryToewijzingRepository(new ArrayList<>());

        repository.save(new Toewijzing(
                jan,
                schakenHerfst,
                ToewijzingsType.AUTOMATISCH
        ));

        // Tim neemt de enige beschikbare plaats in.
        repository.save(new Toewijzing(
                tim,
                voetbalHerfst,
                ToewijzingsType.AUTOMATISCH
        ));

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

        // ACT + ASSERT
        assertThrows(
                IllegalStateException.class,
                () -> service.wijzigToewijzing(
                        herfst,
                        jan,
                        voetbalHerfst
                )
        );

        /*
         * De oorspronkelijke toewijzing moet behouden blijven
         * wanneer de wijziging mislukt.
         */
        Toewijzing toewijzingJan =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(toewijzingJan);
        assertSame(
                schakenHerfst,
                toewijzingJan.getIngerichtTalent()
        );
        assertEquals(
                ToewijzingsType.AUTOMATISCH,
                toewijzingJan.getToewijzingsType()
        );
    }

    /*
     * 5. Het nieuwe ingericht talent moet tot dezelfde
     *    talentenperiode behoren.
     */
    @Test
    void manueleToewijzingNaarTalentUitAnderePeriodeWordtGeweigerd() {
        // ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        TalentenPeriode kerst = new TalentenPeriode(
                "Kerst",
                LocalDate.of(2026, 12, 22),
                LocalDate.of(2027, 1, 21)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent schakenHerfst =
                new IngerichtTalent(schaken, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        IngerichtTalent voetbalKerst =
                new IngerichtTalent(voetbal, kerst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        InMemoryToewijzingRepository repository = new InMemoryToewijzingRepository(new ArrayList<>());

        repository.save(new Toewijzing(
                jan,
                schakenHerfst,
                ToewijzingsType.AUTOMATISCH
        ));

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

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
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(oorspronkelijkeToewijzing);
        assertSame(
                schakenHerfst,
                oorspronkelijkeToewijzing.getIngerichtTalent()
        );
        assertEquals(
                ToewijzingsType.AUTOMATISCH,
                oorspronkelijkeToewijzing.getToewijzingsType()
        );
    }

    /*
     * 6. Wanneer er nog geen actuele toewijzing bestaat,
     *    wordt een nieuwe manuele toewijzing gemaakt.
     */
    @Test
    void manueleToewijzingZonderBestaandeToewijzingMaaktNieuweToewijzing() {
        // ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );

        IngerichtTalent voetbalHerfst =
                new IngerichtTalent(voetbal, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        InMemoryToewijzingRepository repository = new InMemoryToewijzingRepository(new ArrayList<>());

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

        assertNull(
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst)
        );

        // ACT
        service.wijzigToewijzing(herfst, jan, voetbalHerfst);

        // ASSERT
        Toewijzing nieuweToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst);

        assertNotNull(nieuweToewijzing);
        assertSame(
                voetbalHerfst,
                nieuweToewijzing.getIngerichtTalent()
        );
        assertEquals(
                ToewijzingsType.MANUEEL,
                nieuweToewijzing.getToewijzingsType()
        );
    }

    /*
     * 7. Geen van de argumenten mag null zijn.
     */
    @Test
    void manueleToewijzingMetNullArgumentWordtGeweigerd() {
        // ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );

        IngerichtTalent voetbalHerfst =
                new IngerichtTalent(voetbal, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        InMemoryToewijzingRepository repository = new InMemoryToewijzingRepository(new ArrayList<>());

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

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
        Klas klas1AA = new Klas("1AA", "2026",1);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling tim = new Leerling("Tim", "Janssens", klas1AA);
        Leerling sara = new Leerling("Sara", "Mertens", klas1AA);

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );

        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );

        IngerichtTalent schakenHerfst =
                new IngerichtTalent(schaken, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        /*
         * De capaciteit is ruim genoeg voor drie leerlingen.
         * Alleen de klaslimiet van twee leerlingen wordt overschreden.
         */
        IngerichtTalent voetbalHerfst =
                new IngerichtTalent(voetbal, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        InMemoryToewijzingRepository repository =
                new InMemoryToewijzingRepository(new ArrayList<>());

        // Tim en Sara uit 1AA zitten al bij voetbal.
        repository.save(new Toewijzing(
                tim,
                voetbalHerfst,
                ToewijzingsType.AUTOMATISCH
        ));

        repository.save(new Toewijzing(
                sara,
                voetbalHerfst,
                ToewijzingsType.AUTOMATISCH
        ));

        // Jan uit dezelfde klas zit momenteel bij schaken.
        repository.save(new Toewijzing(
                jan,
                schakenHerfst,
                ToewijzingsType.AUTOMATISCH
        ));

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(
                herfst,
                jan,
                voetbalHerfst
        );

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(
                        jan,
                        herfst
                );

        assertNotNull(gewijzigdeToewijzing);

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
                repository.telToewijzingenVoorIngerichtTalent(voetbalHerfst)
        );
    }

    @Test
    void manueleToewijzingHeeftGeenVoorkeurNummer() {
        // ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );

        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );

        IngerichtTalent schakenHerfst =
                new IngerichtTalent(schaken, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        IngerichtTalent voetbalHerfst =
                new IngerichtTalent(voetbal, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voorkeuren.add(new Voorkeur(
                jan,
                herfst,
                schakenHerfst,
                1
        ));

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(voorkeuren);

        VerdelingsResultaat resultaat = verdeler.verdeel();

        Toewijzing automatischeToewijzing =
                resultaat.getToewijzingen().getFirst();

        /*
         * Controle van de beginsituatie:
         * de automatische toewijzing is gebaseerd op voorkeur 1.
         */
        assertEquals(
                1,
                automatischeToewijzing.getVoorkeurNummer()
        );

        InMemoryToewijzingRepository repository =
                new InMemoryToewijzingRepository(new ArrayList<>());

        repository.save(automatischeToewijzing);

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(
                herfst,
                jan,
                voetbalHerfst
        );

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(
                        jan,
                        herfst
                );

        assertNotNull(gewijzigdeToewijzing);

        assertSame(
                voetbalHerfst,
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
    public void manueleToewijzingNaarVerkeerdeDoelgroepWordtGeweigerd() {
        // ARRANGE
        Klas klas2AA = new Klas("2AA", "2026-2027", 2);

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas2AA
        );

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );

        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );

        IngerichtTalent schakenOnderbouw = new IngerichtTalent(
                schaken,
                herfst,
                10,
                Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR
        );

        IngerichtTalent voetbalBovenbouw = new IngerichtTalent(
                voetbal,
                herfst,
                10,
                Doelgroep.VANAF_VIERDE_JAAR
        );

        InMemoryToewijzingRepository repository =
                new InMemoryToewijzingRepository(new ArrayList<>());

        repository.save(
                new Toewijzing(
                        jan,
                        schakenOnderbouw,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.wijzigToewijzing(
                        herfst,
                        jan,
                        voetbalBovenbouw
                )
        );

        Toewijzing oorspronkelijkeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(
                        jan,
                        herfst
                );

        assertSame(
                schakenOnderbouw,
                oorspronkelijkeToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.AUTOMATISCH,
                oorspronkelijkeToewijzing.getToewijzingsType()
        );
    }

}