package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.SchooljaarRepository;

import java.util.List;
import java.util.Optional;

public class SchooljaarService {
    private final SchooljaarRepository schooljaarRepository;

    public SchooljaarService(SchooljaarRepository schooljaarRepository) {
        if (schooljaarRepository == null) {
            throw new IllegalArgumentException("SchooljaarRepository mag niet null zijn");
        }
        this.schooljaarRepository = schooljaarRepository;
    }

    public List<Schooljaar> zoekSelecteerbareSchooljaren() {
        return schooljaarRepository.zoekSelecteerbareSchooljaren();
    }

    public Optional<Schooljaar> zoekActiefSchooljaar() {
        return schooljaarRepository.zoekActiefSchooljaar();
    }

    public void maakActief(Schooljaar schooljaar) {
        if (schooljaar == null) {
            throw new IllegalArgumentException("Schooljaar mag niet null zijn");
        }
        schooljaarRepository.maakActief(schooljaar);
    }
}
