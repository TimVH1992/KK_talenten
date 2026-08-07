package be.kdg.talenten.service;

import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.domain.ToewijzingsType;
import be.kdg.talenten.domain.Voorkeur;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.repository.VoorkeurRepository;
import be.kdg.talenten.verdeling.AutomatischeVerdeler;
import be.kdg.talenten.verdeling.VerdelingsResultaat;

import java.time.LocalDate;
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

    public boolean heeftBestaandeToewijzingen(TalentenPeriode talentenPeriode) {
        valideerTalentenPeriode(talentenPeriode);
        return !toewijzingRepository.zoekVoorPeriode(talentenPeriode).isEmpty();
    }

    public VerdelingsResultaat voerAutomatischeVerdelingUit(TalentenPeriode talentenPeriode) {
        valideerTalentenPeriode(talentenPeriode);
        if (talentenPeriode.getEindDatum().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Een afgelopen talentenperiode mag niet meer automatisch verdeeld worden.");
        }

        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(talentenPeriode);
        List<Toewijzing> historischeToewijzingen = toewijzingRepository.zoekHistorischeToewijzingenVoorSchooljaar(talentenPeriode.getSchooljaar()).stream()
                .filter(toewijzing -> toewijzing.getIngerichtTalent().getTalentenPeriode().getSchooljaar().equals(talentenPeriode.getSchooljaar()))
                .toList();
        List<Toewijzing> manueleToewijzingen = toewijzingRepository.zoekVoorPeriode(talentenPeriode).stream()
                .filter(toewijzing -> toewijzing.getToewijzingsType() == ToewijzingsType.MANUEEL)
                .toList();

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(voorkeuren, historischeToewijzingen, manueleToewijzingen);
        VerdelingsResultaat resultaat = verdeler.verdeel();

        toewijzingRepository.vervangAutomatischeToewijzingenVoorPeriode(talentenPeriode, resultaat.getToewijzingen());
        return resultaat;
    }

    private void valideerTalentenPeriode(TalentenPeriode talentenPeriode) {
        if (talentenPeriode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn.");
        }
    }
}
