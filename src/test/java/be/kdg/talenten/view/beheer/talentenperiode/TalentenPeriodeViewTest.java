package be.kdg.talenten.view.beheer.talentenperiode;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TalentenPeriodeViewTest {
    private final LocalDate vandaag = LocalDate.of(2026, 11, 15);
    private final Schooljaar schooljaar = new Schooljaar(
            1L, "2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30), true
    );

    @Test
    void periodeVoorStartdatumIsKomend() {
        assertEquals("Komend", TalentenPeriodeView.bepaalStatus(
                periode(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 12, 20)), vandaag));
    }

    @Test
    void periodeOpStartEnEinddatumIsLopend() {
        assertEquals("Lopend", TalentenPeriodeView.bepaalStatus(
                periode(vandaag, LocalDate.of(2026, 12, 20)), vandaag));
        assertEquals("Lopend", TalentenPeriodeView.bepaalStatus(
                periode(LocalDate.of(2026, 9, 1), vandaag), vandaag));
    }

    @Test
    void periodeNaEinddatumIsAfgelopen() {
        assertEquals("Afgelopen", TalentenPeriodeView.bepaalStatus(
                periode(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 11, 14)), vandaag));
    }

    private TalentenPeriode periode(LocalDate start, LocalDate einde) {
        return new TalentenPeriode("Periode", start, einde, schooljaar);
    }
}
