package be.kdg.talenten.tijdelijkeTestKlasses;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryIngerichtTalentRepository;
import be.kdg.talenten.repository.inmemory.InMemoryLeerlingRepository;
import be.kdg.talenten.service.voorkeuren.VoorkeurenExcelService;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
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

        Doelgroep doelgroep =
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        // =================================================
        // 5 KLASSEN - 5 LEERLINGEN PER KLAS
        // =================================================

        List<Klas> klassen = List.of(
                new Klas("1AA", schooljaar, 1, doelgroep),
                new Klas("1AB", schooljaar, 1, doelgroep),
                new Klas("1AC", schooljaar, 1, doelgroep),
                new Klas("1AD", schooljaar, 1, doelgroep),
                new Klas("1AE", schooljaar, 1, doelgroep)
        );

        String[] voornamen = {
                "Jan",
                "Sofie",
                "Lucas",
                "Emma",
                "Noah"
        };

        List<Leerling> leerlingen = new ArrayList<>();

        for (int klasIndex = 0; klasIndex < klassen.size(); klasIndex++) {
            Klas klas = klassen.get(klasIndex);

            for (int leerlingIndex = 0; leerlingIndex < 5; leerlingIndex++) {
                Leerling leerling = new Leerling(
                        voornamen[leerlingIndex],
                        "Test" + (klasIndex + 1) + (leerlingIndex + 1),
                        klas
                );

                leerlingen.add(leerling);
            }
        }

        InMemoryLeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(leerlingen);

        // =================================================
        // LEERKRACHTEN
        // =================================================

        Leerkracht tom = new Leerkracht(
                "Tom",
                "Peeters"
        );

        Leerkracht els = new Leerkracht(
                "Els",
                "Janssens"
        );

        Leerkracht bart = new Leerkracht(
                "Bart",
                "Mertens"
        );

        // =================================================
        // 10 TALENTEN
        // =================================================

        Talent voetbal = new Talent(
                "Voetbal",
                "Balsport"
        );

        Talent schaken = new Talent(
                "Schaken",
                "Strategisch denkspel"
        );

        Talent koken = new Talent(
                "Koken",
                "Gerechten leren bereiden"
        );

        Talent dans = new Talent(
                "Dans",
                "Dans en beweging"
        );

        Talent muziek = new Talent(
                "Muziek",
                "Muzikale vaardigheden ontwikkelen"
        );

        Talent houtbewerking = new Talent(
                "Houtbewerking",
                "Werken met hout"
        );

        Talent fotografie = new Talent(
                "Fotografie",
                "Creatief leren fotograferen"
        );

        Talent fietsen = new Talent(
                "Fietsen",
                "Fietsvaardigheid en techniek"
        );

        Talent tekenen = new Talent(
                "Tekenen",
                "Creatief tekenen"
        );

        Talent natuur = new Talent(
                "Natuur",
                "Ontdekken en werken in de natuur"
        );

        // =================================================
        // 10 INGERICHTE TALENTEN
        // =================================================

        IngerichtTalent voetbalObservatie =
                new IngerichtTalent(
                        voetbal,
                        periode,
                        "Voetbal observatie",
                        "Voetbal voor leerlingen uit de observatiefase",
                        12,
                        doelgroep,
                        List.of(tom)
                );

        IngerichtTalent schakenObservatie =
                new IngerichtTalent(
                        schaken,
                        periode,
                        "Schaken observatie",
                        "Schaken en strategisch denken",
                        10,
                        doelgroep,
                        List.of(els)
                );

        IngerichtTalent kokenObservatie =
                new IngerichtTalent(
                        koken,
                        periode,
                        "Koken basis",
                        "Eenvoudige gerechten leren bereiden",
                        8,
                        doelgroep,
                        List.of(bart)
                );

        IngerichtTalent dansObservatie =
                new IngerichtTalent(
                        dans,
                        periode,
                        "Dans en beweging",
                        "Dans, ritme en beweging",
                        12,
                        doelgroep,
                        List.of(els)
                );

        IngerichtTalent muziekObservatie =
                new IngerichtTalent(
                        muziek,
                        periode,
                        "Muziekatelier",
                        "Kennismaken met muziek en instrumenten",
                        10,
                        doelgroep,
                        List.of(tom, els)
                );

        IngerichtTalent houtbewerkingObservatie =
                new IngerichtTalent(
                        houtbewerking,
                        periode,
                        "Houtatelier",
                        "Basisvaardigheden houtbewerking",
                        8,
                        doelgroep,
                        List.of(bart)
                );

        IngerichtTalent fotografieObservatie =
                new IngerichtTalent(
                        fotografie,
                        periode,
                        "Fotografie",
                        "Creatief fotograferen met oog voor compositie",
                        10,
                        doelgroep,
                        List.of(els)
                );

        IngerichtTalent fietsenObservatie =
                new IngerichtTalent(
                        fietsen,
                        periode,
                        "Fietsatelier",
                        "Fietsvaardigheid en eenvoudige techniek",
                        12,
                        doelgroep,
                        List.of(tom)
                );

        IngerichtTalent tekenenObservatie =
                new IngerichtTalent(
                        tekenen,
                        periode,
                        "Creatief tekenen",
                        "Tekenen en creatieve technieken",
                        10,
                        doelgroep,
                        List.of(els)
                );

        IngerichtTalent natuurObservatie =
                new IngerichtTalent(
                        natuur,
                        periode,
                        "Natuuratelier",
                        "Activiteiten rond natuur en buitenleven",
                        10,
                        doelgroep,
                        List.of(bart)
                );

        InMemoryIngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        List.of(
                                voetbalObservatie,
                                schakenObservatie,
                                kokenObservatie,
                                dansObservatie,
                                muziekObservatie,
                                houtbewerkingObservatie,
                                fotografieObservatie,
                                fietsenObservatie,
                                tekenenObservatie,
                                natuurObservatie
                        )
                );

        // =================================================
        // EXCEL GENEREREN
        // =================================================

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
                doelgroep,
                bestand
        );

        System.out.println(
                "Bestand aangemaakt: " + bestand.toAbsolutePath()
        );
    }
}