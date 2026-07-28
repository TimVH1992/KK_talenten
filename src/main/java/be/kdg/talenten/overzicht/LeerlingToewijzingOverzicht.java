package be.kdg.talenten.overzicht;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Toewijzing;

public record LeerlingToewijzingOverzicht (
        Leerling leerling,
        Toewijzing toewijzing
){
}
