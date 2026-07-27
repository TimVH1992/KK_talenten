package be.kdg.talenten;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.repository.InMemoryIngerichtTalentRepository;
import be.kdg.talenten.repository.InMemoryToewijzingRepository;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.service.VerdelingBekijkenService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VerdelingBekijkenServiceTest {
    @Test
    public void bekijkPerIngerichtTalentGroepeertToewijzingenCorrect(){
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

        VerdelingBekijkenService verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository);
//        ACT

        List<IngerichtTalentOverzicht> ingerichtTalentOverzichten =  verdelingBekijkenService.bekijkPerIngerichtTalent(herfst);
//        ASSERT

        Assertions.assertEquals(new IngerichtTalentOverzicht(schakenHerfst, 2, 8, schakenToewijzingen),ingerichtTalentOverzichten.getFirst());
        Assertions.assertEquals(new IngerichtTalentOverzicht(dansenHerfst, 1, 5, dansenToewijzingen), ingerichtTalentOverzichten.get(1));

    }
    @Test
    public void bekijkPerIngerichtTalentToontOokTalentZonderToewijzingen(){
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

        VerdelingBekijkenService verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository);
//        ACT

        List<IngerichtTalentOverzicht> ingerichtTalentOverzichten =  verdelingBekijkenService.bekijkPerIngerichtTalent(herfst);
//        ASSERT


        Assertions.assertEquals(new IngerichtTalentOverzicht(kokenHerfst, 0, 5, new ArrayList<>()), ingerichtTalentOverzichten.get(2));
        Assertions.assertEquals(3, ingerichtTalentOverzichten.size());
    }

    @Test
    public void bekijkPerIngerichtTalentToontAlleenGevraagdePeriode(){
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

        VerdelingBekijkenService verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository);

        List<IngerichtTalentOverzicht> resultaat = new ArrayList<>();
        resultaat.add(new IngerichtTalentOverzicht(schakenHerfst, 2, 8, schakenToewijzingen));
        resultaat.add(new IngerichtTalentOverzicht(dansenHerfst, 1, 5, dansenToewijzingen));
        resultaat.add(new IngerichtTalentOverzicht(kokenHerfst, 0, 5, new ArrayList<>()));
//        ACT

        List<IngerichtTalentOverzicht> ingerichtTalentOverzichten =  verdelingBekijkenService.bekijkPerIngerichtTalent(herfst);


//        ASSERT
        Assertions.assertEquals(resultaat, ingerichtTalentOverzichten);

    }

    @Test
    public void bekijkPerIngerichtTalentMetNullPeriodeWordtGeweigerd(){
//        ARRANGE
        List<IngerichtTalent> ingerichteTalenten = new ArrayList<>();
        IngerichtTalentRepository ingerichtTalentRepository = new InMemoryIngerichtTalentRepository(ingerichteTalenten);

        List<Toewijzing>  toewijzingen = new ArrayList<>();
        ToewijzingRepository toewijzingRepository = new InMemoryToewijzingRepository(toewijzingen);


        VerdelingBekijkenService verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository);
//        ACT

//        ASSERT
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> verdelingBekijkenService.bekijkPerIngerichtTalent(null)
        );
    }
}
