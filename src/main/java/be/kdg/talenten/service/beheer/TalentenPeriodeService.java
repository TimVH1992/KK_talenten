package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.TalentenPeriodeRepository;

import java.time.LocalDate;
import java.util.List;

public class TalentenPeriodeService {

    private final TalentenPeriodeRepository periodeRepository;

    public TalentenPeriodeService(TalentenPeriodeRepository periodeRepository) {
        if (periodeRepository == null) {
            throw new IllegalArgumentException("TalentenPeriodeRepository mag niet null zijn");
        }

        this.periodeRepository = periodeRepository;
    }

    public List<TalentenPeriode> zoekAlle() {
        return periodeRepository.zoekAlle();
    }

    public List<TalentenPeriode> geefPeriodesVoorSchooljaar(Schooljaar schooljaar) {
        return periodeRepository.zoekVoorSchooljaar(schooljaar);
    }

    public TalentenPeriode maakPeriode(String naam, LocalDate startDatum, LocalDate eindDatum, Schooljaar schooljaar) {
        TalentenPeriode periode = new TalentenPeriode(
                naam,
                startDatum,
                eindDatum,
                schooljaar
        );

        return periodeRepository.save(periode);
    }

    public void wijzigPeriode(TalentenPeriode periode, String naam, LocalDate startDatum, LocalDate eindDatum) {
        if (periode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn");
        }

        periode.wijzigGegevens(naam, startDatum, eindDatum);
        periodeRepository.update(periode);
    }

    public void verwijderPeriode(TalentenPeriode periode) {
        if (periode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn");
        }

        periodeRepository.delete(periode);
    }
}