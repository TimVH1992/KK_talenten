package be.kdg.talenten;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.InMemoryToewijzingRepository;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.service.AutomatischeVerdelingService;
import be.kdg.talenten.service.ManueleToewijzingService;
import be.kdg.talenten.verdeling.AutomatischeVerdeler;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManueleToewijzingServiceTest {
    @Test
    public void manueleToewijzingNaarAnderTalent(){
//        ARRANGE
        Leerling jan = new Leerling("Jan", "Smidts");
        Leerling tim = new Leerling("Tim", "VH");

        Talent schaken = new Talent("Schaken", "Strategisch denkspel");
        Talent voetbal = new Talent("Voetbal", "balsport");

        TalentenPeriode herfst = new TalentenPeriode("herfst", LocalDate.of(2026,9,21), LocalDate.of(2026,12,21));
        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 5);
        IngerichtTalent voetbalHerfst = new IngerichtTalent(voetbal, herfst, 10);

        List<Voorkeur> voorkeuren = new ArrayList<>();

        voorkeuren.add(new Voorkeur(jan, herfst, schakenHerfst, 1));
        voorkeuren.add(new Voorkeur(tim, herfst, schakenHerfst, 1));

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren);
        VerdelingsResultaat resultaat = verdeler.verdeel();

        InMemoryToewijzingRepository repository = new InMemoryToewijzingRepository(resultaat.getToewijzingen());


        ManueleToewijzingService manueleToewijzingService = new ManueleToewijzingService(repository);

//        ACT
        manueleToewijzingService.wijzigToewijzing(herfst, jan, voetbalHerfst);
        manueleToewijzingService.wijzigToewijzing(herfst, tim, voetbalHerfst);

//        ASSERT
        Assertions.assertEquals(voetbalHerfst, repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst).getIngerichtTalent());
        Assertions.assertEquals(voetbalHerfst, repository.zoekToewijzingVoorLeerlingEnPeriode(tim, herfst).getIngerichtTalent());
        Assertions.assertEquals(ToewijzingsType.MANUEEL, repository.zoekToewijzingVoorLeerlingEnPeriode(jan, herfst).getToewijzingsType());
        Assertions.assertEquals(ToewijzingsType.MANUEEL, repository.zoekToewijzingVoorLeerlingEnPeriode(tim, herfst).getToewijzingsType());
    }

    @Test
    public void manueleToewijzingNaarAnderTalentHistoriekNegeren() {
        // ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters");

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        TalentenPeriode kerst = new TalentenPeriode(
                "Kerst",
                LocalDate.of(2026, 12, 22),
                LocalDate.of(2027, 1, 21)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");

        IngerichtTalent voetbalHerfst =
                new IngerichtTalent(voetbal, herfst, 10);

        IngerichtTalent schakenKerst =
                new IngerichtTalent(schaken, kerst, 10);

        IngerichtTalent voetbalKerst =
                new IngerichtTalent(voetbal, kerst, 10);

        Toewijzing historischeToewijzing = new Toewijzing(
                jan,
                voetbalHerfst,
                ToewijzingsType.AUTOMATISCH
        );

        List<Toewijzing> historischeToewijzingen = new ArrayList<>();
        historischeToewijzingen.add(historischeToewijzing);

        InMemoryToewijzingRepository repository =
                new InMemoryToewijzingRepository(historischeToewijzingen);

        Toewijzing huidigeToewijzing = new Toewijzing(
                jan,
                schakenKerst,
                ToewijzingsType.AUTOMATISCH
        );

        repository.save(huidigeToewijzing);

        ManueleToewijzingService service =
                new ManueleToewijzingService(repository);

        // ACT
        service.wijzigToewijzing(kerst, jan, voetbalKerst);

        // ASSERT
        Toewijzing gewijzigdeToewijzing =
                repository.zoekToewijzingVoorLeerlingEnPeriode(jan, kerst);

        assertEquals(voetbalKerst, gewijzigdeToewijzing.getIngerichtTalent());
        assertEquals(
                ToewijzingsType.MANUEEL,
                gewijzigdeToewijzing.getToewijzingsType()
        );

        assertEquals(1, repository.zoekHistorischeToewijzingen().size());
        assertEquals(
                voetbalHerfst,
                repository.zoekHistorischeToewijzingen()
                        .get(0)
                        .getIngerichtTalent()
        );
    }

}
