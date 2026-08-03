package be.kdg.talenten.domain;

import java.time.LocalDate;
import java.util.Objects;

public class TalentenPeriode {


    private final Long id;
    private final String naam;
    private final LocalDate startDatum;
    private final LocalDate eindDatum;

    public TalentenPeriode(String naam, LocalDate startDatum, LocalDate eindDatum) {
        this(null, naam, startDatum, eindDatum);
    }

    public TalentenPeriode(Long id, String naam, LocalDate startDatum, LocalDate eindDatum) {
        if (id != null && id < 1){
            throw new IllegalArgumentException("Id mag niet kleiner zijn dan 1");
        }
        if (naam == null){
            throw new IllegalArgumentException("De naam van een talentenperiode mag niet null zijn");
        }
        if (startDatum == null){
            throw new IllegalArgumentException("De startDatum van een talentenPeriode mag niet null zijn");
        }
        if (eindDatum == null){
            throw new IllegalArgumentException("De einddatum van een talentenPeriode mag niet null zijn");
        }
        if (eindDatum == startDatum){
            throw new IllegalArgumentException("De start en einddatum van een talentenPeriode mogen niet gelijk zijn");
        }
        if (eindDatum.isBefore(startDatum)){
            throw new IllegalArgumentException("De einddatum van een talentenPeriode kan niet voor de startDatum komen");
        }
        this.id = id;
        this.naam = naam;
        this.startDatum = startDatum;
        this.eindDatum = eindDatum;
    }

    public LocalDate getEindDatum() {
        return eindDatum;
    }

    public String getNaam() {
        return naam;
    }

    public LocalDate getStartDatum() {
        return startDatum;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TalentenPeriode that = (TalentenPeriode) o;
        return Objects.equals(naam, that.naam) && Objects.equals(startDatum, that.startDatum) && Objects.equals(eindDatum, that.eindDatum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(naam, startDatum, eindDatum);
    }

    @Override
    public String toString() {
        return "TalentenPeriode{" +
                "id=" + id +
                ", naam='" + naam + '\'' +
                ", startDatum=" + startDatum +
                ", eindDatum=" + eindDatum +
                '}';
    }
}
