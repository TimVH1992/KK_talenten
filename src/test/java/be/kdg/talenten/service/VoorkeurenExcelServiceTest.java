package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryIngerichtTalentRepository;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import be.kdg.talenten.service.voorkeuren.VoorkeurenExcelService;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VoorkeurenExcelServiceTest {
    private Schooljaar schooljaar;
    private TalentenPeriode periode;
    private Doelgroep doelgroep;
    private Klas klas;

    private Leerling jan;
    private Leerling sofie;

    private IngerichtTalent voetbalObservatie;
    private IngerichtTalent schakenObservatie;
    private IngerichtTalent kokenObservatie;

    private InMemoryLeerlingRepository leerlingRepository;
    private InMemoryIngerichtTalentRepository ingerichtTalentRepository;

    private VoorkeurenExcelService service;

    @BeforeEach
    void setUp() {
        schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);
        doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        klas = new Klas("1AA", schooljaar, 1, doelgroep);

        jan = new Leerling("Jan", "Mertens", klas);
        sofie = new Leerling("Sofie", "VO", klas);

        leerlingRepository = new InMemoryLeerlingRepository(List.of(jan, sofie));

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );

        service = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
    }

    @Test
    void genereerTemplateMaaktExcelBestandAan(@TempDir Path tempDir) {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(periode, doelgroep, bestand);

        // ASSERT
        assertTrue(Files.exists(bestand));
    }

    @Test
    void genereerTemplateMaaktTabbladAanVoorKlas(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(periode, doelgroep, bestand);

        // ASSERT
        try (XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(bestand))) {
            assertNotNull(workbook.getSheet("1AA"));
        }
    }

    @Test
    void genereerTemplateMaaktJuisteKolomHoofding(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(periode, doelgroep, bestand);

        // ASSERT
        try (XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(bestand))) {
            Sheet sheet = workbook.getSheet("1AA");
            Row header = sheet.getRow(0);

            assertEquals("Voornaam", header.getCell(0).getStringCellValue());
            assertEquals("Achternaam", header.getCell(1).getStringCellValue());
            assertEquals("Keuze 1", header.getCell(2).getStringCellValue());
            assertEquals("Keuze 2", header.getCell(3).getStringCellValue());
            assertEquals("Keuze 3", header.getCell(4).getStringCellValue());
        }
    }

    @Test
    void genereerTemplateSchrijftLeerlingenInJuisteKlas(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(periode, doelgroep, bestand);

        // ASSERT
        try (XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(bestand))) {
            Sheet sheet = workbook.getSheet("1AA");

            assertEquals("Jan", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Mertens", sheet.getRow(1).getCell(1).getStringCellValue());

            assertEquals("Sofie", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("VO", sheet.getRow(2).getCell(1).getStringCellValue());
        }
    }

    @Test
    void genereerTemplateBevatAlleenKlassenVanGekozenDoelgroep(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Klas kwalificatieKlas = new Klas(
                "3KA", schooljaar, 3, Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB
        );

        Leerling tom = new Leerling("Tom", "Peeters", kwalificatieKlas);

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan, sofie, tom));
        VoorkeurenExcelService service = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);

        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(periode, doelgroep, bestand);

        // ASSERT
        try (XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(bestand))) {
            assertNotNull(workbook.getSheet("1AA"));
            assertNull(workbook.getSheet("3KA"));
        }
    }

    @Test
    void genereerTemplateBeschermtIngevuldeCellenEnLaatKeuzesBewerkbaar(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(periode, doelgroep, bestand);

        // ASSERT
        try (XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(bestand))) {
            Sheet sheet = workbook.getSheet("1AA");

            assertTrue(sheet.getProtect());

            Row header = sheet.getRow(0);

            assertTrue(header.getCell(0).getCellStyle().getLocked());
            assertTrue(header.getCell(1).getCellStyle().getLocked());
            assertTrue(header.getCell(2).getCellStyle().getLocked());
            assertTrue(header.getCell(3).getCellStyle().getLocked());
            assertTrue(header.getCell(4).getCellStyle().getLocked());

            Row leerlingRij = sheet.getRow(1);

            assertTrue(leerlingRij.getCell(0).getCellStyle().getLocked());
            assertTrue(leerlingRij.getCell(1).getCellStyle().getLocked());

            assertFalse(leerlingRij.getCell(2).getCellStyle().getLocked());
            assertFalse(leerlingRij.getCell(3).getCellStyle().getLocked());
            assertFalse(leerlingRij.getCell(4).getCellStyle().getLocked());
        }
    }

    @Test
    void genereerTemplateMaaktKeuzelijstMetActieveIngerichteTalenten(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(periode, doelgroep, bestand);

        // ASSERT
        try (XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(bestand))) {
            Sheet keuzelijst = workbook.getSheet("_keuzelijst");

            assertNotNull(keuzelijst);

            assertEquals("Ingerichte talenten (actief)", keuzelijst.getRow(0).getCell(0).getStringCellValue());

            assertEquals("Voetbal observatie", keuzelijst.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Schaken observatie", keuzelijst.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Koken basis", keuzelijst.getRow(3).getCell(0).getStringCellValue());

            Name ingerichteTalentenNaam = workbook.getName("IngerichteTalenten");

            assertNotNull(ingerichteTalentenNaam);
            assertTrue(ingerichteTalentenNaam.getRefersToFormula().contains("$A$2:$A$4"));
        }
    }

    @Test
    void genereerTemplateVoegtDropdownToeAanAlleKeuzeCellen(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(periode, doelgroep, bestand);

        // ASSERT
        try (XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(bestand))) {
            Sheet sheet = workbook.getSheet("1AA");

            DataValidation janKeuze1 = zoekValidatieVoorCel(sheet, 1, 2);
            DataValidation janKeuze2 = zoekValidatieVoorCel(sheet, 1, 3);
            DataValidation janKeuze3 = zoekValidatieVoorCel(sheet, 1, 4);

            DataValidation sofieKeuze1 = zoekValidatieVoorCel(sheet, 2, 2);
            DataValidation sofieKeuze2 = zoekValidatieVoorCel(sheet, 2, 3);
            DataValidation sofieKeuze3 = zoekValidatieVoorCel(sheet, 2, 4);

            assertNotNull(janKeuze1);
            assertNotNull(janKeuze2);
            assertNotNull(janKeuze3);

            assertNotNull(sofieKeuze1);
            assertNotNull(sofieKeuze2);
            assertNotNull(sofieKeuze3);

            assertEquals("IngerichteTalenten", janKeuze1.getValidationConstraint().getFormula1());
            assertEquals("IngerichteTalenten", sofieKeuze1.getValidationConstraint().getFormula1());

            assertNotNull(janKeuze2.getValidationConstraint().getFormula1());
            assertNotNull(janKeuze3.getValidationConstraint().getFormula1());
            assertNotNull(sofieKeuze2.getValidationConstraint().getFormula1());
            assertNotNull(sofieKeuze3.getValidationConstraint().getFormula1());

            assertFalse(janKeuze2.getValidationConstraint().getFormula1().isBlank());
            assertFalse(janKeuze3.getValidationConstraint().getFormula1().isBlank());
            assertFalse(sofieKeuze2.getValidationConstraint().getFormula1().isBlank());
            assertFalse(sofieKeuze3.getValidationConstraint().getFormula1().isBlank());
        }
    }

    @Test
    void genereerTemplateMetMinderDanDrieActieveIngerichteTalentenWordtGeweigerd(@TempDir Path tempDir) {
        // ARRANGE
        InMemoryIngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(List.of(voetbalObservatie, schakenObservatie));

        VoorkeurenExcelService service = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);

        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.genereerTemplate(periode, doelgroep, bestand)
        );

        // ASSERT
        assertEquals(
                "Er moeten minstens drie actieve ingerichte talenten zijn om drie verschillende voorkeuren te kunnen invullen",
                exception.getMessage()
        );
    }

    private DataValidation zoekValidatieVoorCel(Sheet sheet, int rijIndex, int kolomIndex) {
        for (DataValidation validation : sheet.getDataValidations()) {
            for (CellRangeAddress bereik : validation.getRegions().getCellRangeAddresses()) {
                if (bereik.isInRange(rijIndex, kolomIndex)) {
                    return validation;
                }
            }
        }

        return null;
    }
}