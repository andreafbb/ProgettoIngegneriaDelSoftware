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

- **`database/GestorePersistenza.java` e' il file del professore.** I
  metodi CRUD generici originali (`salva`, `salvaTutti`, `trovaPerId`,
  `cercaPerCampo`, `cercaPerCampi`, `cercaPrimoPerCampi`, `aggiorna`,
  `elimina`) restano intoccabili: NON modificarli. Il file e' stato
  esteso dall'utente con DUE metodi domain-specific (entrambi vedi
  sezione 7): `cercaClassiPerUtente(Utente)` per il caso
  "Utente -> classi" e `cercaPerClasse(ClasseVirtuale)` per il caso
  "Classe -> studenti". Sono casi che richiedono una JOIN che
  `cercaPerCampi` non sa generare. Ulteriori estensioni dello stesso
  tipo richiedono autorizzazione esplicita dell'utente (non aggiungere
  metodi a freddo).
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
- **`MainAvviaApp.java`** — entry point. `main` prima imposta
  `UIManager.setLookAndFeel(systemLookAndFeelClassName)` (su macOS attiva
  Aqua, necessario per il segmented control di `FormAggiornaRegistro`;
  try/catch silenzioso se il L&F non c'e'), poi avvia su EDT
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
- **`FormServiziStudente.java`** (+ `.form`) — login studente. Solo
  `fieldNome` + `fieldCognome` + `labelErrore` + `buttonConferma`
  (niente combo classi: la scelta della classe avviene dopo, nel popup
  `FormSceltaClasse`).
  API: `getPanel`, `getNomeInserito`, `getCognomeInserito`,
  `mostraErrore(String)`, `pulisciErrore()`,
  `addConfermaListener(ActionListener)`.
- **`FormServiziDocente.java`** (+ `.form`) — login docente, **speculare**
  a `FormServiziStudente` (stessa struttura, titolo "Accesso Docente").
  Stessa API.
- **`FormSceltaClasse.java`** (+ `.form`) — popup modale per scegliere
  la classe DOPO il login. `JPanel` bound a `.form`; il Controller lo
  wrappa in un `JDialog` `APPLICATION_MODAL` (non un `JFrame`: e' una
  scelta secondaria sopra al login). Combo classi + bottoni
  Annulla/Conferma. Su Annulla / X il dialog si chiude restituendo
  `null`; su Conferma restituisce la classe selezionata.
  API: `getPanel`, `setClassi(List<ClasseVirtuale>)`,
  `getClasseSelezionata`, `addConfermaListener(ActionListener)`,
  `addAnnullaListener(ActionListener)`.
