package controller;

import boundary.FormAggiornaRegistro;
import boundary.FormGestioneRegistro;
import boundary.FormSceltaClasse;
import boundary.FormServiziDocente;
import entity.ClasseVirtuale;
import entity.Compito;
import entity.Docente;
import entity.GestoreAggiornamentiRegistro;
import entity.GestoreRegistroDocente;
import entity.Lezione;
import entity.Studente;
import entity.Tipologia;
import entity.Valutazione;

import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.Dialog;
import java.time.LocalDate;
import java.util.List;

public class GestoreServiziDocente {

	/*
	 * Facade del package entity usati dal flow docente.
	 * - gestoreRegistroDocente: ricerca docente/classi + persistenza
	 *   (cercaDocente, classiDi, registraLezione/Compito/Valutazione).
	 * - gestoreAggiornamenti: creazione (in-memory, niente DB) di nuove
	 *   Entity del registro (creaLezione/Compito/Valutazione).
	 * Istanziati una volta sola per riuso tra avvia() e gli orchestratori
	 * di aggiornaRegistro.
	 */
	private final GestoreRegistroDocente gestoreRegistroDocente = new GestoreRegistroDocente();
	private final GestoreAggiornamentiRegistro gestoreAggiornamenti = new GestoreAggiornamentiRegistro();

