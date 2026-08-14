package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Klas;

import java.util.List;

public interface KlasRepository {
    Klas save(Klas klas);
    List<Klas> zoekAlle();
    Klas zoekOpId(long id);
    void update(Klas klas);
    void delete(Klas klas);
}