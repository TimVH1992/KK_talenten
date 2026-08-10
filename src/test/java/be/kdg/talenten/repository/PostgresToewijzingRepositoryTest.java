package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.postgres.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgresToewijzingRepositoryTest {
    private Schooljaar schooljaar2025_2026;
    private Schooljaar schooljaar2026_2027;

    private PostgresKlasRepository klasRepository;
    private PostgresLeerlingRepository leerlingRepository;
    private PostgresTalentRepository talentRepository;
    private PostgresTalentenPeriodeRepository periodeRepository;
    private PostgresLeerkrachtRepository leerkrachtRepository;
    private PostgresIngerichtTalentRepository ingerichtTalentRepository;
    private PostgresToewijzingRepository toewijzingRepository;

    private Leerling jan;
    private Leerling julie;
    private Talent schaken;
    private TalentenPeriode herfst;
    private Leerkracht tim;
    private IngerichtTalent schakenHerfst;
    private IngerichtTalent dansenHerfst;

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

        klasRepository = new PostgresKlasRepository();
        leerlingRepository = new PostgresLeerlingRepository();
        talentRepository = new PostgresTalentRepository();
        periodeRepository = new PostgresTalentenPeriodeRepository();
        leerkrachtRepository = new PostgresLeerkrachtRepository();
        ingerichtTalentRepository = new PostgresIngerichtTalentRepository();
        toewijzingRepository = new PostgresToewijzingRepository();

        Klas klas = klasRepository.save(
                new Klas(
                        "1AA",
                        schooljaar2026_2027,
                        1,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        jan = leerlingRepository.save(new Leerling("Jan", "Peeters", klas));
        julie = leerlingRepository.save(new Leerling("Julie", "Martens", klas));

        schaken = talentRepository.save(new Talent("Schaken", "Leren schaken"));
        Talent dansen = talentRepository.save(new Talent("Dansen", "Leren dansen"));

        herfst = periodeRepository.save(
                new TalentenPeriode(
                        "Herfst",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 31),
                        schooljaar2026_2027
                )
        );

        tim = leerkrachtRepository.save(
                new Leerkracht("Tim", "Van Herreweghe")
        );

        schakenHerfst = ingerichtTalentRepository.save(
                new IngerichtTalent(
                        schaken,
                        herfst,
                        schaken.getNaam(),
                        schaken.getBeschrijving(),
                        10,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of(tim)
                )
        );

        dansenHerfst = ingerichtTalentRepository.save(
                new IngerichtTalent(
                        dansen,
                        herfst,
                        dansen.getNaam(),
                        dansen.getBeschrijving(),
                        6,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of(tim)
                )
        );
    }

    @Test
    void saveSlaatAutomatischeToewijzingOp() throws SQLException {
        // ARRANGE
        Toewijzing toewijzing = new Toewijzing(
                jan,
                schakenHerfst,
                ToewijzingsType.AUTOMATISCH,
                1
        );

        // ACT
        Toewijzing opgeslagenToewijzing =
                toewijzingRepository.save(toewijzing);

        // ASSERT
        assertNotNull(opgeslagenToewijzing);
        assertNotNull(opgeslagenToewijzing.getId());
        assertTrue(opgeslagenToewijzing.getId() > 0);
        assertSame(jan, opgeslagenToewijzing.getLeerling());
        assertSame(schakenHerfst, opgeslagenToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.AUTOMATISCH, opgeslagenToewijzing.getToewijzingsType());
        assertEquals(1, opgeslagenToewijzing.getVoorkeurNummer());
        assertNotNull(opgeslagenToewijzing.getToegewezenOp());
        assertNull(opgeslagenToewijzing.getGewijzigdOp());

        controleerToewijzingInDatabank(opgeslagenToewijzing);
    }

    @Test
    void saveSlaatManueleToewijzingZonderVoorkeurNummerOp() throws SQLException {
        // ARRANGE
        Toewijzing toewijzing = new Toewijzing(
                jan,
                schakenHerfst,
                ToewijzingsType.MANUEEL
        );

        // ACT
        Toewijzing opgeslagenToewijzing =
                toewijzingRepository.save(toewijzing);

        // ASSERT
        assertNotNull(opgeslagenToewijzing.getId());
        assertEquals(ToewijzingsType.MANUEEL, opgeslagenToewijzing.getToewijzingsType());
        assertNull(opgeslagenToewijzing.getVoorkeurNummer());

        controleerToewijzingInDatabank(opgeslagenToewijzing);
    }

    @Test
    void zoekVoorPeriodeGeeftAlleenToewijzingenVanGevraagdePeriode() {
        // ARRANGE
        Toewijzing janHerfst = toewijzingRepository.save(
                new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH, 1)
        );

        Toewijzing julieHerfst = toewijzingRepository.save(
                new Toewijzing(julie, dansenHerfst, ToewijzingsType.MANUEEL)
        );

        TalentenPeriode winter = periodeRepository.save(
                new TalentenPeriode(
                        "Winter",
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 12, 20),
                        schooljaar2026_2027
                )
        );

        IngerichtTalent schakenWinter = ingerichtTalentRepository.save(
                new IngerichtTalent(
                        schaken,
                        winter,
                        schaken.getNaam(),
                        schaken.getBeschrijving(),
                        8,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of(tim)
                )
        );

        Toewijzing winterToewijzing = toewijzingRepository.save(
                new Toewijzing(
                        jan,
                        schakenWinter,
                        ToewijzingsType.AUTOMATISCH,
                        2
                )
        );

        // ACT
        List<Toewijzing> resultaat =
                toewijzingRepository.zoekVoorPeriode(herfst);

        // ASSERT
        assertEquals(2, resultaat.size());

        assertTrue(resultaat.stream()
                .anyMatch(toewijzing ->
                        toewijzing.getId().equals(janHerfst.getId())));

        assertTrue(resultaat.stream()
                .anyMatch(toewijzing ->
                        toewijzing.getId().equals(julieHerfst.getId())));

        assertFalse(resultaat.stream()
                .anyMatch(toewijzing ->
                        toewijzing.getId().equals(winterToewijzing.getId())));

        assertTrue(resultaat.stream()
                .allMatch(toewijzing ->
                        toewijzing.getIngerichtTalent()
                                .getTalentenPeriode()
                                .getId()
                                .equals(herfst.getId())));
    }

    @Test
    void zoekToewijzingVoorLeerlingEnPeriodeGeeftJuisteToewijzing() {
        // ARRANGE
        Toewijzing opgeslagenToewijzing = toewijzingRepository.save(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        1
                )
        );

        // ACT
        Toewijzing gevondenToewijzing =
                toewijzingRepository.zoekToewijzingVoorLeerlingEnPeriode(
                        jan,
                        herfst
                );

        // ASSERT
        assertNotNull(gevondenToewijzing);
        assertEquals(opgeslagenToewijzing.getId(), gevondenToewijzing.getId());
        assertEquals(jan.getId(), gevondenToewijzing.getLeerling().getId());
        assertEquals(schakenHerfst.getId(), gevondenToewijzing.getIngerichtTalent().getId());
        assertEquals(ToewijzingsType.AUTOMATISCH, gevondenToewijzing.getToewijzingsType());
        assertEquals(1, gevondenToewijzing.getVoorkeurNummer());
    }

    @Test
    void zoekToewijzingVoorLeerlingEnPeriodeGeeftNullWanneerGeenToewijzingBestaat() {
        // ACT
        Toewijzing resultaat =
                toewijzingRepository.zoekToewijzingVoorLeerlingEnPeriode(
                        jan,
                        herfst
                );

        // ASSERT
        assertNull(resultaat);
    }

    @Test
    void telToewijzingenVoorIngerichtTalentGeeftJuisteAantal() {
        // ARRANGE
        toewijzingRepository.save(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        1
                )
        );

        toewijzingRepository.save(
                new Toewijzing(
                        julie,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        2
                )
        );

        // ACT
        int aantalSchaken =
                toewijzingRepository.telToewijzingenVoorIngerichtTalent(schakenHerfst);

        int aantalDansen =
                toewijzingRepository.telToewijzingenVoorIngerichtTalent(dansenHerfst);

        // ASSERT
        assertEquals(2, aantalSchaken);
        assertEquals(0, aantalDansen);
    }

    @Test
    void updateWijzigtBestaandeToewijzing() {
        // ARRANGE
        Toewijzing opgeslagenToewijzing = toewijzingRepository.save(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        1
                )
        );

        opgeslagenToewijzing.wijzigNaar(
                dansenHerfst,
                ToewijzingsType.MANUEEL,
                null
        );

        // ACT
        Toewijzing gewijzigdeToewijzing =
                toewijzingRepository.update(opgeslagenToewijzing);

        Toewijzing opgehaald =
                toewijzingRepository.zoekToewijzingVoorLeerlingEnPeriode(
                        jan,
                        herfst
                );

        // ASSERT
        assertEquals(opgeslagenToewijzing.getId(), gewijzigdeToewijzing.getId());
        assertNotNull(gewijzigdeToewijzing.getGewijzigdOp());

        assertNotNull(opgehaald);
        assertEquals(opgeslagenToewijzing.getId(), opgehaald.getId());
        assertEquals(dansenHerfst.getId(), opgehaald.getIngerichtTalent().getId());
        assertEquals(ToewijzingsType.MANUEEL, opgehaald.getToewijzingsType());
        assertNull(opgehaald.getVoorkeurNummer());
        assertNotNull(opgehaald.getGewijzigdOp());
    }

    @Test
    void saveAllSlaatAlleToewijzingenOp() {
        // ARRANGE
        Toewijzing janToewijzing = new Toewijzing(
                jan,
                schakenHerfst,
                ToewijzingsType.AUTOMATISCH,
                1
        );

        Toewijzing julieToewijzing = new Toewijzing(
                julie,
                dansenHerfst,
                ToewijzingsType.AUTOMATISCH,
                2
        );

        // ACT
        toewijzingRepository.saveAll(
                List.of(janToewijzing, julieToewijzing)
        );

        // ASSERT
        List<Toewijzing> opgeslagenToewijzingen =
                toewijzingRepository.zoekVoorPeriode(herfst);

        assertEquals(2, opgeslagenToewijzingen.size());

        assertTrue(opgeslagenToewijzingen.stream()
                .anyMatch(toewijzing ->
                        toewijzing.getLeerling().getId().equals(jan.getId())));

        assertTrue(opgeslagenToewijzingen.stream()
                .anyMatch(toewijzing ->
                        toewijzing.getLeerling().getId().equals(julie.getId())));
    }

    @Test
    void vervangAutomatischeToewijzingenVoorPeriodeBehoudtManueleToewijzingen() {
        // ARRANGE
        Toewijzing manueleToewijzing = toewijzingRepository.save(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.MANUEEL
                )
        );

        Toewijzing oudeAutomatischeToewijzing = toewijzingRepository.save(
                new Toewijzing(
                        julie,
                        dansenHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        2
                )
        );

        Toewijzing nieuweAutomatischeToewijzing = new Toewijzing(
                julie,
                schakenHerfst,
                ToewijzingsType.AUTOMATISCH,
                1
        );

        // ACT
        toewijzingRepository.vervangAutomatischeToewijzingenVoorPeriode(
                herfst,
                List.of(nieuweAutomatischeToewijzing)
        );

        // ASSERT
        List<Toewijzing> resultaat =
                toewijzingRepository.zoekVoorPeriode(herfst);

        assertEquals(2, resultaat.size());

        assertTrue(resultaat.stream()
                .anyMatch(toewijzing ->
                        toewijzing.getId().equals(manueleToewijzing.getId())));

        assertFalse(resultaat.stream()
                .anyMatch(toewijzing ->
                        toewijzing.getId().equals(oudeAutomatischeToewijzing.getId())));

        assertTrue(resultaat.stream()
                .anyMatch(toewijzing ->
                        toewijzing.getLeerling().getId().equals(julie.getId())
                                && toewijzing.getIngerichtTalent()
                                .getId()
                                .equals(schakenHerfst.getId())
                                && toewijzing.getToewijzingsType()
                                == ToewijzingsType.AUTOMATISCH));
    }

    @Test
    void zoekHistorischeToewijzingenGeeftAlleenToewijzingenUitAfgelopenPeriodesVanSchooljaar() {
        // ARRANGE
        Klas oudeKlas = klasRepository.save(
                new Klas(
                        "1AB",
                        schooljaar2025_2026,
                        1,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        Leerling oudeLeerling = leerlingRepository.save(
                new Leerling(
                        "Oude",
                        "Leerling",
                        oudeKlas
                )
        );

        TalentenPeriode verleden = periodeRepository.save(
                new TalentenPeriode(
                        "Voorjaar",
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 5, 31),
                        schooljaar2025_2026
                )
        );

        IngerichtTalent schakenVerleden =
                ingerichtTalentRepository.save(
                        new IngerichtTalent(
                                schaken,
                                verleden,
                                schaken.getNaam(),
                                schaken.getBeschrijving(),
                                10,
                                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                                List.of(tim)
                        )
                );

        Toewijzing historischeToewijzing =
                toewijzingRepository.save(
                        new Toewijzing(
                                oudeLeerling,
                                schakenVerleden,
                                ToewijzingsType.AUTOMATISCH,
                                1
                        )
                );

        Toewijzing toekomstigeToewijzing =
                toewijzingRepository.save(
                        new Toewijzing(
                                julie,
                                schakenHerfst,
                                ToewijzingsType.AUTOMATISCH,
                                2
                        )
                );

        // ACT
        List<Toewijzing> resultaat =
                toewijzingRepository
                        .zoekHistorischeToewijzingenVoorSchooljaar(
                                schooljaar2025_2026
                        );

        // ASSERT
        assertEquals(1, resultaat.size());
        assertEquals(
                historischeToewijzing.getId(),
                resultaat.getFirst().getId()
        );

        assertFalse(resultaat.stream()
                .anyMatch(toewijzing ->
                        toewijzing.getId().equals(
                                toekomstigeToewijzing.getId()
                        )));
    }

    private void controleerToewijzingInDatabank(Toewijzing toewijzing)
            throws SQLException {

        String sql = """
                SELECT
                    toewijzings_type,
                    voorkeur_nummer,
                    leerling_id,
                    talenten_periode_id,
                    ingericht_talent_id,
                    toegewezen_op,
                    gewijzigd_op
                FROM toewijzingen
                WHERE toewijzing_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, toewijzing.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(
                        resultSet.next(),
                        "De toewijzing werd niet teruggevonden in de databank"
                );

                assertEquals(
                        toewijzing.getToewijzingsType().name(),
                        resultSet.getString("toewijzings_type")
                );

                assertEquals(
                        toewijzing.getLeerling().getId().longValue(),
                        resultSet.getLong("leerling_id")
                );

                assertEquals(
                        toewijzing.getIngerichtTalent()
                                .getTalentenPeriode()
                                .getId()
                                .longValue(),
                        resultSet.getLong("talenten_periode_id")
                );

                assertEquals(
                        toewijzing.getIngerichtTalent().getId().longValue(),
                        resultSet.getLong("ingericht_talent_id")
                );

                assertNotNull(
                        resultSet.getTimestamp("toegewezen_op")
                );

                Object voorkeurNummer =
                        resultSet.getObject("voorkeur_nummer");

                if (toewijzing.getVoorkeurNummer() == null) {
                    assertNull(voorkeurNummer);
                } else {
                    assertEquals(
                            toewijzing.getVoorkeurNummer().intValue(),
                            ((Number) voorkeurNummer).intValue()
                    );
                }

                if (toewijzing.getGewijzigdOp() == null) {
                    assertNull(
                            resultSet.getTimestamp("gewijzigd_op")
                    );
                } else {
                    assertNotNull(
                            resultSet.getTimestamp("gewijzigd_op")
                    );
                }

                assertFalse(
                        resultSet.next(),
                        "Er werden meerdere toewijzingen met hetzelfde ID gevonden"
                );
            }
        }
    }

    private Schooljaar schooljaarVoorPeriode(LocalDate startDatum) {
        return startDatum.getMonthValue() >= 7
                ? schooljaar2026_2027
                : schooljaar2025_2026;
    }
}