- **`FormVisualizzazioneProfilo.java`** (+ `.form`) — profilo studente,
  completo. Layout 4 righe: Back (alto-sinistra, 80x24), label nome
  "Profilo di {Nome} {Cognome}" centrata (font 22 bold), sub-toggle bar
  (Lezioni/Compiti/Valutazioni — identica per dimensioni/stile a quella
  del docente: 95x22, font 11 bold, hgap 0, segmented client properties
  per il look nativo macOS), e `panelContenuto` bordato che ospita
  alternativamente uno di tre `JScrollPane` (uno per tipo) che avvolgono
  altrettanti `JList<String>` sopra `DefaultListModel<String>`.
  Switch interno al Boundary via `mostraSotto(JComponent)`.
  **Trucco HTML per evitare un `ListCellRenderer` custom**: ogni elemento
  del model e' una stringa che inizia con `<html>`; il `DefaultListCellRenderer`
  e' un `JLabel` che la interpreta come HTML basico (`<b>`, `<i>`, `<br>`,
  `<font color>`), ottenendo gratuitamente l'item multi-riga (titolo bold +
  descrizione in italic grigio). Formattazione fatta dentro i setter del
  Boundary (presentazione = responsabilita' del Boundary).
  API: `getPanel`, `mostraProfilo(Studente, ClasseVirtuale)` (setta la
  label nome), `addBackListener(ActionListener)`,
  `setLezioni(List<Lezione>)` / `setCompiti(List<Compito>)` /
  `setValutazioni(List<Valutazione>)` (clear del model + ciclo con
  formattazione HTML degli item).
- **`FormGestioneRegistro.java`** (+ `.form`) — schermata principale
  docente. Layout 3 righe: Back (alto-sinistra, piccolo), togglebar
  con 2 `JToggleButton` ("Aggiorna Registro" / "Consulta Registro"
  in `ButtonGroup`, compatti 130x26 centrati con hgap 8), pannello
  `panelContenuto` con bordo che ospita alternativamente il
  `formAggiorna` (istanza di `FormAggiornaRegistro` — form di
  inserimento reale) e `panelConsulta` (ancora placeholder).
  API: `getPanel`, `addBackListener(ActionListener)`,
  `getFormAggiorna() : FormAggiornaRegistro` (lo espone al Controller
  per registrare i Salva listener). **Lo switch tra i due sotto-pannelli
  e' interno al Boundary**; `mostraSotto(JComponent)` accetta sia il
  `panelConsulta` placeholder sia il `getPanel()` di `FormAggiornaRegistro`.
- **`FormAggiornaRegistro.java`** (+ `.form`) — sotto-form del docente
  agganciato a `panelContenuto` di `FormGestioneRegistro`. Layout 2 righe:
  sub-toggle bar (Lezione/Compito/Valutazione, 95x22 ciascuno, font
  SansSerif 11 bold, hgap 0) + `panelContenuto` interno che ospita
  alternativamente i tre form di inserimento (tutti costruiti
  programmaticamente, niente `.form` per i sotto-pannelli).
  - **Sub-toggle**: client property `JButton.buttonType=segmented`
    + `segmentPosition=first/middle/last`. Su Aqua (macOS) appaiono
    come un segmented control nativo; altrove sono toggle normali.
  - **panelLezione** (`creaFormLezione`): `JSpinner` data + `JTextField`
    argomento + `JTextField` descrizione + bottone Salva + label
    messaggio.
  - **panelCompito** (`creaFormCompito`): `JTextField` titolo + `JSpinner`
    data assegnazione + `JTextField` descrizione + `JSpinner` data
    scadenza + bottone Salva + label messaggio.
  - **panelValutazione** (`creaFormValutazione`): `JSpinner` data +
    `JSpinner` numerico voto (`SpinnerNumberModel(6.0, 0.0, 10.0, 0.5)`)
    + `JTextField` descrizione + `JComboBox<Tipologia>` (riempita da
    `Tipologia.values()`) + `JComboBox<Studente>` (riempita dal
    Controller via `setStudentiClasse`) + bottone Salva + label messaggio.
  - **Spinner data**: `new SpinnerDateModel()` + `new JSpinner.DateEditor(spinner, "dd/MM/yyyy")`.
    `getXxxData() : LocalDate` converte via
    `((Date) sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()`.
  - **API per ogni sotto-caso** (Lezione/Compito/Valutazione, identico
    pattern): `getXxx` per ogni campo, `addSalvaXxxListener(ActionListener)`,
    `mostraErroreXxx(String)` (rosso), `mostraSuccessoXxx(String)` (verde),
    `pulisciMessaggioXxx()`, `pulisciCampiXxx()`. Per Valutazione in
    piu': `setStudentiClasse(List<Studente>)` per popolare la combo
    studenti.

### `controller/`
- **`GestoreServiziStudente.java`** — orchestra il flow studente.
  Campo istanza: `gv = new GestoreVisualizzazione()` (facade entity).
  Metodi:
  - `avvia()` (apre form login e gestisce conferma).
  - `apriSceltaClasse(JFrame parent, Studente, List<ClasseVirtuale>)`
    (apre il `JDialog` modale `FormSceltaClasse` con le classi dello
    studente, gestisce conferma/annulla).
  - `apriProfilo(Studente, ClasseVirtuale)` (apre il profilo, chiama
    `mostraProfilo` per la label nome, poi `visualizzaProfilo(...)` per
    popolare le 3 liste, registra il back, mostra il frame).
  - `visualizzaProfilo(FormVisualizzazioneProfilo, Studente, ClasseVirtuale)`
    privato — orchestratore della UC7: chiede al facade le 3 liste e le
    pusha al Boundary tramite `setLezioni/Compiti/Valutazioni`. Il
    Controller non vede mai `GestorePersistenza`.
- **`GestoreServiziDocente.java`** — orchestra il flow docente. Campi
  istanza: `gestoreRegistroDocente` e `gestoreAggiornamenti` (i due
  facade dell'entity, istanziati una volta sola). Metodi:
  `avvia()` (login docente, usa il campo `gestoreRegistroDocente`),
  `apriSceltaClasse(JFrame parent, Docente, List<ClasseVirtuale>)`
  (popup con le classi del docente),
  `apriGestioneRegistro(Docente, ClasseVirtuale)` (apre il registro,
  registra il back, **carica gli studenti della classe** via
  `gestoreRegistroDocente.cercaStudenti(classe)` e li passa al form
  per la combo Valutazione, registra i 3 listener Salva
  Lezione/Compito/Valutazione).
  Tre orchestratori privati che fanno la catena "crea (facade A) +
  registra (facade B)":
  `aggiornaRegistroLezione(ClasseVirtuale, LocalDate, String, String) : boolean`,
  `aggiornaRegistroCompito(ClasseVirtuale, String, LocalDate, String, LocalDate) : boolean`,
  `aggiornaRegistroValutazione(ClasseVirtuale, LocalDate, double, String, Tipologia, Studente) : boolean`.
  Stub residuo: `consultaRegistro()`.

### `entity/`
- **Modello**: `Utente`, `Studente`, `Docente`, `ClasseVirtuale`,
  `Lezione`, `Compito`, `Valutazione`, `Tipologia`.
  - `Studente.toString()` ritorna `"Nome Cognome"` (usata dalla combo
    `JComboBox<Studente>` nel form Valutazione — niente renderer custom).
  - `Lezione.data`, `Compito.dataDiAssegnazione`/`dataDiScadenza`,
    `Valutazione.data` sono `LocalDate` (non String), per coerenza con
    gli spinner `SpinnerDateModel` lato Boundary.
- **Facade (parlano con `database/`)**:
  - **`GestoreVisualizzazione.java`** — facade del flow studente.
    Attributo `private final GestorePersistenza gestorePersistenza =
    new GestorePersistenza();`. Metodi attivi:
    - `cercaStudente(String nome, String cognome) : Studente` (null se
      assente; non gestisce omonimi, prende il primo).
    - `classiDi(Studente) : List<ClasseVirtuale>` (delega a
      `cercaClassiPerUtente`).
    - `visualizzaLezioni(ClasseVirtuale) : List<Lezione>` — via
      `cercaPerCampo(Lezione.class, "classeVirtuale", classe)`.
    - `visualizzaCompiti(ClasseVirtuale) : List<Compito>` — via
      `cercaPerCampo(Compito.class, "classeVirtuale", classe)`.
    - `visualizzaValutazioni(Studente, ClasseVirtuale) : List<Valutazione>`
      — via `cercaPerCampi(Valutazione.class, Map.of("studenteValutato",
      studente, "classeVirtuale", classe))`. Filtro a 2 campi: serve per
      isolare le valutazioni di un singolo studente in una specifica
      classe (uno studente puo' essere iscritto a piu' classi).
    Stub futuro: `calcolaMediaStudente`.
  - **`GestoreRegistroDocente.java`** — facade del flow docente, copre
    sia ricerca/lettura sia persistenza:
    - `cercaDocente(nome, cognome) : Docente`
    - `classiDi(Docente) : List<ClasseVirtuale>` (delega a `cercaClassiPerUtente`)
    - `cercaStudenti(ClasseVirtuale) : List<Studente>` (delega a
      `cercaPerClasse`; serve per popolare la combo della Valutazione)
    - `registraLezione(Lezione) : boolean` (delega a `salva`)
    - `registraCompito(Compito) : boolean`
    - `registraValutazione(Valutazione) : boolean`
    Stub futuri: `mostraRegistro`, `monitoraAndamento`, `calcolaMediaClasse`.
  - **`GestoreAggiornamentiRegistro.java`** — facade "creatore" del flow
    docente (NON parla con `database/`, ha responsabilita' di sola
    costruzione di Entity, oggi: wrap del costruttore; domani:
    validazioni di dominio):
    - `creaLezione(LocalDate, String, String, ClasseVirtuale) : Lezione`
    - `creaCompito(String, LocalDate, String, LocalDate, ClasseVirtuale) : Compito`
    - `creaValutazione(LocalDate, double, String, Tipologia, ClasseVirtuale, Studente) : Valutazione`

  Nota: nessun facade espone piu' un `elencoClassi()` "global". La
  filosofia e' che il facade non e' Information Expert delle
  ClasseVirtuale globali — ritorna SOLO le classi rilevanti per uno
  specifico utente (`classiDi(Studente)` / `classiDi(Docente)`).
  Stessa filosofia per `cercaStudenti(ClasseVirtuale)`: rilevanti
  rispetto a una specifica classe.

### `database/`
- **`GestorePersistenza.java`** — file del professore (vedi sezione 2
  per la regola attuale). Espone i metodi CRUD generici originali:
  `salva`, `salvaTutti`, `trovaPerId(Class, Long)`,
  `cercaPerCampo(Class, String, Object)`,
  `cercaPerCampi(Class, Map)` — genera JPQL `SELECT e FROM <Classe> e
  WHERE e.<campo> = :<param>` per ogni voce della Map (Map vuota -> no
  WHERE, ritorna tutto), `cercaPrimoPerCampi`, `aggiorna`, `elimina`.
  **Aggiunte dall'utente (entrambe autorizzate)**:
  - `cercaClassiPerUtente(Utente) : List<ClasseVirtuale>` — JPQL ad hoc
    che parte da `ClasseVirtuale` e filtra su `c.docenteReferente = :utente`
    (caso Docente) oppure `JOIN c.studentiIscritti s WHERE s = :utente`
    (caso Studente, M2M).
  - `cercaPerClasse(ClasseVirtuale) : List<Studente>` — JPQL `SELECT s
    FROM Studente s JOIN s.classi c WHERE c = :classe`. Direzione
    inversa: dato una classe, restituisce gli studenti iscritti.
    Necessario perche' `classe.getStudentiIscritti()` e' LAZY e
    `cercaPerCampi` non filtra su collezioni.
  Entrambi i metodi aprono/chiudono l'`EntityManager` dentro al corpo,
  come tutti gli altri.
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
      `EXIT_ON_CLOSE`, centrato, titolo "Accesso Studente". Il form
      espone solo i campi nome+cognome+errore+conferma (niente combo
      classi).
   2. Istanzia il facade: `GestoreVisualizzazione gv = new
      GestoreVisualizzazione();`. **E' l'unico punto del flow studente
      che dialoga con la persistenza.** Non si fa alcuna query
      "preventiva" qui: il DB viene interrogato solo dopo il click
      Conferma.
   3. `form.addConfermaListener(...)` — registra il listener "Conferma".
   4. `frame.setVisible(true)`.

**6.2.2 Click su "Conferma" (validazione + ricerca studente)**

Trigger: click su `buttonConferma`.

1. `form.pulisciErrore()` — resetta la `labelErrore` (lo spazio resta
   occupato perche' inizializzato a `" "`).
2. Letture dal form: `nome = form.getNomeInserito()` (gia' `trim()`),
   `cognome = form.getCognomeInserito()`.
3. **Guardia 1**: se `nome.isEmpty() || cognome.isEmpty()` ->
   `mostraErrore("Inserire nome e cognome")` + return.
4. `Studente studente = gv.cercaStudente(nome, cognome);` — sotto il
   cofano: `cercaPrimoPerCampi(Studente.class, Map.of("nome", nome,
   "cognome", cognome))`. Non gestiamo omonimi (prendiamo il primo).
5. **Guardia 2**: se `studente == null` -> `mostraErrore("Studente non
   trovato")` + return.
6. `List<ClasseVirtuale> classi = gv.classiDi(studente);` — sotto il
   cofano: `gestorePersistenza.cercaClassiPerUtente(studente)` -> JPQL
   `SELECT c FROM ClasseVirtuale c JOIN c.studentiIscritti s WHERE s =
   :utente`. Una sola query, niente filtro Java.
7. **Guardia 3**: se `classi.isEmpty()` -> `mostraErrore("Nessuna classe
   associata a questo studente")` + return.
8. `apriSceltaClasse(frame, studente, classi);` — apre il popup.

**6.2.3 Scelta classe (`apriSceltaClasse`)**

1. `FormSceltaClasse form = new FormSceltaClasse(); form.setClassi(classi);`
2. `JDialog dialog = new JDialog(parent, "Seleziona Classe",
   APPLICATION_MODAL);` — modale rispetto al frame di login. `setSize`
   420x240, `setLocationRelativeTo(parent)`, `DISPOSE_ON_CLOSE` (la X
   chiude solo il dialog, non l'app — differenza intenzionale rispetto
   ai `JFrame` del progetto che fanno `EXIT_ON_CLOSE`).
3. `form.addConfermaListener(e -> { scelta = form.getClasseSelezionata();
   dialog.dispose(); parent.dispose(); apriProfilo(studente, scelta); });`
4. `form.addAnnullaListener(e -> dialog.dispose());` — solo chiude il
   dialog, il frame di login resta a disposizione per riprovare.
5. `dialog.setVisible(true);` — bloccante. Il flow del Controller resta
   fermo qui finche' non parte un dispose dal listener (Conferma o
   Annulla o X).

**6.2.4 Apertura profilo (`apriProfilo`)**

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

Speculare al flow studente: stessa struttura a tre fasi (login →
popup scelta classe → schermata finale). Differenze: ricerca un
`Docente` invece di uno `Studente`, e la schermata finale e' la
gestione registro con toggle (non il profilo).

**6.3.1 Apertura del login docente**

1. Il listener di `MainAvviaApp` chiude la scelta utente e chiama
   `new GestoreServiziDocente().avvia()`.
2. `GestoreServiziDocente.avvia()`:
   1. Crea `FormServiziDocente` (titolo "Accesso Docente") in un
      `JFrame` 500x400 `EXIT_ON_CLOSE`. Form: solo nome+cognome+errore.
   2. Istanzia `GestoreRegistroDocente gr = new
      GestoreRegistroDocente();` — facade unico per il flow docente.
   3. Registra il listener Conferma e `frame.setVisible(true)`.

**6.3.2 Click su "Conferma" (validazione + ricerca docente)**

1. `pulisciErrore()`, lettura `nome`/`cognome`.
2. Guardia: se `nome.isEmpty() || cognome.isEmpty()` ->
   `mostraErrore("Inserire nome e cognome")` + return.
3. `Docente docente = gr.cercaDocente(nome, cognome);` — sotto il
   cofano: `cercaPrimoPerCampi(Docente.class, Map.of("nome", nome,
   "cognome", cognome))`. Niente omonimi.
4. Guardia: se `docente == null` -> `mostraErrore("Docente non
   trovato")` + return.
5. `List<ClasseVirtuale> classi = gr.classiDi(docente);` — JPQL
   `SELECT c FROM ClasseVirtuale c WHERE c.docenteReferente = :utente`
   (caso piu' semplice del JOIN, perche' la FK e' diretta).
6. Guardia: se `classi.isEmpty()` -> `mostraErrore("Nessuna classe
   associata a questo docente")` + return.
7. `apriSceltaClasse(frame, docente, classi);` — popup modale identico
   per struttura al flow studente; su Conferma chiude tutto e chiama
   `apriGestioneRegistro(docente, scelta)`, su Annulla chiude solo il
   dialog.

**6.3.3 Scelta classe (`apriSceltaClasse`)**

Identico a 6.2.3, sostituendo `Studente` con `Docente` e
`apriProfilo` con `apriGestioneRegistro`.

**6.3.4 Apertura gestione registro (`apriGestioneRegistro`)**

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

**6.3.5 Switch del toggle (interno al Boundary)**

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

Per coprire il caso "dato un Utente, dammi le sue ClasseVirtuale" e'
stato aggiunto **`cercaClassiPerUtente(Utente)`** (vedi sezione 5). La
query JPQL e' specializzata sui due rami:

- **Docente**: `SELECT c FROM ClasseVirtuale c WHERE c.docenteReferente
  = :utente` — la relazione e' `@ManyToOne` con FK fisica
  `docente_id`, quindi e' filtraggio diretto su un campo.
- **Studente**: `SELECT c FROM ClasseVirtuale c JOIN c.studentiIscritti
  s WHERE s = :utente` — la relazione e' `@ManyToMany` (tabella
  associativa `iscrizione`), serve un JOIN sulla collezione.

In entrambi i casi si passa **l'entita' utente direttamente come
parametro** (`setParameter("utente", utente)`): JPA confronta sull'id,
non servono `nome`/`cognome` letterali. Una sola query DB, zero filtro
Java.

**Conseguenza sul login**: il pattern "filtro DB sui campi semplici +
filtro Java sulla collezione" che era usato in precedenza per
`cercaStudenteInClasse`/`cercaDocenteDiClasse` non e' piu' necessario.
Il login ora fa: `cercaStudente`/`cercaDocente` per nome+cognome ->
`classiDi(utente)` per la lista delle classi -> popup `FormSceltaClasse`
per la scelta finale. La classe scelta dal popup e' garantita appartenere
all'utente per costruzione (la lista popolata viene da `classiDi`),
quindi non serve piu' validare a posteriori.

**Mapping JPA rilevanti** (in piedi, alcuni "in eccesso" rispetto al
fabbisogno corrente ma utili per accessi futuri fuori dall'`EntityManager`):

- `ClasseVirtuale.docenteReferente` `@ManyToOne` -> EAGER di default.
- `Studente.classi` `@ManyToMany(fetch = FetchType.EAGER)` —
  esplicitamente EAGER. Non serve piu' al login attuale, ma rimane
  EAGER in vista di accessi a `studente.getClassi()` fuori dalla
  sessione (es. profilo arricchito).
- `ClasseVirtuale.studentiIscritti`, `lezioni`, `compiti`, `valutazioni`
  restano LAZY.
- `ClasseVirtuale.equals/hashCode` su `id`: utile per `List.contains`
  tra entity caricate da `EntityManager` diversi. Coerente con il fatto
  che `cercaClassiPerUtente` ritorna entity gestite da un EM gia' chiuso.
- `Studente.equals` / `Docente.equals` su nome+cognome+email
  (case-insensitive): non strettamente usati dal login attuale (sostituiti
  dalla logica del popup), ma in piedi per consistency e per gli use case
  in cui un'entita' va confrontata con un'altra caricata altrove
  (es. `Valutazione.getStudenteValutato().equals(studenteCorrente)`).

**Limitazioni note** (accettate per il perimetro del progetto):

- La ricerca per `nome`/`cognome` via `cercaPerCampi` e' **case-sensitive**
  (JPQL usa `=`). Se servira' case-insensitive nel login, andra'
  introdotto un nuovo metodo o normalizzazione.
- **Omonimi non gestiti**: `cercaStudente`/`cercaDocente` usano
  `cercaPrimoPerCampi` e prendono il primo match. Se nel DB esistono due
  utenti con stesso nome+cognome, l'app sceglie arbitrariamente uno dei
  due. Scelta esplicita per il perimetro didattico del progetto.
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
  `GestoreServiziDocente`. Imposta `UIManager.setLookAndFeel(systemLookAndFeel)`
  prima di `invokeLater` (su macOS attiva Aqua per il segmented control).
- `database/persistence.xml` e `setup/MainSetupInsert` per popolare il DB.

**Flow Studente — completo end-to-end**:
- `FormSceltaUtente` -> `FormServiziStudente` (solo nome+cognome) ->
  popup modale `FormSceltaClasse` (solo le classi dello studente) ->
  `FormVisualizzazioneProfilo` con label nome + sub-toggle a 3
  (Lezioni/Compiti/Valutazioni) + liste scrollabili popolate dal
  facade (vedi UC7).
- Back nel profilo riporta al login. Annulla nel popup torna al login.

**Flow Docente — login completo**:
- `FormSceltaUtente` -> `FormServiziDocente` (solo nome+cognome) ->
  popup modale `FormSceltaClasse` (solo le classi del docente) ->
  `FormGestioneRegistro` con toggle Aggiorna/Consulta funzionante.
- Back nel registro riporta al login docente. Annulla nel popup torna
  al login.

**Caso d'uso "Visualizzazione Profilo Studente" — completo end-to-end**
(vedi UC7 sez. 14 per il flow dettagliato):
- Boundary: `FormVisualizzazioneProfilo` con sub-toggle Lezioni/Compiti/
  Valutazioni + 3 `JList<String>` scrollabili. Item formattati come
  stringhe HTML interpretate dal renderer di default (niente
  `ListCellRenderer` custom).
- Controller: `apriProfilo` chiama il privato `visualizzaProfilo(form,
  studente, classe)` che chiede al facade le 3 liste e le pusha al
  Boundary.
- Facade: `gv.visualizzaLezioni/Compiti/Valutazioni` usano i metodi
  CRUD generici originali del professore (`cercaPerCampo` su
  `classeVirtuale` per Lezione/Compito, `cercaPerCampi` su
  `studenteValutato`+`classeVirtuale` per Valutazione). Niente
  estensioni di `GestorePersistenza` per questo caso d'uso.

**Caso d'uso "Aggiorna Registro" — completo end-to-end** (tutti e 3
i sotto-casi, vedi sezione 14 per il flow dettagliato):
- Boundary: `FormAggiornaRegistro` (sub-toggle Lezione/Compito/Valutazione
  + tre form di inserimento programmaticamente costruiti). Su macOS il
  sub-toggle e' un segmented control nativo.
- Lezione: form (data spinner, argomento, descrizione) + guardie su
  argomento/descrizione + persistenza tramite il facade.
- Compito: form (titolo, data assegnazione spinner, descrizione, data
  scadenza spinner) + guardie su titolo/descrizione/scadenza>=assegnazione.
- Valutazione: form (data spinner, voto spinner numerico 0-10 step 0.5,
  descrizione, combo Tipologia da enum, combo Studente popolata via
  `cercaStudenti(classe)` all'apertura) + guardie su studente!=null
  e descrizione.
- Controller: tre orchestratori privati `aggiornaRegistroLezione/Compito/
  Valutazione` che fanno la catena "crea (facade A) + registra (facade B)"
  e propagano boolean per feedback UI.

**Entity tweaks effettuati**:
- `ClasseVirtuale`: `toString()` (per render combo), `equals/hashCode`
  per id, getter `getDocenteReferente()`.
- `Studente`: `@ManyToMany(fetch = FetchType.EAGER)` su `classi`,
  `equals` per nome+cognome+email, `toString()` ritorna `"Nome Cognome"`
  (usato dalla combo studenti in Valutazione).
- `Docente`: `equals/hashCode` per nome+cognome+email.
- `Lezione`, `Compito`, `Valutazione`: campi data passati da `String`
  a `LocalDate` (coerente con `JSpinner` + `SpinnerDateModel` lato UI).

**Persistenza estesa**:
- `database/GestorePersistenza` arricchito (dall'utente, due estensioni
  autorizzate):
  - `cercaClassiPerUtente(Utente)` per "Utente -> classi"
  - `cercaPerClasse(ClasseVirtuale)` per "Classe -> studenti"

**Facade aggiornati**:
- `GestoreVisualizzazione`: `cercaStudente`, `classiDi(Studente)`.
- `GestoreRegistroDocente`: `cercaDocente`, `classiDi(Docente)`,
  `cercaStudenti(ClasseVirtuale)`, `registraLezione/Compito/Valutazione`.
- `GestoreAggiornamentiRegistro`: `creaLezione/Compito/Valutazione`
  (creatore di Entity, niente DB).

---

## 10. Cosa rimane

**Prossimo step concordato**: implementare il secondo caso d'uso del
docente:

- **Consulta Registro**: visualizzazione (probabilmente liste/tabelle)
  di Lezione/Compito/Valutazione della classe. Verra' delegato a
  `GestoreRegistroDocente` (espandendo metodi come `mostraRegistro`,
  `monitoraAndamento`, `calcolaMediaClasse`).

**Step ulteriori**:

- Calcolo medie nel profilo studente: oggi le valutazioni si vedono
  ma non c'e' una media. `calcolaMediaStudente` di `GestoreVisualizzazione`
  e' ancora stub. Eventualmente da mostrare in testa al pannello
  Valutazioni del profilo.
- Eventuale autenticazione vera (password): non in scope, da decidere.

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
  un metodo ad hoc (vedi `cercaClassiPerUtente` per il caso
  Utente -> classi) oppure un filtro Java post-query.
- **JPQL vs SQL**: in JPQL si naviga su attributi Java
  (`c.docenteReferente`), NON su colonne fisiche (`c.docente_id`).
  Quel tipo di errore e' stato sbagliato a freddo in una sessione e
  scoperto in revisione: i nomi delle colonne nel DB JPA li risolve da
  solo dal mapping.
- **JPQL parametri**: passare l'entita' direttamente
  (`setParameter("utente", utente)`) e confrontare con `=` funziona
  (JPA usa l'id). Niente concatenazione di stringhe nella query
  (rischio SQL injection + brutto).
- **JDialog vs JFrame**: tutti i `JFrame` del progetto usano
  `EXIT_ON_CLOSE` (X chiude la JVM). `FormSceltaClasse` invece e' un
  `JDialog` `DISPOSE_ON_CLOSE`: la X chiude solo il dialog. Scelta
  intenzionale, riflette il fatto che il dialog e' una sotto-scelta
  rispetto al login.
- **Modalita' del dialog**: il `FormSceltaClasse` e' istanziato con
  `Dialog.ModalityType.APPLICATION_MODAL`. `setVisible(true)` blocca
  l'EDT del Controller finche' i listener del dialog non chiamano
  `dispose()`. Il flow del Controller riprende dopo `setVisible`.
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

- `database/GestorePersistenza.java` — file del professore. I metodi
  CRUD generici originali (vedi sezione 2) NON vanno modificati.
  L'utente ha autorizzato esplicitamente DUE estensioni
  (`cercaClassiPerUtente`, `cercaPerClasse`); ulteriori estensioni
  richiedono nuova autorizzazione esplicita.
- `database/JpaUtil.java` — fuori scope (utility JPA, modifiche
  rischiano di rompere la persistenza).
- `setup/*` — fuori scope (popolamento DB iniziale).
- `resources/META-INF/persistence.xml` — fuori scope salvo richiesta.

---

## 13. Ordine di lettura consigliato per un nuovo collaboratore

1. Sezione 1 (Dominio) e 4 (BCED) per il contesto.
2. Sezione 6 (Flow end-to-end) per la sequenza dei click utente.
3. Sezione 14 (Casi d'uso) per il dettaglio della catena BCED in ogni
   caso d'uso implementato (e' il "perche'" delle scelte nei tre layer).
4. Sezione 7 (Persistenza) per capire **perche'** ci sono `EAGER`,
   `equals` per id, JOIN ad hoc (e' il punto piu' delicato del progetto).
5. Sezione 5 (Mappa file) come riferimento mentre si naviga il codice.
6. Sezioni 10, 11, 12 prima di pianificare un nuovo task.

---

## 14. Casi d'uso (Flow di eventi dettagliato)

Per ogni caso d'uso implementato: attore, precondizioni, catena BCED
percorsa, flusso principale numerato, guardie/eccezioni, esito.

### UC1 — Accesso al sistema (Studente)

- **Attore**: Studente.
- **Precondizione**: lo Studente esiste nel DB ed e' iscritto ad almeno
  una `ClasseVirtuale`.
- **Catena BCED**:
  `FormSceltaUtente` -> `GestoreServiziStudente.avvia()` ->
  `GestoreVisualizzazione.cercaStudente` -> `GestoreVisualizzazione.classiDi` ->
  `FormSceltaClasse` (modale) -> `FormVisualizzazioneProfilo`.

**Flusso principale**:

1. Studente clicca "Studente" su `FormSceltaUtente`.
2. `MainAvviaApp` chiude la finestra di scelta e chiama
   `new GestoreServiziStudente().avvia()`.
3. Il Controller apre `FormServiziStudente` (campi nome+cognome).
4. Studente inserisce dati, clicca "Conferma".
5. Controller legge i campi, applica guardie.
6. Controller chiama `gestoreVisualizzazione.cercaStudente(nome, cognome)`.
7. Controller chiama `gestoreVisualizzazione.classiDi(studente)`.
8. Controller apre `FormSceltaClasse` come `JDialog` modale, popolata
   con la lista delle classi.
9. Studente seleziona una classe, clicca "Conferma".
10. Controller chiude dialog + frame login, chiama `apriProfilo(studente, classe)`.
11. `FormVisualizzazioneProfilo` mostra "Profilo di Nome Cognome".

**Guardie**:
- G1 nome o cognome vuoti -> `mostraErrore("Inserire nome e cognome")`.
- G2 studente non trovato -> `mostraErrore("Studente non trovato")`.
- G3 nessuna classe associata -> `mostraErrore("Nessuna classe associata
  a questo studente")`.
- G4 Annulla / X nel dialog -> chiude solo il dialog, il login resta.

**Esito**: il profilo dello studente e' aperto; Back riporta al login.

---

### UC2 — Accesso al sistema (Docente)

- **Attore**: Docente.
- **Precondizione**: il Docente esiste ed e' referente di almeno una
  `ClasseVirtuale`.
- **Catena BCED**:
  `FormSceltaUtente` -> `GestoreServiziDocente.avvia()` ->
  `GestoreRegistroDocente.cercaDocente` -> `GestoreRegistroDocente.classiDi` ->
  `FormSceltaClasse` -> `FormGestioneRegistro` (con `FormAggiornaRegistro`
  agganciato dentro).

**Flusso principale**: analogo a UC1 sostituendo Studente con Docente
e profilo con Gestione Registro. In aggiunta, all'apertura della
gestione registro il Controller carica gli studenti della classe via
`gestoreRegistroDocente.cercaStudenti(classe)` e li passa a
`formAggiorna.setStudentiClasse(...)` (necessari a UC5).

**Guardie**: speculari a UC1 (nome/cognome vuoti, docente non trovato,
nessuna classe associata, Annulla nel dialog).

**Esito**: `FormGestioneRegistro` aperto sulla classe scelta, con i
toggle Aggiorna/Consulta. Il toggle Aggiorna mostra il
`FormAggiornaRegistro` con sub-toggle Lezione (attivo) /Compito/Valutazione.

---

### UC3 — Aggiorna Registro: nuova Lezione

- **Attore**: Docente autenticato su una classe.
- **Precondizione**: UC2 completato; toggle "Aggiorna Registro" e
  sub-toggle "Lezione" attivi (sub-toggle Lezione e' lo stato iniziale).
- **Catena BCED**:
  `FormAggiornaRegistro` -> `GestoreServiziDocente.aggiornaRegistroLezione`
  -> `GestoreAggiornamentiRegistro.creaLezione` (Entity)
  -> `GestoreRegistroDocente.registraLezione`
  -> `GestorePersistenza.salva`.

**Flusso principale**:

1. Docente compila i campi: data (`JSpinner` con `SpinnerDateModel`,
   default oggi), argomento (`JTextField`), descrizione (`JTextField`).
2. Docente clicca "Salva".
3. Boundary notifica il click al listener registrato dal Controller in
   `apriGestioneRegistro`.
4. Controller chiama `formAggiorna.pulisciMessaggioLezione()`.
5. Controller legge i tre campi via `getDataLezione()` (converte
   `Date -> LocalDate` con `ZoneId.systemDefault()`),
   `getArgomentoLezione()`, `getDescrizioneLezione()`.
6. Controller applica le guardie.
7. Controller chiama l'orchestratore privato
   `aggiornaRegistroLezione(classe, data, argomento, descrizione)`.
8. L'orchestratore: `gestoreAggiornamenti.creaLezione(...)` -> nuova
   `Lezione` in memoria; `gestoreRegistroDocente.registraLezione(lezione)`
   -> `gestorePersistenza.salva(lezione)` -> boolean.
9. Su esito ok: `formAggiorna.mostraSuccessoLezione("Lezione registrata")`
   + `pulisciCampiLezione()` (spinner a oggi, text field vuoti).
10. Su esito ko: `formAggiorna.mostraErroreLezione("Errore durante il salvataggio")`.

**Guardie**:
- G1 argomento vuoto -> "Inserire l'argomento trattato".
- G2 descrizione vuota -> "Inserire una descrizione".
- Niente guardia sulla data: `SpinnerDateModel` garantisce sempre un
  valore valido per costruzione.

**Esito**: nuova riga in tabella `lezione` con `classe_id` corretto.

---

### UC4 — Aggiorna Registro: nuovo Compito

- **Attore**: Docente autenticato su una classe.
- **Precondizione**: UC2 completato; sub-toggle "Compito" attivo.
- **Catena BCED**: come UC3, sostituendo `creaLezione`/`registraLezione`
  con `creaCompito`/`registraCompito`.

**Flusso principale**: analogo a UC3 con 4 campi (titolo, data
assegnazione, descrizione, data scadenza). L'orchestratore e'
`aggiornaRegistroCompito(classe, titolo, dataAss, descrizione, dataScad)`.

**Guardie**:
- G1 titolo vuoto -> "Inserire il titolo del compito".
- G2 descrizione vuota -> "Inserire una descrizione".
- G3 `dataScadenza.isBefore(dataAssegnazione)` -> "La scadenza non puo'
  essere prima dell'assegnazione".

**Esito**: nuova riga in `compito`.

---

### UC5 — Aggiorna Registro: nuova Valutazione

- **Attore**: Docente autenticato su una classe.
- **Precondizione**: UC2 completato; sub-toggle "Valutazione" attivo;
  la combo studenti e' stata popolata all'apertura del registro
  (vedi UC2, step "carica studenti").
- **Catena BCED**:
  `FormAggiornaRegistro` -> `GestoreServiziDocente.aggiornaRegistroValutazione`
  -> `GestoreAggiornamentiRegistro.creaValutazione`
  -> `GestoreRegistroDocente.registraValutazione`
  -> `GestorePersistenza.salva`.

**Flusso principale**:

1. Docente compila: data (`JSpinner` data), voto (`JSpinner` numerico
   con `SpinnerNumberModel(6.0, 0.0, 10.0, 0.5)`), descrizione,
   tipologia (`JComboBox<Tipologia>` da `Tipologia.values()`), studente
   (`JComboBox<Studente>` popolata via `setStudentiClasse`; usa
   `Studente.toString()` per "Nome Cognome", niente renderer custom).
2. Docente clicca "Salva".
3. Listener: `pulisciMessaggioValutazione`, lettura campi, guardie.
4. Orchestratore `aggiornaRegistroValutazione(classe, data, voto,
   descrizione, tipologia, studente)`: crea via facade A, persiste via
   facade B, ritorna boolean.
5. Feedback verde/rosso + `pulisciCampiValutazione` (data oggi, voto 6.0,
   descrizione vuota; combo Tipologia e Studente NON resettate per
   ergonomia: di solito si valutano piu' studenti della stessa classe
   in fila).

**Guardie**:
- G1 `studente == null` -> "Nessuno studente da valutare (la classe non
  ha iscritti)". Copre il caso in cui `cercaStudenti(classe)` ha tornato
  lista vuota.
- G2 descrizione vuota -> "Inserire una descrizione".
- Niente guardia su voto (range del `SpinnerNumberModel`), data (spinner
  sempre valido), tipologia (combo da enum, mai null).

**Esito**: nuova riga in `valutazione` con `classe_id` e `studente_id`
corretti.

---

### UC6 — Consulta Registro

**Non ancora implementato.** Toggle "Consulta Registro" presente in
`FormGestioneRegistro`, ma il sotto-pannello e' un placeholder. Verra'
delegato a `GestoreRegistroDocente` (metodi stub: `mostraRegistro`,
`monitoraAndamento`, `calcolaMediaClasse`).

---

### UC7 — Visualizzazione Profilo Studente

- **Attore**: Studente autenticato su una classe.
- **Precondizione**: UC1 completato; il profilo si apre subito dopo la
  scelta classe.
- **Catena BCED**:
  `FormVisualizzazioneProfilo` <- `GestoreServiziStudente.visualizzaProfilo`
  -> `GestoreVisualizzazione.visualizzaLezioni / visualizzaCompiti /
  visualizzaValutazioni` -> `GestorePersistenza.cercaPerCampo /
  cercaPerCampi`.

**Flusso principale**:

1. Subito dopo la scelta classe, `apriProfilo(studente, classe)` crea
   il `FormVisualizzazioneProfilo` e chiama `mostraProfilo(studente,
   classe)` (setta la label "Profilo di Nome Cognome").
2. Sempre dentro `apriProfilo`, prima di rendere visibile il frame,
   il Controller chiama l'orchestratore privato
   `visualizzaProfilo(profilo, studente, classe)`.
3. `visualizzaProfilo` interroga il facade:
   - `gv.visualizzaLezioni(classe)` -> `cercaPerCampo(Lezione.class,
     "classeVirtuale", classe)`.
   - `gv.visualizzaCompiti(classe)` -> `cercaPerCampo(Compito.class,
     "classeVirtuale", classe)`.
   - `gv.visualizzaValutazioni(studente, classe)` ->
     `cercaPerCampi(Valutazione.class, Map.of("studenteValutato",
     studente, "classeVirtuale", classe))`. Filtro a 2 campi:
     fondamentale perche' uno studente puo' essere iscritto a piu'
     classi e vogliamo SOLO le valutazioni nella classe corrente.
4. Le 3 liste vengono pushate al Boundary via `setLezioni`,
   `setCompiti`, `setValutazioni`. Ognuno fa `model.clear()` + ciclo
   con formattazione HTML degli item.
5. `frame.setVisible(true)` -> la finestra si apre con le liste gia'
   popolate. Toggle iniziale: Lezioni.

**Rendering degli item (no `ListCellRenderer` custom)**:
Ogni elemento del model e' una stringa HTML che inizia con `<html>`.
Il renderer di default (`DefaultListCellRenderer extends JLabel`)
interpreta i tag basici (`<b>`, `<i>`, `<br>`, `<font color>`),
ottenendo item multi-riga con grassetto sulla prima e italic grigio
sulla seconda. Formato per tipo:
- Lezione: `<b>argomento — dd/MM/yyyy</b><br><i><font gray>descrizione</font></i>`
- Compito: `<b>titolo — dd/MM/yyyy → dd/MM/yyyy</b><br><i>descrizione</i>` (freccia per chiarire assegnazione → scadenza)
- Valutazione: `<b>X.Y — tipologia leggibile — dd/MM/yyyy</b><br><i>descrizione</i>` (voto con `%.1f`, tipologia da `PROVA_SCRITTA` a "prova scritta" via `name().toLowerCase().replace('_',' ')`)

**Guardie**: nessuna. Se una lista e' vuota (es. lo studente non ha
valutazioni in quella classe) il `JScrollPane` resta vuoto, niente
errore.

**Esito**: profilo aperto e popolato; Back riporta al login studente.

**Limite onesto**: l'HTML del trucco non escapa `<`, `>`, `&` presenti
nei testi dal DB. Per il perimetro didattico (dati controllati) e'
accettabile.
