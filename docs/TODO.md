# TODO

## Stap 1: Analyse
- [x] Domeinmodel uitschrijven
- [x] Businessregels bepalen
- [x] Voorbeelddata maken

## Stap 2: Java model
- [x]  Leerling
- [x] Talent
- [x] TalentenPeriode
- [x] IngerichtTalent
- [x] VoorkeurFormulier
- [x] Voorkeur
- [x] Toewijzing
- [x] Testen of de klassen werken! 

## Stap 3: Verdelingslogica
- [ ] Eerste eenvoudige verdeling maken
- [ ] Capaciteit controleren
- [ ] Historiek meenemen

## Stap 4: Database
- [ ] Tabellen ontwerpen
- [ ] JDBC-connectie maken
- [ ] Repositories schrijven



#### Ontwerpkeuzes: 
- ExcelFormulier is geen domeinklasse.
- ExcelVoorkeurParser leest alleen het Excelbestand.
- VoorkeurImportService coördineert de use case.
- VoorkeurImportValidator valideert de ingelezen data.
- VoorkeurRepository slaat pas op nadat alles geldig is.
- De import is alles-of-niets.