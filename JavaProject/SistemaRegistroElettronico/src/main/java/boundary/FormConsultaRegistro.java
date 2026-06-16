package boundary;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import controller.GestoreServiziDocente;
import entity.ClasseVirtuale;
import entity.Compito;
import entity.Lezione;
import entity.Studente;
import entity.Valutazione;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/*
 * Sotto-form del flow docente per il caso d'uso "Consulta Registro".
 *
 * Viene incastrato dentro panelContenuto di FormGestioneRegistro quando
 * il toggle "Consulta Registro" e' selezionato.
 *
 * Layout: sub-toggle bar (Lezioni / Compiti / Monitora)
 * — e un panelContenuto interno che ospita alternativamente:
 *  - scrollLezioni / scrollCompiti: JList<String> read-only con item
 *    HTML, come con studente
 *  - panelMonitora: form con spinner Da/A + bottone "Mostra" sopra,
 *    elenco valutazioni filtrate + media in basso. Il monitor mostra
 *    nelle valutazioni anche il NOME dello studente valutato, in piu'
 *    rispetto al profilo studente.
 */
public class FormConsultaRegistro {

    private JPanel panel1;
    private JToggleButton toggleLezioni;
    private JToggleButton toggleCompiti;
    private JToggleButton toggleMonitora;
    private JPanel panelContenuto;

    /*
     * Liste read-only per Lezioni e Compiti — pattern identico a
     * FormVisualizzazioneProfilo: JList<String> sopra DefaultListModel
     * + JScrollPane
     *
     * Ogni item è trattato come per la Visualizzazione dello Studente, viene utilizzato
     * un semplice layout HTML (riutilizzo quello dello Studente)
     */
    private final DefaultListModel<String> modelLezioni = new DefaultListModel<>();
    private final DefaultListModel<String> modelCompiti = new DefaultListModel<>();
    private final DefaultListModel<String> modelMonitora = new DefaultListModel<>();

    private final JList<String> listLezioni = new JList<>(modelLezioni);
    private final JList<String> listCompiti = new JList<>(modelCompiti);
    private final JList<String> listMonitora = new JList<>(modelMonitora);

    private final JScrollPane scrollLezioni = new JScrollPane(listLezioni);
    private final JScrollPane scrollCompiti = new JScrollPane(listCompiti);
    private final JScrollPane scrollMonitora = new JScrollPane(listMonitora);

    /*
     * Container Monitora: in alto la barra (spinner Da/A + bottone Mostra +
     * label errore), in basso un sub-container con la lista valutazioni
     * filtrate + label media in basso a destra (stesso layout del
     * Valutazioni del profilo studente).
     */
    private final JPanel panelMonitora = new JPanel(new BorderLayout(0, 8));
    private final JSpinner spinnerDataDa = new JSpinner(new SpinnerDateModel());
    private final JSpinner spinnerDataA = new JSpinner(new SpinnerDateModel());
    private final JButton buttonMostra = new JButton("Mostra");
    private final JLabel labelMessaggioMonitora = new JLabel(" ");
    private final JLabel labelMedia = new JLabel("Media: 0.00", SwingConstants.RIGHT);
    private final JPanel panelRisultatoMonitora = new JPanel(new BorderLayout());

    /*
     * La ClasseVirtuale corrente, settata da FormGestioneRegistro tramite
     * setClasse() all'apertura del registro. Lezioni e Compiti vengono
     * popolati subito (non dipendono dall'intervallo); il listener Mostra
     * la legge al click per chiedere al controller le valutazioni filtrate.
     */
    private ClasseVirtuale classe;

