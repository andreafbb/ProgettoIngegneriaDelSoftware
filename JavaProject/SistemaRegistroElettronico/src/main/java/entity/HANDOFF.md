# Handoff — Sistema Registro Elettronico

Documento di riferimento per chiunque (collaboratore umano o agente AI) entri
nel progetto. Copre: dominio, architettura BCED come adottata nel corso,
mappa dei file, flow of events end-to-end (dall'accensione dell'applicazione
fino alle schermate finali, con tutti i trigger), strategia di persistenza,
convenzioni e vincoli da rispettare.

**Leggere per intero PRIMA di toccare codice.**

---

## 1. Dominio

Il progetto e' un **sistema di registro elettronico scolastico**. Gli attori
sono **Studente** e **Docente**; il "registro" e' tenuto per ogni
`ClasseVirtuale`, che contiene `Lezione`, `Compito` e `Valutazione`.

Concetti chiave:

- **`Utente`** (`@MappedSuperclass`, table-per-class): superclasse astratta
  con `id`, `nome`, `cognome`, `email`, `password`. Non e' una tabella di
  per se' (no `@Entity`), ma `Studente` e `Docente` ereditano i suoi campi.
- **`Studente`** (`@Entity`): collegato a N `ClasseVirtuale` via una
  `@ManyToMany` con tabella associativa `iscrizione`. La M2M e' `EAGER`
  (vedi sezione persistenza per la motivazione).
- **`Docente`** (`@Entity`): collegato a N `ClasseVirtuale` via
  `@OneToMany(mappedBy = "docenteReferente")`. Ogni `ClasseVirtuale` ha
  un solo `Docente` referente.
- **`ClasseVirtuale`** (`@Entity`): ha un `nome`, un `docenteReferente`
  (`@ManyToOne`, di default EAGER), e tre collezioni `@OneToMany`
  (`lezioni`, `compiti`, `valutazioni`) tutte mappate per `classeVirtuale`.
- **`Lezione` / `Compito` / `Valutazione`** (`@Entity`): elementi del
  registro, associati a una `ClasseVirtuale`. `Valutazione` aggiunge il
  riferimento allo `Studente` valutato. `Tipologia` (enum) distingue
  `PROVA_SCRITTA` / `PROVA_ORALE`.

Casi d'uso del progetto (perimetro implementativo):

- **Studente**: login (selezione classe + nome + cognome) e visualizzazione
  del proprio profilo (placeholder: nel profilo finale ci saranno voti,
  lezioni, compiti).
- **Docente**: login (selezione classe + nome + cognome del docente
  referente di quella classe) e accesso al **registro**, dove puo':
  - **Aggiornare il Registro**: aggiungere `Compito`, `Lezione`,
    `Valutazione` alla classe.
  - **Consultare il Registro**: vedere informazioni generiche
    sull'andamento.

Gli altri concetti (iscrizione studente al sistema, autenticazione con
password, ecc.) sono **fuori scope** anche se modellati a livello di
schema DB.

---

## 2. Vincoli CRITICI

- **`database/GestorePersistenza.java` e' il file del professore. NON
  modificare.** Sessioni precedenti hanno provato ad aggiungere metodi
  e l'utente ha vetato. Verificare prima di ogni edit di non puntare
  questo file.
- **`$$$setupUI$$$()`** nei `*.java` legati a `*.form` e'
  auto-generato da IntelliJ GUI Designer. **Non editare a mano**: alla
  prossima apertura del `.form` IntelliJ lo rigenera, sovrascrivendo
  le modifiche. Per cambiare il layout: edita il `.form` (XML); per
  far compilare il progetto subito (senza aspettare che IntelliJ
  rigeneri) puoi mirrorare la modifica anche nel `.java` rispettando
  i bindings.
- **`database/JpaUtil.java` e `setup/*`** sono fuori scope: non
  toccarli salvo richiesta esplicita.

---

## 3. User context

- Studente di Ingegneria Informatica, sta scrivendo questo progetto per
  l'esame di Ingegneria del Software. Lo presentera' al docente.
- Parla italiano. **Rispondere in italiano.**
- Stile di lavoro: **piano, passo passo**. Un piccolo step, motivazione,
  attesa di conferma, prossimo step. Niente "dump" di grossi edit in un
  colpo solo se non concordato.
