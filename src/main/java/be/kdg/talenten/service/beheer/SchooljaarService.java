package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.SchooljaarRepository;

import java.time.LocalDate;
import java.util.Comparator;
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

    public List<Schooljaar> zoekAlleSchooljaren() {
        return schooljaarRepository.zoekAlle();
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

    public Schooljaar voegVolgendSchooljaarToe() {
        List<Schooljaar> schooljaren = schooljaarRepository.zoekAlle();

        if (schooljaren.isEmpty()) {
            throw new IllegalStateException(
                    "Er bestaat nog geen schooljaar waarop verder gebouwd kan worden"
            );
        }

        Schooljaar laatsteSchooljaar = schooljaren.stream()
                .max(Comparator.comparing(Schooljaar::getStartDatum))
                .orElseThrow();

        int startJaar = laatsteSchooljaar.getStartDatum().getYear() + 1;
        int eindJaar = startJaar + 1;

        Schooljaar nieuwSchooljaar = new Schooljaar(
                startJaar + "-" + eindJaar,
                LocalDate.of(startJaar, 9, 1),
                LocalDate.of(eindJaar, 6, 30),
                false
        );

        return schooljaarRepository.save(nieuwSchooljaar);
    }
}