package be.kdg.talenten.domain;

public class Voorkeur {
    private static long volgendeId = 1;

    private long id;
    private Leerling leerling;
    private TalentenPeriode talentenPeriode;
    private IngerichtTalent ingerichtTalent;
    private int voorkeurNummer;

    public Voorkeur(Leerling leerling, TalentenPeriode talentenPeriode, IngerichtTalent ingerichtTalent, int voorkeurNummer) {
        if (leerling == null){
            throw new IllegalArgumentException("Leerling mag niet null zijn.");
        }
        if (talentenPeriode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn.");
        }

        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn.");
        }

        if (voorkeurNummer < 1 || voorkeurNummer > 3) {
            throw new IllegalArgumentException("Voorkeurnummer moet 1, 2 of 3 zijn.");
        }

        if (!ingerichtTalent.behoortTot(talentenPeriode)) {
            throw new IllegalArgumentException("Het ingericht talent behoort niet tot deze talentenperiode.");
        }
        this.id = volgendeId++;
        this.leerling = leerling;
        this.talentenPeriode = talentenPeriode;
        this.ingerichtTalent = ingerichtTalent;
        this.voorkeurNummer = voorkeurNummer;
    }

    public long getId() {
        return id;
    }

    public Leerling getLeerling() {
        return leerling;
    }

    public TalentenPeriode getTalentenPeriode() {
        return talentenPeriode;
    }

    public IngerichtTalent getIngerichtTalent() {
        return ingerichtTalent;
    }

    public int getVoorkeurNummer() {
        return voorkeurNummer;
    }

    @Override
    public String toString() {
        return "Voorkeur{" +
                "voorkeurNummer=" + voorkeurNummer +
                ", ingerichtTalent=" + ingerichtTalent +
                ", leerling=" + leerling +
                ", talentenPeriode=" + talentenPeriode +
                '}';
    }
}