- Preferenze: classi nominate (no anonime), pattern idiomatici, codice
  difendibile davanti al professore.
- Spesso l'utente apre i file nel proprio IDE mentre l'agente lavora —
  arrivano `system-reminder` con righe modificate. Se un `Edit` fallisce
  con "file modified", rileggere il file prima di riprovare.

---

## 4. Architettura BCED (come adottata nel corso)

Variante BCED **specifica del corso** (NON la versione "Larman pura"):

| Package      | Layer | Ruolo                                                        |
|--------------|-------|--------------------------------------------------------------|
| `app/`       | -     | Entry point (`MainAvviaApp`). EDT setup.                     |
| `boundary/`  | B     | Swing forms (passive UI, niente business logic, niente DB).  |
| `controller/`| C     | Orchestratori "Gestori*Servizi". Coordinano use case.        |
| `entity/`    | E     | Modello di dominio + **facade di accesso al DB**.            |
| `database/`  | D     | `GestorePersistenza` (CRUD generico) + `JpaUtil`.            |
| `setup/`     | -     | Setup DB (`MainTablesChek`, `MainSetupInsert`). Fuori scope. |

**Regola critica di accesso al database**:

```
Boundary  --notifica eventi-->  Controller  --invoca-->  Facade in entity/  --usa-->  database/
```

- **Solo i facade in `entity/`** (`GestoreVisualizzazione`,
  `GestoreRegistroDocente`, `GestoreAggiornamentiRegistro`, ...) parlano
  con `database/`. Espongono un'API di alto livello (`elencoClassi()`,
  `cercaStudenteInClasse(...)`, ...) e nascondono al Controller i
  dettagli di JPQL, `EntityManager`, fetch type.
- **Il Controller** non istanzia mai `new GestorePersistenza()` e non
  importa nulla da `database/`. Quando ha bisogno di dati, chiama un
  facade. Quando deve mostrare risultati, parla al Boundary.
- **Il Boundary** non importa nulla da `controller/` ne' da `database/`.
  Espone metodi: `getPanel()`, `addXListener(...)` per registrare azioni
  utente, setter per ricevere dati gia' pronti dal Controller.

Conseguenza: il punto di contatto tra UI ed evento utente e' sempre un
listener Swing registrato dal Controller sul Boundary; il punto di
contatto tra business e persistenza e' sempre un metodo di un facade.

---

## 5. Mappa dei file

### `app/`
- **`MainAvviaApp.java`** — entry point. `main` avvia su EDT
  (`SwingUtilities.invokeLater`) il metodo statico privato
  `avviaInterfaccia()`, che mostra `FormSceltaUtente`.

### `boundary/`
Tutti i form sono bound a un `.form` di IntelliJ GUI Designer (eccezione
storica: nessuna). Tutti espongono `getPanel()` (root `JPanel`) e i
listener/setter elencati.

- **`FormSceltaUtente.java`** (+ `.form`) — schermata iniziale. Bottoni
  "Studente" e "Docente".
  API: `getPanel`, `addStudenteListener(ActionListener)`,
  `addDocenteListener(ActionListener)`.
- **`FormServiziStudente.java`** (+ `.form`) — login studente. Combo
  `ClasseVirtuale` + `fieldNome` + `fieldCognome` + `labelErrore` +
  `buttonConferma`.
  API: `getPanel`, `setClassi(List<ClasseVirtuale>)`,
  `getClasseSelezionata`, `getNomeInserito`, `getCognomeInserito`,
  `mostraErrore(String)`, `pulisciErrore()`,
  `addConfermaListener(ActionListener)`.
- **`FormServiziDocente.java`** (+ `.form`) — login docente, **speculare**
  a `FormServiziStudente` (stessa struttura, titolo "Accesso Docente").
  Stessa API (`setClassi`, `getClasseSelezionata`, ecc.).
- **`FormVisualizzazioneProfilo.java`** (+ `.form`) — profilo studente.
  Per ora: bottone "Indietro" (alto-sinistra, piccolo, 80x24) + label
  centrata "Profilo di {Nome} {Cognome}".
  API: `getPanel`, `mostraProfilo(Studente, ClasseVirtuale)`,
  `addBackListener(ActionListener)`.
