package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.LeerlingRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VoorkeurenExcelService {
    private final LeerlingRepository leerlingRepository;

    public VoorkeurenExcelService(LeerlingRepository leerlingRepository){
        if (leerlingRepository == null){
            throw new IllegalArgumentException("De leerlingrepository mag niet null zijn");
        }
        this.leerlingRepository = leerlingRepository;
    }

    public void genereerTemplate(TalentenPeriode periode, Doelgroep doelgroep, Path bestand) {
        Map<Klas, List<Leerling>> leerlingenPerKlasMap= new LinkedHashMap<>();

        for (Leerling leerling : leerlingRepository.zoekVoorSchooljaar(periode.getSchooljaar())) {
            if (leerling.getKlas().getDoelgroep() != doelgroep) {
                continue;
            }

            leerlingenPerKlasMap
                    .computeIfAbsent(leerling.getKlas(), klas -> new ArrayList<>())
                    .add(leerling);
        }

            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 OutputStream outputStream = Files.newOutputStream(bestand)) {
                CellStyle lockedStyle = workbook.createCellStyle();
                lockedStyle.setLocked(true);

                CellStyle unlockedStyle = workbook.createCellStyle();
                unlockedStyle.setLocked(false);

                for (Map.Entry<Klas, List<Leerling>> entry : leerlingenPerKlasMap.entrySet()) {
                    Klas huidigeKlas = entry.getKey();
                    List<Leerling> huidigeLeerlingen = entry.getValue();
                    int huidigeSchrijfRij = 0;

                    Sheet sheet = workbook.createSheet(huidigeKlas.getNaam());
                    Row row0 = sheet.createRow(huidigeSchrijfRij++);
                    Cell voornaamCell = row0.createCell(0);
                    Cell achternaamCell = row0.createCell(1);
                    Cell eersteKeuzeCell = row0.createCell(2);
                    Cell tweedeKeuzeCell = row0.createCell(3);
                    Cell derdeKeuzeCell = row0.createCell(4);

                    voornaamCell.setCellValue("Voornaam");
                    achternaamCell.setCellValue("Achternaam");
                    eersteKeuzeCell.setCellValue("Keuze 1");
                    tweedeKeuzeCell.setCellValue("Keuze 2");
                    derdeKeuzeCell.setCellValue("Keuze 3");

                    voornaamCell.setCellStyle(lockedStyle);
                    achternaamCell.setCellStyle(lockedStyle);
                    eersteKeuzeCell.setCellStyle(lockedStyle);
                    tweedeKeuzeCell.setCellStyle(lockedStyle);
                    derdeKeuzeCell.setCellStyle(lockedStyle);

                    for (Leerling leerling : huidigeLeerlingen){
                        Row row = sheet.createRow(huidigeSchrijfRij++);
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
                    sheet.protectSheet("kk-talenten");
                }
                workbook.write(outputStream);

            } catch (IOException e) {
                throw new IllegalStateException("Het Excelbestand kon niet aangemaakt worden", e);
            }
        }
    }
