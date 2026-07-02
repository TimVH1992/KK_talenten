# Operation contract — UC-007 Automatische verdeling uitvoeren
**Contract CO2 : Automatische verdeling uitvoeren**

**Operatie: voerAutomatischeVerdelingUit(talentenPeriodeId)**

## Kruisverwijzing

**Use Case(s): automatische_verdeling_uitvoeren**

## Precondities

- De TalentenPeriode bestaat in het systeem.
- Voor de gekozen talentenperiode bestaan er IngerichtTalent-objecten.
- Elk IngerichtTalent heeft een maximumcapaciteit.
- De voorkeuren van de leerlingen voor deze talentenperiode zijn geïmporteerd en geldig.
- Elke leerling heeft voor deze talentenperiode exact drie voorkeuren.
- Elke voorkeur verwijst naar een bestaand IngerichtTalent binnen de gekozen talentenperiode.
- Er bestaat nog geen definitieve verdeling voor deze talentenperiode, of de talentcoördinator heeft bevestigd dat de bestaande automatische verdeling vervangen mag worden.

## Postcondities
- Voor elke leerling die toegewezen kan worden, wordt een Toewijzing aangemaakt.
- Elke aangemaakte Toewijzing wordt gekoppeld aan precies één Leerling.
- Elke aangemaakte Toewijzing wordt gekoppeld aan precies één IngerichtTalent.
- Elke aangemaakte Toewijzing krijgt het type AUTOMATISCH.
- Een leerling heeft maximaal één Toewijzing binnen de gekozen talentenperiode.
- Het aantal toewijzingen aan een IngerichtTalent overschrijdt de maximumcapaciteit niet.
- Bij het kiezen van een toewijzing wordt rekening gehouden met de voorkeuren van de leerling.
- Bij het kiezen van een toewijzing wordt rekening gehouden met eerder gevolgde talenten.
- Leerlingen die niet toegewezen konden worden, worden opgenomen in het verdelingsresultaat.
- De aangemaakte toewijzingen worden persistent opgeslagen.
- Het systeem geeft een verdelingsresultaat terug.

## Betrokken businessregels
- BR-010: De automatische verdeling probeert eerst iedere leerling zijn eerste voorkeur toe te kennen.
- BR-011: Indien de eerste voorkeur niet mogelijk is, wordt de tweede voorkeur bekeken.
- BR-012: Indien ook de tweede voorkeur niet mogelijk is, wordt de derde voorkeur bekeken.
- BR-013: Een leerling krijgt maximaal één definitieve toewijzing per talentenperiode.
- BR-014: Het maximumaantal leerlingen van een ingericht talent mag nooit overschreden worden.
- BR-015: Alle definitieve toewijzingen worden bewaard.
- BR-016: Bij een nieuwe verdeling wordt rekening gehouden met eerder gevolgde talenten.