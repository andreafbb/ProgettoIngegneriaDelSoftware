package app;

import boundary.FormSceltaUtente;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/*
 * Punto di ingresso dell'applicazione.
 *
 * Si limita a impostare il LookAndFeel di sistema e ad aprire il
 * Boundary di root (FormSceltaUtente) sull'EDT. Niente JFrame, niente
 * controller: il Boundary possiede il proprio frame e i propri listener.
 */
public class MainAvviaApp {

    public static void main(String[] args) {
        /*
         * Attivo il LookAndFeel di sistema (su macOS = Aqua) prima di costruire
         * qualunque componente Swing. Serve perche' FormAggiornaRegistro e
         * FormConsultaRegistro usano le client property
         * "JButton.buttonType=segmented" + "segmentPosition" per ottenere il
         * segmented control nativo su Lezione/Compito/Valutazione
         * Sarebbe la gestione dei bottoni come Toggle (utilizzata praticamente su tutto
         * il nostro progetto)
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new FormSceltaUtente().apriSceltaUtente());
    }
}
