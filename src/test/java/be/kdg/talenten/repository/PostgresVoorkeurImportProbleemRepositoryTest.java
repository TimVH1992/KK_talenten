package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.postgres.PostgresKlasRepository;
import be.kdg.talenten.repository.postgres.PostgresLeerlingRepository;
import be.kdg.talenten.repository.postgres.PostgresSchooljaarRepository;
import be.kdg.talenten.repository.postgres.PostgresTalentenPeriodeRepository;
import be.kdg.talenten.repository.postgres.PostgresVoorkeurImportProbleemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgresVoorkeurImportProbleemRepositoryTest {
    private PostgresVoorkeurImportProbleemRepository repository;

    private Leerling jan;
    private Leerling sofie;

    private TalentenPeriode herfst;
    private TalentenPeriode winter;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    TRUNCATE TABLE
                        voorkeur_import_problemen,
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

        PostgresSchooljaarRepository schooljaarRepository = new PostgresSchooljaarRepository();
        PostgresKlasRepository klasRepository = new PostgresKlasRepository();
        PostgresLeerlingRepository leerlingRepository = new PostgresLeerlingRepository();
        PostgresTalentenPeriodeRepository periodeRepository = new PostgresTalentenPeriodeRepository();

        Schooljaar schooljaar = schooljaarRepository.save(
                new Schooljaar(
                        "2026-2027",
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2027, 6, 30),
                        true
                )
        );

        Klas klas = klasRepository.save(
                new Klas(
                        "1AA",
                        schooljaar,
                        1,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        jan = leerlingRepository.save(new Leerling("Jan", "Peeters", klas));
        sofie = leerlingRepository.save(new Leerling("Sofie", "Janssens", klas));

        herfst = periodeRepository.save(
                new TalentenPeriode(
                        "Herfst",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 31),
                        schooljaar
                )
        );

        winter = periodeRepository.save(
                new TalentenPeriode(
                        "Winter",
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 12, 20),
                        schooljaar
                )
        );

        repository = new PostgresVoorkeurImportProbleemRepository();
    }

    @Test
    void saveSlaatVoorkeurImportProbleemOp() throws SQLException {
        // ARRANGE
        VoorkeurImportProbleem probleem = new VoorkeurImportProbleem(
                jan,
                herfst,
                "Keuze 3 is niet ingevuld"
        );

        // ACT
        repository.save(probleem);

        // ASSERT
        String sql = """
                SELECT leerling_id, talenten_periode_id, reden
                FROM voorkeur_import_problemen
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            assertTrue(resultSet.next());

            assertEquals(jan.getId().longValue(), resultSet.getLong("leerling_id"));
            assertEquals(herfst.getId().longValue(), resultSet.getLong("talenten_periode_id"));
            assertEquals("Keuze 3 is niet ingevuld", resultSet.getString("reden"));

            assertFalse(resultSet.next());
        }
    }

    @Test
    void zoekVoorLeerlingEnPeriodeGeeftAlleenProblemenVanGevraagdeLeerlingEnPeriode() {
        // ARRANGE
        repository.save(
                new VoorkeurImportProbleem(
                        jan,
                        herfst,
                        "Keuze 3 is niet ingevuld"
                )
        );

        repository.save(
                new VoorkeurImportProbleem(
                        jan,
                        herfst,
                        "Keuze 2 is dubbel"
                )
        );

        repository.save(
                new VoorkeurImportProbleem(
                        sofie,
                        herfst,
                        "Keuze 1 is niet ingevuld"
                )
        );

        repository.save(
                new VoorkeurImportProbleem(
                        jan,
                        winter,
                        "Keuze 1 bevat een onbekend ingericht talent"
                )
        );

        // ACT
        List<VoorkeurImportProbleem> resultaat = repository.zoekVoorLeerlingEnPeriode(jan, herfst);

        // ASSERT
        assertEquals(2, resultaat.size());

        assertTrue(resultaat.stream()
                .allMatch(probleem -> probleem.getLeerling().getId().equals(jan.getId())));

        assertTrue(resultaat.stream()
                .allMatch(probleem -> probleem.getPeriode().getId().equals(herfst.getId())));

        assertTrue(resultaat.stream()
                .anyMatch(probleem -> probleem.getReden().equals("Keuze 3 is niet ingevuld")));

        assertTrue(resultaat.stream()
                .anyMatch(probleem -> probleem.getReden().equals("Keuze 2 is dubbel")));
    }

    @Test
    void zoekVoorPeriodeGeeftAlleProblemenVanGevraagdePeriode() {
        // ARRANGE
        repository.save(
                new VoorkeurImportProbleem(
                        jan,
                        herfst,
                        "Keuze 3 is niet ingevuld"
                )
        );

        repository.save(
                new VoorkeurImportProbleem(
                        sofie,
                        herfst,
                        "Keuze 2 is dubbel"
                )
        );

        repository.save(
                new VoorkeurImportProbleem(
                        jan,
                        winter,
                        "Keuze 1 is niet ingevuld"
                )
        );

        // ACT
        List<VoorkeurImportProbleem> resultaat = repository.zoekVoorPeriode(herfst);

        // ASSERT
        assertEquals(2, resultaat.size());

        assertTrue(resultaat.stream()
                .allMatch(probleem -> probleem.getPeriode().getId().equals(herfst.getId())));

        assertTrue(resultaat.stream()
                .anyMatch(probleem -> probleem.getLeerling().getId().equals(jan.getId())
                        && probleem.getReden().equals("Keuze 3 is niet ingevuld")));

        assertTrue(resultaat.stream()
                .anyMatch(probleem -> probleem.getLeerling().getId().equals(sofie.getId())
                        && probleem.getReden().equals("Keuze 2 is dubbel")));
    }

    @Test
    void zoekVoorLeerlingEnPeriodeGeeftLegeLijstWanneerGeenProblemenBestaan() {
        // ACT
        List<VoorkeurImportProbleem> resultaat = repository.zoekVoorLeerlingEnPeriode(jan, herfst);

        // ASSERT
        assertNotNull(resultaat);
        assertTrue(resultaat.isEmpty());
    }

    @Test
    void zoekVoorPeriodeGeeftLegeLijstWanneerGeenProblemenBestaan() {
        // ACT
        List<VoorkeurImportProbleem> resultaat = repository.zoekVoorPeriode(herfst);

        // ASSERT
        assertNotNull(resultaat);
        assertTrue(resultaat.isEmpty());
    }

    @Test
    void verwijderVoorLeerlingEnPeriodeVerwijdertAlleenProblemenVanGevraagdeLeerlingEnPeriode() {
        // ARRANGE
        repository.save(new VoorkeurImportProbleem(
                jan,
                herfst,
                "Keuze 3 is niet ingevuld"
        ));

        repository.save(new VoorkeurImportProbleem(
                sofie,
                herfst,
                "Keuze 2 is dubbel"
        ));

        repository.save(new VoorkeurImportProbleem(
                jan,
                winter,
                "Keuze 1 is niet ingevuld"
        ));

        // ACT
        repository.verwijderVoorLeerlingEnPeriode(jan, herfst);

        // ASSERT
        List<VoorkeurImportProbleem> janHerfst =
                repository.zoekVoorLeerlingEnPeriode(jan, herfst);

        List<VoorkeurImportProbleem> sofieHerfst =
                repository.zoekVoorLeerlingEnPeriode(sofie, herfst);

        List<VoorkeurImportProbleem> janWinter =
                repository.zoekVoorLeerlingEnPeriode(jan, winter);

        assertTrue(janHerfst.isEmpty());

        assertEquals(1, sofieHerfst.size());
        assertEquals("Keuze 2 is dubbel", sofieHerfst.getFirst().getReden());

        assertEquals(1, janWinter.size());
        assertEquals("Keuze 1 is niet ingevuld", janWinter.getFirst().getReden());
    }
}