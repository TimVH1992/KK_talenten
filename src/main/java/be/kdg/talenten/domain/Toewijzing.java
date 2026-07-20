package be.kdg.talenten.domain;

import java.time.LocalDateTime;

public class Toewijzing {
    private static long volgendeId =1;

    private long id;
    private Leerling leerling;
    private IngerichtTalent ingerichtTalent;
    private ToewijzingsType toewijzingsType;
    private LocalDateTime toegewezenOp;
    private LocalDateTime gewijzigdOp;

    public Toewijzing(Leerling leerling, IngerichtTalent ingerichtTalent, ToewijzingsType toewijzingsType) {
        if (leerling == null){
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }
        if (ingerichtTalent == null){
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn");
        }
        if (toewijzingsType == null){
            throw new IllegalArgumentException("toewijzingstype mag niet null zijn");
        }
        this.id = volgendeId++;
        this.leerling = leerling;
        this.ingerichtTalent = ingerichtTalent;
        this.toewijzingsType = toewijzingsType;
        this.toegewezenOp = LocalDateTime.now();
        this.gewijzigdOp = null;
    }

    public ToewijzingsType getToewijzingsType() {
        return toewijzingsType;
    }

    public LocalDateTime getToegewezenOp() {
        return toegewezenOp;
    }

    public LocalDateTime getGewijzigdOp() {
        return gewijzigdOp;
    }

    public long getId() {
        return id;
    }

    public void setGewijzigdOp(LocalDateTime gewijzigdOp) {
        this.gewijzigdOp = gewijzigdOp;
    }

    public Leerling getLeerling() {
        return leerling;
    }

    public IngerichtTalent getIngerichtTalent() {
        return ingerichtTalent;
    }

    @Override
    public String toString() {
        return leerling + " -> " + ingerichtTalent + " [" + toewijzingsType + "]";
    }
}
