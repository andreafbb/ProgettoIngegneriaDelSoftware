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
	 * Ritorna tutte le ClasseVirtuale presenti nel DB.
	 * Map vuota -> cercaPerCampi non genera WHERE -> equivale a SELECT *.
	 * Duplicato intenzionale rispetto a GestoreVisualizzazione: ogni facade
	 * copre un caso d'uso ed e' autonomo. In futuro la "lista classi
	 * visibili al docente" potrebbe diventare diversa (es. solo quelle che
	 * lui stesso gestisce) senza impattare il flow studente.
	 */
	public List<ClasseVirtuale> elencoClassi() {
		return gestorePersistenza.cercaPerCampi(ClasseVirtuale.class, Map.of());
	}

	/*
	 * Cerca il Docente con nome+cognome dati che e' referente della classe
	 * indicata.
	 *
	 * Speculare a GestoreVisualizzazione.cercaStudenteInClasse:
	 *  1) filtra al DB per nome+cognome (campi semplici)
	 *  2) filtra in Java verificando che il candidato sia uguale al
	 *     docenteReferente della classe (equals di Docente per
	 *     nome+cognome+email; la @ManyToOne docenteReferente e' EAGER di
	 *     default, quindi accessibile anche con EM chiuso).
	 */
	public Docente cercaDocenteDiClasse(String nome,
										String cognome,
										ClasseVirtuale classe) {

		List<Docente> candidati = gestorePersistenza.cercaPerCampi(
				Docente.class,
				Map.of("nome", nome, "cognome", cognome)
		);

		Docente referente = classe.getDocenteReferente();
		if (referente == null) return null;

		for (Docente d : candidati) {
			if (d.equals(referente)) {
				return d;
			}
		}

		return null;
	}

	public void registraLezione() {
		// TODO - implement entity.GestoreRegistroDocente.registraLezione
		throw new UnsupportedOperationException();
	}

	public void registraCompito() {
		// TODO - implement entity.GestoreRegistroDocente.registraCompito
		throw new UnsupportedOperationException();
	}

	public void registraValutazione() {
		// TODO - implement entity.GestoreRegistroDocente.registraValutazione
		throw new UnsupportedOperationException();
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
