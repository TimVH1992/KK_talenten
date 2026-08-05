package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Schooljaar;

import java.util.List;
import java.util.Optional;

public interface SchooljaarRepository {
    Schooljaar save(Schooljaar schooljaar);
    List<Schooljaar> zoekAlle();
    List<Schooljaar> zoekSelecteerbareSchooljaren();
    Optional<Schooljaar> zoekActiefSchooljaar();
    void maakActief(Schooljaar schooljaar);
}
