package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Toewijzing;

import java.util.ArrayList;
import java.util.List;

public class InMemoryToewijzingRepository implements ToewijzingRepository {
    private List<Toewijzing> historischeToewijzingen;
    private List<Toewijzing> opgeslagenToewijzingen;

    public InMemoryToewijzingRepository(List<Toewijzing> historischeToewijzingen) {
        this.historischeToewijzingen = historischeToewijzingen;
        this.opgeslagenToewijzingen = new ArrayList<>();
    }

    @Override
    public List<Toewijzing> zoekHistorischeToewijzingen() {
        return historischeToewijzingen;
    }

    @Override
    public void saveAll(List<Toewijzing> toewijzingen) {
        opgeslagenToewijzingen.addAll(toewijzingen);
    }

    public List<Toewijzing> getOpgeslagenToewijzingen() {
        return opgeslagenToewijzingen;
    }
}
