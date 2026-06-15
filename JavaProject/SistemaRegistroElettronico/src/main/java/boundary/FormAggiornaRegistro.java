package boundary;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import entity.Studente;
import entity.Tipologia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/*
 * Sotto-form del flow docente per il caso d'uso "Aggiorna Registro".
 *
 * Viene incastrato dentro panelContenuto di FormGestioneRegistro quando il
 * toggle "Aggiorna Registro" e' selezionato. Espone, come gli altri form
 * del progetto, getPanel().
 *
 * Layout: sub-toggle bar (Lezione/Compito/Valutazione) centrata e piccola,
 * sopra un panelContenuto interno che ospita alternativamente i tre
 * sotto-pannelli di inserimento. Per ora i tre sotto-pannelli sono
 * placeholder; verranno trasformati nei form di inserimento veri nei
 * prossimi step.
 */
public class FormAggiornaRegistro {

    private JPanel panel1;
    private JToggleButton toggleLezione;
    private JToggleButton toggleCompito;
    private JToggleButton toggleValutazione;
    private JPanel panelContenuto;

    /*
     * Campi del form Lezione. Li popolo dentro creaFormLezione().
     */
    private JSpinner spinnerDataLezione;
    private JTextField fieldArgomentoLezione;
    private JTextField fieldDescrizioneLezione;
    private JButton buttonSalvaLezione;
    private JLabel labelMessaggioLezione;

    /*
     * Campi del form Compito. Li popolo dentro creaFormCompito().
     * Sono 4 campi (titolo, due date, descrizione): uno in piu' rispetto a Lezione.
     */
    private JTextField fieldTitoloCompito;
    private JSpinner spinnerDataAssegnazioneCompito;
    private JTextField fieldDescrizioneCompito;
    private JSpinner spinnerDataScadenzaCompito;
    private JButton buttonSalvaCompito;
    private JLabel labelMessaggioCompito;

    /*
     * Campi del form Valutazione. Li popolo dentro creaFormValutazione().
     * Qui ho anche due JComboBox: una per la Tipologia (enum) e una per lo
     * Studente. Quella degli studenti la riempie il Controller con la lista
     * della classe (vedi setStudentiClasse).
     */
    private JSpinner spinnerDataValutazione;
    private JSpinner spinnerVotoValutazione;
    private JTextField fieldDescrizioneValutazione;
    private JComboBox<Tipologia> comboTipologia;
    private JComboBox<Studente> comboStudente;
    private JButton buttonSalvaValutazione;
    private JLabel labelMessaggioValutazione;

    /*
     * Sotto-pannelli per ogni tipo di aggiornamento. Tutti e tre sono ora
     * form di inserimento reali (creaFormLezione/Compito/Valutazione).
     */
    private final JPanel panelLezione = creaFormLezione();
    private final JPanel panelCompito = creaFormCompito();
    private final JPanel panelValutazione = creaFormValutazione();

    public FormAggiornaRegistro() {

        /*
         * Client property dell'Aqua LookAndFeel di macOS: trasformano i tre
         * JToggleButton in un segmented control nativo (i pulsanti si toccano
         * e quello selezionato cambia colore). Su L&F non Aqua queste property
         * vengono ignorate silenziosamente: il toggle resta funzionale, solo
         * non segmentato graficamente.
         */
        toggleLezione.putClientProperty("JButton.buttonType", "segmented");
        toggleLezione.putClientProperty("JButton.segmentPosition", "first");
        toggleCompito.putClientProperty("JButton.buttonType", "segmented");
        toggleCompito.putClientProperty("JButton.segmentPosition", "middle");
        toggleValutazione.putClientProperty("JButton.buttonType", "segmented");
        toggleValutazione.putClientProperty("JButton.segmentPosition", "last");

        ButtonGroup gruppo = new ButtonGroup();
        gruppo.add(toggleLezione);
        gruppo.add(toggleCompito);
        gruppo.add(toggleValutazione);

        // Stato iniziale: vista "Lezione" attiva
        toggleLezione.setSelected(true);
        mostraSotto(panelLezione);

        toggleLezione.addActionListener(e -> mostraSotto(panelLezione));
        toggleCompito.addActionListener(e -> mostraSotto(panelCompito));
        toggleValutazione.addActionListener(e -> mostraSotto(panelValutazione));
    }

