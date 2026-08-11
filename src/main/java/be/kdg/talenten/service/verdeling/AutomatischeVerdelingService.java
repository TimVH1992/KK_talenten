package be.kdg.talenten.service.verdeling;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.repository.VoorkeurImportProbleemRepository;
import be.kdg.talenten.repository.VoorkeurRepository;
import be.kdg.talenten.verdeling.AutomatischeVerdeler;
import be.kdg.talenten.verdeling.VerdelingsResultaat;

import java.time.LocalDate;
import java.util.List;

public class AutomatischeVerdelingService {
    private final VoorkeurRepository voorkeurRepository;
    private final ToewijzingRepository toewijzingRepository;
    private final LeerlingRepository leerlingRepository;
    private final VoorkeurImportProbleemRepository voorkeurImportProbleemRepository;

    public AutomatischeVerdelingService(VoorkeurRepository voorkeurRepository, ToewijzingRepository toewijzingRepository, LeerlingRepository leerlingRepository, VoorkeurImportProbleemRepository voorkeurImportProbleemRepository) {
        if (voorkeurRepository == null) {
            throw new IllegalArgumentException("De voorkeurRepository mag niet null zijn");
        }
        if (toewijzingRepository == null) {
            throw new IllegalArgumentException("De toewijzingRepository mag niet null zijn");
        }
        if (leerlingRepository == null) {
            throw new IllegalArgumentException("De leerlingRepository mag niet null zijn");
        }
        if (voorkeurImportProbleemRepository == null) {
            throw new IllegalArgumentException("De voorkeurImportProbleemRepository mag niet null zijn");
        }

        this.voorkeurRepository = voorkeurRepository;
        this.toewijzingRepository = toewijzingRepository;
        this.leerlingRepository = leerlingRepository;
        this.voorkeurImportProbleemRepository = voorkeurImportProbleemRepository;
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

        List<Leerling> leerlingen = leerlingRepository.zoekVoorSchooljaar(talentenPeriode.getSchooljaar());
        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(talentenPeriode);

        List<Toewijzing> historischeToewijzingen = toewijzingRepository.zoekHistorischeToewijzingenVoorSchooljaar(talentenPeriode.getSchooljaar()).stream()
                .filter(toewijzing -> toewijzing.getIngerichtTalent().getTalentenPeriode().getSchooljaar().equals(talentenPeriode.getSchooljaar()))
                .toList();

        List<Toewijzing> manueleToewijzingen = toewijzingRepository.zoekVoorPeriode(talentenPeriode).stream()
                .filter(toewijzing -> toewijzing.getToewijzingsType() == ToewijzingsType.MANUEEL)
                .toList();

        AutomatischeVerdeler verdeler = new AutomatischeVerdeler(leerlingen, voorkeuren, historischeToewijzingen, manueleToewijzingen);
        VerdelingsResultaat resultaat = verdeler.verdeel();


        for (VoorkeurImportProbleem voorkeurImportProbleem : voorkeurImportProbleemRepository.zoekVoorPeriode(talentenPeriode)){

            if (resultaat.getNietToegewezenLeerlingen().contains(voorkeurImportProbleem.getLeerling())){
                resultaat.voegImportProbleemToe(voorkeurImportProbleem);
            }
        }
        toewijzingRepository.vervangAutomatischeToewijzingenVoorPeriode(talentenPeriode, resultaat.getToewijzingen());

        return resultaat;
    }

    private void valideerTalentenPeriode(TalentenPeriode talentenPeriode) {
        if (talentenPeriode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn.");
        }
    }
}
