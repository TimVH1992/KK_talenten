package be.kdg.talenten.verdeling;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.testutil.TestDataFactory;
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
        testLeerkracht = new Leerkracht("Test", "Leerkracht");
    }

    @Test
    void leerlingKrijgtEersteKeuzeAlsErPlaatsIs() {
        // ARRANGE
        LocalDate startDatum = LocalDate.of(2026, 9, 1);
        LocalDate eindDatum = LocalDate.of(2026, 10, 31);

        Schooljaar schooljaar = TestDataFactory.schooljaarVoorPeriode(startDatum, eindDatum);
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                startDatum,
                eindDatum,
                schooljaar
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

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
        voorkeuren.add(new Voorkeur(jan, periode, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(jan, periode, voetbalHerfst, 2));
        voorkeuren.add(new Voorkeur(jan, periode, kokenHerfst, 3));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);

        // ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // ASSERT
        assertEquals(1, resultaat.getAantalToewijzingen());
        assertTrue(resultaat.getNietToegewezenLeerlingen().isEmpty());

        Toewijzing toewijzing = resultaat.getToewijzingen().getFirst();

        assertSame(jan, toewijzing.getLeerling());
        assertSame(schakenHerfst, toewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.AUTOMATISCH, toewijzing.getToewijzingsType());
    }

    @Test
    void leerlingKrijgtTweedeKeuzeAlsEersteKeuzeVolZit() {
        // ARRANGE
        LocalDate startDatum = LocalDate.of(2026, 9, 1);
        LocalDate eindDatum = LocalDate.of(2026, 10, 31);

        Schooljaar schooljaar = TestDataFactory.schooljaarVoorPeriode(startDatum, eindDatum);
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling sara = new Leerling("Sara", "Janssens", klas1AA);

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                startDatum,
                eindDatum,
                schooljaar
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

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

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);

        // ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // ASSERT
        Toewijzing toewijzingSara = zoekToewijzingVoorLeerling(resultaat, sara);

        assertSame(voetbalHerfst, toewijzingSara.getIngerichtTalent());
    }

    @Test
    void leerlingWordtNietToegewezenAlsAlleVoorkeurenVolZitten() {
        // ARRANGE
        LocalDate startDatum = LocalDate.of(2026, 9, 1);
        LocalDate eindDatum = LocalDate.of(2026, 10, 31);

        Schooljaar schooljaar = TestDataFactory.schooljaarVoorPeriode(startDatum, eindDatum);
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling sara = new Leerling("Sara", "Janssens", klas1AA);
        Leerling tom = new Leerling("Tom", "Mertens", klas1AA);
        Leerling emma = new Leerling("Emma", "Vermeulen", klas1AA);

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                startDatum,
                eindDatum,
                schooljaar
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

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

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);

        // ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // ASSERT
        assertEquals(3, resultaat.getAantalToewijzingen());
        assertEquals(1, resultaat.getNietToegewezenLeerlingen().size());
        assertTrue(resultaat.getNietToegewezenLeerlingen().contains(emma));
    }

    @Test
    void capaciteitVanIngerichtTalentWordtNietOverschreden() {
        // ARRANGE
        LocalDate startDatum = LocalDate.of(2026, 9, 1);
        LocalDate eindDatum = LocalDate.of(2026, 10, 31);

        Schooljaar schooljaar = TestDataFactory.schooljaarVoorPeriode(startDatum, eindDatum);
        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling sara = new Leerling("Sara", "Janssens", klas1AA);
        Leerling tom = new Leerling("Tom", "Mertens", klas1AA);

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                startDatum,
                eindDatum,
                schooljaar
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

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

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);

        // ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // ASSERT
        long aantalToegewezenAanSchaken =
                resultaat.getToewijzingen()
                        .stream()
                        .filter(toewijzing ->
                                toewijzing.getIngerichtTalent() == schakenHerfst
                        )
                        .count();

        assertEquals(1, aantalToegewezenAanSchaken);
    }

    @Test
    void leerlingKrijgtLieverTalentDatHijNogNietGevolgdHeeft() {
        // ARRANGE
        LocalDate startHerfst = LocalDate.of(2025, 9, 21);
        LocalDate eindeHerfst = LocalDate.of(2025, 11, 21);
        LocalDate startWinter = LocalDate.of(2025, 11, 22);
        LocalDate eindeWinter = LocalDate.of(2026, 2, 21);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(startHerfst, eindeWinter);

        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                startHerfst,
                eindeHerfst,
                schooljaar
        );

        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                startWinter,
                eindeWinter,
                schooljaar
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

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

        List<Toewijzing> historischeToewijzingen = new ArrayList<>();

        historischeToewijzingen.add(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voorkeuren.add(new Voorkeur(jan, winter, schakenWinter, 1));
        voorkeuren.add(new Voorkeur(jan, winter, voetbalWinter, 2));
        voorkeuren.add(new Voorkeur(jan, winter, kokenWinter, 3));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(
                voorkeuren,
                historischeToewijzingen
        );

        // ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // ASSERT
        Toewijzing toewijzingJan =
                zoekToewijzingVoorLeerling(resultaat, jan);

        assertSame(voetbalWinter, toewijzingJan.getIngerichtTalent());
        assertEquals(2, toewijzingJan.getVoorkeurNummer());
    }

    @Test
    void alsNieuweTalentenVolZittenMagLeerlingGevolgdTalentOpnieuwVolgen() {
        // ARRANGE
        LocalDate startHerfst = LocalDate.of(2025, 9, 21);
        LocalDate eindeHerfst = LocalDate.of(2025, 11, 21);
        LocalDate startWinter = LocalDate.of(2025, 11, 22);
        LocalDate eindeWinter = LocalDate.of(2026, 2, 21);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(startHerfst, eindeWinter);

        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling jos = new Leerling("Jos", "Jacobs", klas1AA);
        Leerling tim = new Leerling("Tim", "VH", klas1AA);

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                startHerfst,
                eindeHerfst,
                schooljaar
        );

        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                startWinter,
                eindeWinter,
                schooljaar
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

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

        List<Toewijzing> historischeToewijzingen = new ArrayList<>();

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

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(
                voorkeuren,
                historischeToewijzingen
        );

        // ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // ASSERT
        Toewijzing toewijzingJan =
                zoekToewijzingVoorLeerling(resultaat, jan);

        assertSame(schakenWinter, toewijzingJan.getIngerichtTalent());
    }

    @Test
    void tweeLeerlingenWillenDezelfdeKeuzeMetAndereHistorischeVoorkeurNummer() {
        // ARRANGE
        LocalDate startHerfst = LocalDate.of(2025, 9, 21);
        LocalDate eindeHerfst = LocalDate.of(2025, 11, 21);
        LocalDate startWinter = LocalDate.of(2025, 11, 22);
        LocalDate eindeWinter = LocalDate.of(2026, 2, 21);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(startHerfst, eindeWinter);

        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling jos = new Leerling("Jos", "Jacobs", klas1AA);

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                startHerfst,
                eindeHerfst,
                schooljaar
        );

        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                startWinter,
                eindeWinter,
                schooljaar
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");
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

        List<Toewijzing> historischeToewijzingen = new ArrayList<>();

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

        voorkeuren.add(new Voorkeur(jan, winter, voetbalWinter, 1));
        voorkeuren.add(new Voorkeur(jan, winter, muziekWinter, 2));
        voorkeuren.add(new Voorkeur(jan, winter, kokenWinter, 3));

        voorkeuren.add(new Voorkeur(jos, winter, voetbalWinter, 1));
        voorkeuren.add(new Voorkeur(jos, winter, schakenWinter, 2));
        voorkeuren.add(new Voorkeur(jos, winter, kokenWinter, 3));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(
                voorkeuren,
                historischeToewijzingen
        );

        // ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // ASSERT
        Toewijzing toewijzingJan =
                zoekToewijzingVoorLeerling(resultaat, jan);

        Toewijzing toewijzingJos =
                zoekToewijzingVoorLeerling(resultaat, jos);

        assertSame(muziekWinter, toewijzingJan.getIngerichtTalent());
        assertSame(voetbalWinter, toewijzingJos.getIngerichtTalent());
    }

    @Test
    void drieLeerlingenUitDezelfdeKlasKiezenVoetbal() {
        // ARRANGE
        LocalDate startDatum = LocalDate.of(2025, 9, 21);
        LocalDate eindDatum = LocalDate.of(2025, 11, 21);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(startDatum, eindDatum);

        Klas klas1AA = maakObservatieKlas("1AA", 1, schooljaar);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling jos = new Leerling("Jos", "Jacobs", klas1AA);
        Leerling tim = new Leerling("Tim", "Van Herreweghe", klas1AA);

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                startDatum,
                eindDatum,
                schooljaar
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

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

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);

        // ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // ASSERT
        Toewijzing toewijzingJan =
                zoekToewijzingVoorLeerling(resultaat, jan);

        Toewijzing toewijzingJos =
                zoekToewijzingVoorLeerling(resultaat, jos);

        Toewijzing toewijzingTim =
                zoekToewijzingVoorLeerling(resultaat, tim);

        assertSame(schakenHerfst, toewijzingJan.getIngerichtTalent());
        assertSame(schakenHerfst, toewijzingJos.getIngerichtTalent());
        assertSame(kokenHerfst, toewijzingTim.getIngerichtTalent());
    }

    @Test
    void automatischeVerdelingWijstLeerlingenToeAanHunEigenDoelgroep() {
        // ARRANGE
        LocalDate startDatum = LocalDate.of(2026, 9, 1);
        LocalDate eindDatum = LocalDate.of(2026, 10, 31);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(startDatum, eindDatum);

        Klas observatieKlas = maakObservatieKlas(
                "2AA",
                2,
                schooljaar
        );

        Klas kwalificatieKlas = maakKwalificatieKlas(
                "5AA",
                5,
                schooljaar
        );

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
                startDatum,
                eindDatum,
                schooljaar
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );

        Talent koken = new Talent("Koken", "Leren koken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent kokenObservatie = richtTalentIn(koken, herfst, 10, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB);
        IngerichtTalent voetbalObservatie = richtTalentIn(voetbal, herfst, 10, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB);

        IngerichtTalent kokenKwalificatie = richtTalentIn(koken, herfst, 10, Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB);
        IngerichtTalent voetbalKwalificatie = richtTalentIn(voetbal, herfst, 10, Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB);

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
                new Voorkeur(jan, herfst, schakenObservatie, 1),
                new Voorkeur(jan, herfst, kokenObservatie, 2),
                new Voorkeur(jan, herfst, voetbalObservatie, 3),

                new Voorkeur(sara, herfst, schakenKwalificatie, 1),
                new Voorkeur(sara, herfst, kokenKwalificatie, 2),
                new Voorkeur(sara, herfst, voetbalKwalificatie, 3)
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(voorkeuren);

        // ACT
        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        // ASSERT
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

    private Klas maakObservatieKlas(String naam, int leerjaar, Schooljaar schooljaar) {
        return new Klas(
                naam,
                schooljaar,
                leerjaar,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
    }

    private Klas maakKwalificatieKlas(String naam, int leerjaar, Schooljaar schooljaar) {
        return new Klas(
                naam,
                schooljaar,
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
                talent.getNaam(),
                talent.getBeschrijving(),
                maximumCapaciteit,
                doelgroep,
                List.of(testLeerkracht)
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
    @Test
    void leerlingMetMinderDanDrieVoorkeurenWordtNietAutomatischToegewezen() {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas = new Klas("1AA", schooljaar, 1, doelgroep);
        Leerling sofie = new Leerling("Sofie", "Peeters", klas);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        Voorkeur voorkeur1 = new Voorkeur(sofie, periode, voetbalObservatie, 1);
        Voorkeur voorkeur2 = new Voorkeur(sofie, periode, schakenObservatie, 2);

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(List.of(voorkeur1, voorkeur2));

        // ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // ASSERT
        assertEquals(0, resultaat.getToewijzingen().size());
        assertEquals(1, resultaat.getNietToegewezenLeerlingen().size());
        assertEquals(sofie, resultaat.getNietToegewezenLeerlingen().get(0));
    }
}