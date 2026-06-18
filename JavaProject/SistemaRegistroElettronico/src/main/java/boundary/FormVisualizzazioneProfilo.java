package boundary;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import controller.GestoreServiziStudente;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * Form di visualizzazione del profilo studente.
 *
 * In alto: bottone Indietro + label "Profilo di Nome Cognome".
 * Sotto: una sub-toggle bar (Lezioni / Compiti / Valutazioni)
 * Che permette di scegliere quali elementi del Registro visualizzare
 *
 * Le 3 liste sono JList<String> sopra un DefaultListModel<String>: ogni
 * elemento e' una stringa HTML che il renderer di default di Swing sa
 * interpretare (multi-riga, grassetto, italic).
 * Abbiamo quindi utilizzato un HTML leggerissmo per permettere una
 * visualizzazione degli elementi più carina, dato che dovendo solo visualizzare
 * i dati, non c'è bisogno di accedere ai dati strutturati, ma possiamo utilizzare gli attributi
 *
 * Il Controller riempie le tre liste chiamando setLezioni / setCompiti /
 * setValutazioni (vedi sotto) tramite cui poi facciamo la formattazione HTML
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
     * "Indietro" registrato nel costruttore (vedi sotto): la lambda function cattura
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
    Questi invece li useremo per scrollare gli elementi (Valutazioni in basso a destra
    avrà anche la media calcolata al momento)
     */
    private final JScrollPane scrollLezioni = new JScrollPane(listLezioni);
    private final JScrollPane scrollCompiti = new JScrollPane(listCompiti);
    private final JScrollPane scrollValutazioni = new JScrollPane(listValutazioni);

    /*
     * Per le Valutazioni vogliamo mostrare la media in basso a destra,
     * con modalità "sticky" cioè non si muove dalla schermata(non scrolla con la lista).
     * Avvolgo lo scroll + lalabel in un container con BorderLayout: scroll al CENTER (prende
     * tutto lo spazio), label allineata a destra in basso.
     *
     * E' questo container, non lo scroll diretto, che il toggle
     * Valutazioni inserisce dentro panelContenuto.
     */
    private final JLabel labelMedia = new JLabel("Media: 0.00", SwingConstants.RIGHT);
    private final JPanel panelValutazioniContainer = new JPanel(new BorderLayout());

    public FormVisualizzazioneProfilo() {

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

        /*
        Liste read-only: niente selezione cliccabile/evidenziabile.
        Non è un problema dato che uno studente non deve poter modificare o accedere
        ai dati strutturati
         */
        ListSelectionModel noSelection = new DefaultListSelectionModel() {
            @Override public void setSelectionInterval(int i, int j) {}
            @Override public void addSelectionInterval(int i, int j) {}
        };
        listLezioni.setSelectionModel(noSelection);
        listCompiti.setSelectionModel(noSelection);
        listValutazioni.setSelectionModel(noSelection);

        /*
         * Tasto indietro: chiudo questo frame e riapro il login studente. La lambda
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
     * Carica i dati dal controller, popola la UI, crea il JFrame e lo
     * mostra.
     *
     * Chiamato dal listener "Conferma" di FormSceltaClasse (dopo che lo
     * studente ha scelto la classe). Il listener Back, registrato qui
     * nel costruttore, chiude `frame` e riapre il login.
     */
    public JFrame apriProfilo(Map<String, String> studente, String classe) {

        labelProfilo.setText("Profilo di " + studente.get("nome") + " " + studente.get("cognome"));

        /*
         * Tiro fuori dal controller le 3 liste + la media. La lista
         * valutazioni la tengo in locale per riusarla nel calcolo media.
         Possiamo farlo perché il controller ha metodi stateless
         */
        setLezioni(GestoreServiziStudente.visualizzaLezioni(classe));
        setCompiti(GestoreServiziStudente.visualizzaCompiti(classe));
        List<Map<String, String>> valutazioni = GestoreServiziStudente.visualizzaValutazioni(studente, classe);
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
     * Ogni elemento del model e' una stringa HTML: <b>argomento — data</b>
     * sopra, descrizione in italic grigio sotto. JLabel riconosce il prefisso <html>
      e la disegna su piu' righe, facendo il clear del model per poi disegnare ogni elemento
      * della lista passata in input. Le Map arrivano gia' adattate dal Controller
      * (data formattata dd/MM/yyyy), il Boundary fa solo presentazione HTML.
     */
    public void setLezioni(List<Map<String, String>> lezioni) {
        modelLezioni.clear();
        for (Map<String, String> l : lezioni) {
            modelLezioni.addElement(
                "<html><b>" + l.get("argomentoTrattato") + " — " + l.get("data") + "</b><br>"
                + "<font color='gray'><i>" + l.get("descrizione") + "</i></font></html>"
            );
        }
    }

    /*
     * Stessa idea della setLezioni: titolo + le due date (con freccia
     * per rendere chiaro che si va da assegnazione a scadenza),
     * descrizione sotto in italic grigio.
     */
    public void setCompiti(List<Map<String, String>> compiti) {
        modelCompiti.clear();
        for (Map<String, String> c : compiti) {
            modelCompiti.addElement(
                "<html><b>" + c.get("titolo") + " — " + c.get("dataDiAssegnazione")
                + " → " + c.get("dataDiScadenza") + "</b><br>"
                + "<font color='gray'><i>" + c.get("descrizione") + "</i></font></html>"
            );
        }
    }

    /*
     * Per le valutazioni: voto, tipologia leggibile ("prova scritta") e
     * data sono gia' pronti dentro la Map adattata dal Controller. Niente
     * formattazione %.1f / .toLowerCase qui — il Boundary fa solo HTML.
     */
    public void setValutazioni(List<Map<String, String>> valutazioni) {
        modelValutazioni.clear();
        for (Map<String, String> v : valutazioni) {
            modelValutazioni.addElement(
                "<html><b>" + v.get("voto") + " — " + v.get("tipologia")
                + " — " + v.get("data") + "</b><br>"
                + "<font color='gray'><i>" + v.get("descrizione") + "</i></font></html>"
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
