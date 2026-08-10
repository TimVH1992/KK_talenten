package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.overzicht.LeerlingDetailsOverzicht;
import be.kdg.talenten.repository.inmemory.InMemoryToewijzingRepository;
import be.kdg.talenten.repository.inmemory.InMemoryVoorkeurRepository;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchooljaarFilteringServiceTest {

    @Test
    void automatischeVerdelingHoudtGeenRekeningMetHistoriekUitVorigSchooljaar() {
        // ARRANGE
        Schooljaar vorigSchooljaar = new Schooljaar(
                "2025-2026",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 6, 30)
        );

        Schooljaar huidigSchooljaar = new Schooljaar(
                "2026-2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 6, 30)
        );

        TalentenPeriode vorig = new TalentenPeriode(
                "Lente",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 5, 31),
                vorigSchooljaar
        );

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31),
                huidigSchooljaar
        );

        Klas klas = new Klas(
                "1AA",
                huidigSchooljaar,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        Leerling alice = new Leerling(
                "Alice",
                "Janssens",
                klas
        );

        Leerkracht leerkracht = new Leerkracht(
                "Sofie",
                "De Clercq"
        );

        Talent schaken = new Talent(
                "Schaken",
                "Strategisch denken"
        );

        Talent koken = new Talent(
                "Koken",
                "Kookvaardigheden"
        );

        IngerichtTalent schakenVorig = new IngerichtTalent(
                schaken,
                vorig,
                schaken.getNaam(),
                schaken.getBeschrijving(),
                10,
                klas.getDoelgroep(),
                List.of(leerkracht)
        );

        IngerichtTalent schakenHerfst = new IngerichtTalent(
                schaken,
                herfst,
                schaken.getNaam(),
                schaken.getBeschrijving(),
                10,
                klas.getDoelgroep(),
                List.of(leerkracht)
        );

        IngerichtTalent kokenHerfst = new IngerichtTalent(
                koken,
                herfst,
                koken.getNaam(),
                koken.getBeschrijving(),
                10,
                klas.getDoelgroep(),
                List.of(leerkracht)
        );

        List<Voorkeur> voorkeuren = List.of(
                new Voorkeur(
                        alice,
                        herfst,
                        schakenHerfst,
                        1
                ),
                new Voorkeur(
                        alice,
                        herfst,
                        kokenHerfst,
                        2
                )
        );

        List<Toewijzing> historiek = List.of(
                new Toewijzing(
                        alice,
                        schakenVorig,
                        ToewijzingsType.AUTOMATISCH,
                        1
                )
        );

        InMemoryToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(historiek);

        AutomatischeVerdelingService service =
                new AutomatischeVerdelingService(
                        new InMemoryVoorkeurRepository(voorkeuren),
                        toewijzingRepository
                );

        // ACT
        VerdelingsResultaat resultaat =
                service.voerAutomatischeVerdelingUit(herfst);

        // ASSERT
        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );

        assertSame(
                schakenHerfst,
                resultaat.getToewijzingen()
                        .getFirst()
                        .getIngerichtTalent()
        );

        assertEquals(
                1,
                resultaat.getToewijzingen()
                        .getFirst()
                        .getVoorkeurNummer()
        );
    }

    @Test
    void leerlingDetailsTonenAlleenHistoriekBinnenGeselecteerdSchooljaar() {
        // ARRANGE
        Schooljaar vorigSchooljaar = new Schooljaar(
                "2025-2026",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 6, 30)
        );

        Schooljaar huidigSchooljaar = new Schooljaar(
                "2026-2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 6, 30)
        );

        TalentenPeriode vorig = new TalentenPeriode(
                "Lente",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 5, 31),
                vorigSchooljaar
        );

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31),
                huidigSchooljaar
        );

        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                LocalDate.of(2026, 11, 9),
                LocalDate.of(2026, 12, 18),
                huidigSchooljaar
        );

        Klas klas = new Klas(
                "1AA",
                huidigSchooljaar,
                1,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
        );

        Leerling alice = new Leerling(
                "Alice",
                "Janssens",
                klas
        );

        Leerkracht leerkracht = new Leerkracht(
                "Sofie",
                "De Clercq"
        );

        Talent schaken = new Talent(
                "Schaken",
                "Strategisch denken"
        );

        Talent koken = new Talent(
                "Koken",
                "Kookvaardigheden"
        );

        IngerichtTalent schakenVorig = new IngerichtTalent(
                schaken,
                vorig,
                schaken.getNaam(),
                schaken.getBeschrijving(),
                10,
                klas.getDoelgroep(),
                List.of(leerkracht)
        );

        IngerichtTalent kokenHerfst = new IngerichtTalent(
                koken,
                herfst,
                koken.getNaam(),
                koken.getBeschrijving(),
                10,
                klas.getDoelgroep(),
                List.of(leerkracht)
        );

        List<Toewijzing> historiek = List.of(
                new Toewijzing(
                        alice,
                        schakenVorig,
                        ToewijzingsType.AUTOMATISCH,
                        1
                ),
                new Toewijzing(
                        alice,
                        kokenHerfst,
                        ToewijzingsType.AUTOMATISCH,
                        2
                )
        );

        LeerlingDetailsService service =
                new LeerlingDetailsService(
                        new InMemoryVoorkeurRepository(new ArrayList<>()),
                        new InMemoryToewijzingRepository(historiek)
                );

        // ACT
        LeerlingDetailsOverzicht overzicht =
                service.bekijk(
                        alice,
                        winter
                );

        // ASSERT
        assertEquals(
                1,
                overzicht.historischeToewijzingen().size()
        );

        assertSame(
                kokenHerfst,
                overzicht.historischeToewijzingen()
                        .getFirst()
                        .getIngerichtTalent()
        );
    }
}