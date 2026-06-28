# UC-006 Voorkeuren importeren

## Use case naam

UC-006 Voorkeuren importeren

## Doel

De talentcoördinator importeert de voorkeuren van leerlingen vanuit een Excelbestand naar het systeem.

Het Excelbestand bevat per klas een apart tabblad. Per leerling worden drie voorkeuren opgegeven voor een talentenperiode.

## Actoren

* Talentcoördinator

## Precondities

* De talentenperiode bestaat in het systeem.
* De ingerichte talenten voor de talentenperiode bestaan in het systeem.
* Het Excelbestand is beschikbaar.
* Het Excelbestand heeft een afgesproken structuur.

## Postcondities

* De geldige voorkeuren zijn opgeslagen in het systeem.
* Elke geïmporteerde leerling heeft voor de gekozen talentenperiode exact drie voorkeuren.
* De geïmporteerde voorkeuren verwijzen naar bestaande ingerichte talenten.
* Indien de import mislukt, worden er geen ongeldige voorkeuren opgeslagen.
* Het systeem toont een overzicht van het importresultaat.

## Startgebeurtenis

De talentcoördinator start de import van voorkeuren voor een talentenperiode.

## Hoofdsucces-scenario

1. De talentcoördinator kiest de talentenperiode waarvoor voorkeuren geïmporteerd moeten worden.
2. De talentcoördinator selecteert het Excelbestand.
3. Het systeem leest het Excelbestand in.
4. Het systeem leest per tabblad de leerlingen en hun drie voorkeuren.
5. Het systeem controleert of elke leerling bestaat.
6. Het systeem controleert of elke voorkeur verwijst naar een bestaand ingericht talent binnen de gekozen talentenperiode.
7. Het systeem controleert of elke leerling exact drie voorkeuren heeft opgegeven.
8. Het systeem controleert of de drie voorkeuren van elke leerling verschillend zijn.
9. Het systeem slaat de voorkeuren op.
10. Het systeem toont dat de import gelukt is.

## Alternatieve scenario's

### A1 - Excelbestand kan niet gelezen worden

1. Het systeem kan het Excelbestand niet openen of verwerken.
2. Het systeem toont een foutmelding.
3. Er worden geen voorkeuren opgeslagen.

### A2 - Excelbestand heeft een foutieve structuur

1. Het systeem merkt dat verplichte kolommen of tabbladen ontbreken.
2. Het systeem toont welke onderdelen ontbreken of fout zijn.
3. Er worden geen voorkeuren opgeslagen.

### A3 - Leerling bestaat niet in het systeem

1. Het systeem vindt een leerling uit het Excelbestand niet terug.
2. Het systeem toont welke leerling niet gevonden werd.
3. De import wordt niet uitgevoerd.
4. Er worden geen voorkeuren opgeslagen.

### A4 - Ingericht talent bestaat niet

1. Het systeem vindt een opgegeven ingericht talent niet terug.
2. Het systeem toont welke keuze ongeldig is.
3. De import wordt niet uitgevoerd.
4. Er worden geen voorkeuren opgeslagen.

### A5 - Leerling heeft minder of meer dan drie voorkeuren

1. Het systeem merkt dat een leerling geen exact drie voorkeuren heeft opgegeven.
2. Het systeem toont voor welke leerling dit probleem voorkomt.
3. De import wordt niet uitgevoerd.
4. Er worden geen voorkeuren opgeslagen.

### A6 - Leerling heeft dezelfde voorkeur meerdere keren opgegeven

1. Het systeem merkt dat een leerling hetzelfde ingericht talent meerdere keren gekozen heeft.
2. Het systeem toont voor welke leerling dit probleem voorkomt.
3. De import wordt niet uitgevoerd.
4. Er worden geen voorkeuren opgeslagen.

## Betrokken businessregels

* BR-001: Een leerling geeft per talentenperiode exact drie voorkeuren op.
* BR-002: Elke voorkeur behoort tot precies één leerling.
* BR-003: Elke voorkeur behoort tot precies één talentenperiode.
* BR-004: Elke voorkeur verwijst naar precies één ingericht talent.
* BR-005: Een leerling moet per talentenperiode drie verschillende ingerichte talenten als voorkeur opgeven.
