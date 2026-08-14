Deze view-package bevat nu ook Leerkrachten beheren.

Voorwaarde in ApplicationConfig:

1. veld:
private final LeerkrachtService leerkrachtService;

2. initialisatie nadat leerkrachtRepository bestaat:
leerkrachtService = new LeerkrachtService(leerkrachtRepository);

3. getter:
public LeerkrachtService getLeerkrachtService() {
    return leerkrachtService;
}

Benodigde import:
import be.kdg.talenten.service.beheer.LeerkrachtService;

De LeerkrachtPresenter gebruikt config.getLeerkrachtService().
