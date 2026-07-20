package be.kdg.talenten.testPackage;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.verdeling.AutomatischeVerdeler;
import be.kdg.talenten.verdeling.VerdelingsResultaat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TestVerdelingen {
    public static void main(String[] args) {
        Klas klas1AA = new Klas("1AA", "2026");

        Leerling leerling1 = new Leerling("Jan", "Peeters");
        Leerling leerling2 = new Leerling("Sara", "Janssens");
        Leerling leerling3 = new Leerling("Tom", "Mertens");
        Leerling leerling4 = new Leerling("Joris", "VH");

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, periode, 10);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, periode, 1);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, periode, 1);

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voorkeuren.add(new Voorkeur(leerling1, periode, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(leerling1, periode, voetbalHerfst, 2));
        voorkeuren.add(new Voorkeur(leerling1, periode, kokenHerfst, 3));

        voorkeuren.add(new Voorkeur(leerling2, periode, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(leerling2, periode, voetbalHerfst, 2));
        voorkeuren.add(new Voorkeur(leerling2, periode, kokenHerfst, 3));

        voorkeuren.add(new Voorkeur(leerling3, periode, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(leerling3, periode, voetbalHerfst, 2));
        voorkeuren.add(new Voorkeur(leerling3, periode, kokenHerfst, 3));

        voorkeuren.add(new Voorkeur(leerling4, periode, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(leerling4, periode, voetbalHerfst, 2));
        voorkeuren.add(new Voorkeur(leerling4, periode, kokenHerfst, 3));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);
        VerdelingsResultaat resultaat = verdeler.verdeel();

        System.out.println("Aantal toewijzingen: " + resultaat.getAantalToewijzingen());
        System.out.println();

        System.out.println("Toewijzingen:");
        for (Toewijzing toewijzing : resultaat.getToewijzingen()) {
            System.out.println(toewijzing);
            System.out.println();
        }

        System.out.println();

        System.out.println("Niet toegewezen leerlingen:");
        for (Leerling leerling : resultaat.getNietToegewezenLeerlingen()) {
            System.out.println(leerling);
        }
    }
}
