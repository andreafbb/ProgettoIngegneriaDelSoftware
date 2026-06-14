package app;

import boundary.FormSceltaUtente;
import controller.GestoreServiziDocente;
import controller.GestoreServiziStudente;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/*
 * Punto di ingresso dell'applicazione.
 *
 * Crea la finestra principale con FormSceltaUtente dentro
 * e aggancia ai due bottoni i rispettivi controller.
 */
public class MainAvviaApp {

    public static void main(String[] args) {
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
