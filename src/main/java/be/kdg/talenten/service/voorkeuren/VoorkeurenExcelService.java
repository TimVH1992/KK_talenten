package be.kdg.talenten.service.voorkeuren;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFPatternFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VoorkeurenExcelService {
    private static final String KEUZELIJST_SHEET = "_keuzelijst";
    private static final String KEUZELIJST_NAAM = "IngerichteTalenten";
    private static final String SHEET_WACHTWOORD = "kk-talenten";

    private static final Color PETROL_DONKER = new Color(0, 105, 119);
    private static final Color PETROL_LICHT = new Color(222, 239, 241);
    private static final Color PETROL_ZEER_LICHT = new Color(242, 249, 249);
    private static final Color NEUTRAAL_ZEER_LICHT = new Color(249, 251, 251);
    private static final Color FOUT_LICHT = new Color(252, 232, 232);

    private final LeerlingRepository leerlingRepository;
    private final IngerichtTalentRepository ingerichtTalentRepository;

    public VoorkeurenExcelService(LeerlingRepository leerlingRepository, IngerichtTalentRepository ingerichtTalentRepository) {
        if (leerlingRepository == null) {
            throw new IllegalArgumentException("De leerlingrepository mag niet null zijn");
        }
        if (ingerichtTalentRepository == null) {
            throw new IllegalArgumentException("De ingerichtTalentRepository mag niet null zijn");
        }

        this.leerlingRepository = leerlingRepository;
        this.ingerichtTalentRepository = ingerichtTalentRepository;
    }

    public void genereerTemplate(TalentenPeriode periode, Doelgroep doelgroep, Path bestand) {
        if (periode == null) {
            throw new IllegalArgumentException("De periode mag niet null zijn");
        }
        if (doelgroep == null) {
            throw new IllegalArgumentException("De doelgroep mag niet null zijn");
        }
        if (bestand == null) {
            throw new IllegalArgumentException("Het bestandspad mag niet null zijn");
        }

        List<IngerichtTalent> ingerichteTalenten = ingerichtTalentRepository.zoekActieveVoorPeriodeEnDoelgroep(periode, doelgroep);

        if (ingerichteTalenten.size() < 3) {
            throw new IllegalStateException("Er moeten minstens drie actieve ingerichte talenten zijn om drie verschillende voorkeuren te kunnen invullen");
        }

        Map<Klas, List<Leerling>> leerlingenPerKlas = verzamelLeerlingenPerKlas(periode, doelgroep);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream outputStream = Files.newOutputStream(bestand)) {
            workbook.setCellFormulaValidation(false);
            workbook.setForceFormulaRecalculation(true);

            CellStyle lockedStyle = maakLockedStyle(workbook);
            CellStyle unlockedStyle = maakUnlockedStyle(workbook);
            CellStyle headerStyle = maakHeaderStyle(workbook);

            Sheet keuzelijstSheet = maakKeuzelijst(workbook, ingerichteTalenten);

            int volgendeHulpKolom = 1;

            for (Map.Entry<Klas, List<Leerling>> entry : leerlingenPerKlas.entrySet()) {
                Klas klas = entry.getKey();
                List<Leerling> leerlingen = entry.getValue();

                Sheet sheet = workbook.createSheet(klas.getNaam());

                schrijfHeader(sheet, headerStyle);
                schrijfLeerlingen(sheet, leerlingen, lockedStyle, unlockedStyle);

                volgendeHulpKolom = voegDynamischeDropdownsToe(
                        workbook,
                        keuzelijstSheet,
                        sheet,
                        leerlingen.size(),
                        ingerichteTalenten.size(),
                        volgendeHulpKolom
                );

                voegOntbrekendeKeuzeMarkeringToe(sheet, leerlingen.size());
                stelSheetOpmaakIn(sheet);

                sheet.protectSheet(SHEET_WACHTWOORD);
            }

            keuzelijstSheet.protectSheet(SHEET_WACHTWOORD);

            workbook.write(outputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Het Excelbestand kon niet aangemaakt worden. Controleer of het bestand niet geopend is in Excel.", e);
        }
    }

    private Map<Klas, List<Leerling>> verzamelLeerlingenPerKlas(TalentenPeriode periode, Doelgroep doelgroep) {
        Map<Klas, List<Leerling>> leerlingenPerKlas = new LinkedHashMap<>();

        for (Leerling leerling : leerlingRepository.zoekVoorSchooljaar(periode.getSchooljaar())) {
            if (leerling.getKlas().getDoelgroep() != doelgroep) {
                continue;
            }

            leerlingenPerKlas.computeIfAbsent(leerling.getKlas(), klas -> new ArrayList<>()).add(leerling);
        }

        return leerlingenPerKlas;
    }

    private Sheet maakKeuzelijst(XSSFWorkbook workbook, List<IngerichtTalent> ingerichteTalenten) {
        Sheet keuzelijstSheet = workbook.createSheet(KEUZELIJST_SHEET);
        CellStyle keuzelijstHeaderStyle = maakKeuzelijstHeaderStyle(workbook);
        CellStyle keuzelijstStyle = maakKeuzelijstStyle(workbook);

        Row header = keuzelijstSheet.createRow(0);
        header.setHeightInPoints(28);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Ingerichte talenten (actief)");
        headerCell.setCellStyle(keuzelijstHeaderStyle);

        for (int i = 0; i < ingerichteTalenten.size(); i++) {
            Row row = keuzelijstSheet.createRow(i + 1);
            row.setHeightInPoints(24);

            Cell cell = row.createCell(0);
            cell.setCellValue(ingerichteTalenten.get(i).getNaam());
            cell.setCellStyle(keuzelijstStyle);
        }

        maakNamedRange(workbook, KEUZELIJST_NAAM, 0, ingerichteTalenten.size());

        keuzelijstSheet.setColumnWidth(0, 42 * 256);
        keuzelijstSheet.setDisplayGridlines(false);
        keuzelijstSheet.createFreezePane(0, 1);

        return keuzelijstSheet;
    }

    private int voegDynamischeDropdownsToe(XSSFWorkbook workbook, Sheet keuzelijstSheet, Sheet klasSheet, int aantalLeerlingen,
                                           int aantalIngerichteTalenten, int volgendeHulpKolom) {

        voegDropdownVoorBereikToe(klasSheet, 1, aantalLeerlingen, 2, KEUZELIJST_NAAM,
                "Kies je eerste voorkeur.");

        for (int rijIndex = 1; rijIndex <= aantalLeerlingen; rijIndex++) {
            int excelRij = rijIndex + 1;

            String keuze2RangeNaam = maakVeiligeRangeNaam("Keuze2", klasSheet.getSheetName(), excelRij);
            String keuze3RangeNaam = maakVeiligeRangeNaam("Keuze3", klasSheet.getSheetName(), excelRij);

            int keuze2HulpKolom = volgendeHulpKolom++;
            int keuze3HulpKolom = volgendeHulpKolom++;

            maakKeuze2Hulplijst(workbook, keuzelijstSheet, klasSheet, excelRij, aantalIngerichteTalenten, keuze2HulpKolom, keuze2RangeNaam);
            maakKeuze3Hulplijst(workbook, keuzelijstSheet, klasSheet, excelRij, aantalIngerichteTalenten, keuze3HulpKolom, keuze3RangeNaam);

            voegDropdownVoorCelToe(klasSheet, rijIndex, 3, keuze2RangeNaam,
                    "Kies eerst Keuze 1. Die keuze is hier niet meer beschikbaar.");

            voegDropdownVoorCelToe(klasSheet, rijIndex, 4, keuze3RangeNaam,
                    "Kies eerst Keuze 1 en Keuze 2. Beide keuzes zijn hier niet meer beschikbaar.");
        }

        return volgendeHulpKolom;
    }

    private void maakKeuze2Hulplijst(XSSFWorkbook workbook, Sheet keuzelijstSheet, Sheet klasSheet, int excelRij,
                                     int aantalIngerichteTalenten, int hulpKolom, String rangeNaam) {

        String sheetNaam = escapeSheetName(klasSheet.getSheetName());
        String bronBereik = "$A$2:$A$" + (aantalIngerichteTalenten + 1);

        Row formuleRij = keuzelijstSheet.getRow(1);
        Cell formuleCel = formuleRij.createCell(hulpKolom);

        String formule = "IF('" + sheetNaam + "'!$C$" + excelRij + "=\"\",\"\",FILTER(" + bronBereik
                + "," + bronBereik + "<>'" + sheetNaam + "'!$C$" + excelRij + "))";

        formuleCel.setCellFormula(formule);

        maakNamedRange(workbook, rangeNaam, hulpKolom, aantalIngerichteTalenten - 1);
        keuzelijstSheet.setColumnHidden(hulpKolom, true);
    }

    private void maakKeuze3Hulplijst(XSSFWorkbook workbook, Sheet keuzelijstSheet, Sheet klasSheet, int excelRij,
                                     int aantalIngerichteTalenten, int hulpKolom, String rangeNaam) {

        String sheetNaam = escapeSheetName(klasSheet.getSheetName());
        String bronBereik = "$A$2:$A$" + (aantalIngerichteTalenten + 1);

        Row formuleRij = keuzelijstSheet.getRow(1);
        Cell formuleCel = formuleRij.createCell(hulpKolom);

        String formule = "IF(OR('" + sheetNaam + "'!$C$" + excelRij + "=\"\",'"
                + sheetNaam + "'!$D$" + excelRij + "=\"\"),\"\",FILTER("
                + bronBereik + ",(" + bronBereik + "<>'" + sheetNaam + "'!$C$" + excelRij + ")*("
                + bronBereik + "<>'" + sheetNaam + "'!$D$" + excelRij + ")))";

        formuleCel.setCellFormula(formule);

        maakNamedRange(workbook, rangeNaam, hulpKolom, aantalIngerichteTalenten - 2);
        keuzelijstSheet.setColumnHidden(hulpKolom, true);
    }

    private void maakNamedRange(XSSFWorkbook workbook, String naam, int kolomIndex, int aantalWaarden) {
        String kolomLetter = CellReference.convertNumToColString(kolomIndex);

        Name namedRange = workbook.createName();
        namedRange.setNameName(naam);
        namedRange.setRefersToFormula(
                "'" + KEUZELIJST_SHEET + "'!$" + kolomLetter + "$2:$" + kolomLetter + "$" + (aantalWaarden + 1)
        );
    }

    private String maakVeiligeRangeNaam(String prefix, String sheetNaam, int excelRij) {
        String veiligeSheetNaam = sheetNaam.replaceAll("[^A-Za-z0-9_]", "_");
        return prefix + "_" + veiligeSheetNaam + "_" + excelRij;
    }

    private String escapeSheetName(String sheetNaam) {
        return sheetNaam.replace("'", "''");
    }

    private void schrijfHeader(Sheet sheet, CellStyle headerStyle) {
        Row header = sheet.createRow(0);
        header.setHeightInPoints(30);

        maakHeaderCell(header, 0, "Voornaam", headerStyle);
        maakHeaderCell(header, 1, "Achternaam", headerStyle);
        maakHeaderCell(header, 2, "Keuze 1", headerStyle);
        maakHeaderCell(header, 3, "Keuze 2", headerStyle);
        maakHeaderCell(header, 4, "Keuze 3", headerStyle);
    }

    private void maakHeaderCell(Row row, int kolom, String waarde, CellStyle headerStyle) {
        Cell cell = row.createCell(kolom);
        cell.setCellValue(waarde);
        cell.setCellStyle(headerStyle);
    }

    private void schrijfLeerlingen(Sheet sheet, List<Leerling> leerlingen, CellStyle lockedStyle, CellStyle unlockedStyle) {
        int huidigeRij = 1;

        for (Leerling leerling : leerlingen) {
            Row row = sheet.createRow(huidigeRij++);
            row.setHeightInPoints(25);

            Cell voornaam = row.createCell(0);
            Cell achternaam = row.createCell(1);

            voornaam.setCellValue(leerling.getVoornaam());
            achternaam.setCellValue(leerling.getAchternaam());

            voornaam.setCellStyle(lockedStyle);
            achternaam.setCellStyle(lockedStyle);

            row.createCell(2, CellType.STRING).setCellStyle(unlockedStyle);
            row.createCell(3, CellType.STRING).setCellStyle(unlockedStyle);
            row.createCell(4, CellType.STRING).setCellStyle(unlockedStyle);
        }
    }

    private void voegDropdownVoorBereikToe(Sheet sheet, int eersteRij, int laatsteRij, int kolom, String rangeNaam, String instructie) {
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(rangeNaam);
        CellRangeAddressList cellen = new CellRangeAddressList(eersteRij, laatsteRij, kolom, kolom);
        DataValidation validation = validationHelper.createValidation(constraint, cellen);

        configureerDropdown(validation, instructie);

        sheet.addValidationData(validation);
    }

    private void voegDropdownVoorCelToe(Sheet sheet, int rijIndex, int kolomIndex, String rangeNaam, String instructie) {
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(rangeNaam);
        CellRangeAddressList cel = new CellRangeAddressList(rijIndex, rijIndex, kolomIndex, kolomIndex);
        DataValidation validation = validationHelper.createValidation(constraint, cel);

        configureerDropdown(validation, instructie);

        sheet.addValidationData(validation);
    }

    private void configureerDropdown(DataValidation validation, String instructie) {
        validation.setEmptyCellAllowed(false);
        validation.setShowErrorBox(true);
        validation.setShowPromptBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);

        validation.createPromptBox("Talentenkeuze", instructie);
        validation.createErrorBox("Ongeldige keuze", "Kies een geldig ingericht talent uit de keuzelijst.");
    }

    private void voegOntbrekendeKeuzeMarkeringToe(Sheet sheet, int aantalLeerlingen) {
        SheetConditionalFormatting conditionalFormatting = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule legeKeuzeRule = conditionalFormatting.createConditionalFormattingRule(
                "AND(C2=\"\",COUNTA($C2:$E2)>0)"
        );

        XSSFPatternFormatting patternFormatting = (XSSFPatternFormatting) legeKeuzeRule.createPatternFormatting();

        patternFormatting.setFillForegroundColor(new XSSFColor(FOUT_LICHT, null));
        patternFormatting.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] bereik = {new CellRangeAddress(1, aantalLeerlingen, 2, 4)};
        conditionalFormatting.addConditionalFormatting(bereik, legeKeuzeRule);
    }

    private void stelSheetOpmaakIn(Sheet sheet) {
        sheet.setColumnWidth(0, 20 * 256);
        sheet.setColumnWidth(1, 25 * 256);
        sheet.setColumnWidth(2, 34 * 256);
        sheet.setColumnWidth(3, 34 * 256);
        sheet.setColumnWidth(4, 34 * 256);

        sheet.createFreezePane(0, 1);
        sheet.setDisplayGridlines(false);
    }

    private CellStyle maakLockedStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();

        style.setLocked(true);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(NEUTRAAL_ZEER_LICHT, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        voegRandenToe(style);

        return style;
    }

    private CellStyle maakUnlockedStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();

        style.setLocked(false);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(PETROL_ZEER_LICHT, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        voegRandenToe(style);

        return style;
    }

    private CellStyle maakHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        style.setFont(font);
        style.setLocked(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(PETROL_DONKER, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        voegRandenToe(style);

        return style;
    }

    private CellStyle maakKeuzelijstHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        style.setFont(font);
        style.setLocked(true);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(PETROL_DONKER, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        voegRandenToe(style);

        return style;
    }

    private CellStyle maakKeuzelijstStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();

        style.setLocked(true);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(PETROL_LICHT, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        voegRandenToe(style);

        return style;
    }

    private void voegRandenToe(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }
}