package be.kdg.talenten;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.InMemoryToewijzingRepository;
import be.kdg.talenten.repository.InMemoryVoorkeurRepository;
import be.kdg.talenten.service.AutomatischeVerdelingService;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutomatischeVerdelingServiceTest {
    @Test
    void serviceVoertAutomatischeVerdelingUitEnSlaatToewijzingenOp() {
        // Arrange
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                LocalDate.of(2025, 11, 22),
                LocalDate.of(2026, 2, 21)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenWinter = new IngerichtTalent(schaken, winter, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalWinter = new IngerichtTalent(voetbal, winter, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenWinter = new IngerichtTalent(koken, winter, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(new Voorkeur(jan, winter, schakenWinter, 1));
        voorkeuren.add(new Voorkeur(jan, winter, voetbalWinter, 2));
        voorkeuren.add(new Voorkeur(jan, winter, kokenWinter, 3));

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(voorkeuren);

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(new ArrayList<>());

        AutomatischeVerdelingService service =
                new AutomatischeVerdelingService(voorkeurRepository, toewijzingRepository);

        // Act
        VerdelingsResultaat resultaat = service.voerAutomatischeVerdelingUit(winter);

        // Assert
        assertEquals(1, resultaat.getAantalToewijzingen());
        assertEquals(1, toewijzingRepository.getOpgeslagenToewijzingen().size());

        Toewijzing opgeslagenToewijzing =
                toewijzingRepository.getOpgeslagenToewijzingen().get(0);

        assertSame(jan, opgeslagenToewijzing.getLeerling());
        assertSame(schakenWinter, opgeslagenToewijzing.getIngerichtTalent());
    }
    @Test
    void serviceGebruiktHistorischeToewijzingenBijAutomatischeVerdeling() {
        // Arrange
        Leerling jan = new Leerling("Jan", "Peeters", new Klas("1AA", "2026-2027", 1));

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2025, 9, 21),
                LocalDate.of(2025, 11, 21)
        );

        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                LocalDate.of(2025, 11, 22),
                LocalDate.of(2026, 2, 21)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        IngerichtTalent schakenWinter = new IngerichtTalent(schaken, winter, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent voetbalWinter = new IngerichtTalent(voetbal, winter, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);
        IngerichtTalent kokenWinter = new IngerichtTalent(koken, winter, 10, Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR);

        List<Toewijzing> historischeToewijzingen = new ArrayList<>();
        historischeToewijzingen.add(
                new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH)
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(new Voorkeur(jan, winter, schakenWinter, 1));
        voorkeuren.add(new Voorkeur(jan, winter, voetbalWinter, 2));
        voorkeuren.add(new Voorkeur(jan, winter, kokenWinter, 3));

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(voorkeuren);

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(historischeToewijzingen);

        AutomatischeVerdelingService service =
                new AutomatischeVerdelingService(voorkeurRepository, toewijzingRepository);

        // Act
        VerdelingsResultaat resultaat = service.voerAutomatischeVerdelingUit(winter);

        // Assert
        assertEquals(1, resultaat.getAantalToewijzingen());
        assertEquals(1, toewijzingRepository.getOpgeslagenToewijzingen().size());

        Toewijzing opgeslagenToewijzing =
                toewijzingRepository.getOpgeslagenToewijzingen().get(0);

        assertSame(jan, opgeslagenToewijzing.getLeerling());
        assertSame(voetbalWinter, opgeslagenToewijzing.getIngerichtTalent());
        assertSame(ToewijzingsType.AUTOMATISCH, opgeslagenToewijzing.getToewijzingsType());
    }
}
