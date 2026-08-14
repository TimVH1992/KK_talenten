package be.kdg.talenten.domain;

import java.time.LocalDate;
import java.util.Objects;

public class TalentenPeriode {
    private final Long id;
    private String naam;
    private LocalDate startDatum;
    private LocalDate eindDatum;
    private final Schooljaar schooljaar;

    public TalentenPeriode(String naam, LocalDate startDatum, LocalDate eindDatum, Schooljaar schooljaar) {
        this(null, naam, startDatum, eindDatum, schooljaar);
    }

    public TalentenPeriode(Long id, String naam, LocalDate startDatum, LocalDate eindDatum, Schooljaar schooljaar) {
        if (id != null && id < 1) {
            throw new IllegalArgumentException("Id mag niet kleiner zijn dan 1");
        }

        valideerGegevens(naam, startDatum, eindDatum, schooljaar);

        this.id = id;
        this.naam = naam;
        this.startDatum = startDatum;
        this.eindDatum = eindDatum;
        this.schooljaar = schooljaar;
    }

    public void wijzigGegevens(String naam, LocalDate startDatum, LocalDate eindDatum) {
        valideerGegevens(naam, startDatum, eindDatum, schooljaar);

        this.naam = naam;
        this.startDatum = startDatum;
        this.eindDatum = eindDatum;
    }

    private void valideerGegevens(String naam, LocalDate startDatum, LocalDate eindDatum, Schooljaar schooljaar) {
        if (naam == null || naam.isBlank()) {
            throw new IllegalArgumentException("De naam van een talentenperiode mag niet leeg of null zijn");
        }
        if (startDatum == null) {
            throw new IllegalArgumentException("De startdatum van een talentenperiode mag niet null zijn");
        }
        if (eindDatum == null) {
            throw new IllegalArgumentException("De einddatum van een talentenperiode mag niet null zijn");
        }
        if (!eindDatum.isAfter(startDatum)) {
            throw new IllegalArgumentException("De einddatum van een talentenperiode moet na de startdatum liggen");
        }
        if (schooljaar == null) {
            throw new IllegalArgumentException("Een talentenperiode moet tot een schooljaar behoren");
        }
        if (startDatum.isBefore(schooljaar.getStartDatum()) || eindDatum.isAfter(schooljaar.getEindDatum())) {
            throw new IllegalArgumentException("De talentenperiode moet volledig binnen het schooljaar vallen");
        }
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

    public Schooljaar getSchooljaar() {
        return schooljaar;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        TalentenPeriode ander = (TalentenPeriode) object;
        return Objects.equals(naam, ander.naam)
                && Objects.equals(startDatum, ander.startDatum)
                && Objects.equals(eindDatum, ander.eindDatum)
                && Objects.equals(schooljaar, ander.schooljaar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(naam, startDatum, eindDatum, schooljaar);
    }

    @Override
    public String toString() {
        return naam;
    }
}