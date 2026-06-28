# Ubiquitous Language

Dit document beschrijft de belangrijkste domeinbegrippen die binnen het project gebruikt worden. Deze terminologie wordt consequent toegepast in de analyse, het domeinmodel, de databank en de broncode.

---

## Leerling

Een leerling is een persoon die tijdens een talentenperiode deelneemt aan één talent.

Een leerling:

* dient per talentenperiode drie voorkeuren in;
* krijgt maximaal één definitieve toewijzing per talentenperiode;
* bouwt een historiek van gevolgde talenten op.

---

## Leerkracht

Een leerkracht begeleidt één of meerdere ingerichte talenten.

Een leerkracht kan:

* meerdere ingerichte talenten begeleiden;
* samen met andere leerkrachten hetzelfde ingerichte talent begeleiden.

---

## Talent

Een talent is een algemeen aanbod van de school.

Voorbeelden:

* Programmeren
* Koken
* Voetbal

Een talent bestaat los van een talentenperiode.

---

## TalentenPeriode

Een talentenperiode is een afgebakende periode waarin leerlingen één talent volgen.

Een talentenperiode:

* bevat meerdere ingerichte talenten;
* bevat de voorkeuren en toewijzingen van die periode.

---

## IngerichtTalent

Een ingericht talent is de concrete organisatie van een talent binnen één talentenperiode.

Een ingericht talent:

* verwijst naar precies één talent;
* behoort tot precies één talentenperiode;
* heeft een maximale capaciteit;
* wordt begeleid door één of meerdere leerkrachten;
* ontvangt leerlingen via toewijzingen.

---

## Voorkeur

Een voorkeur geeft aan welk ingericht talent een leerling verkiest binnen een talentenperiode.

Een voorkeur:

* verwijst naar één ingericht talent;
* heeft een voorkeurnummer (1, 2 of 3).

---

## Toewijzing

Een toewijzing is het resultaat van de automatische of manuele verdeling.

Een toewijzing:

* koppelt één leerling aan één ingericht talent;
* behoort tot één talentenperiode;
* kan automatisch of manueel tot stand gekomen zijn.

---

## Historiek

De historiek bevat de ingerichte talenten die een leerling in vorige talentenperiodes gevolgd heeft.

De historiek wordt gebruikt om toekomstige verdelingen te ondersteunen.

---

## Talentcoördinator

De talentcoördinator is de hoofdgebruiker van het systeem.

De talentcoördinator kan:

* leerlingen beheren;
* leerkrachten beheren;
* talenten beheren;
* talentenperiodes beheren;
* ingerichte talenten beheren;
* voorkeuren importeren;
* de automatische verdeling uitvoeren;
* de resultaten manueel aanpassen.

---

## ExcelKeuzebestand

Het ExcelKeuzebestand is het invoerbestand waarmee voorkeuren worden verzameld.

Kenmerken:

* bevat per klas een afzonderlijk werkblad;
* bevat voor elke leerling drie voorkeuren;
* wordt gebruikt om voorkeuren in de applicatie te importeren.

Het ExcelKeuzebestand is **geen domeinobject**, maar een extern gegevensbestand dat dient als invoer voor het systeem.
