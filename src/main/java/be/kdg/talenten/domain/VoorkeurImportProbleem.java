package be.kdg.talenten.domain;

public class VoorkeurImportProbleem {
    private final Leerling leerling;
    private final TalentenPeriode periode;
    private final String reden;

    public VoorkeurImportProbleem(Leerling leerling, TalentenPeriode periode, String reden) {
        this.leerling = leerling;
        this.periode = periode;
        this.reden = reden;
    }

    public Leerling getLeerling() {
        return leerling;
    }

    public TalentenPeriode getPeriode() {
        return periode;
    }

    public String getReden() {
        return reden;
    }
}