package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
    private Klas klas;
    private VoorkeurenExcelService service;

    @BeforeEach
    void setUp() {
        schooljaar = new Schooljaar(
                "2026-2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 6, 30)
        );

        periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 12, 21),
                schooljaar
        );

        klas = new Klas(
                "1AA",
                schooljaar,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        Leerling jan = new Leerling("Jan", "Mertens", klas);
        Leerling sofie = new Leerling("Sofie", "VO", klas);

        InMemoryLeerlingRepository repository =
                new InMemoryLeerlingRepository(List.of(jan, sofie));

        service = new VoorkeurenExcelService(repository);
    }

    @Test
    void genereerTemplateMaaktExcelBestandAan(@TempDir Path tempDir) {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(
                periode,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                bestand
        );

        // ASSERT
        assertTrue(Files.exists(bestand));
    }

    @Test
    void genereerTemplateMaaktTabbladAanVoorKlas(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(
                periode,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                bestand
        );

        // ASSERT
        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(Files.newInputStream(bestand))) {

            assertNotNull(workbook.getSheet("1AA"));
        }
    }
    @Test
    void genereerTemplateMaaktJuisteKolomHoofding(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(
                periode,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                bestand
        );

        // ASSERT
        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(Files.newInputStream(bestand))) {

            var sheet = workbook.getSheet("1AA");
            var header = sheet.getRow(0);

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
        service.genereerTemplate(
                periode,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                bestand
        );

        // ASSERT
        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(Files.newInputStream(bestand))) {

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
                "3KA",
                schooljaar,
                3,
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB
        );

        Leerling tom = new Leerling(
                "Tom",
                "Peeters",
                kwalificatieKlas
        );

        InMemoryLeerlingRepository repository =
                new InMemoryLeerlingRepository(
                        List.of(
                                new Leerling("Jan", "Mertens", klas),
                                new Leerling("Sofie", "VO", klas),
                                tom
                        )
                );

        VoorkeurenExcelService service =
                new VoorkeurenExcelService(repository);

        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(
                periode,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                bestand
        );

        // ASSERT
        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(Files.newInputStream(bestand))) {

            assertNotNull(workbook.getSheet("1AA"));
            assertNull(workbook.getSheet("3KA"));
        }
    }
    @Test
    void genereerTemplateBeschermtIngevuldeCellenEnLaatKeuzesBewerkbaar(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path bestand = tempDir.resolve("voorkeuren.xlsx");

        // ACT
        service.genereerTemplate(
                periode,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                bestand
        );

        // ASSERT
        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(Files.newInputStream(bestand))) {

            var sheet = workbook.getSheet("1AA");

            // Sheet protection staat aan
            assertTrue(sheet.getProtect());

            // Header is vergrendeld
            Row header = sheet.getRow(0);

            assertTrue(header.getCell(0).getCellStyle().getLocked());
            assertTrue(header.getCell(1).getCellStyle().getLocked());
            assertTrue(header.getCell(2).getCellStyle().getLocked());
            assertTrue(header.getCell(3).getCellStyle().getLocked());
            assertTrue(header.getCell(4).getCellStyle().getLocked());

            // Eerste leerling
            Row leerlingRij = sheet.getRow(1);

            // Naam mag niet aangepast worden
            assertTrue(leerlingRij.getCell(0).getCellStyle().getLocked());
            assertTrue(leerlingRij.getCell(1).getCellStyle().getLocked());

            // Keuzes mogen wel aangepast worden
            assertFalse(leerlingRij.getCell(2).getCellStyle().getLocked());
            assertFalse(leerlingRij.getCell(3).getCellStyle().getLocked());
            assertFalse(leerlingRij.getCell(4).getCellStyle().getLocked());
        }
    }
}