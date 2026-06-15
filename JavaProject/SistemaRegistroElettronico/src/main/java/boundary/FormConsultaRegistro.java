package boundary;

import entity.Compito;
import entity.Lezione;
import entity.Studente;
import entity.Valutazione;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
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
 * Layout: sub-toggle bar (Lezioni / Compiti / Monitora) — identica per
 * stile a quella di FormVisualizzazioneProfilo (segmented su macOS, 95x22)
 * — e un panelContenuto interno che ospita alternativamente:
 *  - scrollLezioni / scrollCompiti: JList<String> read-only con item
 *    HTML, stesso identico rendering del profilo studente;
 *  - panelMonitora: form con spinner Da/A + bottone "Mostra" sopra,
 *    elenco valutazioni filtrate + media in basso. Il monitor mostra
 *    nelle valutazioni anche il NOME dello studente valutato, in piu'
 *    rispetto al profilo studente.
 *
 * NB: questo Form e' costruito tutto programmaticamente (niente .form
 * IntelliJ). Il pattern resta lo stesso dei form bound a .form: getPanel(),
 * setter per i dati, addXxxListener per i trigger.
 */
public class FormConsultaRegistro {

    /*
     * Root del form: in alto la sub-toggle bar, in basso il panelContenuto.
     */
    private final JPanel panel1 = new JPanel(new BorderLayout(0, 10));

    private final JToggleButton toggleLezioni = new JToggleButton("Lezioni");
    private final JToggleButton toggleCompiti = new JToggleButton("Compiti");
    private final JToggleButton toggleMonitora = new JToggleButton("Monitora");

    private final JPanel panelContenuto = new JPanel(new BorderLayout(0, 0));

    /*
     * Liste read-only per Lezioni e Compiti — pattern identico a
     * FormVisualizzazioneProfilo: JList<String> sopra DefaultListModel
     * + JScrollPane. Ogni item e' una stringa HTML che il renderer di
     * default JLabel sa interpretare (no ListCellRenderer custom).
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

    public FormConsultaRegistro() {

        // === Sub-toggle bar (Lezioni / Compiti / Monitora) ===
        toggleLezioni.putClientProperty("JButton.buttonType", "segmented");
        toggleLezioni.putClientProperty("JButton.segmentPosition", "first");
        toggleCompiti.putClientProperty("JButton.buttonType", "segmented");
        toggleCompiti.putClientProperty("JButton.segmentPosition", "middle");
        toggleMonitora.putClientProperty("JButton.buttonType", "segmented");
        toggleMonitora.putClientProperty("JButton.segmentPosition", "last");

        toggleLezioni.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleCompiti.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleMonitora.setFont(new Font("SansSerif", Font.BOLD, 11));

        Dimension dimToggle = new Dimension(95, 22);
        toggleLezioni.setPreferredSize(dimToggle);
        toggleCompiti.setPreferredSize(dimToggle);
        toggleMonitora.setPreferredSize(dimToggle);

        ButtonGroup gruppo = new ButtonGroup();
        gruppo.add(toggleLezioni);
        gruppo.add(toggleCompiti);
        gruppo.add(toggleMonitora);

        JPanel subbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        subbar.add(toggleLezioni);
        subbar.add(toggleCompiti);
        subbar.add(toggleMonitora);

        // === Costruzione panelMonitora ===
        buildPanelMonitora();

        // === Composizione root ===
        panel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel1.add(subbar, BorderLayout.NORTH);
        panel1.add(panelContenuto, BorderLayout.CENTER);
        panelContenuto.setBorder(BorderFactory.createLineBorder(new Color(0xCCCCCC)));

        // Stato iniziale: vista "Lezioni"
        toggleLezioni.setSelected(true);
        mostraSotto(scrollLezioni);

        toggleLezioni.addActionListener(e -> mostraSotto(scrollLezioni));
        toggleCompiti.addActionListener(e -> mostraSotto(scrollCompiti));
        toggleMonitora.addActionListener(e -> mostraSotto(panelMonitora));

        // === Liste read-only: niente selezione cliccabile/evidenziabile ===
        ListSelectionModel noSelection = new DefaultListSelectionModel() {
            @Override public void setSelectionInterval(int i, int j) {}
            @Override public void addSelectionInterval(int i, int j) {}
        };
        listLezioni.setSelectionModel(noSelection);
        listCompiti.setSelectionModel(noSelection);
        listMonitora.setSelectionModel(noSelection);
    }

    /*
     * Sotto-pannello Monitora:
     *   row NORTH: barra "Da: [spinner]   A: [spinner]   [Mostra]"
     *              + labelMessaggio sotto (errore/successo)
     *   row CENTER: scroll lista valutazioni filtrate + labelMedia in basso
     *               a destra (stesso layout del profilo studente Valutazioni).
     */
    private void buildPanelMonitora() {

        // ---- Header con spinner + bottone ----
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

        // ---- Risultato (lista filtrata + media) ----
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
     * "grezzo": qui lo arrotondiamo alla seconda cifra (half-up) col
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

    public void addMostraListener(ActionListener listener) {
        buttonMostra.addActionListener(listener);
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
}