    public FormConsultaRegistro() {

        toggleLezioni.putClientProperty("JButton.buttonType", "segmented");
        toggleLezioni.putClientProperty("JButton.segmentPosition", "first");
        toggleCompiti.putClientProperty("JButton.buttonType", "segmented");
        toggleCompiti.putClientProperty("JButton.segmentPosition", "middle");
        toggleMonitora.putClientProperty("JButton.buttonType", "segmented");
        toggleMonitora.putClientProperty("JButton.segmentPosition", "last");

        ButtonGroup gruppo = new ButtonGroup();
        gruppo.add(toggleLezioni);
        gruppo.add(toggleCompiti);
        gruppo.add(toggleMonitora);

        buildPanelMonitora();
        panelContenuto.setBorder(BorderFactory.createLineBorder(new Color(0xCCCCCC)));

        // Stato iniziale: vista "Lezioni"
        toggleLezioni.setSelected(true);
        mostraSotto(scrollLezioni);

        toggleLezioni.addActionListener(e -> mostraSotto(scrollLezioni));
        toggleCompiti.addActionListener(e -> mostraSotto(scrollCompiti));
        toggleMonitora.addActionListener(e -> mostraSotto(panelMonitora));

       //Stesso utilizzo delle liste read-only non selezionabili come con studente
        ListSelectionModel noSelection = new DefaultListSelectionModel() {
            @Override public void setSelectionInterval(int i, int j) {}
            @Override public void addSelectionInterval(int i, int j) {}
        };
        listLezioni.setSelectionModel(noSelection);
        listCompiti.setSelectionModel(noSelection);
        listMonitora.setSelectionModel(noSelection);

        /*
         * Listener Mostra. Guardia sull'intervallo (data finale prima della
         * iniziale) nel Boundary; il Controller assume input gia' validato.
         * Il facade fa il fetch totale della classe + filtro Java sul range.
         */
        buttonMostra.addActionListener(ev -> {
            pulisciMessaggioMonitora();
            LocalDate da = getDataDa();
            LocalDate a = getDataA();
            if (a.isBefore(da)) {
                mostraErroreMonitora("La data finale non puo' essere prima di quella iniziale");
                return;
            }
            List<Valutazione> valutazioni = GestoreServiziDocente.visualizzaValutazioniConIntervallo(classe, da, a);
            setValutazioniMonitorate(valutazioni);
            setMediaMonitorata(GestoreServiziDocente.calcolaMediaClasse(valutazioni));
        });
    }

    /*
     * Setta la ClasseVirtuale corrente e popola subito Lezioni/Compiti
     * (non dipendono dall'intervallo, sono caricati una sola volta
     * all'apertura). Le valutazioni invece dipendono dal range scelto
     * dal docente e vengono caricate dal listener Mostra.
     */
    public void setClasse(ClasseVirtuale classe) {
        this.classe = classe;
        setLezioni(GestoreServiziDocente.visualizzaLezioni(classe));
        setCompiti(GestoreServiziDocente.visualizzaCompiti(classe));
    }

    /*
     * Sotto-pannello Monitora:
     * Formato dagli spinner per la data, il bottone Mostra e un sotto panel ulteriore
     * per la visualizzazione di tutte le Valutazioni + Media
     */
    private void buildPanelMonitora() {

        spinnerDataDa.setEditor(new JSpinner.DateEditor(spinnerDataDa, "dd/MM/yyyy"));
        spinnerDataA.setEditor(new JSpinner.DateEditor(spinnerDataA, "dd/MM/yyyy"));
        spinnerDataDa.setPreferredSize(new Dimension(140, 26));
        spinnerDataA.setPreferredSize(new Dimension(140, 26));

        // Default: dall'inizio dell'anno scolastico corrente a oggi.
        // L'anno scolastico inizia il 1 settembre: se siamo a settembre o
        // dopo, e' l'anno corrente; altrimenti e' l'anno precedente.
        LocalDate oggi = LocalDate.now();
        int annoInizio = oggi.getMonthValue() >= 9 ? oggi.getYear() : oggi.getYear() - 1;
        LocalDate inizioAnnoScolastico = LocalDate.of(annoInizio, 9, 1);
        spinnerDataDa.setValue(toDate(inizioAnnoScolastico));
        spinnerDataA.setValue(toDate(oggi));

        JPanel barraDate = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        JLabel labelDa = new JLabel("Da:");
        labelDa.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JLabel labelA = new JLabel("A:");
        labelA.setFont(new Font("SansSerif", Font.PLAIN, 13));
        buttonMostra.setFont(new Font("SansSerif", Font.BOLD, 12));
        buttonMostra.setPreferredSize(new Dimension(100, 26));

        barraDate.add(labelDa);
        barraDate.add(spinnerDataDa);
        barraDate.add(Box.createHorizontalStrut(8));
        barraDate.add(labelA);
        barraDate.add(spinnerDataA);
        barraDate.add(Box.createHorizontalStrut(8));
        barraDate.add(buttonMostra);

        labelMessaggioMonitora.setFont(new Font("SansSerif", Font.ITALIC, 13));
        labelMessaggioMonitora.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel header = new JPanel(new BorderLayout(0, 2));
        header.add(barraDate, BorderLayout.CENTER);
        header.add(labelMessaggioMonitora, BorderLayout.SOUTH);

        labelMedia.setFont(new Font("SansSerif", Font.BOLD, 12));
        labelMedia.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        panelRisultatoMonitora.add(scrollMonitora, BorderLayout.CENTER);
        panelRisultatoMonitora.add(labelMedia, BorderLayout.SOUTH);

        panelMonitora.add(header, BorderLayout.NORTH);
        panelMonitora.add(panelRisultatoMonitora, BorderLayout.CENTER);
    }

