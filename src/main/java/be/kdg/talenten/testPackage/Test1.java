package be.kdg.talenten.testPackage;

import be.kdg.talenten.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Test1 {
    public static void main(String[] args) {
        Klas klas1AA = new Klas("1AA", "2026-2027",1);
        Klas klas1AB = new Klas("1AB", "2026-2027",1);
        Klas klas1AC = new Klas( "1AC", "2026-2027",1);
        Klas klas1AD = new Klas( "1AD", "2026-2027",1);

        Leerling tim = new Leerling("Tim", "Van Herreweghe", klas1AA);
        Leerling eveline = new Leerling("Eveline", "Van Oevelen", klas1AA);
        Leerling nele = new Leerling("Nele", "Van Herreweghe", klas1AA);
        Leerling george = new Leerling("george", "Smets", klas1AA);

        tim.wijsKlasToe(klas1AA);


        Talent schaken = new Talent("Schaken", "Schaken is een denksport waarbij je met verschillende strategieen je tegenstander leert verslaan.");
        Talent voetbal = new Talent("Voetbal", "Voetbal is een fantastische teamsport met een bal.");

        TalentenPeriode herfst = new TalentenPeriode("Herfst", LocalDate.of(2026,9,1), LocalDate.of(2026,10,31));

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, herfst, 20, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        System.out.println(schakenHerfst);

        Toewijzing toewijzingTim = new Toewijzing(tim, schakenHerfst, ToewijzingsType.MANUEEL);
        System.out.println(toewijzingTim);
    }
}
