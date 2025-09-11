# tms-event-api

API som tilbyr endepunkter for å hente eventer fra Ditt NAV sin event-cache. Autentisering skjer vha et AzureAD-token.

# Kom i gang
1. Bygg tms-event-api ved å kjøre `gradle build`
2. Start appen lokalt ved å kjøre `gradle runServer`
3. Appen nås på `http://localhost:8101/tms-event-api`
   * F.eks. via `curl http://localhost:8101/tms-event-api/internal/isAlive`

## Sett opp linting ved commit
Legg til følgende kode i githooken pre-commit (ligger i `.git/hooks`) og legg inn
koden under. (Kan være at fila må renamse fra pre-commit.sample til precommit) 
```bash
#!/bin/sh

ktlint -F
git add .
```

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på github.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-minside.
