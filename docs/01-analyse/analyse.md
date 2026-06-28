# Analyse

## Actoren

### Talentcoördinator

De talentcoördinator beheert de talentenperiodes, ingerichte talenten, voorkeuren en toewijzingen.

Leerlingen zijn voorlopig geen actor in het systeem, omdat zij hun voorkeuren via een Excelbestand indienen.

## Belangrijkste use cases

- Beheren van leerlingen
- Beheren van leerkrachten
- Beheren van talenten
- Beheren van talentenperiodes
- Beheren van ingerichte talenten
- Importeren van voorkeurformulieren uit Excel
- Starten van automatische verdeling
- Bekijken van verdelingsresultaat
- Manueel aanpassen van toewijzingen
- Bevestigen van definitieve toewijzingen

## Entiteiten

- Leerling
- Leerkracht
- Talent
- TalentenPeriode
- IngerichtTalent
- VoorkeurFormulier
- Voorkeur
- Toewijzing

## Businessregels eerste versie

- Elke leerling dient exact drie voorkeuren in per talentenperiode.
- De drie voorkeuren van één leerling moeten verschillend zijn.
- Een voorkeur verwijst naar een ingericht talent.
- Elk ingericht talent heeft een maximumcapaciteit.
- Een leerling krijgt maximaal één definitieve toewijzing per talentenperiode.
- Het systeem probeert zoveel mogelijk leerlingen hun eerste voorkeur te geven.
- Als de eerste voorkeur niet mogelijk is, wordt gekeken naar de tweede en daarna de derde voorkeur.
- Historiek wordt bijgehouden zodat leerlingen bij voorkeur niet telkens hetzelfde talent krijgen.
- Na automatische verdeling kan de talentcoördinator manuele wijzigingen uitvoeren.
