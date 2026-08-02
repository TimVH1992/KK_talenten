package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class InMemoryLeerlingRepositoryTest {
    @Test
    public void zoekVoorKlasGeeftAlleenLeerlingenVanGevraagdeKlas() {
        // ARRANGE
        Klas klas1AA = new Klas("1AA", "2026-2027",1);
        Klas klas1AB = new Klas("1AB", "2026-2027",1);

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
