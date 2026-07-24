package be.kdg.talenten;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.InMemoryIngerichtTalentRepository;
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

        IngerichtTalent schakenHerfst = new IngerichtTalent(schaken, herfst, 10);

        IngerichtTalent schakenWinter = new IngerichtTalent(schaken, winter, 10);
        IngerichtTalent voetbalWinter = new IngerichtTalent(voetbal, winter, 10);
        IngerichtTalent kokenWinter = new IngerichtTalent(koken, winter, 10);

        InMemoryIngerichtTalentRepository inMemoryIngerichtTalentRepository = new InMemoryIngerichtTalentRepository(List.of(schakenHerfst, schakenWinter, voetbalWinter, kokenWinter));

        Assertions.assertEquals(List.of(schakenHerfst),inMemoryIngerichtTalentRepository.zoekVoorPeriode(herfst));
        Assertions.assertEquals(List.of(schakenWinter, voetbalWinter, kokenWinter),inMemoryIngerichtTalentRepository.zoekVoorPeriode(winter));
    }
}
