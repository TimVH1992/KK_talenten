package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.LeerlingKlasHistoriekRepository;
import be.kdg.talenten.repository.LeerlingRepository;

import java.time.LocalDate;
import java.util.List;

public class LeerlingService {

    private final LeerlingRepository leerlingRepository;
    private final LeerlingKlasHistoriekRepository leerlingKlasHistoriekRepository;

    public LeerlingService(
            LeerlingRepository leerlingRepository,
            LeerlingKlasHistoriekRepository leerlingKlasHistoriekRepository
    ) {
        if (leerlingRepository == null) {
            throw new IllegalArgumentException("LeerlingRepository mag niet null zijn");
        }
        if (leerlingKlasHistoriekRepository == null) {
            throw new IllegalArgumentException("LeerlingKlasHistoriekRepository mag niet null zijn");
        }

        this.leerlingRepository = leerlingRepository;
        this.leerlingKlasHistoriekRepository = leerlingKlasHistoriekRepository;
    }

    public List<Leerling> geefLeerlingenVoorSchooljaar(Schooljaar schooljaar) {
        if (schooljaar == null) {
            throw new IllegalArgumentException("Schooljaar mag niet null zijn");
        }

        return leerlingRepository.zoekVoorSchooljaar(schooljaar);
    }

    public List<Leerling> geefActieveLeerlingenVoorSchooljaar(Schooljaar schooljaar) {
        if (schooljaar == null) {
            throw new IllegalArgumentException("Schooljaar mag niet null zijn");
        }

        return leerlingRepository.zoekVoorSchooljaar(schooljaar).stream()
                .filter(Leerling::isActief)
                .toList();
    }

    public Leerling maakLeerling(String voornaam, String achternaam, Klas klas) {
        Leerling leerling = leerlingRepository.save(
                new Leerling(voornaam, achternaam, klas)
        );

        leerlingKlasHistoriekRepository.startHistoriek(
                leerling,
                klas,
                klas.getSchooljaar().getStartDatum()
        );

        return leerling;
    }

    public void wijzigLeerling(
            Leerling leerling,
            String voornaam,
            String achternaam
    ) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }

        leerling.wijzigGegevens(
                voornaam,
                achternaam,
                leerling.getKlas()
        );

        leerlingRepository.update(leerling);
    }

    public void wijzigKlas(
            Leerling leerling,
            Klas nieuweKlas,
            LocalDate wisseldatum
    ) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn.");
        }
        if (nieuweKlas == null) {
            throw new IllegalArgumentException("Nieuwe klas mag niet null zijn.");
        }
        if (wisseldatum == null) {
            throw new IllegalArgumentException("Wisseldatum mag niet null zijn.");
        }
        if (leerling.getKlas().equals(nieuweKlas)) {
            throw new IllegalArgumentException("De leerling zit al in deze klas.");
        }

        leerlingKlasHistoriekRepository.sluitHuidigeHistoriekAf(
                leerling,
                wisseldatum
        );

        leerling.wijsKlasToe(nieuweKlas);

        leerlingRepository.update(leerling);

        leerlingKlasHistoriekRepository.startHistoriek(
                leerling,
                nieuweKlas,
                wisseldatum
        );
    }

    public void deactiveerLeerling(Leerling leerling) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }

        leerling.deactiveer();
        leerlingRepository.update(leerling);
    }

    public void activeerLeerling(Leerling leerling) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }

        leerling.activeer();
        leerlingRepository.update(leerling);
    }
}