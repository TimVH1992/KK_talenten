package be.kdg.talenten.service;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.TalentenPeriodeRepository;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TalentenPeriodeServiceTest {
    private TestRepository repository;
    private TalentenPeriodeService service;
    private Schooljaar schooljaar;

    @BeforeEach
    void setUp() {
        repository = new TestRepository();
        service = new TalentenPeriodeService(repository);
        schooljaar = new Schooljaar(1L, "2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30), true);
    }

    @Test
    void makenEnOphalenVoorSchooljaarGebruiktRepository() {
        TalentenPeriode gemaakt = service.maakPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 20), schooljaar);

        assertNotNull(gemaakt.getId());
        assertEquals(List.of(gemaakt), service.geefPeriodesVoorSchooljaar(schooljaar));
    }

    @Test
    void wijzigenPastGegevensAanEnSlaatZeOp() {
        TalentenPeriode periode = service.maakPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 20), schooljaar);

        service.wijzigPeriode(periode, "Periode 1", LocalDate.of(2026, 9, 2), LocalDate.of(2026, 12, 21));

        assertEquals("Periode 1", repository.zoekOpId(periode.getId()).getNaam());
        assertTrue(repository.updateAangeroepen);
    }

    @Test
    void verwijderenDelegeertNaarRepository() {
        TalentenPeriode periode = service.maakPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 20), schooljaar);

        service.verwijderPeriode(periode);

        assertTrue(repository.periodes.isEmpty());
    }

    @Test
    void domeinvalidatieWordtOngewijzigdDoorgegeven() {
        IllegalArgumentException fout = assertThrows(IllegalArgumentException.class,
                () -> service.maakPeriode("", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 20), schooljaar));

        assertEquals("De naam van een talentenperiode mag niet leeg of null zijn", fout.getMessage());
    }

    private static class TestRepository implements TalentenPeriodeRepository {
        private final List<TalentenPeriode> periodes = new ArrayList<>();
        private long volgendId = 1;
        private boolean updateAangeroepen;

        @Override
        public TalentenPeriode save(TalentenPeriode periode) {
            TalentenPeriode opgeslagen = new TalentenPeriode(volgendId++, periode.getNaam(), periode.getStartDatum(), periode.getEindDatum(), periode.getSchooljaar());
            periodes.add(opgeslagen);
            return opgeslagen;
        }

        @Override public List<TalentenPeriode> zoekAlle() { return new ArrayList<>(periodes); }
        @Override public List<TalentenPeriode> zoekVoorSchooljaar(Schooljaar schooljaar) {
            return periodes.stream().filter(p -> p.getSchooljaar().equals(schooljaar)).toList();
        }
        @Override public TalentenPeriode zoekOpId(long id) {
            return periodes.stream().filter(p -> p.getId() == id).findFirst().orElseThrow();
        }
        @Override public void update(TalentenPeriode periode) { updateAangeroepen = true; }
        @Override public void delete(TalentenPeriode periode) { periodes.removeIf(p -> p.getId().equals(periode.getId())); }
    }
}
