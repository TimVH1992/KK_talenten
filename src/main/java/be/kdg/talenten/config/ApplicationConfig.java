package be.kdg.talenten.config;

import be.kdg.talenten.repository.*;
import be.kdg.talenten.repository.postgres.*;
import be.kdg.talenten.service.*;

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
    private final KlasService klasService;
    private final LeerlingDetailsService leerlingDetailsService;

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
        klasService = new KlasService(klasRepository);
        leerlingDetailsService = new LeerlingDetailsService(voorkeurRepository, toewijzingRepository);
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

    public KlasService getKlasService() {
        return klasService;
    }

    public LeerlingDetailsService getLeerlingDetailsService() {
        return leerlingDetailsService;
    }
}
