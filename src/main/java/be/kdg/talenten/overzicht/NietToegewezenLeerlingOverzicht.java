package be.kdg.talenten.overzicht;

import be.kdg.talenten.domain.Leerling;

public record NietToegewezenLeerlingOverzicht(
        Leerling leerling,
        String volledigeNaam,
        String klasNaam
) {
}