package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.postgres.PostgresIngerichtTalentRepository;
import be.kdg.talenten.repository.postgres.PostgresLeerkrachtRepository;
import be.kdg.talenten.repository.postgres.PostgresSchooljaarRepository;
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

    private Schooljaar schooljaar2025_2026;
    private Schooljaar schooljaar2026_2027;

    private PostgresTalentRepository talentRepository;
    private PostgresTalentenPeriodeRepository periodeRepository;
    private PostgresLeerkrachtRepository leerkrachtRepository;
    private PostgresIngerichtTalentRepository ingerichtTalentRepository;

    private Talent opgeslagenTalent;
    private TalentenPeriode opgeslagenPeriode;
    private Leerkracht opgeslagenLeerkracht;
    private Leerkracht tweedeOpgeslagenLeerkracht;

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

        schooljaar2025_2026 = schooljaarRepository.save(
                new Schooljaar(
                        "2025-2026",
                        LocalDate.of(2025, 7, 1),
                        LocalDate.of(2026, 6, 30)
                )
        );

        schooljaar2026_2027 = schooljaarRepository.save(
                new Schooljaar(
                        "2026-2027",
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2027, 6, 30),
                        true
                )
        );

        talentRepository = new PostgresTalentRepository();
        periodeRepository = new PostgresTalentenPeriodeRepository();
        leerkrachtRepository = new PostgresLeerkrachtRepository();
        ingerichtTalentRepository = new PostgresIngerichtTalentRepository();

        opgeslagenTalent = talentRepository.save(
                new Talent("Schaken", "Leren schaken")
        );

        opgeslagenPeriode = periodeRepository.save(
                new TalentenPeriode(
                        "Herfst",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 31),
                        schooljaar2026_2027
                )
        );

        opgeslagenLeerkracht = leerkrachtRepository.save(
                new Leerkracht("Tim", "Van Herreweghe")
        );

        tweedeOpgeslagenLeerkracht = leerkrachtRepository.save(
                new Leerkracht("Sara", "Janssens")
        );
    }

    @Test
    void saveSlaatIngerichtTalentEnLeerkrachtenOp() throws SQLException {
        // ARRANGE
        IngerichtTalent schakenHerfst = new IngerichtTalent(
                opgeslagenTalent,
                opgeslagenPeriode,
                "Schaken - Herfst",
                "Schaken voor de herfstperiode",
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                List.of(opgeslagenLeerkracht, tweedeOpgeslagenLeerkracht)
        );

        // ACT
        IngerichtTalent opgeslagenIngerichtTalent = ingerichtTalentRepository.save(schakenHerfst);

        // ASSERT
        assertNotNull(opgeslagenIngerichtTalent);
        assertNotNull(opgeslagenIngerichtTalent.getId());
        assertTrue(opgeslagenIngerichtTalent.getId() > 0);

        assertSame(opgeslagenTalent, opgeslagenIngerichtTalent.getTalent());
        assertSame(opgeslagenPeriode, opgeslagenIngerichtTalent.getTalentenPeriode());

        assertEquals("Schaken - Herfst", opgeslagenIngerichtTalent.getNaam());
        assertEquals("Schaken voor de herfstperiode", opgeslagenIngerichtTalent.getOmschrijving());
        assertEquals(10, opgeslagenIngerichtTalent.getMaxCapaciteit());
        assertEquals(Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, opgeslagenIngerichtTalent.getDoelgroep());
        assertTrue(opgeslagenIngerichtTalent.isActief());

        assertEquals(
                List.of(opgeslagenLeerkracht, tweedeOpgeslagenLeerkracht),
                opgeslagenIngerichtTalent.getLeerkrachten()
        );

        controleerIngerichtTalentInDatabank(opgeslagenIngerichtTalent);

        List<Long> gekoppeldeLeerkrachtIds = haalGekoppeldeLeerkrachtIdsOp(opgeslagenIngerichtTalent.getId());

        assertEquals(2, gekoppeldeLeerkrachtIds.size());
        assertEquals(
                List.of(opgeslagenLeerkracht.getId(), tweedeOpgeslagenLeerkracht.getId()),
                gekoppeldeLeerkrachtIds
        );
    }

    @Test
    void saveEnZoekOpIdWerktZonderLeerkracht() {
        // ARRANGE
        IngerichtTalent ingerichtTalent = new IngerichtTalent(
                opgeslagenTalent,
                opgeslagenPeriode,
                "Schaken zonder leerkracht",
                "Nog geen leerkracht toegewezen",
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                List.of()
        );

        // ACT
        IngerichtTalent opgeslagen = ingerichtTalentRepository.save(ingerichtTalent);
        IngerichtTalent resultaat = ingerichtTalentRepository.zoekOpId(opgeslagen.getId());

        // ASSERT
        assertNotNull(resultaat);
        assertEquals(opgeslagen.getId(), resultaat.getId());
        assertTrue(resultaat.getLeerkrachten().isEmpty());
    }

    @Test
    void zoekVoorPeriodeGeeftAlleenIngerichteTalentenVanGevraagdePeriode() {
        // ARRANGE
        Talent dansen = talentRepository.save(
                new Talent("Dansen", "Leren dansen")
        );

        TalentenPeriode winter = periodeRepository.save(
                new TalentenPeriode(
                        "Winter",
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 12, 20),
                        schooljaar2026_2027
                )
        );

        IngerichtTalent schakenHerfst = ingerichtTalentRepository.save(
                new IngerichtTalent(
                        opgeslagenTalent,
                        opgeslagenPeriode,
                        "Schaken - Herfst",
                        "Schaken voor de herfstperiode",
                        10,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of(opgeslagenLeerkracht)
                )
        );

        IngerichtTalent dansenHerfst = ingerichtTalentRepository.save(
                new IngerichtTalent(
                        dansen,
                        opgeslagenPeriode,
                        "Dansen - Herfst",
                        "Dansen voor de herfstperiode",
                        6,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of(tweedeOpgeslagenLeerkracht)
                )
        );

        ingerichtTalentRepository.save(
                new IngerichtTalent(
                        opgeslagenTalent,
                        winter,
                        "Schaken - Winter",
                        "Schaken voor de winterperiode",
                        8,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of(opgeslagenLeerkracht)
                )
        );

        // ACT
        List<IngerichtTalent> resultaat = ingerichtTalentRepository.zoekVoorPeriode(opgeslagenPeriode);

        // ASSERT
        assertEquals(2, resultaat.size());

        IngerichtTalent opgehaaldDansen = resultaat.stream()
                .filter(ingerichtTalent -> ingerichtTalent.getTalent().getNaam().equals("Dansen"))
                .findFirst()
                .orElseThrow();

        IngerichtTalent opgehaaldSchaken = resultaat.stream()
                .filter(ingerichtTalent -> ingerichtTalent.getTalent().getNaam().equals("Schaken"))
                .findFirst()
                .orElseThrow();

        assertEquals(dansenHerfst.getId(), opgehaaldDansen.getId());
        assertEquals("Dansen - Herfst", opgehaaldDansen.getNaam());
        assertEquals("Dansen voor de herfstperiode", opgehaaldDansen.getOmschrijving());
        assertTrue(opgehaaldDansen.isActief());
        assertEquals("Dansen", opgehaaldDansen.getTalent().getNaam());
        assertEquals(6, opgehaaldDansen.getMaxCapaciteit());
        assertEquals(1, opgehaaldDansen.getLeerkrachten().size());
        assertEquals(tweedeOpgeslagenLeerkracht.getId(), opgehaaldDansen.getLeerkrachten().getFirst().getId());

        assertEquals(schakenHerfst.getId(), opgehaaldSchaken.getId());
        assertEquals("Schaken - Herfst", opgehaaldSchaken.getNaam());
        assertEquals("Schaken voor de herfstperiode", opgehaaldSchaken.getOmschrijving());
        assertTrue(opgehaaldSchaken.isActief());
        assertEquals("Schaken", opgehaaldSchaken.getTalent().getNaam());
        assertEquals(10, opgehaaldSchaken.getMaxCapaciteit());
        assertEquals(1, opgehaaldSchaken.getLeerkrachten().size());
        assertEquals(opgeslagenLeerkracht.getId(), opgehaaldSchaken.getLeerkrachten().getFirst().getId());

        assertTrue(resultaat.stream().allMatch(
                ingerichtTalent -> ingerichtTalent.getTalentenPeriode().getId().equals(opgeslagenPeriode.getId())
        ));

        assertFalse(resultaat.stream().anyMatch(
                ingerichtTalent -> ingerichtTalent.getTalentenPeriode().getId().equals(winter.getId())
        ));
    }

    @Test
    void zoekOpIdGeeftVolledigIngerichtTalentTerug() {
        // ARRANGE
        IngerichtTalent opgeslagen = ingerichtTalentRepository.save(
                new IngerichtTalent(
                        opgeslagenTalent,
                        opgeslagenPeriode,
                        "Schaken - Herfst",
                        "Schaken voor de herfstperiode",
                        10,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of(opgeslagenLeerkracht, tweedeOpgeslagenLeerkracht)
                )
        );

        // ACT
        IngerichtTalent gevonden = ingerichtTalentRepository.zoekOpId(opgeslagen.getId());

        // ASSERT
        assertNotNull(gevonden);
        assertEquals(opgeslagen.getId(), gevonden.getId());
        assertEquals("Schaken - Herfst", gevonden.getNaam());
        assertEquals("Schaken voor de herfstperiode", gevonden.getOmschrijving());
        assertTrue(gevonden.isActief());

        assertEquals(opgeslagenTalent.getId(), gevonden.getTalent().getId());
        assertEquals("Schaken", gevonden.getTalent().getNaam());
        assertEquals("Leren schaken", gevonden.getTalent().getBeschrijving());

        assertEquals(opgeslagenPeriode.getId(), gevonden.getTalentenPeriode().getId());
        assertEquals("Herfst", gevonden.getTalentenPeriode().getNaam());
        assertEquals(LocalDate.of(2026, 9, 1), gevonden.getTalentenPeriode().getStartDatum());
        assertEquals(LocalDate.of(2026, 10, 31), gevonden.getTalentenPeriode().getEindDatum());

        assertEquals(10, gevonden.getMaxCapaciteit());
        assertEquals(
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                gevonden.getDoelgroep()
        );

        List<Long> gevondenLeerkrachtIds = gevonden.getLeerkrachten()
                .stream()
                .map(Leerkracht::getId)
                .toList();

        assertEquals(2, gevondenLeerkrachtIds.size());
        assertTrue(gevondenLeerkrachtIds.contains(opgeslagenLeerkracht.getId()));
        assertTrue(gevondenLeerkrachtIds.contains(tweedeOpgeslagenLeerkracht.getId()));
    }

    @Test
    void zoekOpIdMetOngeldigIdGeeftException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ingerichtTalentRepository.zoekOpId(0)
        );
    }

    @Test
    void zoekActieveVoorPeriodeEnDoelgroepGeeftAlleenActieveTalentenVanJuisteDoelgroep() {
        // ARRANGE
        Talent voetbal = talentRepository.save(
                new Talent("Voetbal", "Balsport")
        );

        Talent lassen = talentRepository.save(
                new Talent("Lassen", "Leren lassen")
        );

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal,
                opgeslagenPeriode,
                "Voetbal observatie",
                "Voetbal voor de observatiefase",
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                List.of(opgeslagenLeerkracht)
        );

        IngerichtTalent schakenObservatie = new IngerichtTalent(
                opgeslagenTalent,
                opgeslagenPeriode,
                "Schaken observatie",
                "Schaken voor de observatiefase",
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                List.of(opgeslagenLeerkracht)
        );

        schakenObservatie.deactiveer();

        IngerichtTalent lassenKwalificatie = new IngerichtTalent(
                lassen,
                opgeslagenPeriode,
                "Lassen kwalificatie",
                "Lassen voor de kwalificatiefase",
                10,
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB,
                List.of(opgeslagenLeerkracht)
        );

        IngerichtTalent opgeslagenVoetbal = ingerichtTalentRepository.save(voetbalObservatie);

        ingerichtTalentRepository.save(schakenObservatie);
        ingerichtTalentRepository.save(lassenKwalificatie);

        // ACT
        List<IngerichtTalent> resultaat = ingerichtTalentRepository.zoekActieveVoorPeriodeEnDoelgroep(
                opgeslagenPeriode,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        // ASSERT
        assertEquals(1, resultaat.size());

        IngerichtTalent gevonden = resultaat.getFirst();

        assertEquals(opgeslagenVoetbal.getId(), gevonden.getId());
        assertEquals("Voetbal observatie", gevonden.getNaam());
        assertTrue(gevonden.isActief());
        assertEquals(
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                gevonden.getDoelgroep()
        );
    }

    @Test
    void updateWijzigtNaamOmschrijvingCapaciteitEnActief() {
        // ARRANGE
        IngerichtTalent opgeslagen = ingerichtTalentRepository.save(
                maakStandaardIngerichtTalent()
        );

        opgeslagen.wijzigGegevens(
                "Schaken gevorderd",
                "Schaken voor gevorderde leerlingen",
                15
        );

        opgeslagen.deactiveer();

        // ACT
        ingerichtTalentRepository.update(opgeslagen);

        // ASSERT
        IngerichtTalent resultaat = ingerichtTalentRepository.zoekOpId(opgeslagen.getId());

        assertEquals("Schaken gevorderd", resultaat.getNaam());
        assertEquals("Schaken voor gevorderde leerlingen", resultaat.getOmschrijving());
        assertEquals(15, resultaat.getMaxCapaciteit());
        assertFalse(resultaat.isActief());
    }

    @Test
    void updateKanTweedeLeerkrachtToevoegen() {
        // ARRANGE
        IngerichtTalent opgeslagen = ingerichtTalentRepository.save(
                maakStandaardIngerichtTalent()
        );

        opgeslagen.voegLeerkrachtToe(tweedeOpgeslagenLeerkracht);

        // ACT
        ingerichtTalentRepository.update(opgeslagen);

        // ASSERT
        IngerichtTalent resultaat = ingerichtTalentRepository.zoekOpId(opgeslagen.getId());

        assertEquals(2, resultaat.getLeerkrachten().size());
        assertTrue(resultaat.getLeerkrachten().contains(opgeslagenLeerkracht));
        assertTrue(resultaat.getLeerkrachten().contains(tweedeOpgeslagenLeerkracht));
    }

    @Test
    void updateKanLeerkrachtVerwijderen() {
        // ARRANGE
        IngerichtTalent opgeslagen = ingerichtTalentRepository.save(
                new IngerichtTalent(
                        opgeslagenTalent,
                        opgeslagenPeriode,
                        "Schaken beginners",
                        "Leren schaken",
                        10,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of(opgeslagenLeerkracht, tweedeOpgeslagenLeerkracht)
                )
        );

        opgeslagen.verwijderLeerkracht(opgeslagenLeerkracht);

        // ACT
        ingerichtTalentRepository.update(opgeslagen);

        // ASSERT
        IngerichtTalent resultaat = ingerichtTalentRepository.zoekOpId(opgeslagen.getId());

        assertEquals(1, resultaat.getLeerkrachten().size());
        assertEquals(
                tweedeOpgeslagenLeerkracht.getId(),
                resultaat.getLeerkrachten().getFirst().getId()
        );
    }

    @Test
    void updateKanAlleLeerkrachtenVerwijderen() {
        // ARRANGE
        IngerichtTalent opgeslagen = ingerichtTalentRepository.save(
                maakStandaardIngerichtTalent()
        );

        opgeslagen.verwijderLeerkracht(opgeslagenLeerkracht);

        // ACT
        ingerichtTalentRepository.update(opgeslagen);

        // ASSERT
        IngerichtTalent resultaat = ingerichtTalentRepository.zoekOpId(opgeslagen.getId());

        assertTrue(resultaat.getLeerkrachten().isEmpty());
    }

    @Test
    void updateMetNullGeeftException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ingerichtTalentRepository.update(null)
        );
    }

    @Test
    void updateVanNietOpgeslagenIngerichtTalentGeeftException() {
        // ARRANGE
        IngerichtTalent ingerichtTalent = maakStandaardIngerichtTalent();

        // ACT & ASSERT
        assertThrows(
                IllegalStateException.class,
                () -> ingerichtTalentRepository.update(ingerichtTalent)
        );
    }

    private IngerichtTalent maakStandaardIngerichtTalent() {
        return new IngerichtTalent(
                opgeslagenTalent,
                opgeslagenPeriode,
                "Schaken beginners",
                "Leren schaken",
                10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                List.of(opgeslagenLeerkracht)
        );
    }

    private void controleerIngerichtTalentInDatabank(IngerichtTalent ingerichtTalent) throws SQLException {
        String sql = """
                SELECT naam, omschrijving, maximum_capaciteit, doelgroep, actief, talent_id, talenten_periode_id
                FROM ingerichte_talenten
                WHERE ingericht_talent_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ingerichtTalent.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "Het ingerichte talent werd niet teruggevonden in de databank");

                assertEquals("Schaken - Herfst", resultSet.getString("naam"));
                assertEquals("Schaken voor de herfstperiode", resultSet.getString("omschrijving"));
                assertEquals(10, resultSet.getInt("maximum_capaciteit"));
                assertEquals(
                        "OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB",
                        resultSet.getString("doelgroep")
                );
                assertTrue(resultSet.getBoolean("actief"));

                assertEquals(
                        ingerichtTalent.getTalent().getId().longValue(),
                        resultSet.getLong("talent_id")
                );

                assertEquals(
                        ingerichtTalent.getTalentenPeriode().getId().longValue(),
                        resultSet.getLong("talenten_periode_id")
                );

                assertFalse(
                        resultSet.next(),
                        "Er werden meerdere ingerichte talenten met hetzelfde ID gevonden"
                );
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