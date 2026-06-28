# UC-007 Automatische verdeling uitvoeren

## Use case naam

UC-007 Automatische verdeling uitvoeren

## Doel

De talentcoördinator laat het systeem leerlingen automatisch verdelen over de ingerichte talenten van een gekozen talentenperiode.

Het systeem houdt rekening met de opgegeven voorkeuren, de capaciteit van de ingerichte talenten en de historiek van leerlingen.

## Actoren

* Talentcoördinator

## Precondities

* De talentenperiode bestaat in het systeem.
* De ingerichte talenten voor de talentenperiode bestaan in het systeem.
* De voorkeuren van leerlingen voor deze talentenperiode zijn geïmporteerd en geldig.
* De leerlingen bestaan in het systeem.
* De talentcoördinator heeft de talentenperiode geselecteerd.

## Postcondities

* Voor elke leerling wordt maximaal één toewijzing aangemaakt.
* Een toewijzing koppelt één leerling aan één ingericht talent.
* Het maximumaantal leerlingen van een ingericht talent wordt niet overschreden.
* Automatische toewijzingen worden opgeslagen met type `AUTOMATISCH`.
* Leerlingen die niet toegewezen konden worden, worden getoond in het resultaat.
* Het systeem toont een overzicht van de verdeling.

## Startgebeurtenis

De talentcoördinator start de automatische verdeling voor een talentenperiode.

## Hoofdsucces-scenario

1. De talentcoördinator kiest een talentenperiode.
2. De talentcoördinator start de automatische verdeling.
3. Het systeem haalt alle voorkeuren van de gekozen talentenperiode op.
4. Het systeem haalt de beschikbare ingerichte talenten van de gekozen talentenperiode op.
5. Het systeem controleert de capaciteit van de ingerichte talenten.
6. Het systeem houdt rekening met eerder gevolgde talenten van leerlingen.
7. Het systeem probeert elke leerling een zo hoog mogelijke voorkeur toe te kennen.
8. Het systeem maakt automatische toewijzingen aan.
9. Het systeem bewaart de automatische toewijzingen.
10. Het systeem toont het resultaat van de verdeling.

## Alternatieve scenario's

### A1 - Er zijn geen voorkeuren beschikbaar

1. Het systeem stelt vast dat er geen voorkeuren bestaan voor de gekozen talentenperiode.
2. Het systeem toont een foutmelding.
3. Er wordt geen verdeling uitgevoerd.

### A2 - Er zijn geen ingerichte talenten beschikbaar

1. Het systeem stelt vast dat er geen ingerichte talenten bestaan voor de gekozen talentenperiode.
2. Het systeem toont een foutmelding.
3. Er wordt geen verdeling uitgevoerd.

### A3 - Niet alle leerlingen kunnen toegewezen worden

1. Het systeem kan voor één of meerdere leerlingen geen geldige toewijzing maken.
2. Het systeem bewaart de geldige toewijzingen.
3. Het systeem toont welke leerlingen niet toegewezen konden worden.

### A4 - Er bestaat al een verdeling voor deze talentenperiode

1. Het systeem stelt vast dat er al toewijzingen bestaan voor de gekozen talentenperiode.
2. Het systeem vraagt bevestiging om de bestaande automatische verdeling te vervangen.
3. De talentcoördinator bevestigt.
4. Het systeem vervangt de bestaande automatische toewijzingen.
5. Het systeem voert de nieuwe automatische verdeling uit.

### A5 - De talentcoördinator annuleert het vervangen van een bestaande verdeling

1. Het systeem stelt vast dat er al toewijzingen bestaan voor de gekozen talentenperiode.
2. Het systeem vraagt bevestiging om de bestaande automatische verdeling te vervangen.
3. De talentcoördinator annuleert.
4. Het systeem stopt de use case.
5. De bestaande toewijzingen blijven behouden.

## Betrokken businessregels

* BR-010: De automatische verdeling probeert eerst iedere leerling zijn eerste voorkeur toe te kennen.
* BR-011: Indien de eerste voorkeur niet mogelijk is, wordt de tweede voorkeur bekeken.
* BR-012: Indien ook de tweede voorkeur niet mogelijk is, wordt de derde voorkeur bekeken.
* BR-013: Een leerling krijgt maximaal één definitieve toewijzing per talentenperiode.
* BR-014: Het maximumaantal leerlingen van een ingericht talent mag nooit overschreden worden.
* BR-015: Alle definitieve toewijzingen worden bewaard.
* BR-016: Bij een nieuwe verdeling wordt rekening gehouden met eerder gevolgde talenten.
