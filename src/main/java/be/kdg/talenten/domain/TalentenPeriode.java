package be.kdg.talenten.domain;

import java.time.LocalDate;

public class TalentenPeriode {
    private static long volgendeId = 1;

    private long id;
    private String naam;
    private LocalDate startDatum;
    private LocalDate eindDatum;

    public TalentenPeriode(String naam, LocalDate startDatum, LocalDate eindDatum) {
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

    public long getId() {
        return id;
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
