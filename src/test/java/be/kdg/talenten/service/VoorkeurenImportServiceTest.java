package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryIngerichtTalentRepository;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryVoorkeurImportProbleemRepository;
import be.kdg.talenten.repository.inmemory.InMemoryVoorkeurRepository;
import be.kdg.talenten.service.voorkeuren.VoorkeurenExcelService;
import be.kdg.talenten.service.voorkeuren.VoorkeurenImportResultaat;
import be.kdg.talenten.service.voorkeuren.VoorkeurenImportService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VoorkeurenImportServiceTest {

    @Test
    void importeerGeldigBestandSlaatDrieVoorkeurenOp(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas = new Klas("1AA", schooljaar, 1, doelgroep);
        Leerling jan = new Leerling("Jan", "Mertens", klas);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan));
        InMemoryIngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );
        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(new ArrayList<>());
        InMemoryVoorkeurImportProbleemRepository probleemRepository = new InMemoryVoorkeurImportProbleemRepository(new ArrayList<>());

        VoorkeurenExcelService excelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        VoorkeurenImportService importService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, probleemRepository);

        Path templateBestand = tempDir.resolve("voorkeuren_template.xlsx");
        Path ingevuldBestand = tempDir.resolve("voorkeuren_ingevuld.xlsx");

        excelService.genereerTemplate(periode, doelgroep, templateBestand);

        try (InputStream inputStream = Files.newInputStream(templateBestand);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = Files.newOutputStream(ingevuldBestand)) {

            Sheet sheet = workbook.getSheet("1AA");
            Row janRij = sheet.getRow(1);

            janRij.getCell(2).setCellValue("Voetbal observatie");
            janRij.getCell(3).setCellValue("Schaken observatie");
            janRij.getCell(4).setCellValue("Koken basis");

            workbook.write(outputStream);
        }

        // ACT
        importService.importeer(ingevuldBestand, periode, doelgroep);

        // ASSERT
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);

        assertEquals(3, voorkeuren.size());

        assertEquals(1, voorkeuren.get(0).getVoorkeurNummer());
        assertEquals(voetbalObservatie, voorkeuren.get(0).getIngerichtTalent());

        assertEquals(2, voorkeuren.get(1).getVoorkeurNummer());
        assertEquals(schakenObservatie, voorkeuren.get(1).getIngerichtTalent());

        assertEquals(3, voorkeuren.get(2).getVoorkeurNummer());
        assertEquals(kokenObservatie, voorkeuren.get(2).getIngerichtTalent());
    }

    @Test
    void importeerGeldigBestandSlaatVoorkeurenVanAlleLeerlingenUitKlasOp(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas = new Klas("1AA", schooljaar, 1, doelgroep);

        Leerling jan = new Leerling("Jan", "Mertens", klas);
        Leerling sofie = new Leerling("Sofie", "Peeters", klas);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan, sofie));
        InMemoryIngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );
        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(new ArrayList<>());
        InMemoryVoorkeurImportProbleemRepository probleemRepository = new InMemoryVoorkeurImportProbleemRepository(new ArrayList<>());

        VoorkeurenExcelService excelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        VoorkeurenImportService importService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, probleemRepository);

        Path templateBestand = tempDir.resolve("voorkeuren_template.xlsx");
        Path ingevuldBestand = tempDir.resolve("voorkeuren_ingevuld.xlsx");

        excelService.genereerTemplate(periode, doelgroep, templateBestand);

        try (InputStream inputStream = Files.newInputStream(templateBestand);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = Files.newOutputStream(ingevuldBestand)) {

            Sheet sheet = workbook.getSheet("1AA");

            Row janRij = sheet.getRow(1);
            janRij.getCell(2).setCellValue("Voetbal observatie");
            janRij.getCell(3).setCellValue("Schaken observatie");
            janRij.getCell(4).setCellValue("Koken basis");

            Row sofieRij = sheet.getRow(2);
            sofieRij.getCell(2).setCellValue("Schaken observatie");
            sofieRij.getCell(3).setCellValue("Koken basis");
            sofieRij.getCell(4).setCellValue("Voetbal observatie");

            workbook.write(outputStream);
        }

        // ACT
        importService.importeer(ingevuldBestand, periode, doelgroep);

        // ASSERT
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);

        assertEquals(6, voorkeuren.size());
    }

    @Test
    void importeerGeldigBestandSlaatVoorkeurenVanAlleKlastabbladenOp(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas1AA = new Klas("1AA", schooljaar, 1, doelgroep);
        Klas klas1AB = new Klas("1AB", schooljaar, 1, doelgroep);

        Leerling jan = new Leerling("Jan", "Mertens", klas1AA);
        Leerling sofie = new Leerling("Sofie", "Peeters", klas1AA);
        Leerling lucas = new Leerling("Lucas", "Janssens", klas1AB);
        Leerling emma = new Leerling("Emma", "Willems", klas1AB);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan, sofie, lucas, emma));
        InMemoryIngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );
        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(new ArrayList<>());
        InMemoryVoorkeurImportProbleemRepository probleemRepository = new InMemoryVoorkeurImportProbleemRepository(new ArrayList<>());

        VoorkeurenExcelService excelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        VoorkeurenImportService importService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, probleemRepository);

        Path templateBestand = tempDir.resolve("voorkeuren_template.xlsx");
        Path ingevuldBestand = tempDir.resolve("voorkeuren_ingevuld.xlsx");

        excelService.genereerTemplate(periode, doelgroep, templateBestand);

        try (InputStream inputStream = Files.newInputStream(templateBestand);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = Files.newOutputStream(ingevuldBestand)) {

            Sheet sheet1AA = workbook.getSheet("1AA");

            Row janRij = sheet1AA.getRow(1);
            janRij.getCell(2).setCellValue("Voetbal observatie");
            janRij.getCell(3).setCellValue("Schaken observatie");
            janRij.getCell(4).setCellValue("Koken basis");

            Row sofieRij = sheet1AA.getRow(2);
            sofieRij.getCell(2).setCellValue("Schaken observatie");
            sofieRij.getCell(3).setCellValue("Koken basis");
            sofieRij.getCell(4).setCellValue("Voetbal observatie");

            Sheet sheet1AB = workbook.getSheet("1AB");

            Row lucasRij = sheet1AB.getRow(1);
            lucasRij.getCell(2).setCellValue("Koken basis");
            lucasRij.getCell(3).setCellValue("Voetbal observatie");
            lucasRij.getCell(4).setCellValue("Schaken observatie");

            Row emmaRij = sheet1AB.getRow(2);
            emmaRij.getCell(2).setCellValue("Voetbal observatie");
            emmaRij.getCell(3).setCellValue("Koken basis");
            emmaRij.getCell(4).setCellValue("Schaken observatie");

            workbook.write(outputStream);
        }

        // ACT
        importService.importeer(ingevuldBestand, periode, doelgroep);

        // ASSERT
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);

        assertEquals(12, voorkeuren.size());
    }

    @Test
    void importeerLeerlingMetTweeKeuzesSlaatTweeVoorkeurenOp(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas = new Klas("1AA", schooljaar, 1, doelgroep);
        Leerling jan = new Leerling("Jan", "Mertens", klas);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan));
        InMemoryIngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );
        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(new ArrayList<>());
        InMemoryVoorkeurImportProbleemRepository probleemRepository = new InMemoryVoorkeurImportProbleemRepository(new ArrayList<>());

        VoorkeurenExcelService excelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        VoorkeurenImportService importService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, probleemRepository);

        Path templateBestand = tempDir.resolve("voorkeuren_template.xlsx");
        Path ingevuldBestand = tempDir.resolve("voorkeuren_ingevuld.xlsx");

        excelService.genereerTemplate(periode, doelgroep, templateBestand);

        try (InputStream inputStream = Files.newInputStream(templateBestand);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = Files.newOutputStream(ingevuldBestand)) {

            Sheet sheet = workbook.getSheet("1AA");
            Row janRij = sheet.getRow(1);

            janRij.getCell(2).setCellValue("Voetbal observatie");
            janRij.getCell(3).setCellValue("Schaken observatie");

            workbook.write(outputStream);
        }

        // ACT
        importService.importeer(ingevuldBestand, periode, doelgroep);

        // ASSERT
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);

        assertEquals(2, voorkeuren.size());

        assertEquals(1, voorkeuren.get(0).getVoorkeurNummer());
        assertEquals(voetbalObservatie, voorkeuren.get(0).getIngerichtTalent());

        assertEquals(2, voorkeuren.get(1).getVoorkeurNummer());
        assertEquals(schakenObservatie, voorkeuren.get(1).getIngerichtTalent());
    }

    @Test
    void importeerLeerlingMetDubbeleKeuzeSlaatAlleenUniekeVoorkeurenOp(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas = new Klas("1AA", schooljaar, 1, doelgroep);
        Leerling jan = new Leerling("Jan", "Mertens", klas);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan));
        InMemoryIngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );
        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(new ArrayList<>());
        InMemoryVoorkeurImportProbleemRepository probleemRepository = new InMemoryVoorkeurImportProbleemRepository(new ArrayList<>());

        VoorkeurenExcelService excelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        VoorkeurenImportService importService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, probleemRepository);

        Path templateBestand = tempDir.resolve("voorkeuren_template.xlsx");
        Path ingevuldBestand = tempDir.resolve("voorkeuren_ingevuld.xlsx");

        excelService.genereerTemplate(periode, doelgroep, templateBestand);

        try (InputStream inputStream = Files.newInputStream(templateBestand);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = Files.newOutputStream(ingevuldBestand)) {

            Sheet sheet = workbook.getSheet("1AA");
            Row janRij = sheet.getRow(1);

            janRij.getCell(2).setCellValue("Voetbal observatie");
            janRij.getCell(3).setCellValue("Schaken observatie");
            janRij.getCell(4).setCellValue("Voetbal observatie");

            workbook.write(outputStream);
        }

        // ACT
        importService.importeer(ingevuldBestand, periode, doelgroep);

        // ASSERT
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);

        assertEquals(2, voorkeuren.size());

        assertEquals(1, voorkeuren.get(0).getVoorkeurNummer());
        assertEquals(voetbalObservatie, voorkeuren.get(0).getIngerichtTalent());

        assertEquals(2, voorkeuren.get(1).getVoorkeurNummer());
        assertEquals(schakenObservatie, voorkeuren.get(1).getIngerichtTalent());
    }

    @Test
    void importeerLeerlingMetTweeKeuzesGeeftImportProbleemTerug(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas = new Klas("1AA", schooljaar, 1, doelgroep);
        Leerling jan = new Leerling("Jan", "Mertens", klas);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan));
        InMemoryIngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );
        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(new ArrayList<>());
        InMemoryVoorkeurImportProbleemRepository probleemRepository = new InMemoryVoorkeurImportProbleemRepository(new ArrayList<>());

        VoorkeurenExcelService excelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        VoorkeurenImportService importService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, probleemRepository);

        Path templateBestand = tempDir.resolve("voorkeuren_template.xlsx");
        Path ingevuldBestand = tempDir.resolve("voorkeuren_ingevuld.xlsx");

        excelService.genereerTemplate(periode, doelgroep, templateBestand);

        try (InputStream inputStream = Files.newInputStream(templateBestand);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = Files.newOutputStream(ingevuldBestand)) {

            Sheet sheet = workbook.getSheet("1AA");
            Row janRij = sheet.getRow(1);

            janRij.getCell(2).setCellValue("Voetbal observatie");
            janRij.getCell(3).setCellValue("Schaken observatie");

            workbook.write(outputStream);
        }

        // ACT
        VoorkeurenImportResultaat resultaat = importService.importeer(ingevuldBestand, periode, doelgroep);

        // ASSERT
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);

        assertEquals(2, voorkeuren.size());

        assertEquals(1, voorkeuren.get(0).getVoorkeurNummer());
        assertEquals(voetbalObservatie, voorkeuren.get(0).getIngerichtTalent());

        assertEquals(2, voorkeuren.get(1).getVoorkeurNummer());
        assertEquals(schakenObservatie, voorkeuren.get(1).getIngerichtTalent());

        assertEquals(1, resultaat.getProblemen().size());

        VoorkeurImportProbleem probleem = resultaat.getProblemen().get(0);

        assertEquals(jan, probleem.getLeerling());
        assertEquals(periode, probleem.getPeriode());
        assertEquals("Keuze 3 is niet ingevuld", probleem.getReden());
    }

    @Test
    void importeerLeerlingMetDubbeleKeuzeGeeftImportProbleemTerug(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas = new Klas("1AA", schooljaar, 1, doelgroep);
        Leerling jan = new Leerling("Jan", "Mertens", klas);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan));
        InMemoryIngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );
        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(new ArrayList<>());
        InMemoryVoorkeurImportProbleemRepository probleemRepository = new InMemoryVoorkeurImportProbleemRepository(new ArrayList<>());

        VoorkeurenExcelService excelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        VoorkeurenImportService importService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, probleemRepository);

        Path templateBestand = tempDir.resolve("voorkeuren_template.xlsx");
        Path ingevuldBestand = tempDir.resolve("voorkeuren_ingevuld.xlsx");

        excelService.genereerTemplate(periode, doelgroep, templateBestand);

        try (InputStream inputStream = Files.newInputStream(templateBestand);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = Files.newOutputStream(ingevuldBestand)) {

            Sheet sheet = workbook.getSheet("1AA");
            Row janRij = sheet.getRow(1);

            janRij.getCell(2).setCellValue("Voetbal observatie");
            janRij.getCell(3).setCellValue("Schaken observatie");
            janRij.getCell(4).setCellValue("Voetbal observatie");

            workbook.write(outputStream);
        }

        // ACT
        VoorkeurenImportResultaat resultaat = importService.importeer(ingevuldBestand, periode, doelgroep);

        // ASSERT
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);

        assertEquals(2, voorkeuren.size());

        assertEquals(1, voorkeuren.get(0).getVoorkeurNummer());
        assertEquals(voetbalObservatie, voorkeuren.get(0).getIngerichtTalent());

        assertEquals(2, voorkeuren.get(1).getVoorkeurNummer());
        assertEquals(schakenObservatie, voorkeuren.get(1).getIngerichtTalent());

        assertEquals(1, resultaat.getProblemen().size());

        VoorkeurImportProbleem probleem = resultaat.getProblemen().get(0);

        assertEquals(jan, probleem.getLeerling());
        assertEquals(periode, probleem.getPeriode());
        assertEquals("Keuze 3 is dubbel", probleem.getReden());
    }

    @Test
    void importeerLeerlingMetOnbekendeKeuzeSlaatGeldigeVoorkeurenOpEnGeeftImportProbleemTerug(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas = new Klas("1AA", schooljaar, 1, doelgroep);
        Leerling jan = new Leerling("Jan", "Mertens", klas);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan));
        InMemoryIngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );
        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(new ArrayList<>());
        InMemoryVoorkeurImportProbleemRepository probleemRepository = new InMemoryVoorkeurImportProbleemRepository(new ArrayList<>());

        VoorkeurenExcelService excelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        VoorkeurenImportService importService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, probleemRepository);

        Path templateBestand = tempDir.resolve("voorkeuren_template.xlsx");
        Path ingevuldBestand = tempDir.resolve("voorkeuren_ingevuld.xlsx");

        excelService.genereerTemplate(periode, doelgroep, templateBestand);

        try (InputStream inputStream = Files.newInputStream(templateBestand);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = Files.newOutputStream(ingevuldBestand)) {

            Sheet sheet = workbook.getSheet("1AA");
            Row janRij = sheet.getRow(1);

            janRij.getCell(2).setCellValue("Voetbal observatie");
            janRij.getCell(3).setCellValue("Basketbal observatie");
            janRij.getCell(4).setCellValue("Schaken observatie");

            workbook.write(outputStream);
        }

        // ACT
        VoorkeurenImportResultaat resultaat = importService.importeer(ingevuldBestand, periode, doelgroep);

        // ASSERT
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);

        assertEquals(2, voorkeuren.size());

        assertEquals(1, voorkeuren.get(0).getVoorkeurNummer());
        assertEquals(voetbalObservatie, voorkeuren.get(0).getIngerichtTalent());

        assertEquals(3, voorkeuren.get(1).getVoorkeurNummer());
        assertEquals(schakenObservatie, voorkeuren.get(1).getIngerichtTalent());

        assertEquals(1, resultaat.getProblemen().size());

        VoorkeurImportProbleem probleem = resultaat.getProblemen().get(0);

        assertEquals(jan, probleem.getLeerling());
        assertEquals(periode, probleem.getPeriode());
        assertEquals("Keuze 2 bevat een onbekend ingericht talent: Basketbal observatie", probleem.getReden());
    }

    @Test
    void importeerLeerlingMetOntbrekendeKeuzeSlaatImportProbleemOp(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas = new Klas("1AA", schooljaar, 1, doelgroep);
        Leerling jan = new Leerling("Jan", "Mertens", klas);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );
        IngerichtTalent kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan));
        InMemoryIngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );
        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(new ArrayList<>());
        InMemoryVoorkeurImportProbleemRepository probleemRepository = new InMemoryVoorkeurImportProbleemRepository(new ArrayList<>());

        VoorkeurenExcelService excelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        VoorkeurenImportService importService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, probleemRepository);

        Path templateBestand = tempDir.resolve("voorkeuren_template.xlsx");
        Path ingevuldBestand = tempDir.resolve("voorkeuren_ingevuld.xlsx");

        excelService.genereerTemplate(periode, doelgroep, templateBestand);

        try (InputStream inputStream = Files.newInputStream(templateBestand);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = Files.newOutputStream(ingevuldBestand)) {

            Sheet sheet = workbook.getSheet("1AA");
            Row janRij = sheet.getRow(1);

            janRij.getCell(2).setCellValue("Voetbal observatie");
            janRij.getCell(3).setCellValue("Schaken observatie");

            workbook.write(outputStream);
        }

        // ACT
        importService.importeer(ingevuldBestand, periode, doelgroep);

        // ASSERT
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);

        assertEquals(2, voorkeuren.size());

        List<VoorkeurImportProbleem> problemen = probleemRepository.zoekVoorLeerlingEnPeriode(jan, periode);

        assertEquals(1, problemen.size());
        assertEquals(jan, problemen.get(0).getLeerling());
        assertEquals(periode, problemen.get(0).getPeriode());
        assertEquals("Keuze 3 is niet ingevuld", problemen.get(0).getReden());
    }
    @Test
    void importeerBestandMetGeldigeEnOnvolledigeLeerlingVerwerktBeideLeerlingen(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode periode = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21), schooljaar);

        Doelgroep doelgroep = Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas = new Klas("1AA", schooljaar, 1, doelgroep);

        Leerling jan = new Leerling("Jan", "Mertens", klas);
        Leerling sofie = new Leerling("Sofie", "Peeters", klas);

        Leerkracht leerkracht = new Leerkracht("Tom", "Peeters");

        Talent voetbal = new Talent("Voetbal", "Balsport");
        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent voetbalObservatie = new IngerichtTalent(
                voetbal, periode, "Voetbal observatie", "Voetbal voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        IngerichtTalent schakenObservatie = new IngerichtTalent(
                schaken, periode, "Schaken observatie", "Schaken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        IngerichtTalent kokenObservatie = new IngerichtTalent(
                koken, periode, "Koken basis", "Koken voor observatie", 10, doelgroep, List.of(leerkracht)
        );

        InMemoryLeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan, sofie));

        InMemoryIngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(
                List.of(voetbalObservatie, schakenObservatie, kokenObservatie)
        );

        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(new ArrayList<>());
        InMemoryVoorkeurImportProbleemRepository probleemRepository = new InMemoryVoorkeurImportProbleemRepository(new ArrayList<>());

        VoorkeurenExcelService excelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        VoorkeurenImportService importService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, probleemRepository);

        Path templateBestand = tempDir.resolve("voorkeuren_template.xlsx");
        Path ingevuldBestand = tempDir.resolve("voorkeuren_ingevuld.xlsx");

        excelService.genereerTemplate(periode, doelgroep, templateBestand);

        try (InputStream inputStream = Files.newInputStream(templateBestand);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = Files.newOutputStream(ingevuldBestand)) {

            Sheet sheet = workbook.getSheet("1AA");

            // Jan heeft drie geldige keuzes
            Row janRij = sheet.getRow(1);
            janRij.getCell(2).setCellValue("Voetbal observatie");
            janRij.getCell(3).setCellValue("Schaken observatie");
            janRij.getCell(4).setCellValue("Koken basis");

            // Sofie heeft slechts twee keuzes
            Row sofieRij = sheet.getRow(2);
            sofieRij.getCell(2).setCellValue("Schaken observatie");
            sofieRij.getCell(3).setCellValue("Koken basis");
            // Keuze 3 blijft leeg

            workbook.write(outputStream);
        }

        // ACT
        VoorkeurenImportResultaat resultaat = importService.importeer(ingevuldBestand, periode, doelgroep);

        // ASSERT
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);

        assertEquals(5, voorkeuren.size());

        int aantalVoorkeurenJan = 0;
        int aantalVoorkeurenSofie = 0;

        for (Voorkeur voorkeur : voorkeuren) {
            if (voorkeur.getLeerling().equals(jan)) {
                aantalVoorkeurenJan++;
            }

            if (voorkeur.getLeerling().equals(sofie)) {
                aantalVoorkeurenSofie++;
            }
        }

        assertEquals(3, aantalVoorkeurenJan);
        assertEquals(2, aantalVoorkeurenSofie);

        // Alleen Sofie heeft een importprobleem
        assertEquals(1, resultaat.getProblemen().size());

        VoorkeurImportProbleem probleem = resultaat.getProblemen().get(0);

        assertEquals(sofie, probleem.getLeerling());
        assertEquals(periode, probleem.getPeriode());
        assertEquals("Keuze 3 is niet ingevuld", probleem.getReden());

        // Het probleem is ook blijvend opgeslagen
        List<VoorkeurImportProbleem> opgeslagenProblemen = probleemRepository.zoekVoorLeerlingEnPeriode(sofie, periode);

        assertEquals(1, opgeslagenProblemen.size());
        assertEquals("Keuze 3 is niet ingevuld", opgeslagenProblemen.get(0).getReden());

        // Jan heeft geen importproblemen
        assertEquals(0, probleemRepository.zoekVoorLeerlingEnPeriode(jan, periode).size());
    }


}