package app;

import boundary.FormSceltaUtente;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/*
 * Punto di ingresso dell'applicazione.
 *
 * Si limita a impostare il LookAndFeel di sistema e ad aprire il
 * Boundary di root (FormSceltaUtente) sull'EDT. Niente JFrame, niente
 * controller: il Boundary possiede il proprio frame e i propri listener,
 * coerente con il pattern del professore (vedi MainFrame di EsempioBCED).
 */
public class MainAvviaApp {

    public static void main(String[] args) {
        /*
         * Attivo il LookAndFeel di sistema (su macOS = Aqua) prima di costruire
         * qualunque componente Swing. Serve perche' FormAggiornaRegistro e
         * FormConsultaRegistro usano le client property
         * "JButton.buttonType=segmented" + "segmentPosition" per ottenere il
         * segmented control nativo su Lezione/Compito/Valutazione: quelle
         * property vengono renderizzate solo da Aqua. Se per qualche motivo il
         * L&F non e' disponibile, ci si ricade su quello di default e i toggle
         * restano funzionali (solo non segmentati graficamente).
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new FormSceltaUtente().apriSceltaUtente());
    }
}
