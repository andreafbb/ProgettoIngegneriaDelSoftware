package boundary;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import controller.GestoreServiziStudente;
import entity.ClasseVirtuale;
import entity.Compito;
import entity.Lezione;
import entity.Studente;
import entity.Valutazione;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/*
 * Form di visualizzazione del profilo studente.
 *
 * In alto: bottone Indietro + label "Profilo di Nome Cognome".
 * Sotto: una sub-toggle bar (Lezioni / Compiti / Valutazioni) identica
 * a quella del docente, e un panelContenuto bordato che mostra
 * alternativamente la lista (scrollabile) del tipo selezionato.
 *
 * Le 3 liste sono JList<String> sopra un DefaultListModel<String>: ogni
 * elemento e' una stringa HTML che il renderer di default di Swing sa
 * interpretare (multi-riga, grassetto, italic). Cosi' niente
 * ListCellRenderer custom.
 *
 * Il Controller riempie le tre liste chiamando setLezioni / setCompiti /
 * setValutazioni (vedi sotto): e' li' che facciamo la formattazione HTML
 * di ogni item.
 */
public class FormVisualizzazioneProfilo {

    private JPanel panel1;
    private JButton buttonBack;
    private JLabel labelProfilo;
    private JToggleButton toggleLezioni;
    private JToggleButton toggleCompiti;
    private JToggleButton toggleValutazioni;
    private JPanel panelContenuto;

    /*
     * JFrame "di proprieta'" di questo Boundary, creato in apriProfilo().
     * Tenerlo come campo di istanza ci permette di chiuderlo dal listener
     * "Indietro" registrato nel costruttore (vedi sotto): la lambda cattura
     * `this` e legge il campo nel momento del click, quando ormai apriProfilo
     * lo ha gia' valorizzato.
     */
    private JFrame frame;

    /*
     * Model + JList + JScrollPane per ognuna delle tre liste.
     * Li inizializzo qui come final: ogni JList prende il proprio model,
     * ogni JScrollPane avvolge la propria JList. Sono questi tre
     * JScrollPane che il toggle inserisce dentro panelContenuto.
     */
    private final DefaultListModel<String> modelLezioni = new DefaultListModel<>();
    private final DefaultListModel<String> modelCompiti = new DefaultListModel<>();
    private final DefaultListModel<String> modelValutazioni = new DefaultListModel<>();

    /*
    Queste sono le liste che verranno gestite con HTML per avere il layout
    ordinato (codice molto semplice)
     */
    private final JList<String> listLezioni = new JList<>(modelLezioni);
    private final JList<String> listCompiti = new JList<>(modelCompiti);
    private final JList<String> listValutazioni = new JList<>(modelValutazioni);

    /*
    Questi invece li useremo per scrollare gli elementi (Valutazioni in alto
    avrà anche la media calcolata al momento
     */
    private final JScrollPane scrollLezioni = new JScrollPane(listLezioni);
    private final JScrollPane scrollCompiti = new JScrollPane(listCompiti);
    private final JScrollPane scrollValutazioni = new JScrollPane(listValutazioni);

    /*
     * Per le Valutazioni vogliamo mostrare la media in basso a destra,
     * sempre visibile (non scrolla con la lista). Avvolgo lo scroll + la
     * label in un container con BorderLayout: scroll al CENTER (prende
     * tutto lo spazio), label al SOUTH allineata a destra.
     *
     * E' questo container, non lo scroll diretto, che il toggle
     * Valutazioni inserisce dentro panelContenuto.
     */
    private final JLabel labelMedia = new JLabel("Media: 0.00", SwingConstants.RIGHT);
    private final JPanel panelValutazioniContainer = new JPanel(new BorderLayout());

