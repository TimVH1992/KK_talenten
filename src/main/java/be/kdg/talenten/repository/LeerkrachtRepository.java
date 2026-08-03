package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Leerkracht;

import java.util.List;

public interface LeerkrachtRepository {
    Leerkracht save(Leerkracht leerkracht);
    List<Leerkracht> zoekAlle();
}
