package be.kdg.talenten.repository.inmemory;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.repository.ToewijzingRepository;

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

    @Override
    public Toewijzing zoekToewijzingVoorLeerlingEnPeriode(
            Leerling leerling,
            TalentenPeriode talentenPeriode
    ) {
        if (leerling == null) {
            throw new IllegalArgumentException("De leerling mag niet null zijn.");
        }

        if (talentenPeriode == null) {
            throw new IllegalArgumentException("De talentenperiode mag niet null zijn.");
        }

        for (Toewijzing toewijzing : opgeslagenToewijzingen) {
            boolean zelfdeLeerling = toewijzing.getLeerling() == leerling;

            boolean zelfdeTalentenPeriode =
                    toewijzing.getIngerichtTalent()
                            .getTalentenPeriode() == talentenPeriode;

            if (zelfdeLeerling && zelfdeTalentenPeriode) {
                return toewijzing;
            }
        }

        return null;
    }

    @Override
    public int telToewijzingenVoorIngerichtTalent(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Het ingerichtTalent mag niet null zijn");
        }
        int aantalToewijzingen = 0;

        for (Toewijzing toewijzing : opgeslagenToewijzingen) {
            if (toewijzing.getIngerichtTalent() == ingerichtTalent) {
                aantalToewijzingen++;
            }
        }
        return aantalToewijzingen;
    }

    @Override
    public Toewijzing save(Toewijzing toewijzing) {
        if (toewijzing == null){
            throw new IllegalArgumentException("Toewijzing die je wilt opslaan mag niet null zijn");
        }
        opgeslagenToewijzingen.add(toewijzing);
        return toewijzing;
    }

    @Override
    public Toewijzing update(Toewijzing toewijzing) {
        if (toewijzing == null) {
            throw new IllegalArgumentException("Toewijzing die je wilt updaten mag niet null zijn.");
        }

        boolean gevonden = false;

        for (Toewijzing opgeslagenToewijzing : opgeslagenToewijzingen) {
            if (opgeslagenToewijzing == toewijzing) {
                gevonden = true;
                break;
            }
        }

        if (!gevonden) {
            throw new IllegalArgumentException("De toewijzing die je wilt updaten bestaat niet.");
        }
        return toewijzing;
    }

    @Override
    public List<Toewijzing> zoekVoorPeriode(TalentenPeriode periode) {
        if (periode == null){
            throw new IllegalArgumentException("Periode mag niet null zijn");
        }
        List<Toewijzing> toewijzingenVoorPeriode = new ArrayList<>();
        for (Toewijzing toewijzing : opgeslagenToewijzingen){
            if (toewijzing.getIngerichtTalent().getTalentenPeriode() == periode){
                toewijzingenVoorPeriode.add(toewijzing);
            }
        }
        return toewijzingenVoorPeriode;
    }
}
