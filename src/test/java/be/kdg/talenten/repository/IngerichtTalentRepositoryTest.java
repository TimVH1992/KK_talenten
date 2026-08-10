package be.kdg.talenten.repository;

import be.kdg.talenten.testutil.TestDataFactory;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryIngerichtTalentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;


public class IngerichtTalentRepositoryTest {
    @Test
    public void zoekVoorPeriodeGeeftAlleenIngerichteTalentenVanGevraagdePeriode(){
        Leerkracht leerkracht =
                new Leerkracht("Test", "Leerkracht");

        TalentenPeriode herfst = new TalentenPeriode(
                "Herfst",
                LocalDate.of(2025, 9, 21),
                LocalDate.of(2025, 11, 21)
        ,
                TestDataFactory.schooljaarVoorPeriode(LocalDate.of(2025, 9, 21), LocalDate.of(2025, 11, 21)));

        TalentenPeriode winter = new TalentenPeriode(
                "Winter",
                LocalDate.of(2025, 11, 22),
                LocalDate.of(2026, 2, 21)
        ,
                TestDataFactory.schooljaarVoorPeriode(LocalDate.of(2025, 11, 22), LocalDate.of(2026, 2, 21)));

        Talent schaken = new Talent("Schaken", "Leren schaken");
        Talent voetbal = new Talent("Voetbal", "Voetbaltraining");
        Talent koken = new Talent("Koken", "Leren koken");

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, schaken.getNaam(), schaken.getBeschrijving(), 10, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(leerkracht));

        IngerichtTalent schakenWinter = new IngerichtTalent(schaken, winter, schaken.getNaam(), schaken.getBeschrijving(), 10, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(leerkracht));
        IngerichtTalent voetbalWinter = new IngerichtTalent(voetbal, winter, voetbal.getNaam(), voetbal.getBeschrijving(), 10, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(leerkracht));
        IngerichtTalent kokenWinter = new IngerichtTalent(koken, winter, koken.getNaam(), koken.getBeschrijving(), 10, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(leerkracht));

        InMemoryIngerichtTalentRepository inMemoryIngerichtTalentRepository = new InMemoryIngerichtTalentRepository(List.of(schakenHerfst, schakenWinter, voetbalWinter, kokenWinter));

        Assertions.assertEquals(List.of(schakenHerfst),inMemoryIngerichtTalentRepository.zoekVoorPeriode(herfst));
        Assertions.assertEquals(List.of(schakenWinter, voetbalWinter, kokenWinter),inMemoryIngerichtTalentRepository.zoekVoorPeriode(winter));
    }
}
