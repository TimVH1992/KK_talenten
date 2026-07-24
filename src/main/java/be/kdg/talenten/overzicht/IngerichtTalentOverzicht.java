package be.kdg.talenten.overzicht;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Toewijzing;

import java.util.List;

public record IngerichtTalentOverzicht(
        IngerichtTalent ingerichtTalent,
        int aantalToegewezen,
        int aantalVrijePlaatsen,
        List<Toewijzing> toewijzingen
) {
}
