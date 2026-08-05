package be.kdg.talenten.config;

import be.kdg.talenten.repository.*;
import be.kdg.talenten.repository.postgres.*;
import be.kdg.talenten.service.VerdelingBekijkenService;

public final class ApplicationConfig {
    private final KlasRepository klasRepository;
    private final LeerlingRepository leerlingRepository;
    private final LeerkrachtRepository leerkrachtRepository;
    private final TalentRepository talentRepository;
    private final TalentenPeriodeRepository talentenPeriodeRepository;
    private final IngerichtTalentRepository ingerichtTalentRepository;
    private final VoorkeurRepository voorkeurRepository;
    private final ToewijzingRepository toewijzingRepository;

    private final VerdelingBekijkenService verdelingBekijkenService;

    public ApplicationConfig() {
        klasRepository = new PostgresKlasRepository();
        leerlingRepository = new PostgresLeerlingRepository();
        leerkrachtRepository = new PostgresLeerkrachtRepository();
        talentRepository = new PostgresTalentRepository();
        talentenPeriodeRepository = new PostgresTalentenPeriodeRepository();
        ingerichtTalentRepository = new PostgresIngerichtTalentRepository();

        voorkeurRepository = new PostgresVoorkeurRepository(leerlingRepository, ingerichtTalentRepository);
        toewijzingRepository = new PostgresToewijzingRepository(leerlingRepository, ingerichtTalentRepository);

        verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository, leerlingRepository);
    }

    public VerdelingBekijkenService getVerdelingBekijkenService() {
        return verdelingBekijkenService;
    }
}