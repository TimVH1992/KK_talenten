package be.kdg.talenten.repository;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryIngerichtTalentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;


public class IngerichtTalentRepositoryTest {
    @Test
    public void zoekVoorPeriodeGeeftAlleenIngerichteTalentenVanGevraagdePeriode(){
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

        InMemoryIngerichtTalentRepository inMemoryIngerichtTalentRepository = new InMemoryIngerichtTalentRepository(List.of(schakenHerfst, schakenWinter, voetbalWinter, kokenWinter));

        Assertions.assertEquals(List.of(schakenHerfst),inMemoryIngerichtTalentRepository.zoekVoorPeriode(herfst));
        Assertions.assertEquals(List.of(schakenWinter, voetbalWinter, kokenWinter),inMemoryIngerichtTalentRepository.zoekVoorPeriode(winter));
    }
}
