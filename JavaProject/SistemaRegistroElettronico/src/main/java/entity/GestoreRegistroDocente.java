package entity;

import database.GestorePersistenza;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Facade del package entity per i casi d'uso di Aggiornamento e Consultazione del Registro.
 *
 * Come GestoreVisualizzazione (lato studente), e' qui che si parla con
 * database/. Il Controller riceve Entity gia' pronte e non conosce nulla
 * di JPQL/EntityManager
 */
public class GestoreRegistroDocente {

	/*
	 * Una sola istanza di GestorePersistenza condivisa da tutti i metodi
	 * del facade. GestorePersistenza apre/chiude l'EntityManager dentro
	 * ogni singola operazione: non c'e' stato condiviso, l'istanza puo'
	 * essere riusata in sicurezza.
	 */

	private final GestorePersistenza gestorePersistenza = new GestorePersistenza();

	/*
	 * Cerca il Docente per nome+cognome senza vincolo di classe.
	 * Restituisce null se non esiste alcun match. Non trattiamo il caso di
	 * omonimi: prendiamo il primo risultato.
	 *
	 * Anche perché in un ipotetico accesso tramite email e password, l'email già
	 * risolve il problema degli omonimi
	 */
	public Docente cercaDocente(String nome, String cognome) {
		return gestorePersistenza.cercaPrimoPerCampi(
				Docente.class,
				Map.of("nome", nome, "cognome", cognome)
		);
	}

	/*
	Metodo utile per ricevere la lista di studenti di una classe, per poter
	selezionare lo studente desiderato al momento dell'inserimento di una Valutazione
	utilizzato per popolare una comboBox poi nel boundary
	 */

	public List<Studente> cercaStudenti(ClasseVirtuale classe){
			return gestorePersistenza.cercaPerClasse(classe);
	}

	/*
	 * Ritorna le ClasseVirtuale di cui il Docente e' referente.
	 */
	public List<ClasseVirtuale> classiDi(Docente docente) {
		return gestorePersistenza.cercaClassiPerUtente(docente);
	}

	/*
	 * Persiste una Lezione gia' costruita da
	 * GestoreAggiornamentiRegistro.creaLezione. Delega a
	 * GestorePersistenza.salva, che ritorna true se il commit JPA
	 * e' andato a buon fine, false altrimenti. Il Controller usa il
	 * boolean per dare feedback all'utente.
	 */
	public boolean registraLezione(Lezione lezione) {
		return gestorePersistenza.salva(lezione);
	}

	public boolean registraCompito(Compito compito) {
		return gestorePersistenza.salva(compito);
	}

	public boolean registraValutazione(Valutazione valutazione) {
		return gestorePersistenza.salva(valutazione);
	}

	/*
	Metodi per l'accesso al database dei dati relativi al Registro di Classe
	tramite Facade fornisco quindi i dati necessari al controller per popolare
	l'interfaccia di Visualizzazione del profilo
	 */

	public List<Lezione> visualizzaLezioni(ClasseVirtuale classe) {
		return gestorePersistenza.cercaPerCampo(Lezione.class, "classeVirtuale", classe);
	}

	public List<Compito> visualizzaCompiti(ClasseVirtuale classe) {
		return gestorePersistenza.cercaPerCampo(Compito.class, "classeVirtuale", classe);
	}

	/*
	Metodo che fornisce la lista delle valutazioni filtrate in un
	intervallo temporale tra le due date richieste, restituisce
	tutte le valutazioni della classe
	 */
	public List<Valutazione> visualizzaValutazioniConIntervallo(
				ClasseVirtuale classe,
				LocalDate da, LocalDate a) {
		List<Valutazione> valutazioniClasse = gestorePersistenza.cercaPerCampo(
				Valutazione.class,
				"classeVirtuale", classe
		);

		List<Valutazione> valutazioniFiltrate = new ArrayList<>();

		/*
		Filtro per il range fornito in input
		 */
		for(Valutazione v : valutazioniClasse){
			if(!v.getData().isBefore(da) && !v.getData().isAfter(a)){
				valutazioniFiltrate.add(v);
			}
		}

		return valutazioniFiltrate;
	}

	/*
	Calcola la media delle valutazioni della classe.
	Su lista vuota ritorna 0.0 per evitare la divisione 0/0 che
	produrrebbe NaN: il Boundary mostrera' "Media: 0.00".
	 */
	public double calcolaMediaClasse(List<Valutazione> valutazioni) {
		if (valutazioni.isEmpty()) {
			return 0.0;
		}

		double somma = 0;
		for (Valutazione v : valutazioni){
			somma += v.getVoto();
		}

		/*
		Verrà arrotondato poi quando verrà effettuato il parsing a String
		nel Form, approssimandolo alla seconda cifra decimale
		 */
		return somma/valutazioni.size();
	}

}
