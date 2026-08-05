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
