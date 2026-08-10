package be.kdg.talenten.service.leerling;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.domain.Voorkeur;
import be.kdg.talenten.overzicht.LeerlingDetailsOverzicht;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.repository.VoorkeurRepository;

import java.util.Comparator;
import java.util.List;

public class LeerlingDetailsService {
    private final VoorkeurRepository voorkeurRepository;
    private final ToewijzingRepository toewijzingRepository;

    public LeerlingDetailsService(VoorkeurRepository voorkeurRepository, ToewijzingRepository toewijzingRepository) {
        if (voorkeurRepository == null || toewijzingRepository == null) {
            throw new IllegalArgumentException("Repositories mogen niet null zijn");
        }

        this.voorkeurRepository = voorkeurRepository;
        this.toewijzingRepository = toewijzingRepository;
    }

    public LeerlingDetailsOverzicht bekijk(Leerling leerling, TalentenPeriode periode) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }
        if (periode == null) {
            throw new IllegalArgumentException("Periode mag niet null zijn");
        }

        List<Voorkeur> voorkeuren = voorkeurRepository
                .zoekVoorLeerlingEnPeriode(leerling, periode);

        List<Toewijzing> historischeToewijzingen = toewijzingRepository
                .zoekHistorischeToewijzingenVoorLeerlingEnSchooljaar(leerling, periode.getSchooljaar())
                .stream()
                .filter(toewijzing -> !toewijzing.getIngerichtTalent().getTalentenPeriode().equals(periode))
                .sorted(Comparator.comparing(
                        (Toewijzing toewijzing) -> toewijzing
                                .getIngerichtTalent()
                                .getTalentenPeriode()
                                .getEindDatum()
                ).reversed())
                .toList();

        return new LeerlingDetailsOverzicht(leerling, periode, voorkeuren, historischeToewijzingen);
    }
}
