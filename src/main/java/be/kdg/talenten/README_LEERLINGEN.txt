LEERLINGENBEHEER - AANSLUITING APPLICATIONCONFIG
================================================

De view-map bevat nu:
- view/leerling/LeerlingView.java
- view/leerling/LeerlingPresenter.java
- aangepaste MainPresenter zodat "Leerlingen" opent
- refresh-fix blijft behouden voor TalentView en LeerkrachtView

De LeerlingPresenter verwacht onderstaande getter in ApplicationConfig:

    private final LeerlingService leerlingService;

In de constructor, nadat leerlingRepository is aangemaakt:

    leerlingService = new LeerlingService(leerlingRepository);

Getter:

    public LeerlingService getLeerlingService() {
        return leerlingService;
    }

Import:

    import be.kdg.talenten.service.beheer.LeerlingService;

De presenter gebruikt daarnaast bestaande getters:
- getSchooljaarService()
- getKlasService()

FUNCTIONALITEIT
==============
- Actief schooljaar wordt standaard geselecteerd.
- Andere selecteerbare schooljaren kunnen bekeken worden.
- Tabel toont voornaam, achternaam, klas, leerjaar en doelgroep.
- Nieuwe leerling toevoegen.
- Bestaande leerling wijzigen.
- Leerling naar een andere klas binnen het geselecteerde schooljaar verplaatsen.
- Tabel wordt na toevoegen/wijzigen opnieuw geladen en geforceerd refreshed.
- Sidebar op het Leerling-scherm navigeert rechtstreeks naar bestaande werkende schermen:
  Dashboard, Leerkrachten, Talenten, Voorkeuren en Verdeling.
- Nog niet geïmplementeerde schermen tonen een melding in plaats van een dode knop.
