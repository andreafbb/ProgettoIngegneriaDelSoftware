package app;

import boundary.FormSceltaUtente;
import controller.GestoreServiziDocente;
import controller.GestoreServiziStudente;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/*
 * Punto di ingresso dell'applicazione.
 *
 * Crea la finestra principale con FormSceltaUtente dentro
 * e aggancia ai due bottoni i rispettivi controller.
 */
public class MainAvviaApp {

    public static void main(String[] args) {
        /*
         * Attivo il LookAndFeel di sistema (su macOS = Aqua) prima di costruire
         * qualunque componente Swing. Serve perche' FormAggiornaRegistro usa
         * le client property "JButton.buttonType=segmented" + "segmentPosition"
         * per ottenere il segmented control nativo su Lezione/Compito/Valutazione:
         * quelle property vengono renderizzate solo da Aqua. Se per qualche
         * motivo il L&F non e' disponibile, ci si ricade su quello di default
         * e i toggle restano funzionali (solo non segmentati graficamente).
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(MainAvviaApp::avviaInterfaccia);
    }


    private static void avviaInterfaccia() {

        // Creo il form (panel + bottoni)
        FormSceltaUtente form = new FormSceltaUtente();

        // Creo la finestra principale e ci infilo dentro il panel
        JFrame frame = new JFrame("Sistema Registro Elettronico");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(form.getPanel());
        frame.setSize(700, 350);
        frame.setLocationRelativeTo(null);    // centra la finestra sullo schermo

        //Aggancio i listener ai due bottoni del form

        //    Click su "Studente": chiudo questa finestra
        //    e cedo il controllo al GestoreServiziStudente
        form.addStudenteListener(e -> {
            frame.dispose();
            new GestoreServiziStudente().avvia();
        });

        //    Click su "Docente": stessa logica con il GestoreServiziDocente
        form.addDocenteListener(e -> {
            frame.dispose();
            new GestoreServiziDocente().avvia();
        });

                // Mostro la finestra a schermo
        frame.setVisible(true);
    }
}
