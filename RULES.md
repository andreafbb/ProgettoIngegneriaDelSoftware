# RULES.md - Linee Guida del Progetto

## Workflow
> [!WARNING]
> **Non lavorare direttamente su `main`.**

### Creazione Branch
```bash
git checkout -b feature/nome-feature
```

## Pull Request (Passaggi Completi)
1. Crea il branch:
   ```bash
   git checkout -b feature/nome-feature
   ```
2. Aggiungi le modifiche:
   ```bash
   git add .
   ```
3. Crea il commit:
   ```bash
   git commit -m "feat: descrizione modifica"
   ```
4. Esegui il push:
   ```bash
   git push origin feature/nome-feature
   ```
5. Vai su GitHub e clicca su **Compare & pull request**.

## Review
- Almeno **1 approvazione** obbligatoria.
- **Vietata** l'auto-approvazione.

## Vietato
> [!CAUTION]
> - Push diretto su `main`
> - Commit senza significato

## Regola Base
> [!TIP]
> Se non sei sicuro, chiedi prima di procedere.
