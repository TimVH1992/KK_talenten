package be.kdg.talenten.domain;

import java.time.LocalDateTime;

public class Toewijzing {
    private static long volgendeId =1;

    private long id;
    private ToewijzingsType toewijzingsType;
    private LocalDateTime toegewezenOp;
    private LocalDateTime gewijzigdOp;

    public Toewijzing(ToewijzingsType toewijzingsType) {
        this.id = volgendeId++;
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
}
