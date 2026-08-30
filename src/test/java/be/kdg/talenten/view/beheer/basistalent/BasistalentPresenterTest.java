package be.kdg.talenten.view.beheer.basistalent;

import be.kdg.talenten.domain.Talent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasistalentPresenterTest {
    private final Talent schaken = new Talent(1L, "Schaken", "Strategisch denkspel");
    private final Talent dans = new Talent(2L, "Dans", "Bewegen op muziek");

    @Test
    void zoekenFiltertOpNaamZonderHoofdlettergevoeligheid() {
        assertEquals(List.of(schaken), BasistalentPresenter.filter(List.of(schaken, dans), "SCHAK"));
    }

    @Test
    void zoekenFiltertOokOpBeschrijving() {
        assertEquals(List.of(dans), BasistalentPresenter.filter(List.of(schaken, dans), "muziek"));
    }

    @Test
    void legeZoektermToontAlleBasistalenten() {
        assertEquals(List.of(schaken, dans), BasistalentPresenter.filter(List.of(schaken, dans), "  "));
    }
}
