package be.kdg.talenten.overzicht;

import be.kdg.talenten.domain.TalentenPeriode;

public record OverzichtGegevens(
        int aantalLeerlingen,
        TalentenPeriode talentenPeriode,
        int aantalIngerichteTalenten,
        int aantalLeerlingenMetVolledigeVoorkeuren,
        int aantalToewijzingen,
        int aantalImportProblemen
) {
}
