package be.kdg.talenten.config;

import be.kdg.talenten.repository.*;
import be.kdg.talenten.repository.postgres.*;
import be.kdg.talenten.service.AutomatischeVerdelingService;
import be.kdg.talenten.service.ManueleToewijzingService;
import be.kdg.talenten.service.TalentenPeriodeService;
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

    private final AutomatischeVerdelingService automatischeVerdelingService;
    private final ManueleToewijzingService manueleToewijzingService;
    private final VerdelingBekijkenService verdelingBekijkenService;
    private final TalentenPeriodeService talentenPeriodeService;

    public ApplicationConfig() {
        klasRepository = new PostgresKlasRepository();
        leerlingRepository = new PostgresLeerlingRepository();
        leerkrachtRepository = new PostgresLeerkrachtRepository();
        talentRepository = new PostgresTalentRepository();
        talentenPeriodeRepository = new PostgresTalentenPeriodeRepository();
        ingerichtTalentRepository = new PostgresIngerichtTalentRepository();
        voorkeurRepository = new PostgresVoorkeurRepository(leerlingRepository, ingerichtTalentRepository);
        toewijzingRepository = new PostgresToewijzingRepository(leerlingRepository, ingerichtTalentRepository);

        automatischeVerdelingService = new AutomatischeVerdelingService(voorkeurRepository, toewijzingRepository);
        manueleToewijzingService = new ManueleToewijzingService(toewijzingRepository);
        verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository, leerlingRepository);
        talentenPeriodeService = new TalentenPeriodeService(talentenPeriodeRepository);
    }

    public VerdelingBekijkenService getVerdelingBekijkenService() {
        return verdelingBekijkenService;
    }

    public ManueleToewijzingService getManueleToewijzingService() {
        return manueleToewijzingService;
    }

    public AutomatischeVerdelingService getAutomatischeVerdelingService() {
        return automatischeVerdelingService;
    }

    public TalentenPeriodeService getTalentenPeriodeService() {
        return talentenPeriodeService;
    }
}
