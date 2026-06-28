# UC-009 Toewijzing manueel aanpassen

## Use case naam

UC-009 Toewijzing manueel aanpassen

## Doel

De talentcoördinator past na de automatische verdeling een toewijzing manueel aan.

Een leerling kan manueel verplaatst worden naar een ander ingericht talent binnen dezelfde talentenperiode.

## Actoren

* Talentcoördinator

## Precondities

* De talentenperiode bestaat in het systeem.
* Er bestaat een verdelingsresultaat voor de gekozen talentenperiode.
* De leerling bestaat in het systeem.
* Het doeltalent is een ingericht talent binnen dezelfde talentenperiode.

## Postcondities

* De leerling is gekoppeld aan het gekozen ingerichte talent.
* De leerling heeft nog steeds maximaal één toewijzing binnen de talentenperiode.
* De capaciteit van het gekozen ingerichte talent wordt niet overschreden.
* De aangepaste toewijzing wordt opgeslagen met type `MANUEEL`.
* Het systeem toont de aangepaste verdeling.

## Startgebeurtenis

De talentcoördinator kiest een leerling in het verdelingsresultaat en wil de toewijzing aanpassen.

## Hoofdsucces-scenario

1. De talentcoördinator kiest een talentenperiode.
2. Het systeem toont de huidige verdeling.
3. De talentcoördinator selecteert een leerling.
4. Het systeem toont de huidige toewijzing van de leerling.
5. De talentcoördinator kiest een ander ingericht talent binnen dezelfde talentenperiode.
6. Het systeem controleert of het gekozen ingerichte talent nog capaciteit heeft.
7. Het systeem controleert of de leerling na de wijziging maximaal één toewijzing heeft binnen de talentenperiode.
8. Het systeem past de toewijzing aan.
9. Het systeem bewaart de wijziging als manuele toewijzing.
10. Het systeem toont de aangepaste verdeling.

## Alternatieve scenario's

### A1 - Het gekozen ingerichte talent heeft geen vrije plaatsen

1. Het systeem stelt vast dat de capaciteit van het gekozen ingerichte talent bereikt is.
2. Het systeem toont een foutmelding.
3. De toewijzing wordt niet aangepast.

### A2 - De leerling heeft geen bestaande toewijzing

1. Het systeem stelt vast dat de leerling nog geen toewijzing heeft binnen de talentenperiode.
2. De talentcoördinator kiest een ingericht talent.
3. Het systeem controleert de capaciteit.
4. Het systeem maakt een nieuwe manuele toewijzing aan.

### A3 - Het gekozen ingerichte talent behoort tot een andere talentenperiode

1. Het systeem stelt vast dat het gekozen ingerichte talent niet tot de geselecteerde talentenperiode behoort.
2. Het systeem toont een foutmelding.
3. De toewijzing wordt niet aangepast.

### A4 - De talentcoördinator annuleert de wijziging

1. De talentcoördinator annuleert de manuele wijziging.
2. Het systeem stopt de use case.
3. De bestaande toewijzing blijft behouden.

## Betrokken businessregels

* BR-013: Een leerling krijgt maximaal één definitieve toewijzing per talentenperiode.
* BR-014: Het maximumaantal leerlingen van een ingericht talent mag nooit overschreden worden.
* BR-017: Na de automatische verdeling mag de talentcoördinator leerlingen manueel verplaatsen.
* BR-018: Ook na een manuele wijziging mag de capaciteit van een ingericht talent niet overschreden worden.
* BR-019: Manuele wijzigingen worden bewaard zodat later zichtbaar is welke toewijzingen automatisch en welke manueel gebeurden.
* BR-020: Een manuele wijziging vervangt de automatische toewijzing.
