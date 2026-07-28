package be.kdg.talenten.domain;

import java.util.ArrayList;
import java.util.List;

public class IngerichtTalent {
    private long id;
    private Talent talent;
    private TalentenPeriode talentenPeriode;
    private int maxCapaciteit;
    private List<Leerkracht> leerkrachten;
    private Doelgroep doelgroep;

    public IngerichtTalent(Talent talent, TalentenPeriode talentenPeriode, int maxCapaciteit, Doelgroep doelgroep) {
        this.talent = talent;
        this.talentenPeriode = talentenPeriode;
        this.maxCapaciteit = maxCapaciteit;
        this.leerkrachten = new ArrayList<>();
        this.doelgroep = doelgroep;
    }

    public Talent getTalent() {
        return talent;
    }

    public TalentenPeriode getTalentenPeriode() {
        return talentenPeriode;
    }

    public int getMaxCapaciteit() {
        return maxCapaciteit;
    }

    public void voegLeerkrachtToe(Leerkracht leerkracht) {
        if (leerkracht == null) {
            throw new IllegalArgumentException("Leerkracht mag niet null zijn.");
        }

        if (!leerkrachten.contains(leerkracht)) {
            leerkrachten.add(leerkracht);
        }
    }

    public List<Leerkracht> getLeerkrachten() {
        return new ArrayList<>(leerkrachten);
    }

    public void setMaxCapaciteit(int maxCapaciteit) {
        this.maxCapaciteit = maxCapaciteit;
    }

    public boolean behoortTot(TalentenPeriode talentenPeriode) {
        return this.talentenPeriode.equals(talentenPeriode);
    }

    public boolean heeftVrijePlaats(int aantalToewijzingen){
        return aantalToewijzingen < maxCapaciteit;
    }

    @Override
    public String toString() {
        return talent.getNaam() + " (" + talentenPeriode.getNaam() + ")";
    }

    public boolean isGeschiktVoor(Leerling leerling) {
        if (leerling == null) {
            throw new IllegalArgumentException(
                    "Leerling mag niet null zijn."
            );
        }

        return doelgroep == leerling.getKlas().bepaalDoelgroep();
    }
}
