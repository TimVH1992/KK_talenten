package be.kdg.talenten.service.verdeling;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.overzicht.KlasOverzicht;
import be.kdg.talenten.overzicht.LeerlingToewijzingOverzicht;
import be.kdg.talenten.service.beheer.KlasService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class VerdelingExcelService {

    private static final Color PETROL_DONKER =
            new Color(0, 105, 119);

    private static final Color PETROL_LICHT =
            new Color(222, 239, 241);

    private static final Color PETROL_ZEER_LICHT =
            new Color(242, 249, 249);

    private static final Color NEUTRAAL_ZEER_LICHT =
            new Color(249, 251, 251);

    private static final Color FOUT_LICHT =
            new Color(252, 232, 232);

    private static final DateTimeFormatter DATUM_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VerdelingBekijkenService verdelingBekijkenService;
    private final KlasService klasService;

    public VerdelingExcelService(
            VerdelingBekijkenService verdelingBekijkenService,
            KlasService klasService
    ) {
        if (verdelingBekijkenService == null) {
            throw new IllegalArgumentException(
                    "VerdelingBekijkenService mag niet null zijn"
            );
        }

        if (klasService == null) {
            throw new IllegalArgumentException(
                    "KlasService mag niet null zijn"
            );
        }

        this.verdelingBekijkenService =
                verdelingBekijkenService;

        this.klasService =
                klasService;
    }

    public void exporteerPerIngerichtTalent(
            TalentenPeriode periode,
            Path bestand
    ) {
        exporteerPerIngerichtTalent(
                periode,
                null,
                bestand
        );
    }

    public void exporteerPerIngerichtTalent(
            TalentenPeriode periode,
            Doelgroep doelgroep,
            Path bestand
    ) {
        valideerExport(
                periode,
                bestand
        );

        List<IngerichtTalentOverzicht> overzichten =
                verdelingBekijkenService
                        .bekijkPerIngerichtTalent(
                                periode,
                                doelgroep
                        );

        List<Klas> klassen =
                geefKlassenVoorPeriode(
                        periode,
                        doelgroep
                );

        Map<String, Klas> historischeKlasPerLeerling =
                bepaalHistorischeKlassen(
                        periode,
                        klassen
                );

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook();
             OutputStream outputStream =
                     Files.newOutputStream(bestand)) {

            ExcelStijlen stijlen =
                    maakStijlen(
                            workbook
                    );

            maakTalentSamenvatting(
                    workbook,
                    periode,
                    doelgroep,
                    overzichten,
                    stijlen
            );

            Set<String> gebruikteSheetNamen =
                    new HashSet<>();

            gebruikteSheetNamen.add(
                    "Overzicht"
            );

            for (IngerichtTalentOverzicht overzicht :
                    overzichten) {

                maakTalentSheet(
                        workbook,
                        periode,
                        overzicht,
                        historischeKlasPerLeerling,
                        stijlen,
                        gebruikteSheetNamen
                );
            }

            workbook.write(
                    outputStream
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Het Excelbestand kon niet aangemaakt worden. Controleer of het bestand niet geopend is in Excel.",
                    e
            );
        }
    }

    public void exporteerPerKlas(
            TalentenPeriode periode,
            Path bestand
    ) {
        exporteerPerKlas(
                periode,
                null,
                bestand
        );
    }

    public void exporteerPerKlas(
            TalentenPeriode periode,
            Doelgroep doelgroep,
            Path bestand
    ) {
        valideerExport(
                periode,
                bestand
        );

        List<Klas> klassen =
                geefKlassenVoorPeriode(
                        periode,
                        doelgroep
                );

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook();
             OutputStream outputStream =
                     Files.newOutputStream(bestand)) {

            ExcelStijlen stijlen =
                    maakStijlen(
                            workbook
                    );

            Set<String> gebruikteSheetNamen =
                    new HashSet<>();

            if (klassen.isEmpty()) {
                maakLeegSheet(
                        workbook,
                        "Geen klassen",
                        "Er zijn geen klassen gevonden voor "
                                + periode.getSchooljaar().getNaam()
                                + " binnen "
                                + formatteerDoelgroepFilter(doelgroep)
                                + ".",
                        stijlen
                );

            } else {
                for (Klas klas : klassen) {
                    KlasOverzicht overzicht =
                            verdelingBekijkenService
                                    .bekijkVoorKlas(
                                            periode,
                                            klas
                                    );

                    maakKlasSheet(
                            workbook,
                            overzicht,
                            stijlen,
                            gebruikteSheetNamen
                    );
                }
            }

            workbook.write(
                    outputStream
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Het Excelbestand kon niet aangemaakt worden. Controleer of het bestand niet geopend is in Excel.",
                    e
            );
        }
    }

    private void maakTalentSamenvatting(
            XSSFWorkbook workbook,
            TalentenPeriode periode,
            Doelgroep doelgroep,
            List<IngerichtTalentOverzicht> overzichten,
            ExcelStijlen stijlen
    ) {
        Sheet sheet =
                workbook.createSheet(
                        "Overzicht"
                );

        maakTitel(
                sheet,
                "Verdeling per ingericht talent",
                stijlen.titelStyle(),
                4
        );

        schrijfInfoRij(
                sheet,
                1,
                "Periode",
                formatteerPeriode(
                        periode
                ),
                stijlen,
                4
        );

        schrijfInfoRij(
                sheet,
                2,
                "Schooljaar",
                periode
                        .getSchooljaar()
                        .getNaam(),
                stijlen,
                4
        );

        schrijfInfoRij(
                sheet,
                3,
                "Doelgroep",
                formatteerDoelgroepFilter(
                        doelgroep
                ),
                stijlen,
                4
        );

        int headerRij =
                5;

        Row header =
                sheet.createRow(
                        headerRij
                );

        maakHeaderCell(
                header,
                0,
                "Ingericht talent",
                stijlen.headerStyle()
        );

        maakHeaderCell(
                header,
                1,
                "Capaciteit",
                stijlen.headerStyle()
        );

        maakHeaderCell(
                header,
                2,
                "Toegewezen",
                stijlen.headerStyle()
        );

        maakHeaderCell(
                header,
                3,
                "Vrije plaatsen",
                stijlen.headerStyle()
        );

        maakHeaderCell(
                header,
                4,
                "Doelgroep",
                stijlen.headerStyle()
        );

        int rijIndex =
                headerRij + 1;

        for (IngerichtTalentOverzicht overzicht :
                overzichten) {

            Row row =
                    sheet.createRow(
                            rijIndex++
                    );

            IngerichtTalent ingerichtTalent =
                    overzicht.ingerichtTalent();

            schrijfCel(
                    row,
                    0,
                    ingerichtTalent.getNaam(),
                    stijlen.normaalStyle()
            );

            schrijfGetalCel(
                    row,
                    1,
                    ingerichtTalent.getMaxCapaciteit(),
                    stijlen.normaalStyle()
            );

            schrijfGetalCel(
                    row,
                    2,
                    overzicht.aantalToegewezen(),
                    stijlen.normaalStyle()
            );

            schrijfGetalCel(
                    row,
                    3,
                    overzicht.aantalVrijePlaatsen(),
                    stijlen.normaalStyle()
            );

            schrijfCel(
                    row,
                    4,
                    formatteerDoelgroep(
                            ingerichtTalent
                                    .getDoelgroep()
                                    .name()
                    ),
                    stijlen.normaalStyle()
            );
        }

        sheet.setColumnWidth(
                0,
                34 * 256
        );

        sheet.setColumnWidth(
                1,
                14 * 256
        );

        sheet.setColumnWidth(
                2,
                14 * 256
        );

        sheet.setColumnWidth(
                3,
                16 * 256
        );

        sheet.setColumnWidth(
                4,
                38 * 256
        );

        sheet.createFreezePane(
                0,
                headerRij + 1
        );

        sheet.setDisplayGridlines(
                false
        );

        if (!overzichten.isEmpty()) {
            sheet.setAutoFilter(
                    new CellRangeAddress(
                            headerRij,
                            headerRij
                                    + overzichten.size(),
                            0,
                            4
                    )
            );
        }
    }

    private void maakTalentSheet(
            XSSFWorkbook workbook,
            TalentenPeriode periode,
            IngerichtTalentOverzicht overzicht,
            Map<String, Klas> historischeKlasPerLeerling,
            ExcelStijlen stijlen,
            Set<String> gebruikteSheetNamen
    ) {
        IngerichtTalent ingerichtTalent =
                overzicht.ingerichtTalent();

        String sheetNaam =
                maakUniekeSheetNaam(
                        ingerichtTalent.getNaam(),
                        gebruikteSheetNamen
                );

        Sheet sheet =
                workbook.createSheet(
                        sheetNaam
                );

        maakTitel(
                sheet,
                ingerichtTalent.getNaam(),
                stijlen.titelStyle(),
                2
        );

        schrijfInfoRij(
                sheet,
                1,
                "Periode",
                formatteerPeriode(
                        periode
                ),
                stijlen,
                2
        );

        schrijfInfoRij(
                sheet,
                2,
                "Capaciteit",
                ingerichtTalent.getMaxCapaciteit()
                        + " plaatsen · "
                        + overzicht.aantalToegewezen()
                        + " toegewezen · "
                        + overzicht.aantalVrijePlaatsen()
                        + " vrij",
                stijlen,
                2
        );

        int headerRij =
                4;

        Row header =
                sheet.createRow(
                        headerRij
                );

        maakHeaderCell(
                header,
                0,
                "Voornaam",
                stijlen.headerStyle()
        );

        maakHeaderCell(
                header,
                1,
                "Achternaam",
                stijlen.headerStyle()
        );

        maakHeaderCell(
                header,
                2,
                "Klas",
                stijlen.headerStyle()
        );

        List<Toewijzing> toewijzingen =
                new ArrayList<>(
                        overzicht.toewijzingen()
                );

        toewijzingen.sort(
                Comparator
                        .comparing(
                                (Toewijzing toewijzing) ->
                                        geefHistorischeKlasNaam(
                                                toewijzing
                                                        .getLeerling(),
                                                historischeKlasPerLeerling
                                        )
                        )
                        .thenComparing(
                                toewijzing ->
                                        toewijzing
                                                .getLeerling()
                                                .getAchternaam()
                        )
                        .thenComparing(
                                toewijzing ->
                                        toewijzing
                                                .getLeerling()
                                                .getVoornaam()
                        )
        );

        int rijIndex =
                headerRij + 1;

        for (Toewijzing toewijzing :
                toewijzingen) {

            Row row =
                    sheet.createRow(
                            rijIndex++
                    );

            Leerling leerling =
                    toewijzing.getLeerling();

            schrijfCel(
                    row,
                    0,
                    leerling.getVoornaam(),
                    stijlen.normaalStyle()
            );

            schrijfCel(
                    row,
                    1,
                    leerling.getAchternaam(),
                    stijlen.normaalStyle()
            );

            schrijfCel(
                    row,
                    2,
                    geefHistorischeKlasNaam(
                            leerling,
                            historischeKlasPerLeerling
                    ),
                    stijlen.normaalStyle()
            );
        }

        if (toewijzingen.isEmpty()) {
            Row legeRij =
                    sheet.createRow(
                            headerRij + 1
                    );

            Cell cel =
                    legeRij.createCell(
                            0
                    );

            cel.setCellValue(
                    "Geen leerlingen toegewezen."
            );

            cel.setCellStyle(
                    stijlen.mutedStyle()
            );

            sheet.addMergedRegion(
                    new CellRangeAddress(
                            headerRij + 1,
                            headerRij + 1,
                            0,
                            2
                    )
            );
        }

        sheet.setColumnWidth(
                0,
                22 * 256
        );

        sheet.setColumnWidth(
                1,
                28 * 256
        );

        sheet.setColumnWidth(
                2,
                16 * 256
        );

        sheet.createFreezePane(
                0,
                headerRij + 1
        );

        sheet.setDisplayGridlines(
                false
        );

        if (!toewijzingen.isEmpty()) {
            sheet.setAutoFilter(
                    new CellRangeAddress(
                            headerRij,
                            headerRij
                                    + toewijzingen.size(),
                            0,
                            2
                    )
            );
        }
    }

    private void maakKlasSheet(
            XSSFWorkbook workbook,
            KlasOverzicht overzicht,
            ExcelStijlen stijlen,
            Set<String> gebruikteSheetNamen
    ) {
        Klas klas =
                overzicht.klas();

        TalentenPeriode periode =
                overzicht.periode();

        String sheetNaam =
                maakUniekeSheetNaam(
                        klas.getNaam(),
                        gebruikteSheetNamen
                );

        Sheet sheet =
                workbook.createSheet(
                        sheetNaam
                );

        maakTitel(
                sheet,
                "Klas " + klas.getNaam(),
                stijlen.titelStyle(),
                2
        );

        schrijfInfoRij(
                sheet,
                1,
                "Periode",
                formatteerPeriode(
                        periode
                ),
                stijlen,
                2
        );

        schrijfInfoRij(
                sheet,
                2,
                "Doelgroep",
                formatteerDoelgroep(
                        klas
                                .getDoelgroep()
                                .name()
                ),
                stijlen,
                2
        );

        int headerRij =
                4;

        Row header =
                sheet.createRow(
                        headerRij
                );

        maakHeaderCell(
                header,
                0,
                "Voornaam",
                stijlen.headerStyle()
        );

        maakHeaderCell(
                header,
                1,
                "Achternaam",
                stijlen.headerStyle()
        );

        maakHeaderCell(
                header,
                2,
                "Ingericht talent",
                stijlen.headerStyle()
        );

        List<LeerlingToewijzingOverzicht> leerlingen =
                new ArrayList<>(
                        overzicht.leerlingen()
                );

        leerlingen.sort(
                Comparator
                        .comparing(
                                (LeerlingToewijzingOverzicht leerlingOverzicht) ->
                                        leerlingOverzicht
                                                .leerling()
                                                .getAchternaam()
                        )
                        .thenComparing(
                                leerlingOverzicht ->
                                        leerlingOverzicht
                                                .leerling()
                                                .getVoornaam()
                        )
        );

        int rijIndex =
                headerRij + 1;

        for (LeerlingToewijzingOverzicht leerlingOverzicht :
                leerlingen) {

            Leerling leerling =
                    leerlingOverzicht.leerling();

            Toewijzing toewijzing =
                    leerlingOverzicht.toewijzing();

            Row row =
                    sheet.createRow(
                            rijIndex++
                    );

            CellStyle rijStyle =
                    toewijzing == null
                            ? stijlen.nietToegewezenStyle()
                            : stijlen.normaalStyle();

            schrijfCel(
                    row,
                    0,
                    leerling.getVoornaam(),
                    rijStyle
            );

            schrijfCel(
                    row,
                    1,
                    leerling.getAchternaam(),
                    rijStyle
            );

            schrijfCel(
                    row,
                    2,
                    toewijzing == null
                            ? "Niet toegewezen"
                            : toewijzing
                            .getIngerichtTalent()
                            .getNaam(),
                    rijStyle
            );
        }

        if (leerlingen.isEmpty()) {
            Row legeRij =
                    sheet.createRow(
                            headerRij + 1
                    );

            Cell cel =
                    legeRij.createCell(
                            0
                    );

            cel.setCellValue(
                    "Geen leerlingen gevonden voor deze klas."
            );

            cel.setCellStyle(
                    stijlen.mutedStyle()
            );

            sheet.addMergedRegion(
                    new CellRangeAddress(
                            headerRij + 1,
                            headerRij + 1,
                            0,
                            2
                    )
            );
        }

        sheet.setColumnWidth(
                0,
                22 * 256
        );

        sheet.setColumnWidth(
                1,
                28 * 256
        );

        sheet.setColumnWidth(
                2,
                42 * 256
        );

        sheet.createFreezePane(
                0,
                headerRij + 1
        );

        sheet.setDisplayGridlines(
                false
        );

        if (!leerlingen.isEmpty()) {
            sheet.setAutoFilter(
                    new CellRangeAddress(
                            headerRij,
                            headerRij
                                    + leerlingen.size(),
                            0,
                            2
                    )
            );
        }
    }

    private List<Klas> geefKlassenVoorPeriode(
            TalentenPeriode periode,
            Doelgroep doelgroep
    ) {
        return klasService
                .geefAlleKlassen()
                .stream()
                .filter(
                        klas ->
                                klas
                                        .getSchooljaar()
                                        .equals(
                                                periode.getSchooljaar()
                                        )
                )
                .filter(
                        klas ->
                                doelgroep == null
                                        || klas.getDoelgroep()
                                        == doelgroep
                )
                .sorted(
                        Comparator.comparing(
                                Klas::getNaam
                        )
                )
                .toList();
    }

    private Map<String, Klas> bepaalHistorischeKlassen(
            TalentenPeriode periode,
            List<Klas> klassen
    ) {
        Map<String, Klas> klasPerLeerling =
                new HashMap<>();

        for (Klas klas : klassen) {
            KlasOverzicht overzicht =
                    verdelingBekijkenService
                            .bekijkVoorKlas(
                                    periode,
                                    klas
                            );

            for (LeerlingToewijzingOverzicht leerlingOverzicht :
                    overzicht.leerlingen()) {

                klasPerLeerling.put(
                        maakLeerlingSleutel(
                                leerlingOverzicht.leerling()
                        ),
                        klas
                );
            }
        }

        return klasPerLeerling;
    }

    private String geefHistorischeKlasNaam(
            Leerling leerling,
            Map<String, Klas> historischeKlasPerLeerling
    ) {
        Klas klas =
                historischeKlasPerLeerling.get(
                        maakLeerlingSleutel(
                                leerling
                        )
                );

        return klas == null
                ? "—"
                : klas.getNaam();
    }

    private String maakLeerlingSleutel(
            Leerling leerling
    ) {
        if (leerling.getId() != null) {
            return "ID:"
                    + leerling.getId();
        }

        return "NAAM:"
                + leerling.getVoornaam()
                + "|"
                + leerling.getAchternaam();
    }

    private void valideerExport(
            TalentenPeriode periode,
            Path bestand
    ) {
        if (periode == null) {
            throw new IllegalArgumentException(
                    "Talentenperiode mag niet null zijn"
            );
        }

        if (bestand == null) {
            throw new IllegalArgumentException(
                    "Bestandspad mag niet null zijn"
            );
        }
    }

    private String formatteerPeriode(
            TalentenPeriode periode
    ) {
        return periode.getNaam()
                + " · "
                + periode
                .getStartDatum()
                .format(
                        DATUM_FORMATTER
                )
                + " - "
                + periode
                .getEindDatum()
                .format(
                        DATUM_FORMATTER
                );
    }

    private String formatteerDoelgroepFilter(
            Doelgroep doelgroep
    ) {
        if (doelgroep == null) {
            return "Alle doelgroepen";
        }

        return formatteerDoelgroep(
                doelgroep.name()
        );
    }

    private String formatteerDoelgroep(
            String doelgroep
    ) {
        return switch (doelgroep) {
            case "OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB" ->
                    "Observatie / opleidingsfase / 1e graad A-B";

            case "KWALIFICATIEFASE_TWEEDEGRAAD_AB" ->
                    "Kwalificatiefase / 2e graad A-B";

            default ->
                    doelgroep;
        };
    }

    private void maakTitel(
            Sheet sheet,
            String titel,
            CellStyle titelStyle,
            int laatsteKolom
    ) {
        Row row =
                sheet.createRow(
                        0
                );

        row.setHeightInPoints(
                32
        );

        Cell cell =
                row.createCell(
                        0
                );

        cell.setCellValue(
                titel
        );

        cell.setCellStyle(
                titelStyle
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        0,
                        0,
                        0,
                        laatsteKolom
                )
        );

        for (int kolom = 1;
             kolom <= laatsteKolom;
             kolom++) {

            Cell extraCell =
                    row.createCell(
                            kolom
                    );

            extraCell.setCellStyle(
                    titelStyle
            );
        }
    }

    private void schrijfInfoRij(
            Sheet sheet,
            int rijIndex,
            String label,
            String waarde,
            ExcelStijlen stijlen,
            int laatsteKolom
    ) {
        Row row =
                sheet.createRow(
                        rijIndex
                );

        Cell labelCell =
                row.createCell(
                        0
                );

        labelCell.setCellValue(
                label
        );

        labelCell.setCellStyle(
                stijlen.infoLabelStyle()
        );

        Cell waardeCell =
                row.createCell(
                        1
                );

        waardeCell.setCellValue(
                waarde
        );

        waardeCell.setCellStyle(
                stijlen.infoWaardeStyle()
        );

        if (laatsteKolom > 1) {
            sheet.addMergedRegion(
                    new CellRangeAddress(
                            rijIndex,
                            rijIndex,
                            1,
                            laatsteKolom
                    )
            );

            for (int kolom = 2;
                 kolom <= laatsteKolom;
                 kolom++) {

                Cell extraCell =
                        row.createCell(
                                kolom
                        );

                extraCell.setCellStyle(
                        stijlen.infoWaardeStyle()
                );
            }
        }
    }

    private void maakHeaderCell(
            Row row,
            int kolom,
            String waarde,
            CellStyle style
    ) {
        Cell cell =
                row.createCell(
                        kolom
                );

        cell.setCellValue(
                waarde
        );

        cell.setCellStyle(
                style
        );
    }

    private void schrijfCel(
            Row row,
            int kolom,
            String waarde,
            CellStyle style
    ) {
        Cell cell =
                row.createCell(
                        kolom
                );

        cell.setCellValue(
                waarde == null
                        ? ""
                        : waarde
        );

        cell.setCellStyle(
                style
        );
    }

    private void schrijfGetalCel(
            Row row,
            int kolom,
            int waarde,
            CellStyle style
    ) {
        Cell cell =
                row.createCell(
                        kolom
                );

        cell.setCellValue(
                waarde
        );

        cell.setCellStyle(
                style
        );
    }

    private void maakLeegSheet(
            XSSFWorkbook workbook,
            String naam,
            String boodschap,
            ExcelStijlen stijlen
    ) {
        Sheet sheet =
                workbook.createSheet(
                        naam
                );

        maakTitel(
                sheet,
                naam,
                stijlen.titelStyle(),
                2
        );

        Row row =
                sheet.createRow(
                        2
                );

        Cell cell =
                row.createCell(
                        0
                );

        cell.setCellValue(
                boodschap
        );

        cell.setCellStyle(
                stijlen.mutedStyle()
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        2,
                        2,
                        0,
                        2
                )
        );

        sheet.setDisplayGridlines(
                false
        );
    }

    private String maakUniekeSheetNaam(
            String gewensteNaam,
            Set<String> gebruikteNamen
    ) {
        String basisNaam =
                WorkbookUtil.createSafeSheetName(
                        gewensteNaam
                );

        if (basisNaam == null
                || basisNaam.isBlank()) {
            basisNaam =
                    "Overzicht";
        }

        if (!gebruikteNamen.contains(
                basisNaam
        )) {
            gebruikteNamen.add(
                    basisNaam
            );

            return basisNaam;
        }

        int nummer =
                2;

        while (true) {
            String suffix =
                    " (" + nummer + ")";

            int maximaleBasisLengte =
                    31
                            - suffix.length();

            String ingekorteBasis =
                    basisNaam.length()
                            > maximaleBasisLengte
                            ? basisNaam.substring(
                            0,
                            maximaleBasisLengte
                    )
                            : basisNaam;

            String kandidaat =
                    ingekorteBasis
                            + suffix;

            if (!gebruikteNamen.contains(
                    kandidaat
            )) {
                gebruikteNamen.add(
                        kandidaat
                );

                return kandidaat;
            }

            nummer++;
        }
    }

    private ExcelStijlen maakStijlen(
            XSSFWorkbook workbook
    ) {
        return new ExcelStijlen(
                maakTitelStyle(workbook),
                maakHeaderStyle(workbook),
                maakNormaalStyle(workbook),
                maakInfoLabelStyle(workbook),
                maakInfoWaardeStyle(workbook),
                maakMutedStyle(workbook),
                maakNietToegewezenStyle(workbook)
        );
    }

    private CellStyle maakTitelStyle(
            XSSFWorkbook workbook
    ) {
        XSSFCellStyle style =
                workbook.createCellStyle();

        Font font =
                workbook.createFont();

        font.setBold(
                true
        );

        font.setFontHeightInPoints(
                (short) 16
        );

        font.setColor(
                IndexedColors.WHITE.getIndex()
        );

        style.setFont(
                font
        );

        style.setAlignment(
                HorizontalAlignment.LEFT
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setFillForegroundColor(
                new XSSFColor(
                        PETROL_DONKER,
                        null
                )
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        return style;
    }

    private CellStyle maakHeaderStyle(
            XSSFWorkbook workbook
    ) {
        XSSFCellStyle style =
                workbook.createCellStyle();

        Font font =
                workbook.createFont();

        font.setBold(
                true
        );

        font.setColor(
                IndexedColors.WHITE.getIndex()
        );

        style.setFont(
                font
        );

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setFillForegroundColor(
                new XSSFColor(
                        PETROL_DONKER,
                        null
                )
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        voegRandenToe(
                style
        );

        return style;
    }

    private CellStyle maakNormaalStyle(
            XSSFWorkbook workbook
    ) {
        XSSFCellStyle style =
                workbook.createCellStyle();

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setFillForegroundColor(
                new XSSFColor(
                        NEUTRAAL_ZEER_LICHT,
                        null
                )
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        voegRandenToe(
                style
        );

        return style;
    }

    private CellStyle maakInfoLabelStyle(
            XSSFWorkbook workbook
    ) {
        XSSFCellStyle style =
                workbook.createCellStyle();

        Font font =
                workbook.createFont();

        font.setBold(
                true
        );

        style.setFont(
                font
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setFillForegroundColor(
                new XSSFColor(
                        PETROL_LICHT,
                        null
                )
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        voegRandenToe(
                style
        );

        return style;
    }

    private CellStyle maakInfoWaardeStyle(
            XSSFWorkbook workbook
    ) {
        XSSFCellStyle style =
                workbook.createCellStyle();

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setFillForegroundColor(
                new XSSFColor(
                        PETROL_ZEER_LICHT,
                        null
                )
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        voegRandenToe(
                style
        );

        return style;
    }

    private CellStyle maakMutedStyle(
            XSSFWorkbook workbook
    ) {
        XSSFCellStyle style =
                workbook.createCellStyle();

        Font font =
                workbook.createFont();

        font.setItalic(
                true
        );

        font.setColor(
                IndexedColors.GREY_50_PERCENT.getIndex()
        );

        style.setFont(
                font
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setFillForegroundColor(
                new XSSFColor(
                        NEUTRAAL_ZEER_LICHT,
                        null
                )
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        return style;
    }

    private CellStyle maakNietToegewezenStyle(
            XSSFWorkbook workbook
    ) {
        XSSFCellStyle style =
                workbook.createCellStyle();

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setFillForegroundColor(
                new XSSFColor(
                        FOUT_LICHT,
                        null
                )
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        voegRandenToe(
                style
        );

        return style;
    }

    private void voegRandenToe(
            CellStyle style
    ) {
        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );

        style.setTopBorderColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );

        style.setBottomBorderColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );

        style.setLeftBorderColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );

        style.setRightBorderColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );
    }

    private record ExcelStijlen(
            CellStyle titelStyle,
            CellStyle headerStyle,
            CellStyle normaalStyle,
            CellStyle infoLabelStyle,
            CellStyle infoWaardeStyle,
            CellStyle mutedStyle,
            CellStyle nietToegewezenStyle
    ) {
    }
}