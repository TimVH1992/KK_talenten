package be.kdg.talenten.service.voorkeuren;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VoorkeurenExcelService {
    private final LeerlingRepository leerlingRepository;
    private final IngerichtTalentRepository ingerichtTalentRepository;

    public VoorkeurenExcelService(
            LeerlingRepository leerlingRepository,
            IngerichtTalentRepository ingerichtTalentRepository
    ) {
        if (leerlingRepository == null) {
            throw new IllegalArgumentException(
                    "De leerlingrepository mag niet null zijn"
            );
        }

        if (ingerichtTalentRepository == null) {
            throw new IllegalArgumentException(
                    "De ingerichtTalentRepository mag niet null zijn"
            );
        }

        this.leerlingRepository = leerlingRepository;
        this.ingerichtTalentRepository = ingerichtTalentRepository;
    }

    public void genereerTemplate(
            TalentenPeriode periode,
            Doelgroep doelgroep,
            Path bestand
    ) {
        if (periode == null) {
            throw new IllegalArgumentException(
                    "De periode mag niet null zijn"
            );
        }

        if (doelgroep == null) {
            throw new IllegalArgumentException(
                    "De doelgroep mag niet null zijn"
            );
        }

        if (bestand == null) {
            throw new IllegalArgumentException(
                    "Het bestandspad mag niet null zijn"
            );
        }

        List<IngerichtTalent> ingerichteTalenten =
                ingerichtTalentRepository
                        .zoekActieveVoorPeriodeEnDoelgroep(
                                periode,
                                doelgroep
                        );

        if (ingerichteTalenten.isEmpty()) {
            throw new IllegalStateException(
                    "Er zijn geen actieve ingerichte talenten voor deze periode en doelgroep"
            );
        }

        Map<Klas, List<Leerling>> leerlingenPerKlas =
                verzamelLeerlingenPerKlas(
                        periode,
                        doelgroep
                );

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             OutputStream outputStream = Files.newOutputStream(bestand)) {

            CellStyle lockedStyle =
                    maakLockedStyle(workbook);

            CellStyle unlockedStyle =
                    maakUnlockedStyle(workbook);

            CellStyle headerStyle =
                    maakHeaderStyle(workbook);

            maakKeuzelijst(
                    workbook,
                    ingerichteTalenten
            );

            for (Map.Entry<Klas, List<Leerling>> entry :
                    leerlingenPerKlas.entrySet()) {

                Klas klas = entry.getKey();
                List<Leerling> leerlingen = entry.getValue();

                Sheet sheet =
                        workbook.createSheet(
                                klas.getNaam()
                        );

                schrijfHeader(
                        sheet,
                        headerStyle
                );

                schrijfLeerlingen(
                        sheet,
                        leerlingen,
                        lockedStyle,
                        unlockedStyle
                );

                voegDropdownToe(
                        sheet,
                        leerlingen.size()
                );

                stelSheetOpmaakIn(sheet);

                sheet.protectSheet("kk-talenten");
            }

            verbergKeuzelijst(workbook);

            workbook.write(outputStream);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Het Excelbestand kon niet aangemaakt worden. "
                            + "Controleer of het bestand niet geopend is in Excel.",
                    e
            );
        }
    }

    private Map<Klas, List<Leerling>> verzamelLeerlingenPerKlas(
            TalentenPeriode periode,
            Doelgroep doelgroep
    ) {
        Map<Klas, List<Leerling>> leerlingenPerKlas =
                new LinkedHashMap<>();

        for (Leerling leerling : leerlingRepository.zoekVoorSchooljaar(periode.getSchooljaar()
                )) {

            if (leerling.getKlas().getDoelgroep() != doelgroep) {
                continue;
            }

            leerlingenPerKlas
                    .computeIfAbsent(
                            leerling.getKlas(),
                            klas -> new ArrayList<>()
                    )
                    .add(leerling);
        }

        return leerlingenPerKlas;
    }

    private void maakKeuzelijst(
            XSSFWorkbook workbook,
            List<IngerichtTalent> ingerichteTalenten
    ) {
        Sheet keuzelijstSheet =
                workbook.createSheet("_keuzelijst");

        for (int i = 0; i < ingerichteTalenten.size(); i++) {
            Row row =
                    keuzelijstSheet.createRow(i);

            row.createCell(0).setCellValue(
                    ingerichteTalenten.get(i).getNaam()
            );
        }

        Name keuzelijstNaam =
                workbook.createName();

        keuzelijstNaam.setNameName(
                "IngerichteTalenten"
        );

        keuzelijstNaam.setRefersToFormula(
                "'_keuzelijst'!$A$1:$A$"
                        + ingerichteTalenten.size()
        );
    }

    private void schrijfHeader(
            Sheet sheet,
            CellStyle headerStyle
    ) {
        Row header = sheet.createRow(0);

        Cell voornaamCell =
                header.createCell(0);

        Cell achternaamCell =
                header.createCell(1);

        Cell eersteKeuzeCell =
                header.createCell(2);

        Cell tweedeKeuzeCell =
                header.createCell(3);

        Cell derdeKeuzeCell =
                header.createCell(4);

        voornaamCell.setCellValue("Voornaam");
        achternaamCell.setCellValue("Achternaam");
        eersteKeuzeCell.setCellValue("Keuze 1");
        tweedeKeuzeCell.setCellValue("Keuze 2");
        derdeKeuzeCell.setCellValue("Keuze 3");

        voornaamCell.setCellStyle(headerStyle);
        achternaamCell.setCellStyle(headerStyle);
        eersteKeuzeCell.setCellStyle(headerStyle);
        tweedeKeuzeCell.setCellStyle(headerStyle);
        derdeKeuzeCell.setCellStyle(headerStyle);
    }

    private void schrijfLeerlingen(
            Sheet sheet,
            List<Leerling> leerlingen,
            CellStyle lockedStyle,
            CellStyle unlockedStyle
    ) {
        int huidigeRij = 1;

        for (Leerling leerling : leerlingen) {
            Row row =
                    sheet.createRow(
                            huidigeRij++
                    );

            Cell voornaam =
                    row.createCell(0);

            Cell achternaam =
                    row.createCell(1);

            voornaam.setCellValue(
                    leerling.getVoornaam()
            );

            achternaam.setCellValue(
                    leerling.getAchternaam()
            );

            voornaam.setCellStyle(
                    lockedStyle
            );

            achternaam.setCellStyle(
                    lockedStyle
            );

            row.createCell(
                    2,
                    CellType.STRING
            ).setCellStyle(unlockedStyle);

            row.createCell(
                    3,
                    CellType.STRING
            ).setCellStyle(unlockedStyle);

            row.createCell(
                    4,
                    CellType.STRING
            ).setCellStyle(unlockedStyle);
        }
    }

    private void voegDropdownToe(
            Sheet sheet,
            int aantalLeerlingen
    ) {
        DataValidationHelper validationHelper =
                sheet.getDataValidationHelper();

        DataValidationConstraint constraint =
                validationHelper
                        .createFormulaListConstraint(
                                "IngerichteTalenten"
                        );

        CellRangeAddressList keuzeCellen =
                new CellRangeAddressList(
                        1,
                        aantalLeerlingen,
                        2,
                        4
                );

        DataValidation validation =
                validationHelper.createValidation(
                        constraint,
                        keuzeCellen
                );

        validation.setShowErrorBox(true);

        validation.createErrorBox(
                "Ongeldige keuze",
                "Kies een ingericht talent uit de keuzelijst."
        );

        sheet.addValidationData(validation);
    }

    private void stelSheetOpmaakIn(
            Sheet sheet
    ) {
        sheet.setColumnWidth(
                0,
                20 * 256
        );

        sheet.setColumnWidth(
                1,
                25 * 256
        );

        sheet.setColumnWidth(
                2,
                30 * 256
        );

        sheet.setColumnWidth(
                3,
                30 * 256
        );

        sheet.setColumnWidth(
                4,
                30 * 256
        );

        sheet.createFreezePane(
                0,
                1
        );
    }

    private void verbergKeuzelijst(
            XSSFWorkbook workbook
    ) {
        int keuzelijstIndex =
                workbook.getSheetIndex(
                        "_keuzelijst"
                );

        workbook.setSheetHidden(
                keuzelijstIndex,
                true
        );
    }

    private CellStyle maakLockedStyle(
            XSSFWorkbook workbook
    ) {
        CellStyle style =
                workbook.createCellStyle();

        style.setLocked(true);

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        return style;
    }

    private CellStyle maakUnlockedStyle(
            XSSFWorkbook workbook
    ) {
        CellStyle style =
                workbook.createCellStyle();

        style.setLocked(false);

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        return style;
    }

    private CellStyle maakHeaderStyle(
            XSSFWorkbook workbook
    ) {
        CellStyle style =
                workbook.createCellStyle();

        Font font =
                workbook.createFont();

        font.setBold(true);

        style.setFont(font);
        style.setLocked(true);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        return style;
    }
}