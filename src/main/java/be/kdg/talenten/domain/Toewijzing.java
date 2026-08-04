package be.kdg.talenten.domain;

import java.time.LocalDateTime;

public class Toewijzing {
    private final Long id;
    private final Leerling leerling;
    private IngerichtTalent ingerichtTalent;
    private ToewijzingsType toewijzingsType;
    private final LocalDateTime toegewezenOp;
    private LocalDateTime gewijzigdOp;
    private Integer voorkeurNummer;

    public Toewijzing(Leerling leerling, IngerichtTalent ingerichtTalent, ToewijzingsType toewijzingsType) {
        this(leerling, ingerichtTalent, toewijzingsType, null);
    }

    public Toewijzing(Leerling leerling, IngerichtTalent ingerichtTalent, ToewijzingsType toewijzingsType, Integer voorkeurNummer) {
        valideerGegevens(leerling, ingerichtTalent, toewijzingsType, voorkeurNummer);

        this.id = null;
        this.leerling = leerling;
        this.ingerichtTalent = ingerichtTalent;
        this.toewijzingsType = toewijzingsType;
        this.voorkeurNummer = voorkeurNummer;
        this.toegewezenOp = LocalDateTime.now();
        this.gewijzigdOp = null;
    }

    public Toewijzing(Long id, Leerling leerling, IngerichtTalent ingerichtTalent, ToewijzingsType toewijzingsType, LocalDateTime toegewezenOp, LocalDateTime gewijzigdOp, Integer voorkeurNummer) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("Id moet groter zijn dan 0");
        }

        valideerGegevens(leerling, ingerichtTalent, toewijzingsType, voorkeurNummer);

        if (toegewezenOp == null) {
            throw new IllegalArgumentException("ToegewezenOp mag niet null zijn");
        }

        this.id = id;
        this.leerling = leerling;
        this.ingerichtTalent = ingerichtTalent;
        this.toewijzingsType = toewijzingsType;
        this.toegewezenOp = toegewezenOp;
        this.gewijzigdOp = gewijzigdOp;
        this.voorkeurNummer = voorkeurNummer;
    }

    private static void valideerGegevens(Leerling leerling, IngerichtTalent ingerichtTalent, ToewijzingsType toewijzingsType, Integer voorkeurNummer) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn");
        }
        if (toewijzingsType == null) {
            throw new IllegalArgumentException("Toewijzingstype mag niet null zijn");
        }
        if (voorkeurNummer != null && (voorkeurNummer < 1 || voorkeurNummer > 3)) {
            throw new IllegalArgumentException("Voorkeurnummer moet 1, 2, 3 of null zijn");
        }
    }

    public void wijzigNaar(IngerichtTalent ingerichtTalent, ToewijzingsType toewijzingsType, Integer voorkeurNummer) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn");
        }
        if (toewijzingsType == null) {
            throw new IllegalArgumentException("Toewijzingstype mag niet null zijn");
        }
        if (voorkeurNummer != null && (voorkeurNummer < 1 || voorkeurNummer > 3)) {
            throw new IllegalArgumentException("Voorkeurnummer moet 1, 2, 3 of null zijn");
        }

        this.ingerichtTalent = ingerichtTalent;
        this.toewijzingsType = toewijzingsType;
        this.voorkeurNummer = voorkeurNummer;
        this.gewijzigdOp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Leerling getLeerling() {
        return leerling;
    }

    public IngerichtTalent getIngerichtTalent() {
        return ingerichtTalent;
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

    public Integer getVoorkeurNummer() {
        return voorkeurNummer;
    }

    public void setGewijzigdOp(LocalDateTime gewijzigdOp) {
        this.gewijzigdOp = gewijzigdOp;
    }

    @Override
    public String toString() {
        return leerling + " -> " + ingerichtTalent + " [" + toewijzingsType + "]";
    }
}