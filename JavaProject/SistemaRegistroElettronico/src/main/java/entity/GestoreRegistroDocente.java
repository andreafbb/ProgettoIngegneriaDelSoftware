package entity;

import database.GestorePersistenza;

import java.util.List;
import java.util.Map;

/*
 * Facade del package entity per il caso d'uso docente.
 *
 * Come GestoreVisualizzazione (lato studente), e' qui che si parla con
 * database/. Il Controller riceve Entity gia' pronte e non conosce nulla
 * di JPQL/EntityManager.
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
	 */

	public List<Studente> cercaStudenti(ClasseVirtuale classe){
			return gestorePersistenza.cercaPerClasse(classe);
	}

	/*
	 * Ritorna le ClasseVirtuale di cui il Docente e' referente, recuperate
	 * con JOIN al DB tramite cercaClassiPerUtente (no filtro Java).
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

	public void mostraRegistro() {
		// TODO - implement entity.GestoreRegistroDocente.mostraRegistro
		throw new UnsupportedOperationException();
	}

	public void monitoraAndamento() {
		// TODO - implement entity.GestoreRegistroDocente.monitoraAndamento
		throw new UnsupportedOperationException();
	}

	public void calcolaMediaClasse() {
		// TODO - implement entity.GestoreRegistroDocente.calcolaMediaClasse
		throw new UnsupportedOperationException();
	}

}
