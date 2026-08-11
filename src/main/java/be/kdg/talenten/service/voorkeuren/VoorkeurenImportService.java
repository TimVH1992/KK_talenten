package be.kdg.talenten.service.voorkeuren;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.VoorkeurImportProbleemRepository;
import be.kdg.talenten.repository.VoorkeurRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class VoorkeurenImportService {
    private final LeerlingRepository leerlingRepository;
    private final IngerichtTalentRepository ingerichtTalentRepository;
    private final VoorkeurRepository voorkeurRepository;
    private final VoorkeurImportProbleemRepository voorkeurImportProbleemRepository;

    public VoorkeurenImportService(LeerlingRepository leerlingRepository, IngerichtTalentRepository ingerichtTalentRepository, VoorkeurRepository voorkeurRepository, VoorkeurImportProbleemRepository voorkeurImportProbleemRepository) {
        if (leerlingRepository == null) {
            throw new IllegalArgumentException("De leerlingrepository mag niet null zijn");
        }
        if (ingerichtTalentRepository == null) {
            throw new IllegalArgumentException("De ingerichtTalentRepository mag niet null zijn");
        }
        if (voorkeurRepository == null) {
            throw new IllegalArgumentException("De voorkeurRepository mag niet null zijn");
        }
        if (voorkeurImportProbleemRepository == null) {
            throw new IllegalArgumentException("De voorkeurImportProbleemRepository mag niet null zijn");
        }

        this.leerlingRepository = leerlingRepository;
        this.ingerichtTalentRepository = ingerichtTalentRepository;
        this.voorkeurRepository = voorkeurRepository;
        this.voorkeurImportProbleemRepository = voorkeurImportProbleemRepository;
    }

    public VoorkeurenImportResultaat importeer(Path ingevuldBestand, TalentenPeriode periode, Doelgroep doelgroep) {
        List<VoorkeurImportProbleem> problemen = new ArrayList<>();
        try {
            List<IngerichtTalent> ingerichteTalenten = ingerichtTalentRepository.zoekActieveVoorPeriodeEnDoelgroep(periode, doelgroep);
            if (ingerichteTalenten.isEmpty()) {
                throw new IllegalStateException("Er zijn geen actieve ingerichte talenten voor deze periode en doelgroep");
            }
            List<Leerling> leerlingen = leerlingRepository.zoekVoorSchooljaar(periode.getSchooljaar());


            try (InputStream inputStream = Files.newInputStream(ingevuldBestand);
                 XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

                for (Sheet sheet : workbook) {
                    if (sheet.getSheetName().equals("_keuzelijst")) {
                        continue;
                    }

                    for (int rijIndex = 1; rijIndex <= sheet.getLastRowNum(); rijIndex++) {
                        Row row = sheet.getRow(rijIndex);
                        String keuze1 = row.getCell(2).getStringCellValue();
                        String keuze2 = row.getCell(3).getStringCellValue();
                        String keuze3 = row.getCell(4).getStringCellValue();

                        String klasNaam = sheet.getSheetName();
                        String voornaam = row.getCell(0).getStringCellValue();
                        String achternaam = row.getCell(1).getStringCellValue();

                        Leerling huidigeLeerling = zoekLeerling(leerlingen, voornaam, achternaam, klasNaam);
                        voorkeurRepository.verwijderVoorLeerlingEnPeriode(huidigeLeerling, periode);
                        voorkeurImportProbleemRepository.verwijderVoorLeerlingEnPeriode(huidigeLeerling, periode);

                        String[] keuzes = {keuze1, keuze2, keuze3};
                        List<String> reedsGebruikteKeuzes = new ArrayList<>();

                        for (int i = 0; i < keuzes.length; i++) {
                            String keuze = keuzes[i];

                            if (keuze == null || keuze.isBlank()) {
                                registreerProbleem(problemen, huidigeLeerling, periode, "Keuze " + (i + 1) + " is niet ingevuld");
                                continue;
                            }

                            if (reedsGebruikteKeuzes.contains(keuze)) {
                                registreerProbleem(problemen, huidigeLeerling, periode, "Keuze " + (i + 1) + " is dubbel");
                                continue;
                            }

                            IngerichtTalent ingerichtTalent = zoekIngerichtTalentOpNaam(ingerichteTalenten, keuze);

                            if (ingerichtTalent == null) {
                                registreerProbleem(problemen, huidigeLeerling, periode, "Keuze " + (i + 1) + " bevat een onbekend ingericht talent: " + keuze);
                                continue;
                            }

                            Voorkeur voorkeur = new Voorkeur(huidigeLeerling, periode, ingerichtTalent, i + 1);
                            voorkeurRepository.save(voorkeur);
                            reedsGebruikteKeuzes.add(keuze);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Het voorkeurenbestand kon niet gelezen worden.", e);
        }
        return new VoorkeurenImportResultaat(problemen);
    }

    private Leerling zoekLeerling(List<Leerling> leerlingen, String voornaam, String achternaam, String klasNaam) {
        for (Leerling leerling : leerlingen) {
            if (leerling.getVoornaam().equals(voornaam)
                    && leerling.getAchternaam().equals(achternaam)
                    && leerling.getKlas().getNaam().equals(klasNaam)) {
                return leerling;
            }
        }

        throw new IllegalStateException(
                "Leerling " + voornaam + " " + achternaam + " uit klas " + klasNaam + " werd niet gevonden"
        );
    }

    private IngerichtTalent zoekIngerichtTalentOpNaam(List<IngerichtTalent> ingerichteTalenten, String naam) {
        for (IngerichtTalent ingerichtTalent : ingerichteTalenten) {
            if (ingerichtTalent.getNaam().equals(naam)) {
                return ingerichtTalent;
            }
        }
        return null;
    }
    private void registreerProbleem(List<VoorkeurImportProbleem> problemen, Leerling leerling, TalentenPeriode periode, String reden) {
        VoorkeurImportProbleem probleem = new VoorkeurImportProbleem(leerling, periode, reden);
        problemen.add(probleem);
        voorkeurImportProbleemRepository.save(probleem);
    }
}
