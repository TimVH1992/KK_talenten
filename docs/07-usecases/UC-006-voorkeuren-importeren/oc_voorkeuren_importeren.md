# Operation contract — UC-006 Voorkeuren importeren
**Contract CO1 : voorkeuren_importeren**

**Operatie: importeerVoorkeuren(talentenPeriode, excelBestand)**

## Kruisverwijzing

**Use Case(s): voorkeuren_importeren**

## Precondities

- TalentenPeriode tp is aanwezig
- Het excelbestand bevat per leerling drie opgegeven voorkeuren

## Postcondities
- Voor elke leerling uit het Excelbestand worden drie Voorkeur-objecten aangemaakt
- Elke Voorkeur wordt gekoppeld aan precies een TalentenPeriode
- Elke Voorkeur verwijst naar precies een IngerichtTalent
- Elke Voorkeur krijgt een voorkeurNummer met waarde 1, 2 of 3
- Per leerling worden voor de gekozen talentenperiode exact drie voorkeuren opgeslagen
- De drie voorkeuren van een leerling verwijzen naar drie verschillende ingerichte talenten
- De voorkeuren worden persistent opgeslagen in het systeem
- Het systeem geeft een importresultaat terug