package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Klas;
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

    public List<Klas> zoekAlle() {
        return klasRepository.zoekAlle();
    }
}