    /*
     * Converte LocalDate -> Date alla mezzanotte locale, per i SpinnerDateModel.
     */
    private static Date toDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public JComponent getPanel() {
        return panel1;
    }

    /*
     * Sostituisce il contenuto di panelContenuto con il sotto-componente
     * richiesto. revalidate + repaint perche' stiamo cambiando i figli
     * di un container gia' visibile.
     */
    private void mostraSotto(JComponent sotto) {
        panelContenuto.removeAll();
        panelContenuto.add(sotto, BorderLayout.CENTER);
        panelContenuto.revalidate();
        panelContenuto.repaint();
    }

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /*
    Qui i metodi che vanno a generare il layout in HTML per ogni elemento visualizzabile
    nel Panel di Consulta registro
     */

    /*
     * Stesso formato del profilo studente: argomento + data in bold,
     * descrizione in italic grigio sotto.
     */
    public void setLezioni(List<Lezione> lezioni) {
        modelLezioni.clear();
        for (Lezione l : lezioni) {
            modelLezioni.addElement(
                "<html><b>" + l.getArgomentoTrattato() + " — " + l.getData().format(FMT_DATA) + "</b><br>"
                + "<font color='gray'><i>" + l.getDescrizione() + "</i></font></html>"
            );
        }
    }

    public void setCompiti(List<Compito> compiti) {
        modelCompiti.clear();
        for (Compito c : compiti) {
            modelCompiti.addElement(
                "<html><b>" + c.getTitolo() + " — " + c.getDataDiAssegnazione().format(FMT_DATA)
                + " → " + c.getDataDiScadenza().format(FMT_DATA) + "</b><br>"
                + "<font color='gray'><i>" + c.getDescrizione() + "</i></font></html>"
            );
        }
    }

    /*
     * Versione "lato docente" del Valutazione: stesso identico formato del
     * profilo studente, con in piu' il NOME dello studente valutato in testa
     * alla riga bold:
     *   "Nome Cognome — voto — tipologia — data"
     *   "descrizione (italic grigio)"
     */
    public void setValutazioniMonitorate(List<Valutazione> valutazioni) {
        modelMonitora.clear();
        for (Valutazione v : valutazioni) {
            Studente s = v.getStudenteValutato();
            String tipologiaLeggibile = v.getTipologia().name().toLowerCase().replace('_', ' ');
            modelMonitora.addElement(
                "<html><b>" + s.getNome() + " " + s.getCognome()
                + " — " + String.format("%.1f", v.getVoto())
                + " — " + tipologiaLeggibile
                + " — " + v.getData().format(FMT_DATA) + "</b><br>"
                + "<font color='gray'><i>" + v.getDescrizione() + "</i></font></html>"
            );
        }
    }

    /*
     * Media della classe nell'intervallo. Il facade ritorna il double
     * "grezzo": qui lo arrotondiamo alla seconda cifra col
     * formato "%.2f", coerente col profilo studente.
     */
    public void setMediaMonitorata(double media) {
        labelMedia.setText("Media: " + String.format("%.2f", media));
    }

    /*
     * Getter delle due date in formato LocalDate, coerente con gli
     * spinner di FormAggiornaRegistro: il Controller li riceve gia'
     * normalizzati e applica le guardie.
     */
    public LocalDate getDataDa() {
        return ((Date) spinnerDataDa.getValue()).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public LocalDate getDataA() {
        return ((Date) spinnerDataA.getValue()).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    /*
     * Feedback "rosso" sull'header del Monitora — stesso pattern delle
     * label messaggio di FormAggiornaRegistro.
     */
    public void mostraErroreMonitora(String testo) {
        labelMessaggioMonitora.setForeground(new Color(0xC62828));
        labelMessaggioMonitora.setText(testo);
    }

    public void pulisciMessaggioMonitora() {
        labelMessaggioMonitora.setText(" ");
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
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), 0, 10));
        final JPanel subbar = new JPanel();
        subbar.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 0, 0, true, false));
        panel1.add(subbar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toggleLezioni = new JToggleButton();
        toggleLezioni.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleLezioni.setText("Lezioni");
        subbar.add(toggleLezioni, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(95, 22), null, 0, false));
        toggleCompiti = new JToggleButton();
        toggleCompiti.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleCompiti.setText("Compiti");
        subbar.add(toggleCompiti, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(95, 22), null, 0, false));
        toggleMonitora = new JToggleButton();
        toggleMonitora.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleMonitora.setText("Monitora");
        subbar.add(toggleMonitora, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(95, 22), null, 0, false));
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