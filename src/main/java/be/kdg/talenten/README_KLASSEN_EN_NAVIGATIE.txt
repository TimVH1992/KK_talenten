KK TALENTEN — KLASSEN + NAVIGATIE

Toegevoegd:
- view/klas/KlasView.java
- view/klas/KlasPresenter.java
- Klassen beheren voor het actieve schooljaar
- klas aanmaken / wijzigen / lege klas verwijderen
- leerlingenaantal per klas
- leerlingen van geselecteerde klas bekijken
- Voornaam + Achternaam rechtstreeks uit Excel plakken
- preview via LeerlingenPlakService
- pas na geldige preview leerlingen opslaan

Navigatie:
- AppSidebar gebruikt nu één tab 'Verdelen'.
- Automatische verdeling, toewijzingen bekijken en manuele correcties blijven samen in VerdelingView.
- AppNavigator koppelt alle bestaande schermen centraal.
- Klassen is nu vanuit elke sidebar direct bereikbaar.
- De oude navigatie-overrides in LeerlingPresenter zijn verwijderd zodat ze AppNavigator niet meer overschrijven.
- klasService.zoekAlle() is in de viewlaag vervangen door klasService.geefAlleKlassen().
- Talentenperiodes blijft voorlopig 'Nog niet beschikbaar'.

Benodigd in de bestaande applicatie:
- ApplicationConfig.getKlasService()
- ApplicationConfig.getLeerlingService()
- ApplicationConfig.getSchooljaarService()
- KlasService: geefAlleKlassen(), maakKlas(...), wijzigKlas(...), verwijderKlas(...)
- service.leerling.LeerlingenPlakService
- service.leerling.LeerlingenPlakResultaat
- service.leerling.LeerlingPlakRegel

Geen extra ApplicationConfig-getter voor LeerlingenPlakService nodig:
KlasPresenter maakt hem aan met de bestaande LeerlingService.
