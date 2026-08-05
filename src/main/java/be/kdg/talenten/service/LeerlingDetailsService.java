package be.kdg.talenten.service;

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

        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode).stream()
                .filter(voorkeur -> voorkeur.getLeerling().equals(leerling))
                .sorted(Comparator.comparingInt(Voorkeur::getVoorkeurNummer))
                .toList();

        List<Toewijzing> historischeToewijzingen = toewijzingRepository.zoekHistorischeToewijzingen().stream()
                .filter(toewijzing -> toewijzing.getLeerling().equals(leerling))
                .sorted(Comparator.comparing((Toewijzing toewijzing) -> toewijzing.getIngerichtTalent().getTalentenPeriode().getEindDatum()).reversed())
                .toList();

        return new LeerlingDetailsOverzicht(leerling, periode, voorkeuren, historischeToewijzingen);
    }
}
