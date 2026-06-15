package controller;

import boundary.FormSceltaClasse;
import boundary.FormServiziStudente;
import boundary.FormVisualizzazioneProfilo;
import entity.ClasseVirtuale;
import entity.GestoreVisualizzazione;
import entity.Studente;

import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.Dialog;
import java.util.List;

public class GestoreServiziStudente {

	/*
	 * Punto di ingresso del flow studente.
	 * Apre FormServiziStudente (solo nome+cognome), valida l'input e, se lo
	 * studente esiste e ha almeno una classe, apre il popup FormSceltaClasse
	 * con SOLO le sue classi (recuperate via GestoreVisualizzazione.classiDi).
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

		GestoreVisualizzazione gv = new GestoreVisualizzazione();

		form.addConfermaListener(e -> {

			form.pulisciErrore();

			String nome = form.getNomeInserito();
			String cognome = form.getCognomeInserito();

			if (nome.isEmpty() || cognome.isEmpty()) {
				form.mostraErrore("Inserire nome e cognome");
				return;
			}

			Studente studente = gv.cercaStudente(nome, cognome);
			if (studente == null) {
				form.mostraErrore("Studente non trovato");
				return;
			}

			List<ClasseVirtuale> classi = gv.classiDi(studente);
			if (classi.isEmpty()) {
				form.mostraErrore("Nessuna classe associata a questo studente");
				return;
			}

			apriSceltaClasse(frame, studente, classi);
		});

		frame.setVisible(true);
	}

	/*
	 * Mostra il popup FormSceltaClasse con la lista delle classi dello studente.
	 * Il dialog e' modale rispetto al frame di login: blocca il flow finche'
	 * l'utente sceglie o annulla. Su Conferma: chiude tutto e apre il profilo.
	 * Su Annulla (o X): chiude solo il dialog e lascia il login a disposizione.
	 */
	private void apriSceltaClasse(JFrame parent, Studente studente, List<ClasseVirtuale> classi) {

		FormSceltaClasse form = new FormSceltaClasse();
		form.setClassi(classi);

		JDialog dialog = new JDialog(parent, "Seleziona Classe", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setContentPane(form.getPanel());
		dialog.setSize(420, 240);
		dialog.setLocationRelativeTo(parent);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		form.addConfermaListener(e -> {
			ClasseVirtuale scelta = form.getClasseSelezionata();
			dialog.dispose();
			parent.dispose();
			apriProfilo(studente, scelta);
		});

		form.addAnnullaListener(e -> dialog.dispose());

		dialog.setVisible(true);
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
		Back -> chiudo il profilo e riapro il login studente
		riusando avvia(): campi vuoti.
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