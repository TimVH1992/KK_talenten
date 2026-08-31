package be.kdg.talenten.service;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.inmemory.InMemoryToewijzingRepository;
import be.kdg.talenten.service.leerling.LeerlingHistoriekService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class LeerlingHistoriekServiceTest {
    @Test
    void geeftAlleenToewijzingenVanGekozenLeerlingNieuwsteEerst() {
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        TalentenPeriode eerste = new TalentenPeriode("Eerste", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31), schooljaar);
        TalentenPeriode tweede = new TalentenPeriode("Tweede", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 20), schooljaar);
        Klas klas = new Klas("1AA", schooljaar, 1, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB);
        Leerling leerling = new Leerling("Jan", "Peeters", klas);
        Leerling andere = new Leerling("Sofie", "Janssens", klas);
        Leerkracht leerkracht = new Leerkracht("Tom", "Docent");
        Talent talent = new Talent("Schaken", "Strategisch denken");
        IngerichtTalent talentEerste = new IngerichtTalent(talent, eerste, "Schaken 1", "Eerste", 10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(leerkracht));
        IngerichtTalent talentTweede = new IngerichtTalent(talent, tweede, "Schaken 2", "Tweede", 10,
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(leerkracht));
        Toewijzing oudste = new Toewijzing(leerling, talentEerste, ToewijzingsType.AUTOMATISCH, 1);
        Toewijzing nieuwste = new Toewijzing(leerling, talentTweede, ToewijzingsType.MANUEEL, 2);
        Toewijzing vanAndere = new Toewijzing(andere, talentTweede, ToewijzingsType.AUTOMATISCH, 1);
        LeerlingHistoriekService service = new LeerlingHistoriekService(
                new InMemoryToewijzingRepository(List.of(oudste, vanAndere, nieuwste))
        );

        List<Toewijzing> resultaat = service.geefAlleToewijzingen(leerling);

        assertEquals(2, resultaat.size());
        assertSame(nieuwste, resultaat.get(0));
        assertSame(oudste, resultaat.get(1));
    }
}
