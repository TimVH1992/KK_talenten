# Talentontwikkeling

## Doel van het project

Dit project heeft als doel een applicatie te ontwikkelen waarmee een school de talentontwikkeling van leerlingen kan beheren.

Leerlingen kiezen elke talentenperiode drie voorkeuren voor een talent. Het systeem verdeelt de leerlingen automatisch over de beschikbare talenten aan de hand van businessregels. Nadien kan de talentcoördinator de verdeling manueel aanpassen.

## Leerdoel

Het project dient als persoonlijk leerproject en portfolio voor toekomstige sollicitaties als backend developer.

De nadruk ligt op:

- correcte modellering
- nette architectuur
- onderhoudbare code
- duidelijke businesslogica
- backend-denken vóór UI-denken

## Scope versie 1

- leerlingen beheren
- leerkrachten beheren
- talenten beheren
- talentenperiodes beheren
- ingerichte talenten beheren
- voorkeuren registreren
- automatische verdeling uitvoeren
- manuele wijzigingen uitvoeren
- historiek bewaren

Niet in versie 1:

- authenticatie
- communicatie naar ouders
- Smartschool-integratie
- uitgebreide UI

## Doelgebruikers

- Talentcoördinator
- Leerkrachten later eventueel

Leerlingen vullen voorlopig geen keuzes rechtstreeks in de applicatie in. Keuzes worden in eerste instantie geïmporteerd vanuit een Excelbestand.

## Belangrijkste domeinconcepten

- Leerling
- Leerkracht
- Talent
- TalentenPeriode
- IngerichtTalent
- VoorkeurFormulier
- Voorkeur
- Toewijzing

Een `Talent` is het algemene aanbod, bijvoorbeeld Programmeren, Koken of Voetbal.

Een `IngerichtTalent` is de concrete organisatie van een talent binnen één talentenperiode, inclusief capaciteit en begeleidende leerkracht(en).

## Architectuur

```text
Presentation
    ↓
Services
    ↓
Repositories
    ↓
Database
```

Businesslogica hoort in de servicelaag en het domeinmodel, niet rechtstreeks in de UI of database.
