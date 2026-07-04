package be.kdg.talenten.testPackage;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;

public class Test1 {
    public static void main(String[] args) {
        Klas klas1AA = new Klas(1, "1AA", "2026-2027");
        Klas klas1AB = new Klas(2, "1AB", "2026-2027");
        Klas klas1AC = new Klas(3, "1AC", "2026-2027");
        Klas klas1AD = new Klas(4, "1AD", "2026-2027");

        Leerling tim = new Leerling("Tim", "Van Herreweghe");
        Leerling eveline = new Leerling("Eveline", "Van Oevelen");
        Leerling nele = new Leerling("Nele", "Van Herreweghe");
        Leerling george = new Leerling("george", "Smets");

        tim.wijsKlasToe(klas1AA);

        System.out.println(tim.getAchternaam());
        System.out.println(tim.getKlas());
    }
}