- **`FormGestioneRegistro.java`** (+ `.form`) — schermata principale
  docente. Layout 3 righe: Back (alto-sinistra, piccolo), togglebar
  con 2 `JToggleButton` ("Aggiorna Registro" / "Consulta Registro"
  in `ButtonGroup`), pannello `panelContenuto` con bordo che ospita
  alternativamente i sotto-pannelli `panelAggiorna` / `panelConsulta`
  (oggi placeholder con label "— da implementare").
  API: `getPanel`, `addBackListener(ActionListener)`. **Lo switch tra
  i due sotto-pannelli e' interno al Boundary** (gli `ActionListener`
  sui toggle sono registrati nel costruttore del form e chiamano
  `mostraSotto(panel)`).

### `controller/`
- **`GestoreServiziStudente.java`** — orchestra il flow studente.
  Metodi: `avvia()` (apre form login e gestisce conferma),
  `apriProfilo(Studente, ClasseVirtuale)` (apre il profilo e registra il
  back). Stub: `visualizzaProfilo()`.
- **`GestoreServiziDocente.java`** — orchestra il flow docente. Metodi:
  `avvia()` (login docente), `apriGestioneRegistro(Docente,
  ClasseVirtuale)` (apre il registro e registra il back). Stub:
  `consultaRegistro()`, `aggiornaRegistro()`.

### `entity/`
- **Modello**: `Utente`, `Studente`, `Docente`, `ClasseVirtuale`,
  `Lezione`, `Compito`, `Valutazione`, `Tipologia`.
- **Facade (parlano con `database/`)**:
  - **`GestoreVisualizzazione.java`** — facade del flow studente.
    Attributo `private final GestorePersistenza gestorePersistenza =
    new GestorePersistenza();`. Metodi attivi:
    `elencoClassi() : List<ClasseVirtuale>`,
    `cercaStudenteInClasse(String nome, String cognome,
    ClasseVirtuale classe) : Studente`. Stub futuri:
    `calcolaMediaStudente`, `visualizzaLezioni`, `visualizzaCompiti`,
    `visualizzaValutazioni`.
  - **`GestoreRegistroDocente.java`** — facade del flow docente.
    Stesso pattern, metodi attivi: `elencoClassi()`,
    `cercaDocenteDiClasse(nome, cognome, classe)`. Stub futuri:
    `registraLezione`, `registraCompito`, `registraValutazione`,
    `mostraRegistro`, `monitoraAndamento`, `calcolaMediaClasse`.
  - **`GestoreAggiornamentiRegistro.java`** — facade in roadmap per la
    sezione "Aggiorna Registro" del docente (creazione di lezioni,
    compiti, valutazioni). Attualmente tutti i metodi sono stub
    (`creaLezione`, `creaCompito`, `creaValutazione`). **Da popolare**
    nel prossimo step.

### `database/`
- **`GestorePersistenza.java`** — file del professore (NON MODIFICARE).
  Espone: `salva`, `salvaTutti`, `trovaPerId(Class, Long)`,
  `cercaPerCampo(Class, String, Object)`,
  `cercaPerCampi(Class, Map)` — genera JPQL `SELECT e FROM <Classe> e
  WHERE e.<campo> = :<param>` per ogni voce della Map (Map vuota -> no
  WHERE, ritorna tutto), `cercaPrimoPerCampi`, `aggiorna`, `elimina`.
  Apre/chiude `EntityManager` **dentro** ogni metodo.
- **`JpaUtil.java`** — singleton dell'`EntityManagerFactory`.

### `setup/`, `resources/META-INF/persistence.xml`
Fuori scope. Il commit `Popolamento iniziale DB` ha riempito il DB con
classi e utenti di test (necessario per testare il login studente/docente).

---

## 6. Flow of events end-to-end

Tutti i flow girano sull'**Event Dispatch Thread (EDT)** di Swing,
attivato in `MainAvviaApp.main` con `SwingUtilities.invokeLater`. Da
li in poi ogni click bottone, ogni `JFrame.setVisible(true)` e ogni
chiamata a facade/DB succede sull'EDT (le query sono brevi e i tempi
del progetto non richiedono ancora `SwingWorker`).

