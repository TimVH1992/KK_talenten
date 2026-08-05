package be.kdg.talenten.overzicht;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.domain.Voorkeur;

import java.util.List;

public record LeerlingDetailsOverzicht(Leerling leerling, TalentenPeriode periode, List<Voorkeur> voorkeuren, List<Toewijzing> historischeToewijzingen) {
    public LeerlingDetailsOverzicht {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }
        if (periode == null) {
            throw new IllegalArgumentException("Periode mag niet null zijn");
        }
        if (voorkeuren == null || historischeToewijzingen == null) {
            throw new IllegalArgumentException("Voorkeuren en historische toewijzingen mogen niet null zijn");
        }

        voorkeuren = List.copyOf(voorkeuren);
        historischeToewijzingen = List.copyOf(historischeToewijzingen);
    }
}
