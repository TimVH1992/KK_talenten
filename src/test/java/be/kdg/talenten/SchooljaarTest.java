package be.kdg.talenten;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SchooljaarTest {
    @Test
    void schooljaarBewaartNaamDatumsEnActieveStatus() {
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30), true);

        assertEquals("2026-2027", schooljaar.getNaam());
        assertEquals(LocalDate.of(2026, 9, 1), schooljaar.getStartDatum());
        assertEquals(LocalDate.of(2027, 6, 30), schooljaar.getEindDatum());
        assertTrue(schooljaar.isActief());
    }

    @Test
    void talentenPeriodeMoetVolledigBinnenSchooljaarVallen() {
        Schooljaar schooljaar = new Schooljaar("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));

        assertThrows(IllegalArgumentException.class, () -> new TalentenPeriode("Te vroeg", LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 15), schooljaar));
        assertThrows(IllegalArgumentException.class, () -> new TalentenPeriode("Te laat", LocalDate.of(2027, 6, 1), LocalDate.of(2027, 7, 1), schooljaar));
    }

    @Test
    void schooljaarNaamMoetTweeOpeenvolgendeJarenBevatten() {
        assertThrows(IllegalArgumentException.class, () -> new Schooljaar("2026-2028", LocalDate.of(2026, 9, 1), LocalDate.of(2028, 6, 30)));
    }
}
