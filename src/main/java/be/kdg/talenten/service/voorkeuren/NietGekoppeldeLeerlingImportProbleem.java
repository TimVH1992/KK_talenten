package be.kdg.talenten.service.voorkeuren;

import java.util.List;

public record NietGekoppeldeLeerlingImportProbleem(
        String voornaam,
        String achternaam,
        String klasNaam,
        List<String> keuzes,
        String reden
) {
    public NietGekoppeldeLeerlingImportProbleem {
        keuzes = List.copyOf(keuzes);
    }
}