    public FormVisualizzazioneProfilo() {

        /*
         * Stesso trucco del FormAggiornaRegistro: su macOS (Aqua) le
         * client property segmented disegnano i tre toggle come un'unica
         * barra a 3 segmenti. Su altri L&F vengono ignorate e i toggle
         * restano normali (ma funzionali).
         */
        toggleLezioni.putClientProperty("JButton.buttonType", "segmented");
        toggleLezioni.putClientProperty("JButton.segmentPosition", "first");
        toggleCompiti.putClientProperty("JButton.buttonType", "segmented");
        toggleCompiti.putClientProperty("JButton.segmentPosition", "middle");
        toggleValutazioni.putClientProperty("JButton.buttonType", "segmented");
        toggleValutazioni.putClientProperty("JButton.segmentPosition", "last");

        ButtonGroup gruppo = new ButtonGroup();
        gruppo.add(toggleLezioni);
        gruppo.add(toggleCompiti);
        gruppo.add(toggleValutazioni);

        // Default: vista "Lezioni"
        toggleLezioni.setSelected(true);
        mostraSotto(scrollLezioni);

        // Wiring del container Valutazioni: scroll sopra, label media sotto.
        panelValutazioniContainer.add(scrollValutazioni, BorderLayout.CENTER);
        panelValutazioniContainer.add(labelMedia, BorderLayout.SOUTH);
        labelMedia.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        labelMedia.setFont(new Font("SansSerif", Font.BOLD, 12));

        toggleLezioni.addActionListener(e -> mostraSotto(scrollLezioni));
        toggleCompiti.addActionListener(e -> mostraSotto(scrollCompiti));
        toggleValutazioni.addActionListener(e -> mostraSotto(panelValutazioniContainer));

        // Liste read-only: niente selezione cliccabile/evidenziabile.
        ListSelectionModel noSelection = new DefaultListSelectionModel() {
            @Override public void setSelectionInterval(int i, int j) {}
            @Override public void addSelectionInterval(int i, int j) {}
        };
        listLezioni.setSelectionModel(noSelection);
        listCompiti.setSelectionModel(noSelection);
        listValutazioni.setSelectionModel(noSelection);

        /*
         * Back: chiudo questo frame e riapro il login studente. La lambda
         * legge il campo `frame` al momento del click — apriProfilo lo
         * avra' gia' valorizzato.
         */
        buttonBack.addActionListener(e -> {
            frame.dispose();
            new FormServiziStudente().apriFormServiziStudente();
        });
    }

    public JComponent getPanel() {
        return panel1;
    }

