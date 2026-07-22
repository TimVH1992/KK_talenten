package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Toewijzing;

import java.util.List;

public interface ToewijzingRepository {
    List<Toewijzing> zoekHistorischeToewijzingen();
    void saveAll(List<Toewijzing> toewijzingen);
}
