package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.postgres.PostgresSchooljaarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgresSchooljaarRepositoryTest {
    private SchooljaarRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    TRUNCATE TABLE
                        ingericht_talent_leerkrachten,
                        voorkeuren,
                        toewijzingen,
                        leerlingen,
                        ingerichte_talenten,
                        leerkrachten,
                        talenten,
                        talenten_periodes,
                        schooljaren,
                        klassen
                    RESTART IDENTITY CASCADE
                    """);
        }
        repository = new PostgresSchooljaarRepository();
    }

    @Test
    void maakActiefOnthoudtExactEenSchooljaar() {
        Schooljaar vorig = repository.save(new Schooljaar("2025-2026", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 6, 30), true));
        Schooljaar huidig = repository.save(new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30)));

        repository.maakActief(huidig);

        Schooljaar actief = repository.zoekActiefSchooljaar().orElseThrow();
        assertEquals(huidig.getId(), actief.getId());
        assertEquals("2026-2027", actief.getNaam());
        assertTrue(actief.isActief());
        assertFalse(repository.zoekAlle().stream().filter(schooljaar -> schooljaar.getId().equals(vorig.getId())).findFirst().orElseThrow().isActief());
    }

    @Test
    void afgelopenInactiefSchooljaarVerschijntNietInNormaleSelectie() {
        repository.save(new Schooljaar("2020-2021", LocalDate.of(2020, 9, 1), LocalDate.of(2021, 6, 30)));
        Schooljaar huidig = repository.save(new Schooljaar("2099-2100", LocalDate.of(2099, 9, 1), LocalDate.of(2100, 6, 30), true));

        List<Schooljaar> resultaat = repository.zoekSelecteerbareSchooljaren();

        assertEquals(1, resultaat.size());
        assertEquals(huidig.getId(), resultaat.getFirst().getId());
    }
}
