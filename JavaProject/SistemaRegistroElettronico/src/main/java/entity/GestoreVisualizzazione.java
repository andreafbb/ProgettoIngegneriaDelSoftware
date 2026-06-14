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
	 * Ritorna tutte le ClasseVirtuale presenti nel DB.
	 * Map vuota -> cercaPerCampi non genera WHERE -> equivale a SELECT *.
	 * Serve per mostrare la lista delle classi nella comboBox
	 */
	public List<ClasseVirtuale> elencoClassi() {
		return gestorePersistenza.cercaPerCampi(ClasseVirtuale.class, Map.of());
	}

	/*
	 * Cerca lo Studente con nome+cognome iscritto alla classe data.
	 *
	 * cercaPerCampi non sa filtrare su collezioni @ManyToMany, quindi:
	 *  1) filtra al DB per nome+cognome (campi semplici)
	 *  2) filtra in Java per classe sfruttando il fetch EAGER su
	 *     Studente.classi (la collezione e' gia' inizializzata anche con
	 *     l'EntityManager chiuso) e l'equals di ClasseVirtuale (per id)
	 *     che e' stato overridato per permettere questo confronto.
	 */
	public Studente cercaStudenteInClasse(String nome,
										  String cognome,
										  ClasseVirtuale classe) {

		List<Studente> candidati = gestorePersistenza.cercaPerCampi(
				Studente.class,
				Map.of("nome", nome, "cognome", cognome)
		);

		for (Studente s : candidati) {
			if (s.getClassi().contains(classe)) {
				return s;
			}
		}

		return null;
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
