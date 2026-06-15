package entity;

import database.GestorePersistenza;

import java.util.List;
import java.util.Map;

/*
 * Facade del package entity verso la persistenza.
 *
 * Nella variante BCED adottata nel corso, e' qui (e non nei Controller)
 * che si parla con database/. I Controller ricevono Entity gia' pronte e
 * non conoscono nulla di JPQL, EntityManager o fetch type.
 */
public class GestoreVisualizzazione {

	/*
	 * Una sola istanza di GestorePersistenza condivisa da tutti i metodi
	 * del facade. GestorePersistenza apre e chiude l'EntityManager dentro
	 * ogni singola operazione, quindi non c'e' stato condiviso tra chiamate
	 * e l'istanza puo' essere riusata in sicurezza.
	 */
	private final GestorePersistenza gestorePersistenza = new GestorePersistenza();

	/*
	 * Cerca lo Studente per nome+cognome senza vincolo di classe.
	 * Restituisce null se non esiste alcun match. Non trattiamo il caso di
	 * omonimi: prendiamo il primo risultato.
	 */
	public Studente cercaStudente(String nome, String cognome) {
		return gestorePersistenza.cercaPrimoPerCampi(
				Studente.class,
				Map.of("nome", nome, "cognome", cognome)
		);
	}

	/*
	 * Ritorna le ClasseVirtuale a cui lo Studente e' iscritto, recuperate
	 * con JOIN al DB tramite cercaClassiPerUtente (no filtro Java).
	 */
	public List<ClasseVirtuale> classiDi(Studente studente) {
		return gestorePersistenza.cercaClassiPerUtente(studente);
	}

	/*
	Calcola la media delle valutazioni dello studente.
	Su lista vuota ritorna 0.0 per evitare la divisione 0/0 che
	produrrebbe NaN: il Boundary mostrera' "Media: 0.00".
	 */
	public double calcolaMediaStudente(List<Valutazione> valutazioni) {
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
	Qua facciamo una ricerca filtrata sia per Studente che per la Classe
	 */
	public List<Valutazione> visualizzaValutazioni(Studente studente,ClasseVirtuale classe) {
		return gestorePersistenza.cercaPerCampi(
				Valutazione.class,
				Map.of("studenteValutato", studente, "classeVirtuale", classe)
		);
	}

}
