package be.kdg.talenten.verdeling;

import be.kdg.talenten.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AutomatischeVerdelerTest {

    @Test
    void leerlingKrijgtEersteKeuzeAlsErPlaatsIs() {
        // Arrange
        Klas klas = new Klas("1AA", "2026",1);

        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, periode, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, periode, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, periode, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(new Voorkeur(jan, periode, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(jan, periode, voetbalHerfst, 2));
        voorkeuren.add(new Voorkeur(jan, periode, kokenHerfst, 3));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);

        // Act
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // Assert
        assertEquals(1, resultaat.getAantalToewijzingen());
        assertTrue(resultaat.getNietToegewezenLeerlingen().isEmpty());

        Toewijzing toewijzing = resultaat.getToewijzingen().get(0);

        assertSame(jan, toewijzing.getLeerling());
        assertSame(schakenHerfst, toewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.AUTOMATISCH, toewijzing.getToewijzingsType());
    }

    @Test
    void leerlingKrijgtTweedeKeuzeAlsEersteKeuzeVolZit(){
        Klas klas = new Klas("1AA", "2026",1);

        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));
        Leerling sara = new Leerling("Sara", "Janssens", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, periode, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, periode, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, periode, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voorkeuren.add(new Voorkeur(jan, periode, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(jan, periode, voetbalHerfst, 2));
        voorkeuren.add(new Voorkeur(jan, periode, kokenHerfst, 3));

        voorkeuren.add(new Voorkeur(sara, periode, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(sara, periode, voetbalHerfst, 2));
        voorkeuren.add(new Voorkeur(sara, periode, kokenHerfst, 3));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);

        VerdelingsResultaat resultaat = verdeler.verdeel();

        Toewijzing toewijzingSara = zoekToewijzingVoorLeerling(resultaat, sara);

        assertSame(voetbalHerfst, toewijzingSara.getIngerichtTalent());
    }

    private Toewijzing zoekToewijzingVoorLeerling(VerdelingsResultaat resultaat, Leerling leerling) {
        return resultaat.getToewijzingen()
                .stream()
                .filter(toewijzing -> toewijzing.getLeerling() == leerling)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void leerlingWordtNietToegewezenAlsAlleVoorkeurenVolZitten() {
        Klas klas = new Klas("1AA", "2026",1);

        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));
        Leerling sara = new Leerling("Sara", "Janssens", new Klas("1AA", "2026-2027", 1));
        Leerling tom = new Leerling("Tom", "Mertens", new Klas("1AA", "2026-2027", 1));
        Leerling emma = new Leerling("Emma", "Vermeulen", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, periode, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, periode, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, periode, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voegZelfdeVoorkeurenToe(voorkeuren, jan, periode, schakenHerfst, voetbalHerfst, kokenHerfst);
        voegZelfdeVoorkeurenToe(voorkeuren, sara, periode, schakenHerfst, voetbalHerfst, kokenHerfst);
        voegZelfdeVoorkeurenToe(voorkeuren, tom, periode, schakenHerfst, voetbalHerfst, kokenHerfst);
        voegZelfdeVoorkeurenToe(voorkeuren, emma, periode, schakenHerfst, voetbalHerfst, kokenHerfst);

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);

        VerdelingsResultaat resultaat = verdeler.verdeel();

        assertEquals(3, resultaat.getAantalToewijzingen());
        assertEquals(1, resultaat.getNietToegewezenLeerlingen().size());
        assertTrue(resultaat.getNietToegewezenLeerlingen().contains(emma));
    }

    private void voegZelfdeVoorkeurenToe(
            List<Voorkeur> voorkeuren,
            Leerling leerling,
            TalentenPeriode periode,
            IngerichtTalent keuze1,
            IngerichtTalent keuze2,
            IngerichtTalent keuze3
    ) {
        voorkeuren.add(new Voorkeur(leerling, periode, keuze1, 1));
        voorkeuren.add(new Voorkeur(leerling, periode, keuze2, 2));
        voorkeuren.add(new Voorkeur(leerling, periode, keuze3, 3));
    }

    @Test
    void capaciteitVanIngerichtTalentWordtNietOverschreden() {
        Klas klas = new Klas("1AA", "2026",1);

        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));
        Leerling sara = new Leerling("Sara", "Janssens", new Klas("1AA", "2026-2027", 1));
        Leerling tom = new Leerling("Tom", "Mertens", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, periode, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, periode, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, periode, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voegZelfdeVoorkeurenToe(voorkeuren, jan, periode, schakenHerfst, voetbalHerfst, kokenHerfst);
        voegZelfdeVoorkeurenToe(voorkeuren, sara, periode, schakenHerfst, voetbalHerfst, kokenHerfst);
        voegZelfdeVoorkeurenToe(voorkeuren, tom, periode, schakenHerfst, voetbalHerfst, kokenHerfst);

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);

        VerdelingsResultaat resultaat = verdeler.verdeel();

        long aantalToegewezenAanSchaken = resultaat.getToewijzingen()
                .stream()
                .filter(toewijzing -> toewijzing.getIngerichtTalent() == schakenHerfst)
                .count();

        assertEquals(1, aantalToegewezenAanSchaken);
    }

    @Test
    void leerlingKrijgtLieverTalentDatHijNogNietGevolgdHeeft() {
        // Arrange
        Klas klas = new Klas("1AA", "2026",1);

        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode("Herfst", LocalDate.of(2025, 9,21), LocalDate.of(2025,11,21));
        TalentenPeriode winter = new TalentenPeriode("Winter", LocalDate.of(2025, 11,22), LocalDate.of(2026,02,21));

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10,Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent schakenWinter = new IngerichtTalent(schaken, winter, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalWinter = new IngerichtTalent(voetbal, winter, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenWinter = new IngerichtTalent(koken, winter, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        List<Toewijzing> historischeToewijzingen = new ArrayList<>();
        historischeToewijzingen.add(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH));

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(new Voorkeur(jan, winter, schakenWinter, 1));
        voorkeuren.add(new Voorkeur(jan, winter, voetbalWinter, 2));
        voorkeuren.add(new Voorkeur(jan, winter, kokenWinter, 3));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren, historischeToewijzingen);

        // Act
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // Assert
        Toewijzing toewijzingJan = zoekToewijzingVoorLeerling(resultaat, jan);

        assertSame(voetbalWinter, toewijzingJan.getIngerichtTalent());
        assertEquals(2, toewijzingJan.getVoorkeurNummer());
    }

    @Test
    void alsNieuweTalentenVolZittenMagLeerlingGevolgdTalentOpnieuwVolgen(){
        Klas klas = new Klas("1AA", "2026",1);

        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));
        Leerling jos = new Leerling("Jos", "Jacobs", new Klas("1AA", "2026-2027", 1));
        Leerling tim = new Leerling("Tim", "VH", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode("Herfst", LocalDate.of(2025, 9,21), LocalDate.of(2025,11,21));
        TalentenPeriode winter = new TalentenPeriode("Winter", LocalDate.of(2025, 11,22), LocalDate.of(2026,2,21));

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent schakenWinter = new IngerichtTalent(schaken, winter, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalWinter = new IngerichtTalent(voetbal, winter, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenWinter = new IngerichtTalent(koken, winter, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        List<Toewijzing> historischeToewijzingen = new ArrayList<>();
        historischeToewijzingen.add(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH));

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(new Voorkeur(jos, winter, voetbalWinter, 1));
        voorkeuren.add(new Voorkeur(jos, winter, schakenWinter, 2));
        voorkeuren.add(new Voorkeur(jos, winter, kokenWinter, 3));

        // Tim vult Koken op
        voorkeuren.add(new Voorkeur(tim, winter, kokenWinter, 1));
        voorkeuren.add(new Voorkeur(tim, winter, schakenWinter, 2));
        voorkeuren.add(new Voorkeur(tim, winter, voetbalWinter, 3));

        // Jan heeft Schaken al gevolgd, maar Voetbal en Koken zullen vol zitten
        voorkeuren.add(new Voorkeur(jan, winter, schakenWinter, 1));
        voorkeuren.add(new Voorkeur(jan, winter, voetbalWinter, 2));
        voorkeuren.add(new Voorkeur(jan, winter, kokenWinter, 3));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren, historischeToewijzingen);

        // Act
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // Assert
        Toewijzing toewijzingJan = zoekToewijzingVoorLeerling(resultaat, jan);

        assertSame(schakenWinter, toewijzingJan.getIngerichtTalent());
    }

    @Test
    public void tweeLeerlingenWillenDezelfdeKeuzeMetAndereHistorischeVoorkeurNummer(){
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));
        Leerling jos = new Leerling("Jos", "Jacobs", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode("Herfst", LocalDate.of(2025, 9,21), LocalDate.of(2025,11,21));
        TalentenPeriode winter = new TalentenPeriode("Winter", LocalDate.of(2025, 11,22), LocalDate.of(2026,2,21));

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");
        Talent muziek = new Talent("Muziek", "Muziek leren spelen op een instrument");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, herfst, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent muziekHerfst = new IngerichtTalent(muziek, herfst, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        IngerichtTalent schakenWinter = new IngerichtTalent(schaken, winter, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalWinter = new IngerichtTalent(voetbal, winter, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenWinter = new IngerichtTalent(koken, winter, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent muziekWinter = new IngerichtTalent(muziek, winter, 1, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);


        List<Toewijzing> historischeToewijzingen = new ArrayList<>();
        historischeToewijzingen.add(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH, 1));
        historischeToewijzingen.add(new Toewijzing(jos, kokenHerfst, ToewijzingsType.AUTOMATISCH, 2));

//        Jan heeft prio 1 gehad en Jos prio 2 dus wanneer ze beiden hetzelfde willen en er slechts plaats is voor 1 iemand zou jos nu zijn eerste keuze moeten krijgen
        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(new Voorkeur(jan, winter, voetbalWinter, 1));
        voorkeuren.add(new Voorkeur(jan, winter, muziekWinter, 2));
        voorkeuren.add(new Voorkeur(jan, winter, kokenWinter, 3));

        voorkeuren.add(new Voorkeur(jos, winter, voetbalWinter, 1));
        voorkeuren.add(new Voorkeur(jos, winter, schakenWinter, 2));
        voorkeuren.add(new Voorkeur(jos, winter, kokenWinter, 3));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren, historischeToewijzingen);

        // Act
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // Assert
        Toewijzing toewijzingJan = zoekToewijzingVoorLeerling(resultaat, jan);
        Toewijzing toewijzingJos = zoekToewijzingVoorLeerling(resultaat, jos);

        assertSame(muziekWinter, toewijzingJan.getIngerichtTalent());
        assertSame(voetbalWinter, toewijzingJos.getIngerichtTalent());
    }

    @Test
    public void drieLeerlingenUitDezelfdeKlasKiezenVoetbal(){
        Klas klas1AA = new Klas("1AA", "2026-2027",1);

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling jos = new Leerling("Jos", "Jacobs", klas1AA);
        Leerling tim = new Leerling("Tim", "Van Herreweghe", klas1AA);


        TalentenPeriode herfst = new TalentenPeriode("Herfst", LocalDate.of(2025, 9,21), LocalDate.of(2025,11,21));

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(new Voorkeur(jan, herfst, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(jan, herfst, kokenHerfst, 2));
        voorkeuren.add(new Voorkeur(jan, herfst, voetbalHerfst, 3));

        voorkeuren.add(new Voorkeur(jos, herfst, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(jos, herfst, kokenHerfst, 2));
        voorkeuren.add(new Voorkeur(jos, herfst, voetbalHerfst, 3));

        voorkeuren.add(new Voorkeur(tim, herfst, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(tim, herfst, kokenHerfst, 2));
        voorkeuren.add(new Voorkeur(tim, herfst, voetbalHerfst, 3));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);

//        ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

//        ASSERT
        Toewijzing toewijzingJan = zoekToewijzingVoorLeerling(resultaat, jan);
        Toewijzing toewijzingJos = zoekToewijzingVoorLeerling(resultaat, jos);
        Toewijzing toewijzingTim = zoekToewijzingVoorLeerling(resultaat, tim);

        assertSame(schakenHerfst, toewijzingJan.getIngerichtTalent());
        assertSame(schakenHerfst, toewijzingJos.getIngerichtTalent());
        assertSame(kokenHerfst, toewijzingTim.getIngerichtTalent());
    }
    @Test
    public void automatischeVerdelingWijstLeerlingenToeAanHunEigenDoelgroep() {
        // ARRANGE
        Klas klas2AA = new Klas("2AA", "2026-2027", 2);
        Klas klas5AA = new Klas("5AA", "2026-2027", 5);

        Leerling jan = new Leerling("Jan", "Peeters", klas2AA);
        Leerling sara = new Leerling("Sara", "Janssens", klas5AA);

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );

        IngerichtTalent schakenOnderbouw = new IngerichtTalent(
                schaken,
                herfst,
                10,
                Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR
        );

        IngerichtTalent schakenBovenbouw = new IngerichtTalent(
                schaken,
                herfst,
                10,
                Doelgroep.VANAF_VIERDE_JAAR
        );

        List<Voorkeur> voorkeuren = List.of(
                new Voorkeur(
                        jan,
                        herfst,
                        schakenOnderbouw,
                        1
                ),
                new Voorkeur(
                        sara,
                        herfst,
                        schakenBovenbouw,
                        1
                )
        );

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(voorkeuren);

        // ACT
        VerdelingsResultaat resultaat = verdeler.verdeel();

        // ASSERT
        assertEquals(2, resultaat.getAantalToewijzingen());

        Toewijzing toewijzingJan = resultaat.getToewijzingen()
                .stream()
                .filter(toewijzing -> toewijzing.getLeerling() == jan)
                .findFirst()
                .orElseThrow();

        Toewijzing toewijzingSara = resultaat.getToewijzingen()
                .stream()
                .filter(toewijzing -> toewijzing.getLeerling() == sara)
                .findFirst()
                .orElseThrow();

        assertSame(
                schakenOnderbouw,
                toewijzingJan.getIngerichtTalent()
        );

        assertSame(
                schakenBovenbouw,
                toewijzingSara.getIngerichtTalent()
        );
    }
}

