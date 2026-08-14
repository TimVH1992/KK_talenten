TALENTENPERIODES & SCHOOLJAREN - JAVAFX
======================================

Nieuw:
- view/talentenperiode/TalentenPeriodeView.java
- view/talentenperiode/TalentenPeriodePresenter.java

Aangepast:
- view/navigation/AppNavigator.java
- view/main/MainView.java
- view/main/MainPresenter.java

Functionaliteit:
- alle schooljaren bekijken
- actief schooljaar duidelijk tonen
- volgend opeenvolgend schooljaar automatisch toevoegen
- gepland schooljaar actief maken
- talentenperiodes per geselecteerd schooljaar bekijken
- talentenperiode aanmaken
- talentenperiode wijzigen
- talentenperiode verwijderen zolang backend dit toelaat
- schooljaar van een bestaande talentenperiode is niet wijzigbaar
- directe sidebar-navigatie naar alle bestaande views

Verwachte bestaande services:
SchooljaarService:
- List<Schooljaar> zoekAlleSchooljaren()
- Optional<Schooljaar> zoekActiefSchooljaar()
- void maakActief(Schooljaar schooljaar)
- Schooljaar voegVolgendSchooljaarToe()

TalentenPeriodeService:
- List<TalentenPeriode> geefPeriodesVoorSchooljaar(Schooljaar schooljaar)
- TalentenPeriode maakPeriode(String naam, LocalDate startDatum, LocalDate eindDatum, Schooljaar schooljaar)
- void wijzigPeriode(TalentenPeriode periode, String naam, LocalDate startDatum, LocalDate eindDatum)
- void verwijderPeriode(TalentenPeriode periode)

ApplicationConfig moet bestaande getters hebben:
- getSchooljaarService()
- getTalentenPeriodeService()

Let op voor een VOLLEDIG lege database:
De huidige SchooljaarService.voegVolgendSchooljaarToe() bouwt voort op een bestaand schooljaar.
Als er werkelijk 0 schooljaren bestaan, kan de applicatie momenteel niet zelfstandig het allereerste schooljaar bootstrappen.
Dat is een backendbeslissing en is bewust niet omzeild vanuit JavaFX.
