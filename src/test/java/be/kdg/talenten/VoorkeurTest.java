package be.kdg.talenten;

import be.kdg.talenten.domain.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

public class VoorkeurTest {
    @Test
    public void voorkeurVoorVerkeerdeDoelgroepWordtGeweigerd() {
        // ARRANGE
        Klas klas2AA = new Klas("2AA", "2026-2027", 2, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB);

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

        Leerkracht leerkracht =
                new Leerkracht("Tim", "Van Herreweghe");

        IngerichtTalent schakenBovenbouw = new IngerichtTalent(
                schaken,
                herfst,
                10,
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB,
                List.of(leerkracht)
        );
        // ACT + ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Voorkeur(
                        null,
                        jan,
                        herfst,
                        schakenBovenbouw,
                        1
                )
        );
    }
}
