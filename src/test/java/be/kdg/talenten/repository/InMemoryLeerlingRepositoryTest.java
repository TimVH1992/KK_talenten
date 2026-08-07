package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

public class InMemoryLeerlingRepositoryTest {

    @Test
    public void zoekVoorKlasGeeftAlleenLeerlingenVanGevraagdeKlas() {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar(
                "2026-2027",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2027, 6, 30)
        );

        Klas klas1AA = new Klas(
                "1AA",
                schooljaar,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        Klas klas1AB = new Klas(
                "1AB",
                schooljaar,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );

        Leerling julie = new Leerling(
                "Julie",
                "Martens",
                klas1AA
        );

        Leerling eveline = new Leerling(
                "Eveline",
                "Van Oevelen",
                klas1AB
        );

        InMemoryLeerlingRepository repository =
                new InMemoryLeerlingRepository(
                        List.of(jan, julie, eveline)
                );

        // ACT
        List<Leerling> resultaat =
                repository.zoekVoorKlas(klas1AA);

        // ASSERT
        Assertions.assertNotEquals(klas1AA, klas1AB);
        Assertions.assertEquals(klas1AA, jan.getKlas());

        Assertions.assertEquals(
                List.of(jan, julie),
                resultaat
        );
    }

    @Test
    public void zoekVoorKlasMetNullKlasWordtGeweigerd() {
        // ARRANGE
        InMemoryLeerlingRepository repository =
                new InMemoryLeerlingRepository(List.of());

        // ACT + ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.zoekVoorKlas(null)
        );
    }
}