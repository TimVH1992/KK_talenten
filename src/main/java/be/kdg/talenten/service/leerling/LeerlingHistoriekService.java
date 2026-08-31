package be.kdg.talenten.service.leerling;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.repository.ToewijzingRepository;

import java.util.Comparator;
import java.util.List;

public class LeerlingHistoriekService {
    private final ToewijzingRepository toewijzingRepository;

    public LeerlingHistoriekService(ToewijzingRepository toewijzingRepository) {
        if (toewijzingRepository == null) throw new IllegalArgumentException("ToewijzingRepository mag niet null zijn");
        this.toewijzingRepository = toewijzingRepository;
    }

    public List<Toewijzing> geefAlleToewijzingen(Leerling leerling) {
        if (leerling == null) throw new IllegalArgumentException("Leerling mag niet null zijn");
        return toewijzingRepository.zoekAlleVoorLeerling(leerling).stream()
                .sorted(Comparator.comparing((Toewijzing toewijzing) ->
                        toewijzing.getIngerichtTalent().getTalentenPeriode().getStartDatum()).reversed())
                .toList();
    }
}