### 6.1 Avvio applicazione

**Trigger**: esecuzione di `MainAvviaApp.main(String[])` dalla JVM.

**Sequenza**:

1. `SwingUtilities.invokeLater(MainAvviaApp::avviaInterfaccia)` — sposta
   l'inizializzazione UI sull'EDT.
2. `avviaInterfaccia()`:
   1. `FormSceltaUtente form = new FormSceltaUtente();` — il costruttore
      implicito esegue il blocco initializer `{ $$$setupUI$$$(); }`, che
      popola `panel1`, `buttonStudente`, `buttonDocente`.
   2. Costruisce un `JFrame` 700x350, `EXIT_ON_CLOSE`, centrato
      (`setLocationRelativeTo(null)`), contentPane = `form.getPanel()`.
   3. Registra i listener:
      - `form.addStudenteListener(e -> { frame.dispose(); new
        GestoreServiziStudente().avvia(); });`
      - `form.addDocenteListener(e -> { frame.dispose(); new
        GestoreServiziDocente().avvia(); });`
   4. `frame.setVisible(true)`.

**Esito**: l'utente vede la schermata "Come vuoi accedere?" con due
bottoni.

### 6.2 Flow Studente

**Trigger**: click su "Studente" in `FormSceltaUtente`.

**6.2.1 Apertura del login studente**

1. Il listener registrato in `MainAvviaApp` chiude la finestra di scelta
   (`frame.dispose()`) e istanzia un nuovo `GestoreServiziStudente`,
   chiamando `avvia()`.
2. `GestoreServiziStudente.avvia()`:
   1. Crea `FormServiziStudente` e lo mette in un `JFrame` 500x400,
      `EXIT_ON_CLOSE`, centrato, titolo "Accesso Studente".
   2. Istanzia il facade: `GestoreVisualizzazione gv = new
      GestoreVisualizzazione();`. **E' l'unico punto del flow studente
      che dialoga con la persistenza.**
   3. `List<ClasseVirtuale> classi = gv.elencoClassi();` — sotto il
      cofano: `gestorePersistenza.cercaPerCampi(ClasseVirtuale.class,
      Map.of())` -> `SELECT e FROM ClasseVirtuale e`. L'`EntityManager`
      viene aperto e chiuso dentro `cercaPerCampi`.
   4. `form.setClassi(classi)` — popola la `JComboBox` (rendering via
      `ClasseVirtuale.toString()` -> `getNome()`).
   5. `form.addConfermaListener(...)` — registra il listener "Conferma".
   6. `frame.setVisible(true)`.

**6.2.2 Click su "Conferma" (validazione + ricerca studente)**

Trigger: click su `buttonConferma`.

