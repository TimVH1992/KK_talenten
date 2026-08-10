package be.kdg.talenten.tijdelijkeTestKlasses;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryIngerichtTalentRepository;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import be.kdg.talenten.service.voorkeuren.VoorkeurenExcelService;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class VoorkeurenExcelServiceManualTest {

    public static void main(String[] args) {
        Schooljaar schooljaar = new Schooljaar(
                "2026-2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 6, 30)
        );

        TalentenPeriode periode = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 12, 21),
                schooljaar
        );

        Klas klas = new Klas(
                "1AA",
                schooljaar,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        Leerling jan = new Leerling(
                "Jan",
                "Mertens",
                klas
        );

        Leerling sofie = new Leerling(
                "Sofie",
                "VO",
                klas
        );

        InMemoryLeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(jan, sofie)
                );

        // -------------------------------------------------
        // INGERICHTE TALENTEN VOOR DE DROPDOWN
        // -------------------------------------------------

        Leerkracht leerkracht = new Leerkracht(
                "Tom",
                "Peeters"
        );

        Talent voetbal = new Talent(
                "Voetbal",
                "Balsport"
        );

        Talent schaken = new Talent(
                "Schaken",
                "Strategisch denkspel"
        );

        IngerichtTalent voetbalObservatie =
                new IngerichtTalent(
                        voetbal,
                        periode,
                        "Voetbal observatie",
                        "Voetbal voor de observatiefase",
                        12,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of(leerkracht)
                );

        IngerichtTalent schakenObservatie =
                new IngerichtTalent(
                        schaken,
                        periode,
                        "Schaken observatie",
                        "Schaken voor de observatiefase",
                        10,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                        List.of(leerkracht)
                );

        InMemoryIngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        List.of(
                                voetbalObservatie,
                                schakenObservatie
                        )
                );

        VoorkeurenExcelService service =
                new VoorkeurenExcelService(
                        leerlingRepository,
                        ingerichtTalentRepository
                );

        Path bestand = Path.of(
                System.getenv("OneDrive"),
                "Bureaublad",
                "voorkeuren_test.xlsx"
        );

        service.genereerTemplate(
                periode,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                bestand
        );

        System.out.println(
                "Bestand aangemaakt: " + bestand.toAbsolutePath()
        );
    }
}