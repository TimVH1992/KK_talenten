package be.kdg.talenten.testutil;

import be.kdg.talenten.domain.Schooljaar;

import java.time.LocalDate;
import java.time.Month;

public final class TestDataFactory {
    private TestDataFactory() {
    }

    public static Schooljaar schooljaarVoorPeriode(LocalDate startDatum, LocalDate eindDatum) {
        if (startDatum == null || eindDatum == null) {
            throw new IllegalArgumentException("Datums mogen niet null zijn");
        }

        int beginJaar = startDatum.getMonthValue() >= Month.JULY.getValue() ? startDatum.getYear() : startDatum.getYear() - 1;
        String naam = beginJaar + "-" + (beginJaar + 1);
        return new Schooljaar(naam, LocalDate.of(beginJaar, 7, 1), LocalDate.of(beginJaar + 1, 6, 30));
    }
}
