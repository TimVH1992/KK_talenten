package be.kdg.talenten.service;

import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.domain.Voorkeur;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.repository.VoorkeurRepository;
import be.kdg.talenten.verdeling.AutomatischeVerdeler;
import be.kdg.talenten.verdeling.VerdelingsResultaat;

import java.util.List;

public class AutomatischeVerdelingService {
    private final VoorkeurRepository voorkeurRepository;
    private final ToewijzingRepository toewijzingRepository;

    public AutomatischeVerdelingService(VoorkeurRepository voorkeurRepository, ToewijzingRepository toewijzingRepository) {
        if (voorkeurRepository == null) {
            throw new IllegalArgumentException("De voorkeurRepository mag niet null zijn");
        }
        if (toewijzingRepository == null) {
            throw new IllegalArgumentException("De toewijzingRepository mag niet null zijn");
        }
        this.voorkeurRepository = voorkeurRepository;
        this.toewijzingRepository = toewijzingRepository;
    }

    public VerdelingsResultaat voerAutomatischeVerdelingUit(TalentenPeriode talentenPeriode) {
        if (talentenPeriode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn.");
        }
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(talentenPeriode);
        List<Toewijzing> historischeToewijzingen = toewijzingRepository.zoekHistorischeToewijzingen();

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren, historischeToewijzingen);
        VerdelingsResultaat resultaat = verdeler.verdeel();

        toewijzingRepository.saveAll(resultaat.getToewijzingen());

        return resultaat;
    }
}