1. `form.pulisciErrore()` — resetta la `labelErrore` (lo spazio resta
   occupato perche' inizializzato a `" "`).
2. Letture dal form: `classeSelezionata = form.getClasseSelezionata()`,
   `nome = form.getNomeInserito()` (gia' `trim()`), `cognome =
   form.getCognomeInserito()`.
3. **Guardia 1**: se `classeSelezionata == null` (caso possibile solo se
   il DB non ha classi -> combo vuota) -> `mostraErrore("Selezionare una
   classe")` + return.
4. **Guardia 2**: se `nome.isEmpty() || cognome.isEmpty()` ->
   `mostraErrore("Inserire nome e cognome")` + return.
5. `Studente studente = gv.cercaStudenteInClasse(nome, cognome,
   classeSelezionata);` — sotto il cofano:
   1. `cercaPerCampi(Studente.class, Map.of("nome", nome, "cognome",
      cognome))` -> filtra al DB per nome+cognome (campi semplici,
      uguaglianza esatta case-sensitive).
   2. Filtra in Java: `for (Studente s : candidati) if
      (s.getClassi().contains(classe)) return s;`. Funziona perche':
      - `Studente.classi` e' `@ManyToMany(fetch = FetchType.EAGER)` ->
        la collezione e' gia' popolata fuori dall'`EntityManager`.
      - `ClasseVirtuale.equals/hashCode` sono overridati sull'`id` ->
        `contains(classe)` funziona anche tra entity caricate da
        `EntityManager` diversi.
6. **Guardia 3**: se `studente == null` -> `mostraErrore("Studente non
   trovato")` + return.
7. `frame.dispose()` + `apriProfilo(studente, classeSelezionata)`.

**6.2.3 Apertura profilo (`apriProfilo`)**

1. `FormVisualizzazioneProfilo profilo = new
   FormVisualizzazioneProfilo();` — costruisce il pannello con
   `buttonBack` (piccolo, 80x24, alto-sinistra) e `labelProfilo`
   (centrata).
2. `profilo.mostraProfilo(studente, classe)` — aggiorna
   `labelProfilo.setText("Profilo di " + nome + " " + cognome)`. Il
   parametro `classe` e' gia' nella firma per quando il profilo
   verra' arricchito (voti/lezioni/compiti dipendenti dalla classe);
   oggi non viene scritto in UI.
3. Wrappa in un `JFrame` 500x400, `EXIT_ON_CLOSE`, centrato.
4. Registra il **back**: `profilo.addBackListener(e -> {
   frame.dispose(); avvia(); });`. Effetto: chiude il profilo e
   riavvia il flow di login (combo ripopolata da DB, campi vuoti).
5. `frame.setVisible(true)`.

### 6.3 Flow Docente

**Trigger**: click su "Docente" in `FormSceltaUtente`.

Speculare al flow studente, con due differenze sostanziali nell'esito
del login (cerca un `Docente`, non uno `Studente`) e nella schermata
finale (gestione registro con toggle, non profilo).

**6.3.1 Apertura del login docente**

1. Il listener di `MainAvviaApp` chiude la scelta utente e chiama
   `new GestoreServiziDocente().avvia()`.
2. `GestoreServiziDocente.avvia()`:
   1. Crea `FormServiziDocente` (titolo "Accesso Docente") in un
      `JFrame` 500x400 `EXIT_ON_CLOSE`.
   2. Istanzia `GestoreRegistroDocente gr = new
      GestoreRegistroDocente();` — facade unico per il flow docente.
   3. `gr.elencoClassi()` -> popola la combo. (Sotto il cofano:
      stessa query di `GestoreVisualizzazione.elencoClassi`, **duplicata
      intenzionalmente** perche' ogni facade copre un caso d'uso ed e'
      autonomo. In futuro la "lista classi visibili al docente"
      potrebbe diventare "solo le sue classi", e il flow studente non
      verrebbe impattato.)
   4. Registra il listener Conferma e `frame.setVisible(true)`.

**6.3.2 Click su "Conferma" (validazione + ricerca docente)**

Stesse 3 guardie del flow studente, poi:

1. `Docente docente = gr.cercaDocenteDiClasse(nome, cognome,
   classeSelezionata);` — sotto il cofano:
   1. `cercaPerCampi(Docente.class, Map.of("nome", nome, "cognome",
      cognome))` -> candidati al DB.
   2. `Docente referente = classe.getDocenteReferente();` —
      accessibile fuori dall'`EntityManager` perche'
      `ClasseVirtuale.docenteReferente` e' `@ManyToOne` (EAGER di
      default in JPA).
   3. Per ciascun candidato: `if (d.equals(referente)) return d;`.
      Funziona perche' `Docente.equals` e' overridato
      (nome+cognome+email, case-insensitive — stesso pattern di
      `Studente.equals`).
2. Se `docente == null` -> `mostraErrore("Docente non trovato per
   questa classe")`.
3. Altrimenti `frame.dispose()` + `apriGestioneRegistro(docente,
   classeSelezionata)`.

**6.3.3 Apertura gestione registro (`apriGestioneRegistro`)**

1. `FormGestioneRegistro form = new FormGestioneRegistro();`.
   Costruttore del Boundary:
   1. `$$$setupUI$$$` popola `panel1`, `buttonBack` (80x24),
      `toggleAggiorna`, `toggleConsulta`, `panelContenuto`.
   2. Costruttore esplicito:
      - crea `ButtonGroup` e aggiunge i due toggle (uno solo
        selezionabile alla volta);
      - `toggleAggiorna.setSelected(true)` -> stato iniziale "Aggiorna";
      - `mostraSotto(panelAggiorna)` -> mette il placeholder
        "Aggiornamento Registro — da implementare" nel `panelContenuto`;
      - registra gli `ActionListener` interni:
        `toggleAggiorna.addActionListener(e -> mostraSotto(panelAggiorna))`,
        `toggleConsulta.addActionListener(e -> mostraSotto(panelConsulta))`.
2. `JFrame` 600x450, `EXIT_ON_CLOSE`, centrato, titolo
   `"Gestione Registro — " + classe.getNome()`.
3. `form.addBackListener(e -> { frame.dispose(); avvia(); })` — il back
   torna al login docente (combo ripopolata, campi vuoti). **Non** torna
   alla scelta utente: questa e' una scelta esplicita (`FormSceltaUtente`
   e' la root, non ha back).
4. `frame.setVisible(true)`.

**6.3.4 Switch del toggle (interno al Boundary)**

Trigger: click su `toggleAggiorna` o `toggleConsulta`.

1. L'`ActionListener` registrato nel costruttore del form chiama
   `mostraSotto(panelAggiorna)` o `mostraSotto(panelConsulta)`.
2. `mostraSotto(JPanel sotto)`:
   1. `panelContenuto.removeAll();`
   2. `panelContenuto.add(sotto, BorderLayout.CENTER);`
   3. `panelContenuto.revalidate(); panelContenuto.repaint();`
3. Il Controller non viene coinvolto. **I casi d'uso interni
   (Aggiorna/Consulta) sono placeholder vuoti**; saranno implementati
   nello step successivo.

### 6.4 Back / navigazione

| Da                              | Pulsante | Verso                                |
|---------------------------------|----------|--------------------------------------|
| `FormSceltaUtente`              | — (root) | —                                    |
| `FormServiziStudente`           | —        | — (no back, scelta dell'utente)      |
| `FormVisualizzazioneProfilo`    | Indietro | `FormServiziStudente` (campi puliti) |
| `FormServiziDocente`            | —        | — (no back, scelta dell'utente)      |
| `FormGestioneRegistro`          | Indietro | `FormServiziDocente` (campi puliti)  |

L'utente ha esplicitamente scelto di non mettere il Back nei due form
di login (`FormServiziStudente`, `FormServiziDocente`). Il Back e'
presente solo nelle due schermate "destination" del flow.

---

## 7. Strategia di persistenza

`GestorePersistenza.cercaPerCampi(Class, Map)` genera JPQL del tipo
`SELECT e FROM <Classe> e WHERE e.<f1> = :p1 AND e.<f2> = :p2 ...`. La
costruzione e' generica ma **non sa filtrare su collezioni**
(`@OneToMany`, `@ManyToMany`): un'espressione tipo
`e.classi = :classeSelezionata` darebbe JPQL non valido (servirebbe
`MEMBER OF` o `JOIN`).

Conseguenza: per filtrare su collezioni si usa il pattern **filtro DB
sui campi semplici + filtro Java sulla collezione**, sfruttando:

1. **Fetch type adatto sulle relazioni necessarie**:
   - `Studente.classi` e' stato esplicitamente cambiato a
     `@ManyToMany(fetch = FetchType.EAGER)`. Ragione: dopo il
     `cercaPerCampi` l'`EntityManager` e' chiuso; accedere a una
     collezione `LAZY` fuori dalla sessione lancia
     `LazyInitializationException`. Con `EAGER` la collezione e' gia'
     inizializzata.
   - `ClasseVirtuale.docenteReferente` e' `@ManyToOne` -> EAGER di
     default in JPA -> nessuna modifica necessaria.
   - Le altre collezioni di `ClasseVirtuale` (`lezioni`, `compiti`,
     `valutazioni`, `studentiIscritti`) restano LAZY: non vengono
     accedute fuori dall'`EntityManager` nei flow attuali.
2. **`equals/hashCode` coerenti**:
   - `ClasseVirtuale.equals/hashCode`: basati su `id`. Servono perche'
     `Studente.getClassi().contains(classeSelezionata)` confronta entity
     caricate da `EntityManager` diversi.
   - `Docente.equals/hashCode`: basati su nome+cognome+email
     (case-insensitive). Stesso pattern di `Studente.equals` per
     coerenza tra le sotto-classi di `Utente`. Servono per
     `d.equals(classe.getDocenteReferente())`.

**Limitazioni note** (accettate per il perimetro del progetto):

- La ricerca per `nome`/`cognome` via `cercaPerCampi` e' **case-sensitive**
  (JPQL usa `=`). Se servira' case-insensitive nel login, andra'
  introdotto un nuovo metodo o normalizzazione.
- Le query girano sull'EDT: se i dataset crescessero, occorrerebbe
  spostarle su `SwingWorker`. Oggi non e' un problema.
- `new GestorePersistenza()` viene istanziato dentro ogni facade. Stateless,
  non ci sono leak; se in futuro si dovesse condividere l'istanza tra facade,
  il punto di iniezione potrebbe essere `MainAvviaApp`.

---

## 8. Convenzioni di codice e UI

- **Indentazione**: `boundary/` usa 4 spazi (file generati da IntelliJ);
  `entity/`, `controller/` usano tab. **Allineare allo stile del file
  esistente** quando si edita.
- **Naming Boundary**: i form si chiamano `Form<NomeCasoDUso>.java`.
- **Naming Controller**: i controller si chiamano
  `GestoreServizi<Attore>.java`.
- **Naming Facade**: i facade in `entity/` si chiamano `Gestore<Funzione>`
  (`GestoreVisualizzazione`, `GestoreRegistroDocente`,
  `GestoreAggiornamentiRegistro`).
- **Setter vs verbal method nei Boundary**:
  - Setter (`setX`) quando l'azione e' "metti questo valore in quel
    componente". Coerente con JavaBean.
  - Verbo (`mostraProfilo`, `aggiornaVista`) quando l'azione e' "renderizza
    una vista intera a partire da piu' dati". Adottato in
    `FormVisualizzazioneProfilo.mostraProfilo(Studente, ClasseVirtuale)`.
- **JFrame**:
  - Tutti i frame del progetto usano `EXIT_ON_CLOSE`: chiudere col tasto X
    qualsiasi finestra termina la JVM. Scelta esplicita per il perimetro
    del progetto.
  - Tutti i frame chiamano `setLocationRelativeTo(null)` per partire
    centrati.
- **Bottone "Indietro"**:
  - Posizione: alto-sinistra (anchor `WEST`).
  - Dimensioni: 80x24 px, font SansSerif 12 (`SIZEPOLICY_FIXED` su
    entrambi gli assi). Deve dare poco fastidio visivo, lasciando spazio
    al contenuto sotto.
  - La cella sotto al back ha `SIZEPOLICY_WANT_GROW` (Profilo) o il
    `panelContenuto` ce l'ha (Gestione Registro), per assorbire lo
    spazio rimanente.
- **Forms bound a `.form`**:
  - Lo `$$$setupUI$$$` viene rigenerato da IntelliJ all'apertura del
    `.form`. Modifiche manuali al metodo vanno perse. Per cambi al
    layout: editare il `.form`. Per far compilare *subito* (senza
    attendere IntelliJ), mirrorare la stessa struttura nel `.java`
    rispettando i binding names dal `.form`.

---

## 9. Stato attuale (cosa e' fatto)

**Infrastruttura**:
- `app/MainAvviaApp` su EDT con dispatch a `GestoreServiziStudente` /
  `GestoreServiziDocente`.
- `database/persistence.xml` e `setup/MainSetupInsert` per popolare il DB.

**Flow Studente — completo end-to-end**:
- `FormSceltaUtente` -> `FormServiziStudente` -> login con validazione
  -> `FormVisualizzazioneProfilo` (placeholder con label).
- Back nel profilo riporta al login.

**Flow Docente — fino al toggle**:
- `FormSceltaUtente` -> `FormServiziDocente` -> login con validazione
  -> `FormGestioneRegistro` con toggle funzionante e sotto-pannelli
  placeholder.
- Back nel registro riporta al login docente.

**Entity tweaks effettuati**:
- `ClasseVirtuale`: `toString()` (per render combo), `equals/hashCode`
  per id, getter `getDocenteReferente()`.
- `Studente`: `@ManyToMany(fetch = FetchType.EAGER)` su `classi`,
  `equals` per nome+cognome+email.
- `Docente`: `equals/hashCode` per nome+cognome+email.

**Facade pronti**:
- `GestoreVisualizzazione` -> `elencoClassi`, `cercaStudenteInClasse`.
- `GestoreRegistroDocente` -> `elencoClassi`, `cercaDocenteDiClasse`.

---

## 10. Cosa rimane

**Prossimo step concordato**: implementare i due casi d'uso del docente
dentro `FormGestioneRegistro` (oggi placeholder):

- **Aggiorna Registro**: form di inserimento per `Compito` / `Lezione`
  / `Valutazione` da aggiungere alla classe scelta. Verra' delegato a
  `GestoreAggiornamentiRegistro` (facade gia' presente, stub).
- **Consulta Registro**: visualizzazione (probabilmente liste/tabelle)
  di Lezione/Compito/Valutazione della classe. Verra' delegato a
  `GestoreRegistroDocente` (espandendo metodi come `mostraRegistro`).

**Step successivi (oltre il toggle)**:

- Profilo studente reale: voti, lezioni, compiti, calcolo medie. Verra'
  delegato a `GestoreVisualizzazione` (`calcolaMediaStudente`,
  `visualizzaLezioni`, `visualizzaCompiti`, `visualizzaValutazioni` —
  oggi stub).
- Eventuale autenticazione vera (password): non in scope, da decidere.

---

## 11. Technical gotchas (cose gia' scoperte / non ovvie)

- `@ManyToMany` LAZY -> `LazyInitializationException` fuori
  dall'`EntityManager`. Risolto su `Studente.classi` con `EAGER`.
- `@ManyToOne` di default EAGER -> `ClasseVirtuale.docenteReferente`
  e' accessibile fuori dalla sessione senza override.
- Entity caricate da `EntityManager` diversi sono oggetti Java distinti.
  Confronto via `equals` per id (`ClasseVirtuale`) o per
  nome+cognome+email (`Studente`, `Docente`).
- `cercaPerCampi(Map.of())` (Map vuota) e' equivalente a `SELECT *`.
  Idiomatic per "elenca tutto".
- `cercaPerCampi` con campo che e' una collezione (`@OneToMany`,
  `@ManyToMany`) -> JPQL non valido. **Non si puo' fare**, serve
  filtro Java post-query.
- `getNomeInserito()` / `getCognomeInserito()` nei form fanno gia'
  `.trim()` -> nel Controller usare direttamente `.isEmpty()`.
- `BorderFactory.createTitledBorder(null, "", ...)` puo' apparire nel
  `$$$setupUI$$$` rigenerato se nel `.form` c'e' `title=""` lasciato
  dal designer. Inerte ma aggiunge padding minimo.
- `MainAvviaApp.avviaInterfaccia()` e' attualmente `private static`.
  Per consentire un eventuale "Back fino alla scelta utente" andrebbe
  reso `public static` (decisione rinviata: l'utente non vuole il Back
  in `FormSceltaUtente`).

---

## 12. Files explicitly NOT to touch

- `database/GestorePersistenza.java` — file del professore, vincolo
  esplicito dell'utente.
- `database/JpaUtil.java` — fuori scope (utility JPA, modifiche
  rischiano di rompere la persistenza).
- `setup/*` — fuori scope (popolamento DB iniziale).
- `resources/META-INF/persistence.xml` — fuori scope salvo richiesta.

---

## 13. Ordine di lettura consigliato per un nuovo collaboratore

1. Sezione 1 (Dominio) e 4 (BCED) per il contesto.
2. Sezione 6 (Flow end-to-end) per capire cosa fa l'applicazione.
3. Sezione 7 (Persistenza) per capire **perche'** ci sono `EAGER`,
   `equals` per id, filtro Java post-query (e' il punto piu' delicato).
4. Sezione 5 (Mappa file) come riferimento mentre si naviga il codice.
5. Sezioni 10, 11, 12 prima di pianificare un nuovo task.
