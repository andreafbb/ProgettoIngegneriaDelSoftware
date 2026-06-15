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

	public void calcolaMediaStudente() {
		// TODO - implement entity.GestoreVisualizzazione.calcolaMediaStudente
		throw new UnsupportedOperationException();
	}

	public void visualizzaLezioni() {
		// TODO - implement entity.GestoreVisualizzazione.visualizzaLezioni
		throw new UnsupportedOperationException();
	}

	public void visualizzaCompiti() {
		// TODO - implement entity.GestoreVisualizzazione.visualizzaCompiti
		throw new UnsupportedOperationException();
	}

	public void visualizzaValutazioni() {
		// TODO - implement entity.GestoreVisualizzazione.visualizzaValutazioni
		throw new UnsupportedOperationException();
	}

}
