package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.KlasRepository;
import be.kdg.talenten.repository.LeerlingKlasHistoriekRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryIngerichtTalentRepository;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryToewijzingRepository;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.verdeling.VerdelingBekijkenService;
import be.kdg.talenten.service.verdeling.VerdelingExcelService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VerdelingExcelServiceTest {

    private static final Doelgroep OBSERVATIE =
            Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

    @TempDir
    Path tempDir;

    private Schooljaar schooljaar;
    private TalentenPeriode herfst;

    private Klas klas1AA;
    private Klas klas1AB;

    private Leerling jan;
    private Leerling sofie;
    private Leerling julie;

    private IngerichtTalent schakenHerfst;
    private IngerichtTalent dansenHerfst;

    private ToewijzingRepository toewijzingRepository;
    private TestLeerlingKlasHistoriekRepository historiekRepository;

    private VerdelingExcelService service;

    @BeforeEach
    void setUp() {
        schooljaar =
                new Schooljaar(
                        1L,
                        "2026-2027",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2027, 6, 30),
                        true
                );

        klas1AA =
                new Klas(
                        1L,
                        "1AA",
                        schooljaar,
                        1,
                        OBSERVATIE
                );

        klas1AB =
                new Klas(
                        2L,
                        "1AB",
                        schooljaar,
                        1,
                        OBSERVATIE
                );

        jan =
                new Leerling(
                        1L,
                        "Jan",
                        "Peeters",
                        klas1AA,
                        true
                );

        sofie =
                new Leerling(
                        2L,
                        "Sofie",
                        "Janssens",
                        klas1AA,
                        true
                );

        julie =
                new Leerling(
                        3L,
                        "Julie",
                        "Martens",
                        klas1AB,
                        true
                );

        herfst =
                new TalentenPeriode(
                        "Herfst",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 31),
                        schooljaar
                );

        Talent schaken =
                new Talent(
                        "Schaken",
                        "Leren schaken"
                );

        Talent dansen =
                new Talent(
                        "Dansen",
                        "Leren dansen"
                );

        Leerkracht leerkracht =
                new Leerkracht(
                        "Test",
                        "Leerkracht"
                );

        schakenHerfst =
                new IngerichtTalent(
                        schaken,
                        herfst,
                        schaken.getNaam(),
                        schaken.getBeschrijving(),
                        10,
                        OBSERVATIE,
                        List.of(leerkracht)
                );

        dansenHerfst =
                new IngerichtTalent(
                        dansen,
                        herfst,
                        dansen.getNaam(),
                        dansen.getBeschrijving(),
                        8,
                        OBSERVATIE,
                        List.of(leerkracht)
                );

        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        List.of(
                                schakenHerfst,
                                dansenHerfst
                        )
                );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan,
                                sofie,
                                julie
                        )
                );

        toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        toewijzingRepository.saveAll(
                List.of(
                        new Toewijzing(
                                jan,
                                schakenHerfst,
                                ToewijzingsType.AUTOMATISCH,
                                1
                        ),
                        new Toewijzing(
                                julie,
                                dansenHerfst,
                                ToewijzingsType.MANUEEL,
                                null
                        )
                )
        );

        historiekRepository =
                new TestLeerlingKlasHistoriekRepository();

        historiekRepository.startHistoriek(
                jan,
                klas1AA,
                schooljaar.getStartDatum()
        );

        historiekRepository.startHistoriek(
                sofie,
                klas1AA,
                schooljaar.getStartDatum()
        );

        historiekRepository.startHistoriek(
                julie,
                klas1AB,
                schooljaar.getStartDatum()
        );

        VerdelingBekijkenService verdelingBekijkenService =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        historiekRepository
                );

        KlasRepository klasRepository =
                new TestKlasRepository(
                        List.of(
                                klas1AA,
                                klas1AB
                        )
                );

        KlasService klasService =
                new KlasService(
                        klasRepository
                );

        service =
                new VerdelingExcelService(
                        verdelingBekijkenService,
                        klasService
                );
    }

    @Test
    void exporteerPerKlasMaaktExcelbestandMetSheetPerKlas()
            throws Exception {

        // ARRANGE
        Path bestand =
                tempDir.resolve(
                        "verdeling-per-klas.xlsx"
                );

        // ACT
        service.exporteerPerKlas(
                herfst,
                OBSERVATIE,
                bestand
        );

        // ASSERT
        assertTrue(
                Files.exists(
                        bestand
                )
        );

        assertTrue(
                Files.size(
                        bestand
                ) > 0
        );

        try (InputStream inputStream =
                     Files.newInputStream(
                             bestand
                     );
             XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             inputStream
                     )) {

            assertEquals(
                    2,
                    workbook.getNumberOfSheets()
            );

            assertNotNull(
                    workbook.getSheet(
                            "1AA"
                    )
            );

            assertNotNull(
                    workbook.getSheet(
                            "1AB"
                    )
            );
        }
    }

    @Test
    void exporteerPerKlasBevatEnkelVoornaamAchternaamEnIngerichtTalent()
            throws Exception {

        // ARRANGE
        Path bestand =
                tempDir.resolve(
                        "verdeling-per-klas.xlsx"
                );

        // ACT
        service.exporteerPerKlas(
                herfst,
                OBSERVATIE,
                bestand
        );

        // ASSERT
        try (InputStream inputStream =
                     Files.newInputStream(
                             bestand
                     );
             XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             inputStream
                     )) {

            Sheet sheet =
                    workbook.getSheet(
                            "1AA"
                    );

            assertNotNull(
                    sheet
            );

            Row header =
                    sheet.getRow(
                            4
                    );

            assertEquals(
                    "Voornaam",
                    header
                            .getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    "Achternaam",
                    header
                            .getCell(1)
                            .getStringCellValue()
            );

            assertEquals(
                    "Ingericht talent",
                    header
                            .getCell(2)
                            .getStringCellValue()
            );

            assertNull(
                    header.getCell(
                            3
                    )
            );
        }
    }

    @Test
    void exporteerPerKlasToontCorrectIngerichtTalentVoorLeerling()
            throws Exception {

        // ARRANGE
        Path bestand =
                tempDir.resolve(
                        "verdeling-per-klas.xlsx"
                );

        // ACT
        service.exporteerPerKlas(
                herfst,
                OBSERVATIE,
                bestand
        );

        // ASSERT
        try (InputStream inputStream =
                     Files.newInputStream(
                             bestand
                     );
             XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             inputStream
                     )) {

            Sheet sheet =
                    workbook.getSheet(
                            "1AA"
                    );

            Row janRij =
                    zoekLeerlingRij(
                            sheet,
                            "Jan",
                            "Peeters"
                    );

            assertNotNull(
                    janRij
            );

            assertEquals(
                    "Schaken",
                    janRij
                            .getCell(2)
                            .getStringCellValue()
            );

            assertNull(
                    janRij.getCell(
                            3
                    )
            );
        }
    }

    @Test
    void exporteerPerKlasToontNietToegewezenLeerling()
            throws Exception {

        // ARRANGE
        Path bestand =
                tempDir.resolve(
                        "verdeling-per-klas.xlsx"
                );

        // ACT
        service.exporteerPerKlas(
                herfst,
                OBSERVATIE,
                bestand
        );

        // ASSERT
        try (InputStream inputStream =
                     Files.newInputStream(
                             bestand
                     );
             XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             inputStream
                     )) {

            Sheet sheet =
                    workbook.getSheet(
                            "1AA"
                    );

            Row sofieRij =
                    zoekLeerlingRij(
                            sheet,
                            "Sofie",
                            "Janssens"
                    );

            assertNotNull(
                    sofieRij
            );

            assertEquals(
                    "Niet toegewezen",
                    sofieRij
                            .getCell(2)
                            .getStringCellValue()
            );
        }
    }

    @Test
    void exporteerPerIngerichtTalentMaaktOverzichtEnSheetPerTalent()
            throws Exception {

        // ARRANGE
        Path bestand =
                tempDir.resolve(
                        "verdeling-per-talent.xlsx"
                );

        // ACT
        service.exporteerPerIngerichtTalent(
                herfst,
                OBSERVATIE,
                bestand
        );

        // ASSERT
        assertTrue(
                Files.exists(
                        bestand
                )
        );

        try (InputStream inputStream =
                     Files.newInputStream(
                             bestand
                     );
             XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             inputStream
                     )) {

            assertEquals(
                    3,
                    workbook.getNumberOfSheets()
            );

            assertNotNull(
                    workbook.getSheet(
                            "Overzicht"
                    )
            );

            assertNotNull(
                    workbook.getSheet(
                            "Schaken"
                    )
            );

            assertNotNull(
                    workbook.getSheet(
                            "Dansen"
                    )
            );
        }
    }

    @Test
    void exporteerPerIngerichtTalentBevatEnkelVoornaamAchternaamEnKlas()
            throws Exception {

        // ARRANGE
        Path bestand =
                tempDir.resolve(
                        "verdeling-per-talent.xlsx"
                );

        // ACT
        service.exporteerPerIngerichtTalent(
                herfst,
                OBSERVATIE,
                bestand
        );

        // ASSERT
        try (InputStream inputStream =
                     Files.newInputStream(
                             bestand
                     );
             XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             inputStream
                     )) {

            Sheet sheet =
                    workbook.getSheet(
                            "Schaken"
                    );

            assertNotNull(
                    sheet
            );

            Row header =
                    sheet.getRow(
                            4
                    );

            assertEquals(
                    "Voornaam",
                    header
                            .getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    "Achternaam",
                    header
                            .getCell(1)
                            .getStringCellValue()
            );

            assertEquals(
                    "Klas",
                    header
                            .getCell(2)
                            .getStringCellValue()
            );

            assertNull(
                    header.getCell(
                            3
                    )
            );

            Row janRij =
                    zoekLeerlingRij(
                            sheet,
                            "Jan",
                            "Peeters"
                    );

            assertNotNull(
                    janRij
            );

            assertEquals(
                    "1AA",
                    janRij
                            .getCell(2)
                            .getStringCellValue()
            );
        }
    }

    @Test
    void exporteerPerIngerichtTalentGebruiktHistorischeKlasVanStartPeriode()
            throws Exception {

        // ARRANGE
        LocalDate wisseldatum =
                LocalDate.of(
                        2026,
                        10,
                        15
                );

        historiekRepository
                .sluitHuidigeHistoriekAf(
                        sofie,
                        wisseldatum
                );

        sofie.wijsKlasToe(
                klas1AB
        );

        historiekRepository.startHistoriek(
                sofie,
                klas1AB,
                wisseldatum
        );

        toewijzingRepository.saveAll(
                List.of(
                        new Toewijzing(
                                sofie,
                                schakenHerfst,
                                ToewijzingsType.MANUEEL,
                                null
                        )
                )
        );

        Path bestand =
                tempDir.resolve(
                        "historische-verdeling.xlsx"
                );

        // ACT
        service.exporteerPerIngerichtTalent(
                herfst,
                OBSERVATIE,
                bestand
        );

        // ASSERT
        try (InputStream inputStream =
                     Files.newInputStream(
                             bestand
                     );
             XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             inputStream
                     )) {

            Sheet sheet =
                    workbook.getSheet(
                            "Schaken"
                    );

            Row sofieRij =
                    zoekLeerlingRij(
                            sheet,
                            "Sofie",
                            "Janssens"
                    );

            assertNotNull(
                    sofieRij
            );

            assertEquals(
                    "1AA",
                    sofieRij
                            .getCell(2)
                            .getStringCellValue()
            );
        }
    }

    @Test
    void exportPerTalentFiltertOpDoelgroep()
            throws Exception {

        // ARRANGE
        Klas kwalificatieKlas =
                new Klas(
                        3L,
                        "3KA",
                        schooljaar,
                        3,
                        Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB
                );

        Leerling tom =
                new Leerling(
                        4L,
                        "Tom",
                        "Vermeulen",
                        kwalificatieKlas,
                        true
                );

        Talent koken =
                new Talent(
                        "Koken",
                        "Leren koken"
                );

        Leerkracht leerkracht =
                new Leerkracht(
                        "Test",
                        "Leerkracht"
                );

        IngerichtTalent kokenHerfst =
                new IngerichtTalent(
                        koken,
                        herfst,
                        koken.getNaam(),
                        koken.getBeschrijving(),
                        10,
                        Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB,
                        List.of(
                                leerkracht
                        )
                );

        VerdelingExcelService filterService =
                maakFilterTestService(
                        kwalificatieKlas,
                        tom,
                        kokenHerfst
                );

        Path bestand =
                tempDir.resolve(
                        "talenten-observatie.xlsx"
                );

        // ACT
        filterService.exporteerPerIngerichtTalent(
                herfst,
                OBSERVATIE,
                bestand
        );

        // ASSERT
        try (InputStream inputStream =
                     Files.newInputStream(
                             bestand
                     );
             XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             inputStream
                     )) {

            assertNotNull(
                    workbook.getSheet(
                            "Overzicht"
                    )
            );

            assertNotNull(
                    workbook.getSheet(
                            "Schaken"
                    )
            );

            assertNotNull(
                    workbook.getSheet(
                            "Dansen"
                    )
            );

            assertNull(
                    workbook.getSheet(
                            "Koken"
                    )
            );

            assertEquals(
                    3,
                    workbook.getNumberOfSheets()
            );
        }
    }

    @Test
    void exportPerKlasFiltertOpDoelgroep()
            throws Exception {

        // ARRANGE
        Klas kwalificatieKlas =
                new Klas(
                        3L,
                        "3KA",
                        schooljaar,
                        3,
                        Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB
                );

        Leerling tom =
                new Leerling(
                        4L,
                        "Tom",
                        "Vermeulen",
                        kwalificatieKlas,
                        true
                );

        Talent koken =
                new Talent(
                        "Koken",
                        "Leren koken"
                );

        Leerkracht leerkracht =
                new Leerkracht(
                        "Test",
                        "Leerkracht"
                );

        IngerichtTalent kokenHerfst =
                new IngerichtTalent(
                        koken,
                        herfst,
                        koken.getNaam(),
                        koken.getBeschrijving(),
                        10,
                        Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB,
                        List.of(
                                leerkracht
                        )
                );

        VerdelingExcelService filterService =
                maakFilterTestService(
                        kwalificatieKlas,
                        tom,
                        kokenHerfst
                );

        Path bestand =
                tempDir.resolve(
                        "klassen-observatie.xlsx"
                );

        // ACT
        filterService.exporteerPerKlas(
                herfst,
                OBSERVATIE,
                bestand
        );

        // ASSERT
        try (InputStream inputStream =
                     Files.newInputStream(
                             bestand
                     );
             XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             inputStream
                     )) {

            assertNotNull(
                    workbook.getSheet(
                            "1AA"
                    )
            );

            assertNotNull(
                    workbook.getSheet(
                            "1AB"
                    )
            );

            assertNull(
                    workbook.getSheet(
                            "3KA"
                    )
            );

            assertEquals(
                    2,
                    workbook.getNumberOfSheets()
            );
        }
    }

    @Test
    void exportMetNullDoelgroepWordtGeweigerd() {
        // ARRANGE
        Path klasBestand =
                tempDir.resolve(
                        "klassen.xlsx"
                );

        Path talentBestand =
                tempDir.resolve(
                        "talenten.xlsx"
                );

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.exporteerPerKlas(
                        herfst,
                        null,
                        klasBestand
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.exporteerPerIngerichtTalent(
                        herfst,
                        null,
                        talentBestand
                )
        );
    }

    @Test
    void exporteerPerKlasMetNullPeriodeGeeftException() {
        // ARRANGE
        Path bestand =
                tempDir.resolve(
                        "verdeling.xlsx"
                );

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.exporteerPerKlas(
                        null,
                        OBSERVATIE,
                        bestand
                )
        );
    }

    @Test
    void exporteerPerKlasMetNullBestandGeeftException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.exporteerPerKlas(
                        herfst,
                        OBSERVATIE,
                        null
                )
        );
    }

    @Test
    void exporteerPerIngerichtTalentMetNullPeriodeGeeftException() {
        // ARRANGE
        Path bestand =
                tempDir.resolve(
                        "verdeling.xlsx"
                );

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> service.exporteerPerIngerichtTalent(
                        null,
                        OBSERVATIE,
                        bestand
                )
        );
    }

    @Test
    void exporteerPerIngerichtTalentMetNullBestandGeeftException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.exporteerPerIngerichtTalent(
                        herfst,
                        OBSERVATIE,
                        null
                )
        );
    }

    @Test
    void constructorMetNullVerdelingBekijkenServiceGeeftException() {
        // ARRANGE
        KlasService klasService =
                new KlasService(
                        new TestKlasRepository(
                                List.of()
                        )
                );

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> new VerdelingExcelService(
                        null,
                        klasService
                )
        );
    }

    @Test
    void constructorMetNullKlasServiceGeeftException() {
        // ARRANGE
        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        new ArrayList<>()
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        new ArrayList<>()
                );

        VerdelingBekijkenService verdelingBekijkenService =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        new TestLeerlingKlasHistoriekRepository()
                );

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> new VerdelingExcelService(
                        verdelingBekijkenService,
                        null
                )
        );
    }

    private VerdelingExcelService maakFilterTestService(
            Klas kwalificatieKlas,
            Leerling kwalificatieLeerling,
            IngerichtTalent kwalificatieTalent
    ) {
        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        List.of(
                                schakenHerfst,
                                dansenHerfst,
                                kwalificatieTalent
                        )
                );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(
                                jan,
                                sofie,
                                julie,
                                kwalificatieLeerling
                        )
                );

        ToewijzingRepository filterToewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        filterToewijzingRepository.saveAll(
                List.of(
                        new Toewijzing(
                                jan,
                                schakenHerfst,
                                ToewijzingsType.AUTOMATISCH,
                                1
                        ),
                        new Toewijzing(
                                julie,
                                dansenHerfst,
                                ToewijzingsType.MANUEEL,
                                null
                        ),
                        new Toewijzing(
                                kwalificatieLeerling,
                                kwalificatieTalent,
                                ToewijzingsType.AUTOMATISCH,
                                1
                        )
                )
        );

        TestLeerlingKlasHistoriekRepository filterHistoriekRepository =
                new TestLeerlingKlasHistoriekRepository();

        filterHistoriekRepository.startHistoriek(
                jan,
                klas1AA,
                schooljaar.getStartDatum()
        );

        filterHistoriekRepository.startHistoriek(
                sofie,
                klas1AA,
                schooljaar.getStartDatum()
        );

        filterHistoriekRepository.startHistoriek(
                julie,
                klas1AB,
                schooljaar.getStartDatum()
        );

        filterHistoriekRepository.startHistoriek(
                kwalificatieLeerling,
                kwalificatieKlas,
                schooljaar.getStartDatum()
        );

        VerdelingBekijkenService verdelingBekijkenService =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        filterToewijzingRepository,
                        leerlingRepository,
                        filterHistoriekRepository
                );

        KlasService filterKlasService =
                new KlasService(
                        new TestKlasRepository(
                                List.of(
                                        klas1AA,
                                        klas1AB,
                                        kwalificatieKlas
                                )
                        )
                );

        return new VerdelingExcelService(
                verdelingBekijkenService,
                filterKlasService
        );
    }

    private Row zoekLeerlingRij(
            Sheet sheet,
            String voornaam,
            String achternaam
    ) {
        for (int rijIndex = 5;
             rijIndex <= sheet.getLastRowNum();
             rijIndex++) {

            Row row =
                    sheet.getRow(
                            rijIndex
                    );

            if (row == null
                    || row.getCell(0) == null
                    || row.getCell(1) == null) {

                continue;
            }

            String gevondenVoornaam =
                    row.getCell(0)
                            .getStringCellValue();

            String gevondenAchternaam =
                    row.getCell(1)
                            .getStringCellValue();

            if (gevondenVoornaam.equals(
                    voornaam
            )
                    && gevondenAchternaam.equals(
                    achternaam
            )) {

                return row;
            }
        }

        return null;
    }

    private static class TestKlasRepository
            implements KlasRepository {

        private final List<Klas> klassen;

        private TestKlasRepository(
                List<Klas> klassen
        ) {
            this.klassen =
                    new ArrayList<>(
                            klassen
                    );
        }

        @Override
        public Klas save(
                Klas klas
        ) {
            klassen.add(
                    klas
            );

            return klas;
        }

        @Override
        public List<Klas> zoekAlle() {
            return List.copyOf(
                    klassen
            );
        }

        @Override
        public Klas zoekOpId(
                long id
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(
                Klas klas
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(
                Klas klas
        ) {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestLeerlingKlasHistoriekRepository
            implements LeerlingKlasHistoriekRepository {

        private final List<LeerlingKlasHistoriek> historiek =
                new ArrayList<>();

        private long volgendId =
                1;

        @Override
        public void startHistoriek(
                Leerling leerling,
                Klas klas,
                LocalDate vanaf
        ) {
            historiek.add(
                    new LeerlingKlasHistoriek(
                            volgendId++,
                            leerling,
                            klas,
                            vanaf,
                            null
                    )
            );
        }

        @Override
        public void sluitHuidigeHistoriekAf(
                Leerling leerling,
                LocalDate tot
        ) {
            for (int i = 0;
                 i < historiek.size();
                 i++) {

                LeerlingKlasHistoriek registratie =
                        historiek.get(
                                i
                        );

                if (registratie
                        .getLeerling()
                        .equals(
                                leerling
                        )
                        && registratie.isHuidig()) {

                    historiek.set(
                            i,
                            new LeerlingKlasHistoriek(
                                    registratie.getId(),
                                    registratie.getLeerling(),
                                    registratie.getKlas(),
                                    registratie.getVanaf(),
                                    tot
                            )
                    );

                    return;
                }
            }

            throw new IllegalStateException(
                    "Geen huidige klashistoriek gevonden."
            );
        }

        @Override
        public void wijzigHuidigeKlas(
                Leerling leerling,
                Klas nieuweKlas
        ) {
            for (int i = 0;
                 i < historiek.size();
                 i++) {

                LeerlingKlasHistoriek registratie =
                        historiek.get(
                                i
                        );

                if (registratie
                        .getLeerling()
                        .equals(
                                leerling
                        )
                        && registratie.isHuidig()) {

                    historiek.set(
                            i,
                            new LeerlingKlasHistoriek(
                                    registratie.getId(),
                                    registratie.getLeerling(),
                                    nieuweKlas,
                                    registratie.getVanaf(),
                                    null
                            )
                    );

                    return;
                }
            }

            throw new IllegalStateException(
                    "Geen huidige klashistoriek gevonden."
            );
        }

        @Override
        public List<LeerlingKlasHistoriek> zoekVoorLeerling(
                Leerling leerling
        ) {
            return historiek
                    .stream()
                    .filter(
                            registratie ->
                                    registratie
                                            .getLeerling()
                                            .equals(
                                                    leerling
                                            )
                    )
                    .toList();
        }

        @Override
        public List<LeerlingKlasHistoriek> zoekVoorKlasOpDatum(
                Klas klas,
                LocalDate datum
        ) {
            return historiek
                    .stream()
                    .filter(
                            registratie ->
                                    registratie
                                            .getKlas()
                                            .equals(
                                                    klas
                                            )
                                            && !registratie
                                            .getVanaf()
                                            .isAfter(
                                                    datum
                                            )
                                            && (
                                            registratie.getTot() == null
                                                    || registratie
                                                    .getTot()
                                                    .isAfter(
                                                            datum
                                                    )
                                    )
                    )
                    .toList();
        }
    }
}