package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.KlasRepository;

import java.util.List;

public class KlasService {

    private final KlasRepository klasRepository;

    public KlasService(KlasRepository klasRepository) {
        if (klasRepository == null) {
            throw new IllegalArgumentException("KlasRepository mag niet null zijn");
        }

        this.klasRepository = klasRepository;
    }

    public List<Klas> geefAlleKlassen() {
        return klasRepository.zoekAlle();
    }

    public Klas maakKlas(String naam, Schooljaar schooljaar, int leerjaar, Doelgroep doelgroep) {
        return klasRepository.save(
                new Klas(
                        naam,
                        schooljaar,
                        leerjaar,
                        doelgroep
                )
        );
    }

    public void wijzigKlas(Klas klas, String naam, int leerjaar, Doelgroep doelgroep) {
        if (klas == null) {
            throw new IllegalArgumentException("Klas mag niet null zijn");
        }

        klas.wijzigGegevens(
                naam,
                leerjaar,
                doelgroep
        );

        klasRepository.update(klas);
    }

    public void verwijderKlas(Klas klas) {
        if (klas == null) {
            throw new IllegalArgumentException("Klas mag niet null zijn");
        }

        klasRepository.delete(klas);
    }
}