    public JComponent getPanel() {
        return panel1;
    }

    /*
     * Sostituisce il contenuto di panelContenuto con il sotto-pannello
     * richiesto. revalidate + repaint perche' stiamo cambiando i figli
     * di un container gia' visibile.
     */
    private void mostraSotto(JPanel sotto) {
        panelContenuto.removeAll();
        panelContenuto.add(sotto, BorderLayout.CENTER);
        panelContenuto.revalidate();
        panelContenuto.repaint();
    }

    /*
    Questo serve per porzioni non ancora implementate
     */

    private static JPanel creaPlaceholder(String testo) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(testo, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.ITALIC, 14));
        l.setForeground(new Color(120, 120, 120));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    /*
     * Costruisce il pannello "Nuova Lezione" e popola
     * i campi spinner/textfield/button/label di livello classe (necessari
     * al Controller per leggere/scrivere lo stato)
     *
     * Layout:
     *   row 0: pannello campi 3x2 (Data | spinner, Argomento | field,
     *          Descrizione | field)
     *   row 1: labelMessaggioLezione (errore/successo)
     *   row 2: buttonSalvaLezione (centrato)
     */
    private JPanel creaFormLezione() {

        JPanel root = new JPanel();
        root.setLayout(new GridLayoutManager(3, 1, new Insets(20, 30, 20, 30), 0, 12));

        JPanel campi = new JPanel();
        campi.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), 15, 10));
        root.add(campi, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        /*
        Spinner per forzare il formato data, in modo che chi utilizza il programma
        non può inserire valori errati per il campo data
         */

        JLabel labelData = new JLabel("Data:");
        labelData.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelData, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerDataLezione = new JSpinner(new SpinnerDateModel());
        spinnerDataLezione.setEditor(new JSpinner.DateEditor(spinnerDataLezione, "dd/MM/yyyy"));
        spinnerDataLezione.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(spinnerDataLezione, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, 28), null, 0, false));

        JLabel labelArgomento = new JLabel("Argomento:");
        labelArgomento.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelArgomento, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fieldArgomentoLezione = new JTextField();
        fieldArgomentoLezione.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(fieldArgomentoLezione, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, 28), null, 0, false));

        JLabel labelDescrizione = new JLabel("Descrizione:");
        labelDescrizione.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelDescrizione, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fieldDescrizioneLezione = new JTextField();
        fieldDescrizioneLezione.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(fieldDescrizioneLezione, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, 28), null, 0, false));

        // Spazio fisso (" ") anche da vuota per evitare layout shift quando appare il messaggio
        labelMessaggioLezione = new JLabel(" ");
        labelMessaggioLezione.setFont(new Font("SansSerif", Font.ITALIC, 13));
        labelMessaggioLezione.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(labelMessaggioLezione, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        buttonSalvaLezione = new JButton("Salva");
        buttonSalvaLezione.setFont(new Font("SansSerif", Font.BOLD, 14));
        root.add(buttonSalvaLezione, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 32), null, 0, false));

        return root;
    }

    /*
     * Converte il valore del spinner (java.util.Date) in LocalDate via la
     * timezone di sistema. Per un'app desktop locale e' corretto; per usi
     * cross-zone andrebbe parametrizzato.
     */
    public LocalDate getDataLezione() {
        Date d = (Date) spinnerDataLezione.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public String getArgomentoLezione() {
        return fieldArgomentoLezione.getText().trim();
    }

    public String getDescrizioneLezione() {
        return fieldDescrizioneLezione.getText().trim();
    }

    public void addSalvaLezioneListener(ActionListener listener) {
        buttonSalvaLezione.addActionListener(listener);
    }

    public void mostraErroreLezione(String messaggio) {
        labelMessaggioLezione.setForeground(new Color(200, 0, 0));
        labelMessaggioLezione.setText(messaggio);
    }

    public void mostraSuccessoLezione(String messaggio) {
        labelMessaggioLezione.setForeground(new Color(0, 128, 0));
        labelMessaggioLezione.setText(messaggio);
    }

    public void pulisciMessaggioLezione() {
        labelMessaggioLezione.setText(" ");
    }

    /*
     * Reset dei campi dopo un salvataggio andato a buon fine. Non tocca la
     * label messaggio: il feedback di successo rimane visibile finche'
     * l'utente non riprova il salva.
     */
    public void pulisciCampiLezione() {
        spinnerDataLezione.setValue(new Date());
        fieldArgomentoLezione.setText("");
        fieldDescrizioneLezione.setText("");
    }

    /*
     * Form per inserire un Compito. Stessa logica della Lezione,
     * solo che qui i campi sono 4: titolo, data di assegnazione,
     * descrizione, data di scadenza. Uso due JSpinner per le date
     * (cosi' non rischio formati sbagliati) e due JTextField per
     * titolo e descrizione.
     *
     * Layout:
     *   row 0: pannello campi 4x2
     *   row 1: labelMessaggioCompito (errore/successo)
     *   row 2: buttonSalvaCompito (centrato)
     */
    private JPanel creaFormCompito() {

        JPanel root = new JPanel();
        root.setLayout(new GridLayoutManager(3, 1, new Insets(20, 30, 20, 30), 0, 12));

        JPanel campi = new JPanel();
        campi.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), 15, 10));
        root.add(campi, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        JLabel labelTitolo = new JLabel("Titolo:");
        labelTitolo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelTitolo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fieldTitoloCompito = new JTextField();
        fieldTitoloCompito.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(fieldTitoloCompito, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, 28), null, 0, false));

        /*
        Anche qui spinner data, stesso motivo della Lezione: formato sicuro,
        niente da parsare a mano
         */
        JLabel labelDataAss = new JLabel("Assegnazione:");
        labelDataAss.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelDataAss, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerDataAssegnazioneCompito = new JSpinner(new SpinnerDateModel());
        spinnerDataAssegnazioneCompito.setEditor(new JSpinner.DateEditor(spinnerDataAssegnazioneCompito, "dd/MM/yyyy"));
        spinnerDataAssegnazioneCompito.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(spinnerDataAssegnazioneCompito, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, 28), null, 0, false));

        JLabel labelDescrizione = new JLabel("Descrizione:");
        labelDescrizione.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelDescrizione, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fieldDescrizioneCompito = new JTextField();
        fieldDescrizioneCompito.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(fieldDescrizioneCompito, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, 28), null, 0, false));

        JLabel labelDataScad = new JLabel("Scadenza:");
        labelDataScad.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelDataScad, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerDataScadenzaCompito = new JSpinner(new SpinnerDateModel());
        spinnerDataScadenzaCompito.setEditor(new JSpinner.DateEditor(spinnerDataScadenzaCompito, "dd/MM/yyyy"));
        spinnerDataScadenzaCompito.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(spinnerDataScadenzaCompito, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, 28), null, 0, false));

        // Spazio fisso (" ") anche da vuota, cosi' non balla il layout quando appare un messaggio
        labelMessaggioCompito = new JLabel(" ");
        labelMessaggioCompito.setFont(new Font("SansSerif", Font.ITALIC, 13));
        labelMessaggioCompito.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(labelMessaggioCompito, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        buttonSalvaCompito = new JButton("Salva");
        buttonSalvaCompito.setFont(new Font("SansSerif", Font.BOLD, 14));
        root.add(buttonSalvaCompito, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 32), null, 0, false));

        return root;
    }

    // ---- API per il Controller, sotto-caso Compito ----

    public String getTitoloCompito() {
        return fieldTitoloCompito.getText().trim();
    }

    /*
     * Stessa conversione di getDataLezione: prendo il Date dallo spinner
     * e lo trasformo in LocalDate usando la timezone del sistema.
     */
    public LocalDate getDataAssegnazioneCompito() {
        Date d = (Date) spinnerDataAssegnazioneCompito.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public String getDescrizioneCompito() {
        return fieldDescrizioneCompito.getText().trim();
    }

    public LocalDate getDataScadenzaCompito() {
        Date d = (Date) spinnerDataScadenzaCompito.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void addSalvaCompitoListener(ActionListener listener) {
        buttonSalvaCompito.addActionListener(listener);
    }

    public void mostraErroreCompito(String messaggio) {
        labelMessaggioCompito.setForeground(new Color(200, 0, 0));
        labelMessaggioCompito.setText(messaggio);
    }

    public void mostraSuccessoCompito(String messaggio) {
        labelMessaggioCompito.setForeground(new Color(0, 128, 0));
        labelMessaggioCompito.setText(messaggio);
    }

    public void pulisciMessaggioCompito() {
        labelMessaggioCompito.setText(" ");
    }

    /*
     * Reset campi dopo un salvataggio ok. Le due date tornano a "oggi",
     * i text field si svuotano. Non tocco il messaggio: cosi' l'utente
     * vede ancora "Compito registrato" finche' non riclicca Salva.
     */
    public void pulisciCampiCompito() {
        fieldTitoloCompito.setText("");
        spinnerDataAssegnazioneCompito.setValue(new Date());
        fieldDescrizioneCompito.setText("");
        spinnerDataScadenzaCompito.setValue(new Date());
    }

    /*
     * Form per inserire una Valutazione. E' il piu' "ricco" dei tre:
     * 5 campi (data, voto, descrizione, tipologia, studente).
     *
     * - voto: JSpinner numerico col range 0-10, step 0.5. Cosi' non posso
     *   inserire voti tipo 11 o -3 e mi risparmio una guardia.
     * - tipologia: combo gia' riempita con i valori dell'enum (oggi
     *   PROVA_SCRITTA / PROVA_ORALE).
     * - studente: combo vuota all'inizio. La riempie il Controller con la
     *   lista degli studenti della classe (vedi setStudentiClasse).
     *
     * Layout:
     *   row 0: pannello campi 5x2
     *   row 1: labelMessaggioValutazione (errore/successo)
     *   row 2: buttonSalvaValutazione (centrato)
     */
    private JPanel creaFormValutazione() {

        JPanel root = new JPanel();
        root.setLayout(new GridLayoutManager(3, 1, new Insets(20, 30, 20, 30), 0, 12));

        JPanel campi = new JPanel();
        campi.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), 15, 10));
        root.add(campi, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        JLabel labelData = new JLabel("Data:");
        labelData.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelData, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerDataValutazione = new JSpinner(new SpinnerDateModel());
        spinnerDataValutazione.setEditor(new JSpinner.DateEditor(spinnerDataValutazione, "dd/MM/yyyy"));
        spinnerDataValutazione.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(spinnerDataValutazione, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, 28), null, 0, false));

        /*
        Voto: parto da 6, range 0-10, step 0.5. Cosi' lo spinner mi vincola
        l'input e non devo controllare il range con una guardia.
         */
        JLabel labelVoto = new JLabel("Voto:");
        labelVoto.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelVoto, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerVotoValutazione = new JSpinner(new SpinnerNumberModel(6.0, 0.0, 10.0, 0.5));
        spinnerVotoValutazione.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(spinnerVotoValutazione, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, 28), null, 0, false));

        JLabel labelDescrizione = new JLabel("Descrizione:");
        labelDescrizione.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelDescrizione, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fieldDescrizioneValutazione = new JTextField();
        fieldDescrizioneValutazione.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(fieldDescrizioneValutazione, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, 28), null, 0, false));

        /*
        Tipologia: la combo la creo gia' piena con tutti i valori dell'enum
        Tipologia. Cosi' se domani aggiungo un'altra tipologia mi appare
        senza dover toccare la UI.
         */
        JLabel labelTipologia = new JLabel("Tipologia:");
        labelTipologia.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelTipologia, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboTipologia = new JComboBox<>(Tipologia.values());
        comboTipologia.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(comboTipologia, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, 28), null, 0, false));

        /*
        Studente: la combo la creo vuota. Sara' il Controller, dentro
        apriGestioneRegistro, a riempirla con la lista degli studenti
        della classe (setStudentiClasse). Lo Studente.toString() restituisce
        "Nome Cognome", quindi nella combo si legge subito chi e' chi.
         */
        JLabel labelStudente = new JLabel("Studente:");
        labelStudente.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(labelStudente, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboStudente = new JComboBox<>();
        comboStudente.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campi.add(comboStudente, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, 28), null, 0, false));

        labelMessaggioValutazione = new JLabel(" ");
        labelMessaggioValutazione.setFont(new Font("SansSerif", Font.ITALIC, 13));
        labelMessaggioValutazione.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(labelMessaggioValutazione, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        buttonSalvaValutazione = new JButton("Salva");
        buttonSalvaValutazione.setFont(new Font("SansSerif", Font.BOLD, 14));
        root.add(buttonSalvaValutazione, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 32), null, 0, false));

        return root;
    }

    // ---- API per il Controller, sotto-caso Valutazione ----

    public LocalDate getDataValutazione() {
        Date d = (Date) spinnerDataValutazione.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /*
     * Il SpinnerNumberModel mi torna un Number (di solito Double). Lo
     * casto a Double e ritorno il double primitivo: cosi' il Controller
     * lo passa dritto a creaValutazione.
     */
    public double getVotoValutazione() {
        return ((Number) spinnerVotoValutazione.getValue()).doubleValue();
    }

    public String getDescrizioneValutazione() {
        return fieldDescrizioneValutazione.getText().trim();
    }

    public Tipologia getTipologiaValutazione() {
        return (Tipologia) comboTipologia.getSelectedItem();
    }

    /*
     * Puo' essere null se la combo studenti e' vuota (es. classe senza
     * iscritti). Il Controller gestisce questo caso con una guardia.
     */
    public Studente getStudenteValutazione() {
        return (Studente) comboStudente.getSelectedItem();
    }

    /*
     * Il Controller chiama questo metodo quando apre la gestione registro,
     * passandoci la lista degli studenti della classe. Io svuoto la combo
     * e la riempio. Se la lista e' vuota la combo resta senza opzioni
     * (getStudenteValutazione tornera' null) e ci pensera' la guardia.
     */
    public void setStudentiClasse(List<Studente> studenti) {
        comboStudente.removeAllItems();
        for (Studente s : studenti) {
            comboStudente.addItem(s);
        }
    }

    public void addSalvaValutazioneListener(ActionListener listener) {
        buttonSalvaValutazione.addActionListener(listener);
    }

    public void mostraErroreValutazione(String messaggio) {
        labelMessaggioValutazione.setForeground(new Color(200, 0, 0));
        labelMessaggioValutazione.setText(messaggio);
    }

    public void mostraSuccessoValutazione(String messaggio) {
        labelMessaggioValutazione.setForeground(new Color(0, 128, 0));
        labelMessaggioValutazione.setText(messaggio);
    }

    public void pulisciMessaggioValutazione() {
        labelMessaggioValutazione.setText(" ");
    }

    /*
     * Reset dopo un salvataggio ok. Data a oggi, voto a 6, descrizione
     * vuota. Le combo Tipologia e Studente le lascio come sono (di solito
     * uno valuta piu' alunni della stessa classe in fila, e' utile
     * mantenere la selezione precedente).
     */
    public void pulisciCampiValutazione() {
        spinnerDataValutazione.setValue(new Date());
        spinnerVotoValutazione.setValue(6.0);
        fieldDescrizioneValutazione.setText("");
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(15, 15, 15, 15), 0, 10));
        final JPanel subbar = new JPanel();
        subbar.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 0, 0, true, false));
        panel1.add(subbar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toggleLezione = new JToggleButton();
        toggleLezione.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleLezione.setText("Lezione");
        subbar.add(toggleLezione, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(95, 22), null, 0, false));
        toggleCompito = new JToggleButton();
        toggleCompito.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleCompito.setText("Compito");
        subbar.add(toggleCompito, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(95, 22), null, 0, false));
        toggleValutazione = new JToggleButton();
        toggleValutazione.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleValutazione.setText("Valutazione");
        subbar.add(toggleValutazione, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(95, 22), null, 0, false));
        panelContenuto = new JPanel();
        panelContenuto.setLayout(new BorderLayout(0, 0));
        panel1.add(panelContenuto, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
