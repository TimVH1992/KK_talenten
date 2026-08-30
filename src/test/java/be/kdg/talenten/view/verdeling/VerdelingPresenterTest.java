package be.kdg.talenten.view.verdeling;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VerdelingPresenterTest {
    @Test
    void lopendePeriodeWordtAutomatischGeselecteerd() {
        Schooljaar jaar = new Schooljaar(1L, "2026-2027", LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 6, 30), true);
        TalentenPeriode herfst = new TalentenPeriode(1L, "Herfst", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 12, 20), jaar);
        TalentenPeriode lente = new TalentenPeriode(2L, "Lente", LocalDate.of(2027, 1, 10),
                LocalDate.of(2027, 6, 20), jaar);

        assertEquals(lente, VerdelingPresenter.kiesPassendePeriode(
                List.of(herfst, lente), LocalDate.of(2027, 2, 1)));
    }
}
