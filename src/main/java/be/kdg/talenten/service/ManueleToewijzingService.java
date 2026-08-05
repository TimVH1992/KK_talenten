package be.kdg.talenten.service;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.domain.ToewijzingsType;
import be.kdg.talenten.repository.ToewijzingRepository;

import java.time.LocalDate;

public class ManueleToewijzingService {
    private final ToewijzingRepository toewijzingRepository;

    public ManueleToewijzingService(ToewijzingRepository toewijzingRepository) {
        if (toewijzingRepository == null) {
            throw new IllegalArgumentException("ToewijzingRepository mag niet null zijn.");
        }

        this.toewijzingRepository = toewijzingRepository;
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

        if (bestaandeToewijzing == null) {
            Toewijzing nieuweToewijzing = new Toewijzing(
                    leerling,
                    nieuwIngerichtTalent,
                    ToewijzingsType.MANUEEL,
                    null
            );

            return toewijzingRepository.save(nieuweToewijzing);
        }

        bestaandeToewijzing.wijzigNaar(
                nieuwIngerichtTalent,
                ToewijzingsType.MANUEEL,
                null
        );

        return toewijzingRepository.update(bestaandeToewijzing);
    }
}
