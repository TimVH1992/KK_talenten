# Operation contract — UC-009 Toewijzing manueel aanpassen

**Contract CO3 : Toewijzing manueel aanpassen**

**Operatie: wijzigToewijzing(talentenPeriodeId, leerlingId, ingerichtTalentId)**

## Kruisverwijzing

**Use Case(s): Toewijzing manueel aanpassen**

## Precondities
- De TalentenPeriode bestaat in het systeem.
- De Leerling bestaat in het systeem.
- Het gekozen IngerichtTalent bestaat in het systeem.
- Het gekozen IngerichtTalent behoort tot de gekozen TalentenPeriode.
- Er bestaat een verdelingsresultaat voor de gekozen talentenperiode.
- De capaciteit van het gekozen IngerichtTalent is nog niet bereikt.
- De talentcoördinator heeft een leerling en een doel-IngerichtTalent geselecteerd.

## Postcondities
- De leerling is gekoppeld aan het gekozen IngerichtTalent.
- Indien de leerling al een bestaande Toewijzing had binnen deze talentenperiode, werd deze vervangen.
- Indien de leerling nog geen bestaande Toewijzing had binnen deze talentenperiode, werd een nieuwe Toewijzing aangemaakt.
- De leerling heeft maximaal één Toewijzing binnen de gekozen talentenperiode.
- De Toewijzing is gekoppeld aan precies één Leerling.
- De Toewijzing is gekoppeld aan precies één IngerichtTalent.
- De Toewijzing krijgt het type MANUEEL.
- Het aantal toewijzingen aan het gekozen IngerichtTalent overschrijdt de maximumcapaciteit niet.
- De manuele wijziging wordt persistent opgeslagen.
- Het systeem geeft de aangepaste verdeling terug.

## Betrokken businessregels
- BR-013: Een leerling krijgt maximaal één definitieve toewijzing per talentenperiode.
- BR-014: Het maximumaantal leerlingen van een ingericht talent mag nooit overschreden worden.
- BR-017: Na de automatische verdeling mag de talentcoördinator leerlingen manueel verplaatsen.
- BR-018: Ook na een manuele wijziging mag de capaciteit van een ingericht talent niet overschreden worden.
- BR-019: Manuele wijzigingen worden bewaard zodat later zichtbaar is welke toewijzingen automatisch en welke manueel gebeurden.
- BR-020: Een manuele wijziging vervangt de automatische toewijzing.