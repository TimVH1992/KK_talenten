package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.LeerlingKlasHistoriek;

import java.time.LocalDate;
import java.util.List;

public interface LeerlingKlasHistoriekRepository {

    void startHistoriek(Leerling leerling, Klas klas, LocalDate vanaf);

    void sluitHuidigeHistoriekAf(Leerling leerling, LocalDate tot);

    List<LeerlingKlasHistoriek> zoekVoorLeerling(Leerling leerling);

    List<LeerlingKlasHistoriek> zoekVoorKlasOpDatum(Klas klas, LocalDate datum);

    void wijzigHuidigeKlas(
            Leerling leerling,
            Klas nieuweKlas
    );
}