package be.kdg.talenten.tijdelijkeTestKlasses;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import be.kdg.talenten.service.VoorkeurenExcelService;

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

        Leerling jan = new Leerling("Jan", "Mertens", klas);
        Leerling sofie = new Leerling("Sofie", "VO", klas);

        InMemoryLeerlingRepository repository =
                new InMemoryLeerlingRepository(List.of(jan, sofie));

        VoorkeurenExcelService service =
                new VoorkeurenExcelService(repository);

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

        service.genereerTemplate(
                periode,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                bestand
        );

        System.out.println("Bestand aangemaakt: " + bestand.toAbsolutePath());
    }
}
