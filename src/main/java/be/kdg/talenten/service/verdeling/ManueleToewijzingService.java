package be.kdg.talenten.service.verdeling;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.repository.VoorkeurRepository;

import java.time.LocalDate;
import java.util.List;

public class ManueleToewijzingService {
    private final ToewijzingRepository toewijzingRepository;
    private final VoorkeurRepository voorkeurRepository;

    public ManueleToewijzingService(ToewijzingRepository toewijzingRepository, VoorkeurRepository voorkeurRepository) {
        if (toewijzingRepository == null) {
            throw new IllegalArgumentException("ToewijzingRepository mag niet null zijn.");
        }
        if(voorkeurRepository == null){
            throw new IllegalArgumentException("VoorkeurRepository mag niet null zijn");
        }

        this.toewijzingRepository = toewijzingRepository;
        this.voorkeurRepository = voorkeurRepository;
    }

    public Toewijzing wijzigToewijzing(
            TalentenPeriode talentenPeriode,
            Leerling leerling,
            IngerichtTalent nieuwIngerichtTalent
    ) {
        if (talentenPeriode == null) {
            throw new IllegalArgumentException("TalentenPeriode mag niet null zijn.");
        }

        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn.");
        }

        if (nieuwIngerichtTalent == null) {
            throw new IllegalArgumentException("Nieuw ingericht talent mag niet null zijn.");
        }
        if (talentenPeriode.getEindDatum().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Een afgelopen talentenperiode mag niet meer gewijzigd worden.");
        }

        if (!nieuwIngerichtTalent.getTalentenPeriode().equals(talentenPeriode)) {
            throw new IllegalArgumentException("Het ingericht talent behoort niet tot deze talentenperiode.");
        }
        if (!nieuwIngerichtTalent.isGeschiktVoor(leerling)) {
            throw new IllegalArgumentException(
                    "De leerling behoort niet tot de doelgroep van het ingericht talent."
            );
        }

        Toewijzing bestaandeToewijzing =
                toewijzingRepository.zoekToewijzingVoorLeerlingEnPeriode(leerling, talentenPeriode);

        Integer voorkeurNummer = bepaalVoorkeurNummer(leerling, talentenPeriode, nieuwIngerichtTalent);

        if (bestaandeToewijzing == null) {
            Toewijzing nieuweToewijzing = new Toewijzing(
                    leerling,
                    nieuwIngerichtTalent,
                    ToewijzingsType.MANUEEL,
                    voorkeurNummer
            );

            return toewijzingRepository.save(nieuweToewijzing);
        }

        bestaandeToewijzing.wijzigNaar(
                nieuwIngerichtTalent,
                ToewijzingsType.MANUEEL,
                voorkeurNummer
        );

        return toewijzingRepository.update(bestaandeToewijzing);
    }

    private Integer bepaalVoorkeurNummer(
            Leerling leerling,
            TalentenPeriode periode,
            IngerichtTalent gekozenTalent){

        List<Voorkeur> voorkeurenVoorLeerling = voorkeurRepository.zoekVoorLeerlingEnPeriode(leerling, periode);
        for (Voorkeur voorkeur : voorkeurenVoorLeerling){
            if (voorkeur.getIngerichtTalent().equals(gekozenTalent)){
                return voorkeur.getVoorkeurNummer();
            }
        }
        return null;
    }
}
