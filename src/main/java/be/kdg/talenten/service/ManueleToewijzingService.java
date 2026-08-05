package be.kdg.talenten.service;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.domain.ToewijzingsType;
import be.kdg.talenten.repository.ToewijzingRepository;

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

        boolean leerlingZitAlInDitIngerichtTalent =
                bestaandeToewijzing != null
                        && bestaandeToewijzing.getIngerichtTalent() == nieuwIngerichtTalent;

        if (!leerlingZitAlInDitIngerichtTalent) {
            int huidigAantal =
                    toewijzingRepository.telToewijzingenVoorIngerichtTalent(nieuwIngerichtTalent);

            if (!nieuwIngerichtTalent.heeftVrijePlaats(huidigAantal)) {
                throw new IllegalStateException("Het ingericht talent heeft geen vrije plaatsen meer.");
            }
        }

        if (bestaandeToewijzing == null) {
            Toewijzing nieuweToewijzing = new Toewijzing(
                    leerling,
                    nieuwIngerichtTalent,
                    ToewijzingsType.MANUEEL,
                    null
            );

            toewijzingRepository.save(nieuweToewijzing);

            return nieuweToewijzing;
        }

        bestaandeToewijzing.wijzigNaar(
                nieuwIngerichtTalent,
                ToewijzingsType.MANUEEL,
                null
        );

        toewijzingRepository.update(bestaandeToewijzing);

        return bestaandeToewijzing;
    }
}