package be.kdg.talenten.domain;

import java.time.LocalDate;

public class LeerlingKlasHistoriek {

    private final Long id;
    private final Leerling leerling;
    private final Klas klas;
    private final LocalDate vanaf;
    private final LocalDate tot;

    public LeerlingKlasHistoriek(
            Long id,
            Leerling leerling,
            Klas klas,
            LocalDate vanaf,
            LocalDate tot
    ) {
        if (id != null && id < 1) {
            throw new IllegalArgumentException("ID moet groter zijn dan 0.");
        }
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn.");
        }
        if (klas == null) {
            throw new IllegalArgumentException("Klas mag niet null zijn.");
        }
        if (vanaf == null) {
            throw new IllegalArgumentException("Vanaf-datum mag niet null zijn.");
        }
        if (tot != null && !tot.isAfter(vanaf)) {
            throw new IllegalArgumentException(
                    "Tot-datum moet na de vanaf-datum liggen."
            );
        }

        this.id = id;
        this.leerling = leerling;
        this.klas = klas;
        this.vanaf = vanaf;
        this.tot = tot;
    }

    public Long getId() {
        return id;
    }

    public Leerling getLeerling() {
        return leerling;
    }

    public Klas getKlas() {
        return klas;
    }

    public LocalDate getVanaf() {
        return vanaf;
    }

    public LocalDate getTot() {
        return tot;
    }

    public boolean isHuidig() {
        return tot == null;
    }
}