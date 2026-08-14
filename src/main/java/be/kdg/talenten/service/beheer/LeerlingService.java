package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.LeerlingRepository;

import java.util.List;

public class LeerlingService {

    private final LeerlingRepository leerlingRepository;

    public LeerlingService(LeerlingRepository leerlingRepository) {
        if (leerlingRepository == null) {
            throw new IllegalArgumentException("LeerlingRepository mag niet null zijn");
        }

        this.leerlingRepository = leerlingRepository;
    }

    public List<Leerling> geefLeerlingenVoorSchooljaar(Schooljaar schooljaar) {
        return leerlingRepository.zoekVoorSchooljaar(schooljaar);
    }

    public Leerling maakLeerling(String voornaam, String achternaam, Klas klas) {
        return leerlingRepository.save(new Leerling(voornaam, achternaam, klas));
    }

    public void wijzigLeerling(Leerling leerling, String voornaam, String achternaam, Klas klas) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }

        leerling.wijzigGegevens(voornaam, achternaam, klas);
        leerlingRepository.update(leerling);
    }
}