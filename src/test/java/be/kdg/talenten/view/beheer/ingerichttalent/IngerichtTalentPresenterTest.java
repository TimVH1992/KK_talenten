package be.kdg.talenten.view.beheer.ingerichttalent;

import be.kdg.talenten.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngerichtTalentPresenterTest {
    private final Schooljaar schooljaar = new Schooljaar(
            1L, "2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30), true);
    private final TalentenPeriode herfst = new TalentenPeriode(
            1L, "Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 20), schooljaar);
    private final TalentenPeriode lente = new TalentenPeriode(
            2L, "Lente", LocalDate.of(2027, 1, 10), LocalDate.of(2027, 6, 20), schooljaar);
    private final Talent basis = new Talent(1L, "Schaken", "Strategisch denkspel");

    @Test
    void doelgroepAToontNooitTalentVanDoelgroepB() {
        IngerichtTalent doelgroepA = ingericht(1L, "Schaken A",
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB);
        IngerichtTalent doelgroepB = ingericht(2L, "Schaken B",
                Doelgroep.KWALIFICATIEFASE_TWEEDEGRAAD_AB);

        List<IngerichtTalent> resultaat = IngerichtTalentPresenter.filterOpDoelgroep(
                List.of(doelgroepB, doelgroepA),
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB);

        assertEquals(List.of(doelgroepA), resultaat);
    }

    @Test
    void lopendePeriodeWordtAutomatischGeselecteerd() {
        TalentenPeriode resultaat = IngerichtTalentPresenter.kiesPassendePeriode(
                List.of(herfst, lente), LocalDate.of(2027, 2, 1));

        assertEquals(lente, resultaat);
    }

    @Test
    void eerstvolgendePeriodeWordtGekozenAlsErGeenLopendeIs() {
        TalentenPeriode resultaat = IngerichtTalentPresenter.kiesPassendePeriode(
                List.of(herfst, lente), LocalDate.of(2026, 8, 1));

        assertEquals(herfst, resultaat);
    }

    private IngerichtTalent ingericht(Long id, String naam, Doelgroep doelgroep) {
        return new IngerichtTalent(id, basis, herfst, naam, "Omschrijving", 10,
                doelgroep, List.of(), true);
    }
}
