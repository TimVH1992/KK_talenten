package be.kdg.talenten;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.verdeling.AutomatischeVerdeler;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AutomatischeVerdelerTest {

    @Test
    void leerlingKrijgtEersteKeuzeAlsErPlaatsIs() {
        // Arrange
        Klas klas = new Klas("1AA", "2026");

        Leerling jan = new Leerling("Jan", "Peeters");

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, periode, 1);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, periode, 1);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, periode, 1);

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
        Klas klas = new Klas("1AA", "2026");

        Leerling jan = new Leerling("Jan", "Peeters");
        Leerling sara = new Leerling("Sara", "Janssens");

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, periode, 1);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, periode, 1);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, periode, 1);

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
        Klas klas = new Klas("1AA", "2026");

        Leerling jan = new Leerling("Jan", "Peeters");
        Leerling sara = new Leerling("Sara", "Janssens");
        Leerling tom = new Leerling("Tom", "Mertens");
        Leerling emma = new Leerling("Emma", "Vermeulen");

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, periode, 1);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, periode, 1);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, periode, 1);

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
        Klas klas = new Klas("1AA", "2026");

        Leerling jan = new Leerling("Jan", "Peeters");
        Leerling sara = new Leerling("Sara", "Janssens");
        Leerling tom = new Leerling("Tom", "Mertens");

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, periode, 1);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, periode, 10);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, periode, 10);

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
        Klas klas = new Klas("1AA", "2026");

        Leerling jan = new Leerling("Jan", "Peeters");

        TalentenPeriode herfst = new TalentenPeriode("Herfst", LocalDate.of(2025, 9,21), LocalDate.of(2025,11,21));
        TalentenPeriode winter = new TalentenPeriode("Winter", LocalDate.of(2025, 11,22), LocalDate.of(2026,02,21));

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10);
        IngerichtTalent schakenWinter = new IngerichtTalent(schaken, winter, 10);
        IngerichtTalent voetbalWinter = new IngerichtTalent(voetbal, winter, 10);
        IngerichtTalent kokenWinter = new IngerichtTalent(koken, winter, 10);

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
    }

    @Test
    void alsNieuweTalentenVolZittenMagLeerlingGevolgdTalentOpnieuwVolgen(){
        Klas klas = new Klas("1AA", "2026");

        Leerling jan = new Leerling("Jan", "Peeters");
        Leerling jos = new Leerling("Jos", "Jacobs");
        Leerling tim = new Leerling("Tim", "VH");

        TalentenPeriode herfst = new TalentenPeriode("Herfst", LocalDate.of(2025, 9,21), LocalDate.of(2025,11,21));
        TalentenPeriode winter = new TalentenPeriode("Winter", LocalDate.of(2025, 11,22), LocalDate.of(2026,2,21));

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10);
        IngerichtTalent schakenWinter = new IngerichtTalent(schaken, winter, 10);
        IngerichtTalent voetbalWinter = new IngerichtTalent(voetbal, winter, 1);
        IngerichtTalent kokenWinter = new IngerichtTalent(koken, winter, 1);

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
}

