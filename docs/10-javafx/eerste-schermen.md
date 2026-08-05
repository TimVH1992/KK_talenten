# Eerste JavaFX-schermen

De eerste JavaFX-versie gebruikt hetzelfde MVP-principe als het Calavera-project:

```text
View ↔ Presenter → Service → Repository-interface → PostgreSQL-repository
```

## Toegevoegde onderdelen

- `TalentenApplication`: start de JavaFX-applicatie.
- `SceneManager`: bewaart één `Stage` en wisselt de root van de `Scene`.
- `MainView` en `MainPresenter`: hoofdmenu.
- `VerdelingView` en `VerdelingPresenter`: periode kiezen, verdeling bekijken, automatische verdeling starten en een leerling manueel verplaatsen.
- `TalentenPeriodeService`: levert de beschikbare talentenperiodes aan de presenter.
- `application.css`: basisopmaak.

## Starten

1. Controleer of PostgreSQL actief is en of `src/main/resources/db.properties` correct is.
2. Herlaad het Maven-project in IntelliJ.
3. Start `be.kdg.talenten.Launcher`.

De applicatie kan ook via Maven gestart worden met:

```text
mvn javafx:run
```

## Eerste flow

```text
Hoofdmenu
→ Verdeling bekijken
→ Talentenperiode kiezen
→ Ingerichte talenten en bezetting bekijken
→ Leerling selecteren
→ Doeltalent kiezen
→ Leerling verplaatsen
```

## Uitbreiding: overzicht per klas

Het scherm `Talentenverdeling` bevat nu twee tabbladen:

- **Per talent**: toont de bezetting per ingericht talent en de toegewezen leerlingen voor het geselecteerde talent.
- **Per klas**: toont alle leerlingen van de geselecteerde klas, inclusief leerlingen die nog niet toegewezen zijn.

De titel boven de rechtertabel vermeldt voortaan expliciet voor welk talent de leerlingen getoond worden. Statusmeldingen vermelden dat het aantal toegewezen leerlingen een totaal over alle ingerichte talenten is.

## Leerlinginformatie bij manuele wijziging

Na het selecteren van een leerling toont het verdelingsscherm:

- de drie voorkeuren voor de geselecteerde talentenperiode;
- de huidige toewijzing;
- eerder gevolgde talenten uit afgelopen talentenperiodes;
- het type en eventuele voorkeurnummer van de historische toewijzing.

Een leerling kan zowel in het overzicht per talent als in het overzicht per klas geselecteerd worden. Ook een nog niet toegewezen leerling kan vanuit het klasoverzicht manueel aan een ingericht talent worden toegewezen.

Het optionele script `database/DEMO_HISTORIE.sql` voegt historische demotoewijzingen toe om dit scherm te testen.


## Veilig herberekenen van de automatische verdeling

Bij een nieuwe uitvoering worden alleen de bestaande automatische toewijzingen van de gekozen periode vervangen. Manuele toewijzingen blijven bestaan en tellen mee voor de totale capaciteit en de maximumbezetting per klas. Het verwijderen en opnieuw opslaan gebeurt in één PostgreSQL-transactie, zodat een fout geen gedeeltelijke herverdeling achterlaat.
