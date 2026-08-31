# KK Talenten — macOS installatie en packaging

Dit document beschrijft hoe je **KK Talenten** op een Mac voorbereidt als lokale desktopapplicatie voor één gebruiker.

## Doelopstelling

Uiteindelijk wil je op de Mac alleen dit nodig hebben voor dagelijks gebruik:

```text
Applications
└── Talenten KK.app

PostgreSQL
└── lokale kk_talenten database
```

IntelliJ, Maven, Codex en de broncode zijn dan niet nodig voor normaal gebruik.

---

## 1. Apple developer tools installeren

Open Terminal en voer uit:

```bash
xcode-select --install
```

---

## 2. Homebrew installeren

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

Sluit Terminal daarna volledig af en open hem opnieuw.

Controleer:

```bash
brew --version
git --version
```

---

## 3. JDK 21 installeren

```bash
brew install openjdk@21
```

Controleer:

```bash
java -version
javac -version
jpackage --version
```

Alle drie moeten beschikbaar zijn en Java 21 gebruiken.

Als `java` nog niet gevonden wordt:

```bash
echo 'export PATH="$(brew --prefix openjdk@21)/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

Controleer daarna opnieuw:

```bash
java -version
javac -version
jpackage --version
```

---

## 4. Maven installeren

```bash
brew install maven
```

Controle:

```bash
mvn --version
```

---

## 5. PostgreSQL installeren

```bash
brew install postgresql
```

Start PostgreSQL:

```bash
brew services start postgresql
```

Controle:

```bash
psql --version
```

---

## 6. Project van GitHub downloaden

Ga bijvoorbeeld naar `Documents`:

```bash
cd ~/Documents
```

Clone de repository:

```bash
git clone <JOUW-GITHUB-REPOSITORY-URL>
```

Voorbeeld:

```bash
git clone https://github.com/jouwnaam/KK_talenten.git
```

Ga daarna naar het project:

```bash
cd KK_talenten
```

Controleer:

```bash
git status
```

---

## 7. Lokale PostgreSQL-database maken

Als de applicatie de database `kk_talenten` gebruikt:

```bash
createdb kk_talenten
```

Controleer of je kunt verbinden:

```bash
psql kk_talenten
```

PostgreSQL afsluiten:

```text
\q
```

### Tabellen aanmaken

Als het project een script bevat op:

```text
database/CREATE_SCRIPT.sql
```

dan kun je dit uitvoeren met:

```bash
psql -d kk_talenten -f database/CREATE_SCRIPT.sql
```

> Controleer vóór uitvoering altijd even of dit nog het actuele pad en de juiste databaseconfiguratie zijn.

---

## 8. Project testen

Vanuit de projectmap:

```bash
mvn clean test
```

Alle tests moeten slagen.

Daarna:

```bash
mvn clean package
```

---

## 9. Codex installeren op de Mac

Alleen nodig wanneer je Codex op de Mac wilt gebruiken om de macOS-package te bouwen.

Installeer Node.js:

```bash
brew install node
```

Controle:

```bash
node --version
npm --version
```

Installeer Codex:

```bash
npm install -g @openai/codex
```

Ga naar het project:

```bash
cd ~/Documents/KK_talenten
```

Start Codex:

```bash
codex
```

---

## 10. Opdracht voor Codex om de macOS-app te bouwen

Geef Codex bijvoorbeeld deze opdracht:

```text
Analyseer dit bestaande Java 21 / JavaFX / Maven-project.

De applicatie werkt reeds op Windows en gebruikt lokaal PostgreSQL.
Deze Mac wordt de uiteindelijke computer waarop de enige gebruiker de applicatie zal gebruiken.

Maak een self-contained macOS-versie met jpackage.

Doel:
- Talenten KK.app
- indien mogelijk ook Talenten KK.dmg
- Java-runtime moet mee verpakt zijn
- de gebruiker mag geen Java, Maven of IntelliJ nodig hebben om de uiteindelijke app te starten
- behoud de bestaande PostgreSQL-configuratie
- wijzig geen businesslogica
- breek de Windows-build niet

Controleer eerst de bestaande Maven-configuratie en main class.
Geef daarna aan welke wijzigingen nodig zijn voordat je ze uitvoert.

Draai na wijzigingen de volledige Maven-testsuite.
```

---

## 11. Verwachte eindresultaten

Na een succesvolle macOS-build verwacht je bijvoorbeeld:

```text
Talenten KK.app
```

en eventueel:

```text
Talenten KK.dmg
```

De `.app` kan daarna in de map `Applications` geplaatst worden.

---

## 12. Belangrijk: database blijft lokaal

Voor deze opstelling draait alles op dezelfde Mac:

```text
Talenten KK.app
        ↓
PostgreSQL op localhost
        ↓
kk_talenten database
```

Een verbinding via bijvoorbeeld:

```text
localhost:5432
```

is daarom geschikt.

Er is geen cloudserver of externe database nodig zolang deze Mac de enige computer is waarop de applicatie gebruikt wordt.

---

## 13. Back-up

Omdat alle schoolgegevens lokaal op één Mac staan, is back-up belangrijk.

Een eenvoudige toekomstige oplossing is bijvoorbeeld:

```text
Documents/
└── Talenten KK/
    └── Backups/
```

Eventueel kan deze map via iCloud Drive gesynchroniseerd worden.

Later kan de applicatie eventueel een eigen knop **Back-up maken** krijgen die een PostgreSQL-back-up uitvoert.
