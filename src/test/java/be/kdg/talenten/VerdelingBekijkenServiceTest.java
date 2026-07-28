package be.kdg.talenten;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.overzicht.KlasOverzicht;
import be.kdg.talenten.overzicht.LeerlingToewijzingOverzicht;
import be.kdg.talenten.repository.*;
import be.kdg.talenten.service.VerdelingBekijkenService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VerdelingBekijkenServiceTest {
    @Test
    public void bekijkPerIngerichtTalentGroepeertToewijzingenCorrect() {
//        ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters");
        Leerling julie = new Leerling("Julie", "Martens");
        Leerling eveline = new Leerling("Eveline", "Van Oevelen");

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent dansen = new Talent("Dansen", "shake shake shake");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10);
        IngerichtTalent dansenHerfst = new IngerichtTalent(dansen, herfst, 6);

        List<IngerichtTalent> ingerichteTalenten = new ArrayList<>();
        ingerichteTalenten.add(schakenHerfst);
        ingerichteTalenten.add(dansenHerfst);

        List<Toewijzing> schakenToewijzingen = new ArrayList<>();
        schakenToewijzingen.add(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH));
        schakenToewijzingen.add(new Toewijzing(julie, schakenHerfst, ToewijzingsType.MANUEEL));

        List<Toewijzing> dansenToewijzingen = new ArrayList<>();
        dansenToewijzingen.add(new Toewijzing(eveline, dansenHerfst, ToewijzingsType.AUTOMATISCH));

        IngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(ingerichteTalenten);
        ToewijzingRepository toewijzingRepository = new InMemoryToewijzingRepository(new ArrayList<>());
        toewijzingRepository.saveAll(schakenToewijzingen);
        toewijzingRepository.saveAll(dansenToewijzingen);

        LeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(new ArrayList<>());

        VerdelingBekijkenService verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository, leerlingRepository);
//        ACT

        List<IngerichtTalentOverzicht> ingerichtTalentOverzichten = verdelingBekijkenService.bekijkPerIngerichtTalent(herfst);
//        ASSERT

        Assertions.assertEquals(new IngerichtTalentOverzicht(schakenHerfst, 2, 8, schakenToewijzingen), ingerichtTalentOverzichten.getFirst());
        Assertions.assertEquals(new IngerichtTalentOverzicht(dansenHerfst, 1, 5, dansenToewijzingen), ingerichtTalentOverzichten.get(1));

    }

    @Test
    public void bekijkPerIngerichtTalentToontOokTalentZonderToewijzingen() {
        //        ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters");
        Leerling julie = new Leerling("Julie", "Martens");
        Leerling eveline = new Leerling("Eveline", "Van Oevelen");

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent dansen = new Talent("Dansen", "shake shake shake");
        Talent koken = new Talent("Koken", "Een warme keuken is vaak lekker");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10);
        IngerichtTalent dansenHerfst = new IngerichtTalent(dansen, herfst, 6);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, herfst, 5);

        List<IngerichtTalent> ingerichteTalenten = new ArrayList<>();
        ingerichteTalenten.add(schakenHerfst);
        ingerichteTalenten.add(dansenHerfst);
        ingerichteTalenten.add(kokenHerfst);

        List<Toewijzing> schakenToewijzingen = new ArrayList<>();
        schakenToewijzingen.add(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH));
        schakenToewijzingen.add(new Toewijzing(julie, schakenHerfst, ToewijzingsType.MANUEEL));

        List<Toewijzing> dansenToewijzingen = new ArrayList<>();
        dansenToewijzingen.add(new Toewijzing(eveline, dansenHerfst, ToewijzingsType.AUTOMATISCH));

        IngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(ingerichteTalenten);
        ToewijzingRepository toewijzingRepository = new InMemoryToewijzingRepository(new ArrayList<>());
        toewijzingRepository.saveAll(schakenToewijzingen);
        toewijzingRepository.saveAll(dansenToewijzingen);

        LeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(new ArrayList<>());

        VerdelingBekijkenService verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository, leerlingRepository);
//        ACT

        List<IngerichtTalentOverzicht> ingerichtTalentOverzichten = verdelingBekijkenService.bekijkPerIngerichtTalent(herfst);
