package controller;

import boundary.FormGestioneRegistro;
import boundary.FormServiziDocente;
import entity.ClasseVirtuale;
import entity.Docente;
import entity.GestoreRegistroDocente;

import javax.swing.JFrame;
import java.util.List;

public class GestoreServiziDocente {

	/*
	 * Punto di ingresso del flow docente.
	 * Apre FormServiziDocente, popola la combo con le ClasseVirtuale e,
	 * al click su "Conferma", valida l'input e (se il docente esiste)
	 * apre la schermata di gestione registro.
	 *
	 * Tutto l'accesso al DB e' delegato a GestoreRegistroDocente (facade
	 * in entity/): il controller non conosce JPQL ne' GestorePersistenza.
	 */
	public void avvia() {

		FormServiziDocente form = new FormServiziDocente();

		JFrame frame = new JFrame("Accesso Docente");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(form.getPanel());
		frame.setSize(500, 400);
		frame.setLocationRelativeTo(null);

		GestoreRegistroDocente gr = new GestoreRegistroDocente();

		List<ClasseVirtuale> classi = gr.elencoClassi();
		form.setClassi(classi);

		form.addConfermaListener(e -> {

			form.pulisciErrore();

			ClasseVirtuale classeSelezionata = form.getClasseSelezionata();
			String nome = form.getNomeInserito();
			String cognome = form.getCognomeInserito();

			if (classeSelezionata == null) {
				form.mostraErrore("Selezionare una classe");
				return;
			}

			if (nome.isEmpty() || cognome.isEmpty()) {
				form.mostraErrore("Inserire nome e cognome");
				return;
			}

			Docente docente = gr.cercaDocenteDiClasse(nome, cognome, classeSelezionata);

			if (docente == null) {
				form.mostraErrore("Docente non trovato per questa classe");
				return;
			}

			frame.dispose();
			apriGestioneRegistro(docente, classeSelezionata);
		});

		frame.setVisible(true);
	}

	/*
	 * Apre la finestra di gestione registro per il docente autenticato.
	 * Per ora i due sotto-pannelli (Aggiorna/Consulta) sono placeholder:
	 * verranno riempiti quando implementeremo i casi d'uso.
	 */
	private void apriGestioneRegistro(Docente docente, ClasseVirtuale classe) {

		FormGestioneRegistro form = new FormGestioneRegistro();

		JFrame frame = new JFrame("Gestione Registro — " + classe.getNome());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(form.getPanel());
		frame.setSize(600, 450);
		frame.setLocationRelativeTo(null);

		/*
		Back -> chiudo la gestione registro e riapro la schermata
		di scelta classe/docente riusando avvia().
		 */
		form.addBackListener(e -> {
			frame.dispose();
			avvia();
		});

		frame.setVisible(true);
	}

	public void consultaRegistro() {
		// TODO - implement controller.GestoreServiziDocente.consultaRegistro
		throw new UnsupportedOperationException();
	}

	public void aggiornaRegistro() {
		// TODO - implement controller.GestoreServiziDocente.aggiornaRegistro
		throw new UnsupportedOperationException();
	}

}
