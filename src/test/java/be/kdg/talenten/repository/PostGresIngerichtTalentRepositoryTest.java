package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.postgres.PostgresIngerichtTalentRepository;
import be.kdg.talenten.repository.postgres.PostgresLeerkrachtRepository;
import be.kdg.talenten.repository.postgres.PostgresTalentRepository;
import be.kdg.talenten.repository.postgres.PostgresTalentenPeriodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgresIngerichtTalentRepositoryTest {

    private PostgresTalentRepository talentRepository;
    private PostgresTalentenPeriodeRepository periodeRepository;
    private PostgresLeerkrachtRepository leerkrachtRepository;
    private PostgresIngerichtTalentRepository ingerichtTalentRepository;

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
                        klassen
                    RESTART IDENTITY CASCADE
                    """);
        }

        talentRepository = new PostgresTalentRepository();
        periodeRepository = new PostgresTalentenPeriodeRepository();
        leerkrachtRepository = new PostgresLeerkrachtRepository();
        ingerichtTalentRepository = new PostgresIngerichtTalentRepository();
    }

    @Test
    void saveSlaatIngerichtTalentEnLeerkrachtenOp() throws SQLException {
        // ARRANGE
        Talent schaken = talentRepository.save(new Talent("Schaken", "Leren schaken"));

        TalentenPeriode herfst = periodeRepository.save(new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31)));

        Leerkracht tim = leerkrachtRepository.save(new Leerkracht("Tim", "Van Herreweghe"));
        Leerkracht sara = leerkrachtRepository.save(new Leerkracht("Sara", "Janssens"));

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(tim, sara));

        // ACT
        IngerichtTalent opgeslagenIngerichtTalent = ingerichtTalentRepository.save(schakenHerfst);

        // ASSERT
        assertNotNull(opgeslagenIngerichtTalent);
        assertNotNull(opgeslagenIngerichtTalent.getId());
        assertTrue(opgeslagenIngerichtTalent.getId() > 0);
        assertSame(schaken, opgeslagenIngerichtTalent.getTalent());
        assertSame(herfst, opgeslagenIngerichtTalent.getTalentenPeriode());
        assertEquals(10, opgeslagenIngerichtTalent.getMaxCapaciteit());
        assertEquals(Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, opgeslagenIngerichtTalent.getDoelgroep());
        assertEquals(List.of(tim, sara), opgeslagenIngerichtTalent.getLeerkrachten());

        controleerIngerichtTalentInDatabank(opgeslagenIngerichtTalent);

        List<Long> gekoppeldeLeerkrachtIds = haalGekoppeldeLeerkrachtIdsOp(opgeslagenIngerichtTalent.getId());

        assertEquals(2, gekoppeldeLeerkrachtIds.size());
        assertEquals(List.of(tim.getId(), sara.getId()), gekoppeldeLeerkrachtIds);
    }

    @Test
    void zoekVoorPeriodeGeeftAlleenIngerichteTalentenVanGevraagdePeriode() {
        // ARRANGE
        Talent schaken = talentRepository.save(new Talent("Schaken", "Leren schaken"));
        Talent dansen = talentRepository.save(new Talent("Dansen", "Leren dansen"));

        TalentenPeriode herfst = periodeRepository.save(new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31)));
        TalentenPeriode winter = periodeRepository.save(new TalentenPeriode("Winter", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 20)));

        Leerkracht tim = leerkrachtRepository.save(new Leerkracht("Tim", "Van Herreweghe"));
        Leerkracht sara = leerkrachtRepository.save(new Leerkracht("Sara", "Janssens"));

        IngerichtTalent schakenHerfst = ingerichtTalentRepository.save(new IngerichtTalent(schaken, herfst, 10, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(tim)));
        IngerichtTalent dansenHerfst = ingerichtTalentRepository.save(new IngerichtTalent(dansen, herfst, 6, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(sara)));
        ingerichtTalentRepository.save(new IngerichtTalent(schaken, winter, 8, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(tim)));

        // ACT
        List<IngerichtTalent> resultaat = ingerichtTalentRepository.zoekVoorPeriode(herfst);

        // ASSERT
        assertEquals(2, resultaat.size());

        IngerichtTalent opgehaaldDansen = resultaat.stream().filter(ingerichtTalent -> ingerichtTalent.getTalent().getNaam().equals("Dansen")).findFirst().orElseThrow();
        IngerichtTalent opgehaaldSchaken = resultaat.stream().filter(ingerichtTalent -> ingerichtTalent.getTalent().getNaam().equals("Schaken")).findFirst().orElseThrow();

        assertEquals(dansenHerfst.getId(), opgehaaldDansen.getId());
        assertEquals("Dansen", opgehaaldDansen.getTalent().getNaam());
        assertEquals(6, opgehaaldDansen.getMaxCapaciteit());
        assertEquals(Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, opgehaaldDansen.getDoelgroep());
        assertEquals(1, opgehaaldDansen.getLeerkrachten().size());
        assertEquals(sara.getId(), opgehaaldDansen.getLeerkrachten().getFirst().getId());

        assertEquals(schakenHerfst.getId(), opgehaaldSchaken.getId());
        assertEquals("Schaken", opgehaaldSchaken.getTalent().getNaam());
        assertEquals(10, opgehaaldSchaken.getMaxCapaciteit());
        assertEquals(Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, opgehaaldSchaken.getDoelgroep());
        assertEquals(1, opgehaaldSchaken.getLeerkrachten().size());
        assertEquals(tim.getId(), opgehaaldSchaken.getLeerkrachten().getFirst().getId());

        assertTrue(resultaat.stream().allMatch(ingerichtTalent -> ingerichtTalent.getTalentenPeriode().getId().equals(herfst.getId())));
        assertFalse(resultaat.stream().anyMatch(ingerichtTalent -> ingerichtTalent.getTalentenPeriode().getId().equals(winter.getId())));
    }

    private void controleerIngerichtTalentInDatabank(IngerichtTalent ingerichtTalent) throws SQLException {
        String sql = """
                SELECT maximum_capaciteit, doelgroep, talent_id, talenten_periode_id
                FROM ingerichte_talenten
                WHERE ingericht_talent_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ingerichtTalent.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "Het ingerichte talent werd niet teruggevonden in de databank");
                assertEquals(10, resultSet.getInt("maximum_capaciteit"));
                assertEquals("OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB", resultSet.getString("doelgroep"));
                assertEquals(ingerichtTalent.getTalent().getId().longValue(), resultSet.getLong("talent_id"));
                assertEquals(ingerichtTalent.getTalentenPeriode().getId().longValue(), resultSet.getLong("talenten_periode_id"));
                assertFalse(resultSet.next(), "Er werden meerdere ingerichte talenten met hetzelfde ID gevonden");
            }
        }
    }

    private List<Long> haalGekoppeldeLeerkrachtIdsOp(long ingerichtTalentId) throws SQLException {
        String sql = """
                SELECT leerkracht_id
                FROM ingericht_talent_leerkrachten
                WHERE ingericht_talent_id = ?
                ORDER BY leerkracht_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ingerichtTalentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Long> leerkrachtIds = new ArrayList<>();

                while (resultSet.next()) {
                    leerkrachtIds.add(resultSet.getLong("leerkracht_id"));
                }

                return leerkrachtIds;
            }
        }
    }
}