	/*
	 * Punto di ingresso del flow docente.
	 * Apre FormServiziDocente (solo nome+cognome), valida l'input e, se il
	 * docente esiste e ha almeno una classe, apre il popup FormSceltaClasse
	 * con SOLO le sue classi (recuperate via GestoreRegistroDocente.classiDi).
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

		form.addConfermaListener(e -> {

			form.pulisciErrore();

			String nome = form.getNomeInserito();
			String cognome = form.getCognomeInserito();

			if (nome.isEmpty() || cognome.isEmpty()) {
				form.mostraErrore("Inserire nome e cognome");
				return;
			}

			Docente docente = gestoreRegistroDocente.cercaDocente(nome, cognome);
			if (docente == null) {
				form.mostraErrore("Docente non trovato");
				return;
			}

			List<ClasseVirtuale> classi = gestoreRegistroDocente.classiDi(docente);
			if (classi.isEmpty()) {
				form.mostraErrore("Nessuna classe associata a questo docente");
				return;
			}

			apriSceltaClasse(frame, docente, classi);
		});

		frame.setVisible(true);
	}

	/*
	 * Mostra il popup FormSceltaClasse con la lista delle classi del docente.
	 * Il dialog e' modale rispetto al frame di login: blocca il flow finche'
	 * l'utente sceglie o annulla. Su Conferma: chiude tutto e apre la
	 * gestione registro. Su Annulla (o X): chiude solo il dialog e lascia
	 * il login a disposizione.
	 */
	private void apriSceltaClasse(JFrame parent, Docente docente, List<ClasseVirtuale> classi) {

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
			apriGestioneRegistro(docente, scelta);
		});

		form.addAnnullaListener(e -> dialog.dispose());

		dialog.setVisible(true);
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
		Back -> chiudo la gestione registro e riapro il login docente
		riusando avvia().
		 */
		form.addBackListener(e -> {
			frame.dispose();
			avvia();
		});

		/*

		Aggiornamento della Lezione

		Il Boundary notifica il click "Salva"; qui leggiamo i campi,
		applichiamo le guardie e deleghiamo all'orchestratore privato
		aggiornaRegistroLezione, che fa la catena crea (facade A) +
		registra (facade B). L'esito boolean diventa feedback UI.
		 */
		FormAggiornaRegistro formAggiorna = form.getFormAggiorna();

		formAggiorna.addSalvaLezioneListener(e -> {

			formAggiorna.pulisciMessaggioLezione();

			LocalDate data = formAggiorna.getDataLezione();
			String argomento = formAggiorna.getArgomentoLezione();
			String descrizione = formAggiorna.getDescrizioneLezione();

			// Niente guardia su data: SpinnerDateModel garantisce sempre un valore valido.
			if (argomento.isEmpty()) {
				formAggiorna.mostraErroreLezione("Inserire l'argomento trattato");
				return;
			}
			if (descrizione.isEmpty()) {
				formAggiorna.mostraErroreLezione("Inserire una descrizione");
				return;
			}

			boolean ok = aggiornaRegistroLezione(classe, data, argomento, descrizione);
			if (ok) {
				formAggiorna.mostraSuccessoLezione("Lezione registrata");
				formAggiorna.pulisciCampiLezione();
			} else {
				formAggiorna.mostraErroreLezione("Errore durante il salvataggio");
			}
		});

		/*
		Aggiornamento del Compito

		Stessa logica della Lezione. Guardia in piu': la scadenza non puo'
		essere prima dell'assegnazione (sarebbe assurdo).
		 */
		formAggiorna.addSalvaCompitoListener(e -> {

			formAggiorna.pulisciMessaggioCompito();

			String titolo = formAggiorna.getTitoloCompito();
			LocalDate dataAssegnazione = formAggiorna.getDataAssegnazioneCompito();
			String descrizione = formAggiorna.getDescrizioneCompito();
			LocalDate dataScadenza = formAggiorna.getDataScadenzaCompito();

			if (titolo.isEmpty()) {
				formAggiorna.mostraErroreCompito("Inserire il titolo del compito");
				return;
			}
			if (descrizione.isEmpty()) {
				formAggiorna.mostraErroreCompito("Inserire una descrizione");
				return;
			}
			if (dataScadenza.isBefore(dataAssegnazione)) {
				formAggiorna.mostraErroreCompito("La scadenza non puo' essere prima dell'assegnazione");
				return;
			}

			boolean ok = aggiornaRegistroCompito(classe, titolo, dataAssegnazione, descrizione, dataScadenza);
			if (ok) {
				formAggiorna.mostraSuccessoCompito("Compito registrato");
				formAggiorna.pulisciCampiCompito();
			} else {
				formAggiorna.mostraErroreCompito("Errore durante il salvataggio");
			}
		});

		/*
		Per la Valutazione mi serve la lista degli studenti della classe:
		la chiedo al facade e la passo subito alla combo del form. Lo faccio
		una sola volta qui all'apertura del registro: la lista non cambia
		mentre la finestra e' aperta.
		 */
		formAggiorna.setStudentiClasse(gestoreRegistroDocente.cercaStudenti(classe));

		/*
		Aggiornamento della Valutazione

		Guardie ridotte perche' lo spinner del voto e' gia' vincolato al range
		0-10, lo spinner data ha sempre un valore valido, e la combo Tipologia
		e' riempita dall'enum (quindi mai null). Controllo solo che ci sia
		uno studente selezionato (la combo studente puo' essere vuota se la
		classe non ha iscritti) e che la descrizione non sia vuota.
		 */
		formAggiorna.addSalvaValutazioneListener(e -> {

			formAggiorna.pulisciMessaggioValutazione();

			LocalDate data = formAggiorna.getDataValutazione();
			double voto = formAggiorna.getVotoValutazione();
			String descrizione = formAggiorna.getDescrizioneValutazione();
			Tipologia tipologia = formAggiorna.getTipologiaValutazione();
			Studente studente = formAggiorna.getStudenteValutazione();

			if (studente == null) {
				formAggiorna.mostraErroreValutazione("Nessuno studente da valutare (la classe non ha iscritti)");
				return;
			}
			if (descrizione.isEmpty()) {
				formAggiorna.mostraErroreValutazione("Inserire una descrizione");
				return;
			}

			boolean ok = aggiornaRegistroValutazione(classe, data, voto, descrizione, tipologia, studente);
			if (ok) {
				formAggiorna.mostraSuccessoValutazione("Valutazione registrata");
				formAggiorna.pulisciCampiValutazione();
			} else {
				formAggiorna.mostraErroreValutazione("Errore durante il salvataggio");
			}
		});

		frame.setVisible(true);
	}

	public void consultaRegistro() {
		// TODO - implement controller.GestoreServiziDocente.consultaRegistro
		throw new UnsupportedOperationException();
	}

	/*
	 * Orchestratori del caso d'uso "Aggiorna Registro".
	 *
	 * Ognuno riceve la ClasseVirtuale corrente + i dati grezzi del form,
	 * costruisce l'Entity via gestoreAggiornamenti (facade "creatore"), la
	 * persiste via gestoreRegistroDocente (facade "persistente") e propaga
	 * il boolean di esito al chiamante, che lo usera' per il feedback UI.
	 *
	 * Verranno triggerati dai listener "Salva" del FormAggiornaRegistro,
	 * registrati in apriGestioneRegistro nello step successivo.
	 */
	private boolean aggiornaRegistroLezione(ClasseVirtuale classe, LocalDate data, String argomento, String descrizione) {
		Lezione lezione = gestoreAggiornamenti.creaLezione(data, argomento, descrizione, classe);
		return gestoreRegistroDocente.registraLezione(lezione);
	}

	private boolean aggiornaRegistroCompito(ClasseVirtuale classe, String titolo, LocalDate dataAssegnazione, String descrizione, LocalDate dataScadenza) {
		Compito compito = gestoreAggiornamenti.creaCompito(titolo, dataAssegnazione, descrizione, dataScadenza, classe);
		return gestoreRegistroDocente.registraCompito(compito);
	}

	private boolean aggiornaRegistroValutazione(ClasseVirtuale classe, LocalDate data, double voto, String descrizione, Tipologia tipologia, Studente studente) {
		Valutazione valutazione = gestoreAggiornamenti.creaValutazione(data, voto, descrizione, tipologia, classe, studente);
		return gestoreRegistroDocente.registraValutazione(valutazione);
	}

}