//        ASSERT


        Assertions.assertEquals(new IngerichtTalentOverzicht(kokenHerfst, 0, 5, new ArrayList<>()), ingerichtTalentOverzichten.get(2));
        Assertions.assertEquals(3, ingerichtTalentOverzichten.size());
    }

    @Test
    public void bekijkPerIngerichtTalentToontAlleenGevraagdePeriode() {
        //        ARRANGE
        Leerling jan = new Leerling("Jan", "Peeters");
        Leerling julie = new Leerling("Julie", "Martens");
        Leerling eveline = new Leerling("Eveline", "Van Oevelen");

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
        Talent dansen = new Talent("Dansen", "shake shake shake");
        Talent koken = new Talent("Koken", "Een warme keuken is vaak lekker");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10);
        IngerichtTalent dansenHerfst = new IngerichtTalent(dansen, herfst, 6);
        IngerichtTalent kokenHerfst = new IngerichtTalent(koken, herfst, 5);

        IngerichtTalent schakenKerst = new IngerichtTalent(schaken, kerst, 10);
        IngerichtTalent dansenKerst = new IngerichtTalent(dansen, kerst, 2);

        List<IngerichtTalent> ingerichteTalenten = new ArrayList<>();
        ingerichteTalenten.add(schakenHerfst);
        ingerichteTalenten.add(dansenHerfst);
        ingerichteTalenten.add(kokenHerfst);
        ingerichteTalenten.add(schakenKerst);
        ingerichteTalenten.add(dansenKerst);

        List<Toewijzing> schakenToewijzingen = new ArrayList<>();
        schakenToewijzingen.add(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH));
        schakenToewijzingen.add(new Toewijzing(julie, schakenHerfst, ToewijzingsType.MANUEEL));

        List<Toewijzing> schakenToewijzingenKerst = new ArrayList<>();
        schakenToewijzingenKerst.add(new Toewijzing(jan, schakenKerst, ToewijzingsType.MANUEEL));

        List<Toewijzing> dansenToewijzingen = new ArrayList<>();
        dansenToewijzingen.add(new Toewijzing(eveline, dansenHerfst, ToewijzingsType.AUTOMATISCH));

        List<Toewijzing> dansenToewijzingenKerst = new ArrayList<>();
        dansenToewijzingenKerst.add(new Toewijzing(eveline, dansenKerst, ToewijzingsType.AUTOMATISCH));

        IngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(ingerichteTalenten);
        ToewijzingRepository toewijzingRepository = new InMemoryToewijzingRepository(new ArrayList<>());
        toewijzingRepository.saveAll(schakenToewijzingen);
        toewijzingRepository.saveAll(schakenToewijzingenKerst);
        toewijzingRepository.saveAll(dansenToewijzingen);
        toewijzingRepository.saveAll(dansenToewijzingenKerst);

        LeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(new ArrayList<>());

        VerdelingBekijkenService verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository, leerlingRepository);

        List<IngerichtTalentOverzicht> resultaat = new ArrayList<>();
        resultaat.add(new IngerichtTalentOverzicht(schakenHerfst, 2, 8, schakenToewijzingen));
        resultaat.add(new IngerichtTalentOverzicht(dansenHerfst, 1, 5, dansenToewijzingen));
        resultaat.add(new IngerichtTalentOverzicht(kokenHerfst, 0, 5, new ArrayList<>()));
//        ACT

        List<IngerichtTalentOverzicht> ingerichtTalentOverzichten = verdelingBekijkenService.bekijkPerIngerichtTalent(herfst);


//        ASSERT
        Assertions.assertEquals(resultaat, ingerichtTalentOverzichten);

    }

    @Test
    public void bekijkPerIngerichtTalentMetNullPeriodeWordtGeweigerd() {
//        ARRANGE
        List<IngerichtTalent> ingerichteTalenten = new ArrayList<>();
        IngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(ingerichteTalenten);

        List<Toewijzing> toewijzingen = new ArrayList<>();
        ToewijzingRepository toewijzingRepository = new InMemoryToewijzingRepository(toewijzingen);


        LeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(new ArrayList<>());

        VerdelingBekijkenService verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository, leerlingRepository);
//        ACT

//        ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> verdelingBekijkenService.bekijkPerIngerichtTalent(null)
        );
    }

    @Test
    public void bekijkVoorKlasToontElkeLeerlingMetEventueleToewijzing() {
//        ARRANGE
        Klas klas1A = new Klas("1AA", "2026-2027");

        Leerling jan = new Leerling("Jan", "Peeters", klas1A);
        Leerling julie = new Leerling("Julie", "Martens", klas1A);
        Leerling eveline = new Leerling("Eveline", "Van Oevelen", klas1A);

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );


        Talent schaken = new Talent("Schaken", "Leren schaken");
        ;

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10);

        List<IngerichtTalent> ingerichteTalenten = new ArrayList<>();
        ingerichteTalenten.add(schakenHerfst);

        List<Toewijzing> schakenToewijzingen = new ArrayList<>();
        schakenToewijzingen.add(new Toewijzing(jan, schakenHerfst, ToewijzingsType.AUTOMATISCH));
        schakenToewijzingen.add(new Toewijzing(julie, schakenHerfst, ToewijzingsType.MANUEEL));

        IngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(ingerichteTalenten);
        ToewijzingRepository toewijzingRepository = new InMemoryToewijzingRepository(new ArrayList<>());
        toewijzingRepository.saveAll(schakenToewijzingen);


        LeerlingRepository leerlingRepository = new InMemoryLeerlingRepository(List.of(jan, eveline, julie));

        VerdelingBekijkenService verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository, leerlingRepository);

