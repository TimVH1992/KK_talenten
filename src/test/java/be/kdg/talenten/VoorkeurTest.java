package be.kdg.talenten;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.testutil.TestDataFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

public class VoorkeurTest {
    @Test
    public void voorkeurVoorVerkeerdeDoelgroepWordtGeweigerd() {
        // ARRANGE
        LocalDate startDatum =
                LocalDate.of(2026, 9, 1);

        LocalDate eindDatum =
                LocalDate.of(2026, 10, 31);

        Schooljaar schooljaar =
                TestDataFactory.schooljaarVoorPeriode(
                        startDatum,
                        eindDatum
                );

        Klas klas2AA =
                new Klas(
                        "2AA",
                        schooljaar,
                        2,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                );

        Leerling jan =
                new Leerling(
                        "Jan",
                        "Peeters",
                        klas2AA
                );

        TalentenPeriode herfst =
                new TalentenPeriode(
                        "Herfst",
                        startDatum,
                        eindDatum,
                        schooljaar
                );

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Leerkracht leerkracht =
                new Leerkracht(
                        "Tim",
                        "Van Herreweghe"
                );

        IngerichtTalent schakenBovenbouw =
                new IngerichtTalent(
                        schaken,
                        herfst,
                        schaken.getNaam(),
                        schaken.getBeschrijving(),
                        10,
                        Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB,
                        List.of(leerkracht)
                );

        // ACT
        IllegalArgumentException exception =
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

        // ASSERT
        Assertions.assertEquals(
                "De leerling behoort niet tot de doelgroep van het ingericht talent.",
                exception.getMessage()
        );
    }
}