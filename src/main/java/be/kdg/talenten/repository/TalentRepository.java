package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Talent;

import java.util.List;

public interface TalentRepository {
    Talent save (Talent talent);
    List<Talent> zoekAlle();
}
