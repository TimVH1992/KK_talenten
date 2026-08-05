package be.kdg.talenten.service;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.TalentenPeriodeRepository;

import java.util.List;

public class TalentenPeriodeService {
    private final TalentenPeriodeRepository talentenPeriodeRepository;

    public TalentenPeriodeService(TalentenPeriodeRepository talentenPeriodeRepository) {
        if (talentenPeriodeRepository == null) {
            throw new IllegalArgumentException("TalentenPeriodeRepository mag niet null zijn");
        }
        this.talentenPeriodeRepository = talentenPeriodeRepository;
    }

    public List<TalentenPeriode> zoekAlle() {
        return talentenPeriodeRepository.zoekAlle();
    }

    public List<TalentenPeriode> zoekVoorSchooljaar(Schooljaar schooljaar) {
        if (schooljaar == null) {
            throw new IllegalArgumentException("Schooljaar mag niet null zijn");
        }
        return talentenPeriodeRepository.zoekVoorSchooljaar(schooljaar);
    }
}
