package be.kdg.talenten.overzicht;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;

import java.util.List;

public record KlasOverzicht(
    Klas klas,
    TalentenPeriode periode,
    List<LeerlingToewijzingOverzicht> leerlingen){}
