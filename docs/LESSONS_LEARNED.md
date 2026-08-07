#### GELEERDE LESSEN UIT DIT PROJECT
1. Domein opbouwen is altijd de eerste stap
2. Een applicatie bouw je op in lagen, je maakt per laag best een package
   3. domeinlaag
   4. verdeling - de belangrijkste use case functionaliteiten
5. testen schrijven met door JTEST toe te voegen aan je pom.xml helpt fantastich om te kijken of je code nog werkt na aanpassingen
3. Services verzamelen de functionaliteiten die je wilt 'berekenen' binnenin je domein
4. Interfaces zorgen ervoor dat je verschillende technologieen kunt uittesten om hetzelfde te bereiken, je geeft deze mee aan je services. Een goed voorbeeld hiervan is de ToewijzingRepository interface
   5. Door het nodige gedrag te verplichten in de interface kan ik een InMemory versie maken waarin ik de logica uittest met pure java code en een Postgres versie maken waarin ik de data uit een databank ophaal.


#### OPTIMALISATIE 7/8
- Net zoals bij DATABANKEN 2 met PLSQL is het belangrijk om voor zo weinig mogelijk contextswitchen te zorgen. Dus haal al je data op met een query, steek deze records in maps of lijsten (hier moet ik er tijdelijk java objecten van maken), doe je berekeningen en geef je resultaat terug