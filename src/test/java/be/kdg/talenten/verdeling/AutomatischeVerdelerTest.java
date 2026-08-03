package be.kdg.talenten.verdeling;

import be.kdg.talenten.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AutomatischeVerdelerTest {

    private Leerkracht testLeerkracht;

    @BeforeEach
    void setUp() {
        testLeerkracht = new Leerkracht(
                "Test",
                "Leerkracht"
        );
    }

    private Klas maakObservatieKlas(
            String naam,
            int leerjaar
    ) {
        return new Klas(
                naam,
                "2026-2027",
                leerjaar,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
    }

    private Klas maakKwalificatieKlas(
            String naam,
            int leerjaar
    ) {
        return new Klas(
                naam,
                "2026-2027",
                leerjaar,
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB
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

    @Test
    void leerlingKrijgtEersteKeuzeAlsErPlaatsIs() {
        // Arrange
        Klas klas1AA = maakObservatieKlas("1AA", 1);
        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );

        TalentenPeriode periode = new TalentenPeriode(
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
        Talent koken = new Talent(
                "Koken",
                "Leren koken"
        );

        IngerichtTalent schakenHerfst = richtTalentIn(
                schaken,
                periode,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        IngerichtTalent voetbalHerfst = richtTalentIn(
                voetbal,
                periode,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        IngerichtTalent kokenHerfst = richtTalentIn(
                koken,
                periode,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(
                new Voorkeur(jan, periode, schakenHerfst, 1)
        );
        voorkeuren.add(
                new Voorkeur(jan, periode, voetbalHerfst, 2)
        );
        voorkeuren.add(
                new Voorkeur(jan, periode, kokenHerfst, 3)
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(voorkeuren);

        // Act
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // Assert
        assertEquals(1, resultaat.getAantalToewijzingen());
        assertTrue(
                resultaat.getNietToegewezenLeerlingen().isEmpty()
        );

        Toewijzing toewijzing =
                resultaat.getToewijzingen().getFirst();

        assertSame(jan, toewijzing.getLeerling());
        assertSame(
                schakenHerfst,
                toewijzing.getIngerichtTalent()
        );
        assertEquals(
                ToewijzingsType.AUTOMATISCH,
                toewijzing.getToewijzingsType()
        );
    }

    @Test
    void leerlingKrijgtTweedeKeuzeAlsEersteKeuzeVolZit() {
        // Arrange
        Klas klas1AA = maakObservatieKlas("1AA", 1);

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );
        Leerling sara = new Leerling(
                "Sara",
                "Janssens",
                klas1AA
        );

        TalentenPeriode periode = new TalentenPeriode(
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
        Talent koken = new Talent(
                "Koken",
                "Leren koken"
        );

        IngerichtTalent schakenHerfst = richtTalentIn(
                schaken,
                periode,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent voetbalHerfst = richtTalentIn(
                voetbal,
                periode,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent kokenHerfst = richtTalentIn(
                koken,
                periode,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voegZelfdeVoorkeurenToe(
                voorkeuren,
                jan,
                periode,
                schakenHerfst,
                voetbalHerfst,
                kokenHerfst
        );

        voegZelfdeVoorkeurenToe(
                voorkeuren,
                sara,
                periode,
                schakenHerfst,
                voetbalHerfst,
                kokenHerfst
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(voorkeuren);

        // Act
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // Assert
        Toewijzing toewijzingSara =
                zoekToewijzingVoorLeerling(
                        resultaat,
                        sara
                );

        assertSame(
                voetbalHerfst,
                toewijzingSara.getIngerichtTalent()
        );
    }

    @Test
    void leerlingWordtNietToegewezenAlsAlleVoorkeurenVolZitten() {
        // Arrange
        Klas klas1AA = maakObservatieKlas("1AA", 1);

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );
        Leerling sara = new Leerling(
                "Sara",
                "Janssens",
                klas1AA
        );
        Leerling tom = new Leerling(
                "Tom",
                "Mertens",
                klas1AA
        );
        Leerling emma = new Leerling(
                "Emma",
                "Vermeulen",
                klas1AA
        );

        TalentenPeriode periode = new TalentenPeriode(
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
        Talent koken = new Talent(
                "Koken",
                "Leren koken"
        );

        IngerichtTalent schakenHerfst = richtTalentIn(
                schaken,
                periode,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent voetbalHerfst = richtTalentIn(
                voetbal,
                periode,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent kokenHerfst = richtTalentIn(
                koken,
                periode,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voegZelfdeVoorkeurenToe(
                voorkeuren,
                jan,
                periode,
                schakenHerfst,
                voetbalHerfst,
                kokenHerfst
        );
        voegZelfdeVoorkeurenToe(
                voorkeuren,
                sara,
                periode,
                schakenHerfst,
                voetbalHerfst,
                kokenHerfst
        );
        voegZelfdeVoorkeurenToe(
                voorkeuren,
                tom,
                periode,
                schakenHerfst,
                voetbalHerfst,
                kokenHerfst
        );
        voegZelfdeVoorkeurenToe(
                voorkeuren,
                emma,
                periode,
                schakenHerfst,
                voetbalHerfst,
                kokenHerfst
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(voorkeuren);

        // Act
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // Assert
        assertEquals(3, resultaat.getAantalToewijzingen());
        assertEquals(
                1,
                resultaat
                        .getNietToegewezenLeerlingen()
                        .size()
        );
        assertTrue(
                resultaat
                        .getNietToegewezenLeerlingen()
                        .contains(emma)
        );
    }

    @Test
    void capaciteitVanIngerichtTalentWordtNietOverschreden() {
        // Arrange
        Klas klas1AA = maakObservatieKlas("1AA", 1);

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );
        Leerling sara = new Leerling(
                "Sara",
                "Janssens",
                klas1AA
        );
        Leerling tom = new Leerling(
                "Tom",
                "Mertens",
                klas1AA
        );

        TalentenPeriode periode = new TalentenPeriode(
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
        Talent koken = new Talent(
                "Koken",
                "Leren koken"
        );

        IngerichtTalent schakenHerfst = richtTalentIn(
                schaken,
                periode,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent voetbalHerfst = richtTalentIn(
                voetbal,
                periode,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent kokenHerfst = richtTalentIn(
                koken,
                periode,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voegZelfdeVoorkeurenToe(
                voorkeuren,
                jan,
                periode,
                schakenHerfst,
                voetbalHerfst,
                kokenHerfst
        );
        voegZelfdeVoorkeurenToe(
                voorkeuren,
                sara,
                periode,
                schakenHerfst,
                voetbalHerfst,
                kokenHerfst
        );
        voegZelfdeVoorkeurenToe(
                voorkeuren,
                tom,
                periode,
                schakenHerfst,
                voetbalHerfst,
                kokenHerfst
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(voorkeuren);

        // Act
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // Assert
        long aantalToegewezenAanSchaken =
                resultaat.getToewijzingen()
                        .stream()
                        .filter(toewijzing ->
                                toewijzing.getIngerichtTalent()
                                        == schakenHerfst
                        )
                        .count();

        assertEquals(1, aantalToegewezenAanSchaken);
    }

    @Test
    void leerlingKrijgtLieverTalentDatHijNogNietGevolgdHeeft() {
        // Arrange
        Klas klas1AA = maakObservatieKlas("1AA", 1);

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2025, 9, 21),
                LocalDate.of(2025, 11, 21)
        );
        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                LocalDate.of(2025, 11, 22),
                LocalDate.of(2026, 2, 21)
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );
        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );
        Talent koken = new Talent(
                "Koken",
                "Leren koken"
        );

        IngerichtTalent schakenHerfst = richtTalentIn(
                schaken,
                herfst,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent schakenWinter = richtTalentIn(
                schaken,
                winter,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent voetbalWinter = richtTalentIn(
                voetbal,
                winter,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent kokenWinter = richtTalentIn(
                koken,
                winter,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        List<Toewijzing> historischeToewijzingen =
                new ArrayList<>();

        historischeToewijzingen.add(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(
                new Voorkeur(jan, winter, schakenWinter, 1)
        );
        voorkeuren.add(
                new Voorkeur(jan, winter, voetbalWinter, 2)
        );
        voorkeuren.add(
                new Voorkeur(jan, winter, kokenWinter, 3)
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(
                        voorkeuren,
                        historischeToewijzingen
                );

        // Act
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // Assert
        Toewijzing toewijzingJan =
                zoekToewijzingVoorLeerling(
                        resultaat,
                        jan
                );

        assertSame(
                voetbalWinter,
                toewijzingJan.getIngerichtTalent()
        );
        assertEquals(
                2,
                toewijzingJan.getVoorkeurNummer()
        );
    }

    @Test
    void alsNieuweTalentenVolZittenMagLeerlingGevolgdTalentOpnieuwVolgen() {
        // Arrange
        Klas klas1AA = maakObservatieKlas("1AA", 1);

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );
        Leerling jos = new Leerling(
                "Jos",
                "Jacobs",
                klas1AA
        );
        Leerling tim = new Leerling(
                "Tim",
                "VH",
                klas1AA
        );

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2025, 9, 21),
                LocalDate.of(2025, 11, 21)
        );
        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                LocalDate.of(2025, 11, 22),
                LocalDate.of(2026, 2, 21)
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );
        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );
        Talent koken = new Talent(
                "Koken",
                "Leren koken"
        );

        IngerichtTalent schakenHerfst = richtTalentIn(
                schaken,
                herfst,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent schakenWinter = richtTalentIn(
                schaken,
                winter,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent voetbalWinter = richtTalentIn(
                voetbal,
                winter,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent kokenWinter = richtTalentIn(
                koken,
                winter,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        List<Toewijzing> historischeToewijzingen =
                new ArrayList<>();

        historischeToewijzingen.add(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voegZelfdeVoorkeurenToe(
                voorkeuren,
                jos,
                winter,
                voetbalWinter,
                schakenWinter,
                kokenWinter
        );

        voegZelfdeVoorkeurenToe(
                voorkeuren,
                tim,
                winter,
                kokenWinter,
                schakenWinter,
                voetbalWinter
        );

        voegZelfdeVoorkeurenToe(
                voorkeuren,
                jan,
                winter,
                schakenWinter,
                voetbalWinter,
                kokenWinter
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(
                        voorkeuren,
                        historischeToewijzingen
                );

        // Act
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // Assert
        Toewijzing toewijzingJan =
                zoekToewijzingVoorLeerling(
                        resultaat,
                        jan
                );

        assertSame(
                schakenWinter,
                toewijzingJan.getIngerichtTalent()
        );
    }

    @Test
    void tweeLeerlingenWillenDezelfdeKeuzeMetAndereHistorischeVoorkeurNummer() {
        // Arrange
        Klas klas1AA = maakObservatieKlas("1AA", 1);

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );
        Leerling jos = new Leerling(
                "Jos",
                "Jacobs",
                klas1AA
        );

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2025, 9, 21),
                LocalDate.of(2025, 11, 21)
        );
        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                LocalDate.of(2025, 11, 22),
                LocalDate.of(2026, 2, 21)
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );
        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );
        Talent koken = new Talent(
                "Koken",
                "Leren koken"
        );
        Talent muziek = new Talent(
                "Muziek",
                "Muziek leren spelen op een instrument"
        );

        IngerichtTalent schakenHerfst = richtTalentIn(
                schaken,
                herfst,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent kokenHerfst = richtTalentIn(
                koken,
                herfst,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        IngerichtTalent schakenWinter = richtTalentIn(
                schaken,
                winter,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent voetbalWinter = richtTalentIn(
                voetbal,
                winter,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent kokenWinter = richtTalentIn(
                koken,
                winter,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent muziekWinter = richtTalentIn(
                muziek,
                winter,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        List<Toewijzing> historischeToewijzingen =
                new ArrayList<>();

        historischeToewijzingen.add(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        1
                )
        );
        historischeToewijzingen.add(
                new Toewijzing(
                        jos,
                        kokenHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        2
                )
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voorkeuren.add(
                new Voorkeur(jan, winter, voetbalWinter, 1)
        );
        voorkeuren.add(
                new Voorkeur(jan, winter, muziekWinter, 2)
        );
        voorkeuren.add(
                new Voorkeur(jan, winter, kokenWinter, 3)
        );

        voorkeuren.add(
                new Voorkeur(jos, winter, voetbalWinter, 1)
        );
        voorkeuren.add(
                new Voorkeur(jos, winter, schakenWinter, 2)
        );
        voorkeuren.add(
                new Voorkeur(jos, winter, kokenWinter, 3)
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(
                        voorkeuren,
                        historischeToewijzingen
                );

        // Act
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // Assert
        Toewijzing toewijzingJan =
                zoekToewijzingVoorLeerling(
                        resultaat,
                        jan
                );
        Toewijzing toewijzingJos =
                zoekToewijzingVoorLeerling(
                        resultaat,
                        jos
                );

        assertSame(
                muziekWinter,
                toewijzingJan.getIngerichtTalent()
        );
        assertSame(
                voetbalWinter,
                toewijzingJos.getIngerichtTalent()
        );
    }

    @Test
    void drieLeerlingenUitDezelfdeKlasKiezenVoetbal() {
        // Arrange
        Klas klas1AA = maakObservatieKlas("1AA", 1);

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );
        Leerling jos = new Leerling(
                "Jos",
                "Jacobs",
                klas1AA
        );
        Leerling tim = new Leerling(
                "Tim",
                "Van Herreweghe",
                klas1AA
        );

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2025, 9, 21),
                LocalDate.of(2025, 11, 21)
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );
        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );
        Talent koken = new Talent(
                "Koken",
                "Leren koken"
        );

        IngerichtTalent schakenHerfst = richtTalentIn(
                schaken,
                herfst,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent kokenHerfst = richtTalentIn(
                koken,
                herfst,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
        IngerichtTalent voetbalHerfst = richtTalentIn(
                voetbal,
                herfst,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voegZelfdeVoorkeurenToe(
                voorkeuren,
                jan,
                herfst,
                schakenHerfst,
                kokenHerfst,
                voetbalHerfst
        );
        voegZelfdeVoorkeurenToe(
                voorkeuren,
                jos,
                herfst,
                schakenHerfst,
                kokenHerfst,
                voetbalHerfst
        );
        voegZelfdeVoorkeurenToe(
                voorkeuren,
                tim,
                herfst,
                schakenHerfst,
                kokenHerfst,
                voetbalHerfst
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(voorkeuren);

        // Act
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // Assert
        Toewijzing toewijzingJan =
                zoekToewijzingVoorLeerling(
                        resultaat,
                        jan
                );
        Toewijzing toewijzingJos =
                zoekToewijzingVoorLeerling(
                        resultaat,
                        jos
                );
        Toewijzing toewijzingTim =
                zoekToewijzingVoorLeerling(
                        resultaat,
                        tim
                );

        assertSame(
                schakenHerfst,
                toewijzingJan.getIngerichtTalent()
        );
        assertSame(
                schakenHerfst,
                toewijzingJos.getIngerichtTalent()
        );
        assertSame(
                kokenHerfst,
                toewijzingTim.getIngerichtTalent()
        );
    }

    @Test
    void automatischeVerdelingWijstLeerlingenToeAanHunEigenDoelgroep() {
        // Arrange
        Klas observatieKlas =
                maakObservatieKlas("2AA", 2);

        Klas kwalificatieKlas =
                maakKwalificatieKlas("5AA", 5);

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                observatieKlas
        );
        Leerling sara = new Leerling(
                "Sara",
                "Janssens",
                kwalificatieKlas
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

        IngerichtTalent schakenObservatie = richtTalentIn(
                schaken,
                herfst,
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        IngerichtTalent schakenKwalificatie = richtTalentIn(
                schaken,
                herfst,
                10,
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB
        );

        List<Voorkeur> voorkeuren = List.of(
                new Voorkeur(
                        jan,
                        herfst,
                        schakenObservatie,
                        1
                ),
                new Voorkeur(
                        sara,
                        herfst,
                        schakenKwalificatie,
                        1
                )
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(voorkeuren);

        // Act
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // Assert
        assertEquals(2, resultaat.getAantalToewijzingen());

        Toewijzing toewijzingJan =
                zoekToewijzingVoorLeerling(
                        resultaat,
                        jan
                );

        Toewijzing toewijzingSara =
                zoekToewijzingVoorLeerling(
                        resultaat,
                        sara
                );

        assertSame(
                schakenObservatie,
                toewijzingJan.getIngerichtTalent()
        );
        assertSame(
                schakenKwalificatie,
                toewijzingSara.getIngerichtTalent()
        );
    }

    private void voegZelfdeVoorkeurenToe(
            List<Voorkeur> voorkeuren,
            Leerling leerling,
            TalentenPeriode periode,
            IngerichtTalent keuze1,
            IngerichtTalent keuze2,
            IngerichtTalent keuze3
    ) {
        voorkeuren.add(
                new Voorkeur(leerling, periode, keuze1, 1)
        );
        voorkeuren.add(
                new Voorkeur(leerling, periode, keuze2, 2)
        );
        voorkeuren.add(
                new Voorkeur(leerling, periode, keuze3, 3)
        );
    }

    private Toewijzing zoekToewijzingVoorLeerling(
            VerdelingsResultaat resultaat,
            Leerling leerling
    ) {
        return resultaat.getToewijzingen()
                .stream()
                .filter(toewijzing ->
                        toewijzing.getLeerling() == leerling
                )
                .findFirst()
                .orElseThrow();
    }
}