//        ACT
        KlasOverzicht overzicht = verdelingBekijkenService.bekijkVoorKlas(herfst, klas1A);
//        ASSERT
        List<LeerlingToewijzingOverzicht> verwachteLeerlingOverzichten =
                List.of(
                        new LeerlingToewijzingOverzicht(jan, schakenToewijzingen.get(0)),
                        new LeerlingToewijzingOverzicht(eveline, null),
                        new LeerlingToewijzingOverzicht(julie, schakenToewijzingen.get(1))
                );

        KlasOverzicht verwacht = new KlasOverzicht(klas1A, herfst, verwachteLeerlingOverzichten);

        Assertions.assertEquals(verwacht, overzicht);
    }

    @Test
    public void bekijkVoorKlasMetNullPeriodeWordtGeweigerd() {
        // ARRANGE
        Klas klas1A = new Klas("1AA", "2026-2027");

        VerdelingBekijkenService service =
                maakLegeVerdelingBekijkenService();

        // ACT + ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.bekijkVoorKlas(null, klas1A)
        );
    }

    @Test
    public void bekijkVoorKlasMetNullKlasWordtGeweigerd() {
        // ARRANGE
        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        VerdelingBekijkenService service = maakLegeVerdelingBekijkenService();

        // ACT + ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.bekijkVoorKlas(herfst, null)
        );
    }

    private VerdelingBekijkenService maakLegeVerdelingBekijkenService() {
        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(new ArrayList<>());

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(new ArrayList<>());

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(new ArrayList<>());

        return new VerdelingBekijkenService(
                ingerichtTalentRepository,
                toewijzingRepository,
                leerlingRepository
        );
    }
    @Test
    public void bekijkVoorKlasToontAlleenLeerlingenVanGevraagdeKlas() {
        // ARRANGE
        Klas klas1AA = new Klas("1AA", "2026-2027");
        Klas klas1AB = new Klas("1AB", "2026-2027");

        Leerling jan = new Leerling("Jan", "Peeters", klas1AA);
        Leerling julie = new Leerling("Julie", "Martens", klas1AA);
        Leerling eveline = new Leerling(
                "Eveline",
                "Van Oevelen",
                klas1AB
        );

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 31)
        );

        Talent schaken = new Talent(
                "Schaken",
                "Leren schaken"
        );

        IngerichtTalent schakenHerfst =
                new IngerichtTalent(schaken, herfst, 10);

        Toewijzing toewijzingJan = new Toewijzing(
                jan,
                schakenHerfst,
                ToewijzingsType.AUTOMATISCH
        );

        Toewijzing toewijzingJulie = new Toewijzing(
                julie,
                schakenHerfst,
                ToewijzingsType.MANUEEL
        );

        Toewijzing toewijzingEveline = new Toewijzing(
                eveline,
                schakenHerfst,
                ToewijzingsType.AUTOMATISCH
        );

        IngerichtTalentRepository ingerichtTalentRepository =
                new InMemoryIngerichtTalentRepository(
                        List.of(schakenHerfst)
                );

        ToewijzingRepository toewijzingRepository =
                new InMemoryToewijzingRepository(new ArrayList<>());

        toewijzingRepository.saveAll(
                List.of(
                        toewijzingJan,
                        toewijzingJulie,
                        toewijzingEveline
                )
        );

        LeerlingRepository leerlingRepository =
                new InMemoryLeerlingRepository(
                        List.of(jan, julie, eveline)
                );

        VerdelingBekijkenService service =
                new VerdelingBekijkenService(
                        ingerichtTalentRepository,
                        toewijzingRepository,
                        leerlingRepository
                );

        // ACT
        KlasOverzicht werkelijk =
                service.bekijkVoorKlas(herfst, klas1AA);

        // ASSERT
        KlasOverzicht verwacht = new KlasOverzicht(
                klas1AA,
                herfst,
                List.of(
                        new LeerlingToewijzingOverzicht(
                                jan,
                                toewijzingJan
                        ),
                        new LeerlingToewijzingOverzicht(
                                julie,
                                toewijzingJulie
                        )
                )
        );

        Assertions.assertEquals(verwacht, werkelijk);
    }

}
