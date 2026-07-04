package be.kdg.talenten.domain;

import java.util.List;

public class IngerichtTalent {
    private long id;
    private Talent talent;
    private TalentenPeriode talentenPeriode;
    private int maxCapaciteit;
    private List<Leerkracht> leerkrachten;

    public IngerichtTalent(Talent talent, TalentenPeriode talentenPeriode, int maxCapaciteit) {
        this.talent = talent;
        this.talentenPeriode = talentenPeriode;
        this.maxCapaciteit = maxCapaciteit;
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

    public List<Leerkracht> getLeerkrachten() {
        return leerkrachten;
    }

    public void setLeerkrachten(List<Leerkracht> leerkrachten) {
        this.leerkrachten = leerkrachten;
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
}
