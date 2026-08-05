# Schooljaarselectie

De toepassing werkt in de normale verdelingsflow altijd binnen één geselecteerd schooljaar.

## Gedrag

- Een `TalentenPeriode` behoort verplicht tot precies één `Schooljaar`.
- Het geselecteerde schooljaar wordt in PostgreSQL bewaard via `schooljaren.actief`.
- Bij de volgende opstart wordt dat schooljaar automatisch opnieuw geselecteerd.
- Afgelopen, niet-actieve schooljaren blijven in de databank bewaard, maar verschijnen niet in de normale schooljaarselectie.
- Alleen de periodes en klassen van het geselecteerde schooljaar worden geladen.
- Historische toewijzingen uit oudere schooljaren beïnvloeden de automatische verdeling niet en worden niet in de gewone leerlingdetails getoond.

## Databank bijwerken

Voor een lege databank gebruik je `database/CREATE_SCRIPT.sql` en daarna `database/DEMO_DATA_KK_TALENTEN.sql`.

Voor een bestaande databank gebruik je eerst `database/MIGRATIE_SCHOOLJAAR.sql`. Controleer vooraf of alle bestaande periodes binnen schooljaar 2025-2026 of 2026-2027 vallen.

## Uitgebreide demodata

De demodata bevat:

- 4 klassen;
- 8 leerlingen per klas;
- 32 leerlingen in totaal;
- 3 voorkeuren per leerling;
- een totale capaciteit van 27 plaatsen.

Met de huidige verdeler levert Herfst 2026 bewust dit resultaat op:

- 19 eerste voorkeuren;
- 6 tweede voorkeuren;
- 2 derde voorkeuren;
- 5 niet-toegewezen leerlingen.
