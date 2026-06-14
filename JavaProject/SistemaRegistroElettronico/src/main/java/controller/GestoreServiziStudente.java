package controller;

import boundary.FormServiziStudente;
import boundary.FormVisualizzazioneProfilo;
import entity.ClasseVirtuale;
import entity.GestoreVisualizzazione;
import entity.Studente;

import javax.swing.JFrame;
import java.util.List;

public class GestoreServiziStudente {

	/*
	 * Punto di ingresso del flow studente.
	 * Apre FormServiziStudente in una nuova finestra, popola la combo
	 * con le ClasseVirtuale e, al click su "Conferma", valida l'input
	 * e (se lo studente esiste) apre il profilo.
	 *
	 * Tutto l'accesso al DB e' delegato a GestoreVisualizzazione (facade
	 * in entity/): il controller non conosce JPQL ne' GestorePersistenza.
	 */
	public void avvia() {

		FormServiziStudente form = new FormServiziStudente();

		JFrame frame = new JFrame("Accesso Studente");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(form.getPanel());
		frame.setSize(500, 400);
		frame.setLocationRelativeTo(null);

		/*
		Conversiamo col Facade nel package entity che copra il caso d'uso della
		visualizzazione del profilo, per interrogarlo riguardo il database
		 */
		GestoreVisualizzazione gv = new GestoreVisualizzazione();

		List<ClasseVirtuale> classi = gv.elencoClassi();
		form.setClassi(classi);

		form.addConfermaListener(e -> {

			form.pulisciErrore();

			/*
			Prendo i dati inseriti nel form
			 */
			ClasseVirtuale classeSelezionata = form.getClasseSelezionata();
			String nome = form.getNomeInserito();
			String cognome = form.getCognomeInserito();

			/*
			Guardie a livello boundary per evitare errori in fase di inserimento
			Se tutto va bene viene aperto il JPanel della Visualizzazione
			 */

			if (classeSelezionata == null) {
				form.mostraErrore("Selezionare una classe");
				return;
			}

			if (nome.isEmpty() || cognome.isEmpty()) {
				form.mostraErrore("Inserire nome e cognome");
				return;
			}

			Studente studente = gv.cercaStudenteInClasse(nome, cognome, classeSelezionata);

			if (studente == null) {
				form.mostraErrore("Studente non trovato");
				return;
			}

			frame.dispose();
			apriProfilo(studente, classeSelezionata);
		});

		frame.setVisible(true);
	}

	/*
	 * Apre la finestra del profilo per lo studente trovato.
	 * Helper separato da avvia() per mantenere il flow principale leggibile.
	 */
	private void apriProfilo(Studente studente, ClasseVirtuale classe) {

		FormVisualizzazioneProfilo profilo = new FormVisualizzazioneProfilo();
		profilo.mostraProfilo(studente, classe);

		JFrame frame = new JFrame("Profilo Studente");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(profilo.getPanel());
		frame.setSize(500, 400);
		frame.setLocationRelativeTo(null);

		/*
		Back -> chiudo il profilo e riapro la schermata di scelta studente
		riusando avvia(): combo ripopolata da DB, campi vuoti.
		 */
		profilo.addBackListener(e -> {
			frame.dispose();
			avvia();
		});

		frame.setVisible(true);
	}

	public void visualizzaProfilo() {
		// TODO - implement controller.GestoreServiziStudente.visualizzaProfilo
		throw new UnsupportedOperationException();
	}

}
