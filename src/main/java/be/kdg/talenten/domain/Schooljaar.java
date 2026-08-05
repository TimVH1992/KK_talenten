package be.kdg.talenten.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Schooljaar {
    private final Long id;
    private final String naam;
    private final LocalDate startDatum;
    private final LocalDate eindDatum;
    private final boolean actief;

    public Schooljaar(String naam, LocalDate startDatum, LocalDate eindDatum) {
        this(null, naam, startDatum, eindDatum, false);
    }

    public Schooljaar(String naam, LocalDate startDatum, LocalDate eindDatum, boolean actief) {
        this(null, naam, startDatum, eindDatum, actief);
    }

    public Schooljaar(Long id, String naam, LocalDate startDatum, LocalDate eindDatum, boolean actief) {
        if (id != null && id < 1) {
            throw new IllegalArgumentException("Id mag niet kleiner zijn dan 1");
        }
        if (naam == null || !naam.matches("^[0-9]{4}-[0-9]{4}$")) {
            throw new IllegalArgumentException("De naam van een schooljaar moet het formaat 2026-2027 hebben");
        }
        if (startDatum == null || eindDatum == null) {
            throw new IllegalArgumentException("De datums van een schooljaar mogen niet null zijn");
        }
        if (!eindDatum.isAfter(startDatum)) {
            throw new IllegalArgumentException("De einddatum van een schooljaar moet na de startdatum liggen");
        }

        int startJaar = Integer.parseInt(naam.substring(0, 4));
        int eindJaar = Integer.parseInt(naam.substring(5));
        if (eindJaar != startJaar + 1) {
            throw new IllegalArgumentException("Een schooljaar moet twee opeenvolgende kalenderjaren bevatten");
        }
        if (startDatum.getYear() != startJaar || eindDatum.getYear() != eindJaar) {
            throw new IllegalArgumentException("De naam van het schooljaar moet overeenkomen met de start- en einddatum");
        }

        this.id = id;
        this.naam = naam;
        this.startDatum = startDatum;
        this.eindDatum = eindDatum;
        this.actief = actief;
    }

    public Long getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public LocalDate getStartDatum() {
        return startDatum;
    }

    public LocalDate getEindDatum() {
        return eindDatum;
    }

    public boolean isActief() {
        return actief;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Schooljaar ander = (Schooljaar) object;
        return Objects.equals(naam, ander.naam) && Objects.equals(startDatum, ander.startDatum) && Objects.equals(eindDatum, ander.eindDatum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(naam, startDatum, eindDatum);
    }

    @Override
    public String toString() {
        return naam;
    }
}