    /*
     * Entry point del Boundary, pattern del professore (apriXxx -> JFrame).
     *
     * Carica i dati dal controller, popola la UI, crea il JFrame e lo
     * mostra. Niente piu' Controller che fa setContentPane / setVisible:
     * il Boundary possiede il proprio frame.
     *
     * Chiamato dal listener "Conferma" di FormSceltaClasse (dopo che lo
     * studente ha scelto la classe). Il listener Back, registrato qui
     * nel costruttore, chiude `frame` e riapre il login.
     */
    public JFrame apriProfilo(Studente studente, ClasseVirtuale classe) {

        labelProfilo.setText("Profilo di " + studente.getNome() + " " + studente.getCognome());

        /*
         * Tiro fuori dal controller le 3 liste + la media. La lista
         * valutazioni la tengo in locale per riusarla nel calcolo media
         * (il controller, stateless, accetta la lista invece di rifare il
         * fetch al DB).
         */
        setLezioni(GestoreServiziStudente.visualizzaLezioni(classe));
        setCompiti(GestoreServiziStudente.visualizzaCompiti(classe));
        List<Valutazione> valutazioni = GestoreServiziStudente.visualizzaValutazioni(studente, classe);
        setValutazioni(valutazioni);
        setMediaStudente(GestoreServiziStudente.calcolaMediaStudente(valutazioni));

        frame = new JFrame("Profilo Studente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel1);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return frame;
    }

    /*
     * Swappa il contenuto di panelContenuto con il sotto-componente
     * passato. revalidate + repaint perche' stiamo cambiando i figli
     * di un container gia' visibile.
     */
    private void mostraSotto(JComponent sotto) {
        panelContenuto.removeAll();
        panelContenuto.add(sotto, BorderLayout.CENTER);
        panelContenuto.revalidate();
        panelContenuto.repaint();
    }

    /*
     * Formato data unico per tutte le tre liste, cosi' resta coerente
     * con gli spinner del docente (dd/MM/yyyy).
     */
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /*
     * Ogni elemento del model e' una stringa HTML: <b>argomento — data</b>
     * sopra, descrizione in italic grigio sotto. JLabel (il renderer di
     * default della JList) riconosce il prefisso <html> e la disegna su
     * piu' righe, senza che dobbiamo scrivere un renderer custom.
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

    /*
     * Stessa idea della setLezioni: titolo + le due date (con freccia
     * per rendere chiaro che si va da assegnazione a scadenza),
     * descrizione sotto in italic grigio.
     */
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
     * Per le valutazioni:
     * - voto formattato con un decimale ("%.1f"), cosi' "6.0" / "8.5"
     *   restano leggibili anche se internamente sono double;
     * - tipologia: l'enum di default si stamperebbe "PROVA_SCRITTA",
     *   che e' brutto; lo trasformo al volo in "prova scritta".
     */
    public void setValutazioni(List<Valutazione> valutazioni) {
        modelValutazioni.clear();
        for (Valutazione v : valutazioni) {
            String tipologiaLeggibile = v.getTipologia().name().toLowerCase().replace('_', ' ');
            modelValutazioni.addElement(
                "<html><b>" + String.format("%.1f", v.getVoto()) + " — " + tipologiaLeggibile
                + " — " + v.getData().format(FMT_DATA) + "</b><br>"
                + "<font color='gray'><i>" + v.getDescrizione() + "</i></font></html>"
            );
        }
    }

    /*
     * Mostra la media nella label in basso a destra del container
     * Valutazioni. Il facade ritorna il double "grezzo": qui lo
     * arrotondiamo alla seconda cifra (half-up) col formato "%.2f",
     * coerente con il formato del voto nelle liste.
     */
    public void setMediaStudente(double media) {
        labelMedia.setText("Media: " + String.format("%.2f", media));
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
        panel1.setLayout(new GridLayoutManager(4, 1, new Insets(20, 30, 20, 30), 0, 10));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        buttonBack = new JButton();
        Font buttonBackFont = this.$$$getFont$$$("SansSerif", -1, 12, buttonBack.getFont());
        if (buttonBackFont != null) buttonBack.setFont(buttonBackFont);
        buttonBack.setText("Indietro");
        panel1.add(buttonBack, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, 24), null, 0, false));
        labelProfilo = new JLabel();
        Font labelProfiloFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 22, labelProfilo.getFont());
        if (labelProfiloFont != null) labelProfilo.setFont(labelProfiloFont);
        labelProfilo.setHorizontalAlignment(0);
        labelProfilo.setText("Profilo");
        panel1.add(labelProfilo, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel subbar = new JPanel();
        subbar.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 0, 0, true, false));
        panel1.add(subbar, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toggleLezioni = new JToggleButton();
        toggleLezioni.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleLezioni.setText("Lezioni");
        subbar.add(toggleLezioni, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(95, 22), null, 0, false));
        toggleCompiti = new JToggleButton();
        toggleCompiti.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleCompiti.setText("Compiti");
        subbar.add(toggleCompiti, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(95, 22), null, 0, false));
        toggleValutazioni = new JToggleButton();
        toggleValutazioni.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggleValutazioni.setText("Valutazioni");
        subbar.add(toggleValutazioni, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(95, 22), null, 0, false));
        panelContenuto = new JPanel();
        panelContenuto.setLayout(new BorderLayout(0, 0));
        panelContenuto.setBorder(BorderFactory.createLineBorder(new Color(-3355444)));
        panel1.add(panelContenuto, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
