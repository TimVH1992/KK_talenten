package be.kdg.talenten.verdeling;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryToewijzingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryVoorkeurRepository;
import be.kdg.talenten.service.AutomatischeVerdelingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AutomatischeVerdelingServiceTest {

    private Leerkracht testLeerkracht;

    @BeforeEach
    void setUp() {
        testLeerkracht = new Leerkracht(
                "Test",
                "Leerkracht"
        );
    }

    @Test
    void serviceVoertAutomatischeVerdelingUitEnSlaatToewijzingenOp() {
        // Arrange
        Klas klas1AA = maakObservatieKlas();

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );

        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                LocalDate.of(2025, 11, 22),
                LocalDate.of(2026, 2, 21)
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );
        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );
        Talent koken = new Talent(
                "Koken",
                "Leren koken"
        );

        IngerichtTalent schakenWinter = richtTalentIn(
                schaken,
                winter,
                10
        );

        IngerichtTalent voetbalWinter = richtTalentIn(
                voetbal,
                winter,
                10
        );

        IngerichtTalent kokenWinter = richtTalentIn(
                koken,
                winter,
                10
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(
                new Voorkeur(
                        jan,
                        winter,
                        schakenWinter,
                        1
                )
        );
        voorkeuren.add(
                new Voorkeur(
                        jan,
                        winter,
                        voetbalWinter,
                        2
                )
        );
        voorkeuren.add(
                new Voorkeur(
                        jan,
                        winter,
                        kokenWinter,
                        3
                )
        );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(voorkeuren);

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        new ArrayList<>()
                );

        AutomatischeVerdelingService service =
                new AutomatischeVerdelingService(
                        voorkeurRepository,
                        toewijzingRepository
                );

        // Act
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(winter);

        // Assert
        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );

        assertEquals(
                1,
                toewijzingRepository
                        .getOpgeslagenToewijzingen()
                        .size()
        );

        Toewijzing opgeslagenToewijzing =
                toewijzingRepository
                        .getOpgeslagenToewijzingen()
                        .getFirst();

        assertSame(
                jan,
                opgeslagenToewijzing.getLeerling()
        );

        assertSame(
                schakenWinter,
                opgeslagenToewijzing.getIngerichtTalent()
        );
    }

    @Test
    void serviceGebruiktHistorischeToewijzingenBijAutomatischeVerdeling() {
        // Arrange
        Klas klas1AA = maakObservatieKlas();

        Leerling jan = new Leerling(
                "Jan",
                "Peeters",
                klas1AA
        );

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

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );
        Talent voetbal = new Talent(
                "Voetbal",
                "Voetbaltraining"
        );
        Talent koken = new Talent(
                "Koken",
                "Leren koken"
        );

        IngerichtTalent schakenHerfst = richtTalentIn(
                schaken,
                herfst,
                10
        );

        IngerichtTalent schakenWinter = richtTalentIn(
                schaken,
                winter,
                10
        );

        IngerichtTalent voetbalWinter = richtTalentIn(
                voetbal,
                winter,
                10
        );

        IngerichtTalent kokenWinter = richtTalentIn(
                koken,
                winter,
                10
        );

        List<Toewijzing> historischeToewijzingen =
                new ArrayList<>();

        historischeToewijzingen.add(
                new Toewijzing(
                        jan,
                        schakenHerfst,
                        ToewijzingsType.AUTOMATISCH
                )
        );

        List<Voorkeur> voorkeuren = new ArrayList<>();
        voorkeuren.add(
                new Voorkeur(
                        jan,
                        winter,
                        schakenWinter,
                        1
                )
        );
        voorkeuren.add(
                new Voorkeur(
                        jan,
                        winter,
                        voetbalWinter,
                        2
                )
        );
        voorkeuren.add(
                new Voorkeur(
                        jan,
                        winter,
                        kokenWinter,
                        3
                )
        );

        InMemoryVoorkeurRepository voorkeurRepository =
                new InMemoryVoorkeurRepository(voorkeuren);

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(
                        historischeToewijzingen
                );

        AutomatischeVerdelingService service =
                new AutomatischeVerdelingService(
                        voorkeurRepository,
                        toewijzingRepository
                );

        // Act
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(winter);

        // Assert
        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );

        /*
         * Eén historische toewijzing en één nieuwe
         * toewijzing voor de winter.
         */
        assertEquals(
                1,
                toewijzingRepository
                        .getOpgeslagenToewijzingen()
                        .size()
        );

        Toewijzing nieuweToewijzing =
                resultaat.getToewijzingen().getFirst();

        assertSame(
                jan,
                nieuweToewijzing.getLeerling()
        );

        assertSame(
                voetbalWinter,
                nieuweToewijzing.getIngerichtTalent()
        );

        assertEquals(
                ToewijzingsType.AUTOMATISCH,
                nieuweToewijzing.getToewijzingsType()
        );

        assertTrue(
                toewijzingRepository
                        .getOpgeslagenToewijzingen()
                        .contains(nieuweToewijzing)
        );
    }

    @Test
    void serviceBehoudtManueleToewijzingenEnVervangtAlleenAutomatischeToewijzingen() {
        // ARRANGE
        Klas klas1AA = maakObservatieKlas();
        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling julie = new Leerling("Julie", "Martens", klas1AA);
        TalentenPeriode winter = new TalentenPeriode("Winter", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 20));

        IngerichtTalent schakenWinter = richtTalentIn(new Talent("Schaken", "Leren schaken"), winter, 1);
        IngerichtTalent voetbalWinter = richtTalentIn(new Talent("Voetbal", "Voetbaltraining"), winter, 10);
        IngerichtTalent kokenWinter = richtTalentIn(new Talent("Koken", "Leren koken"), winter, 10);

        List<Voorkeur> voorkeuren = List.of(
                new Voorkeur(jan, winter, schakenWinter, 1),
                new Voorkeur(jan, winter, voetbalWinter, 2),
                new Voorkeur(jan, winter, kokenWinter, 3),
                new Voorkeur(julie, winter, schakenWinter, 1),
                new Voorkeur(julie, winter, voetbalWinter, 2),
                new Voorkeur(julie, winter, kokenWinter, 3)
        );

        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(voorkeuren);
        InMemoryToewijzingRepository toewijzingRepository = new InMemoryToewijzingRepository(new ArrayList<>());

        Toewijzing manueleToewijzing = toewijzingRepository.save(new Toewijzing(jan, schakenWinter, ToewijzingsType.MANUEEL));
        Toewijzing oudeAutomatischeToewijzing = toewijzingRepository.save(new Toewijzing(julie, kokenWinter, ToewijzingsType.AUTOMATISCH, 3));

        AutomatischeVerdelingService service = new AutomatischeVerdelingService(voorkeurRepository, toewijzingRepository);

        // ACT
        VerdelingsResultaat resultaat = service.voerAutomatischeVerdelingUit(winter);

        // ASSERT
        List<Toewijzing> opgeslagenToewijzingen = toewijzingRepository.getOpgeslagenToewijzingen();
        assertEquals(2, opgeslagenToewijzingen.size());
        assertTrue(opgeslagenToewijzingen.contains(manueleToewijzing));
        assertFalse(opgeslagenToewijzingen.contains(oudeAutomatischeToewijzing));

        Toewijzing nieuweAutomatischeToewijzing = opgeslagenToewijzingen.stream()
                .filter(toewijzing -> toewijzing.getToewijzingsType() == ToewijzingsType.AUTOMATISCH)
                .findFirst()
                .orElseThrow();

        assertSame(julie, nieuweAutomatischeToewijzing.getLeerling());
        assertSame(voetbalWinter, nieuweAutomatischeToewijzing.getIngerichtTalent());
        assertEquals(2, nieuweAutomatischeToewijzing.getVoorkeurNummer());
        assertEquals(1, resultaat.getAantalToewijzingen());
    }

    @Test
    void herverdelingWijzigtToewijzingenVanAnderePeriodeNiet() {
        // ARRANGE
        Klas klas1AA = maakObservatieKlas();
        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling julie = new Leerling("Julie", "Martens", klas1AA);

        TalentenPeriode herfst = new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31));
        TalentenPeriode winter = new TalentenPeriode("Winter", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 20));

        IngerichtTalent schakenHerfst = richtTalentIn(new Talent("Schaken", "Leren schaken"), herfst, 10);

        IngerichtTalent schakenWinter = richtTalentIn(new Talent("Schaken", "Leren schaken"), winter, 10);
        IngerichtTalent voetbalWinter = richtTalentIn(new Talent("Voetbal", "Voetbaltraining"), winter, 10);
        IngerichtTalent kokenWinter = richtTalentIn(new Talent("Koken", "Leren koken"), winter, 10);

        List<Voorkeur> voorkeuren = List.of(
                new Voorkeur(julie, winter, voetbalWinter, 1),
                new Voorkeur(julie, winter, kokenWinter, 2),
                new Voorkeur(julie, winter, schakenWinter, 3)
        );

        InMemoryVoorkeurRepository voorkeurRepository = new InMemoryVoorkeurRepository(voorkeuren);
        InMemoryToewijzingRepository toewijzingRepository = new InMemoryToewijzingRepository(new ArrayList<>());

        Toewijzing herfstToewijzing = toewijzingRepository.save(
                new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH, 1)
        );

        Toewijzing oudeWinterToewijzing = toewijzingRepository.save(
                new Toewijzing(julie, schakenWinter, ToewijzingsType.AUTOMATISCH, 3)
        );

        AutomatischeVerdelingService service = new AutomatischeVerdelingService(voorkeurRepository, toewijzingRepository);

        // ACT
        service.voerAutomatischeVerdelingUit(winter);

        // ASSERT
        List<Toewijzing> opgeslagenToewijzingen = toewijzingRepository.getOpgeslagenToewijzingen();

        assertEquals(2, opgeslagenToewijzingen.size());

        assertTrue(opgeslagenToewijzingen.contains(herfstToewijzing));
        assertFalse(opgeslagenToewijzingen.contains(oudeWinterToewijzing));

        List<Toewijzing> herfstToewijzingen = toewijzingRepository.zoekVoorPeriode(herfst);
        assertEquals(1, herfstToewijzingen.size());
        assertSame(herfstToewijzing, herfstToewijzingen.getFirst());
        assertSame(schakenHerfst, herfstToewijzingen.getFirst().getIngerichtTalent());

        List<Toewijzing> winterToewijzingen = toewijzingRepository.zoekVoorPeriode(winter);
        assertEquals(1, winterToewijzingen.size());

        Toewijzing nieuweWinterToewijzing = winterToewijzingen.getFirst();
        assertSame(julie, nieuweWinterToewijzing.getLeerling());
        assertSame(voetbalWinter, nieuweWinterToewijzing.getIngerichtTalent());
        assertEquals(ToewijzingsType.AUTOMATISCH, nieuweWinterToewijzing.getToewijzingsType());
        assertEquals(1, nieuweWinterToewijzing.getVoorkeurNummer());
    }

    private Klas maakObservatieKlas() {
        return new Klas(
                "1AA",
                "2026-2027",
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );
    }

    private IngerichtTalent richtTalentIn(
            Talent talent,
            TalentenPeriode periode,
            int maximumCapaciteit
    ) {
        return new IngerichtTalent(
                talent,
                periode,
                maximumCapaciteit,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                List.of(testLeerkracht)
        );
    }
}