# Geleerde lessen uit het KK Talenten-project

Dit document is een overzicht van de belangrijkste lessen die ik tijdens het bouwen van deze applicatie geleerd heb. Het gaat zowel over domeinmodellering, architectuur, testen, repositories, PostgreSQL als optimalisatie.

---

# 1. Begin bij het domein

Voor ik begin te programmeren moet ik eerst begrijpen **welke objecten er bestaan, hoe ze met elkaar verbonden zijn en welke regels er gelden**.

Voorbeelden van domeinobjecten in dit project:

- `Leerling`
- `Klas`
- `Schooljaar`
- `TalentenPeriode`
- `Talent`
- `IngerichtTalent`
- `Leerkracht`
- `Voorkeur`
- `Toewijzing`

Het domein beschrijft dus de werkelijkheid van de applicatie.

Bijvoorbeeld:

```text
Schooljaar
    ├── Klas
    │    └── Leerling
    │
    └── TalentenPeriode
          └── IngerichtTalent
                ├── Talent
                └── Leerkrachten

