KK Talenten - JavaFX Ingerichte talenten
========================================

Deze view-package voegt het beheerscherm voor Ingerichte talenten toe.

Functionaliteit:
- filteren per talentenperiode;
- nieuw ingericht talent aanmaken;
- basistalent, periode en doelgroep kiezen bij aanmaken;
- naam, omschrijving en maximumcapaciteit wijzigen;
- actief/inactief wijzigen;
- 0, 1 of 2 leerkrachten koppelen;
- leerkrachten toevoegen/verwijderen;
- tabel wordt na wijzigingen opnieuw geladen en refreshed;
- de nieuwe centrale sidebar werkt rechtstreeks tussen alle reeds geïmplementeerde schermen.

Vereiste ApplicationConfig
--------------------------
Controleer dat ApplicationConfig de service één keer aanmaakt met dezelfde repository-instance:

private final IngerichtTalentService ingerichtTalentService;

ingerichtTalentService = new IngerichtTalentService(ingerichtTalentRepository);

public IngerichtTalentService getIngerichtTalentService() {
    return ingerichtTalentService;
}

De view gaat er daarnaast van uit dat deze bestaande getters aanwezig zijn:
- getTalentService()
- getLeerkrachtService()
- getTalentenPeriodeService()
- getLeerlingService()
- getSchooljaarService()
- getKlasService()

Belangrijk
----------
Bij een bestaand ingericht talent zijn basistalent, talentenperiode en doelgroep bewust read-only.
Naam, omschrijving, capaciteit, actief/inactief en leerkrachten kunnen wel gewijzigd worden.
Een ingericht talent mag 0 tot en met 2 leerkrachten hebben.

Klassen en Talentenperiodes zijn nog niet als CRUD-scherm gebouwd. De centrale sidebar toont daarvoor voorlopig een melding.
