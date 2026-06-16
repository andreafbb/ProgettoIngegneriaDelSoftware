package controller;

import entity.ClasseVirtuale;
import entity.Compito;
import entity.Docente;
import entity.GestoreAggiornamentiRegistro;
import entity.GestoreRegistroDocente;
import entity.Lezione;
import entity.Studente;
import entity.Tipologia;
import entity.Valutazione;

import java.time.LocalDate;
import java.util.List;

/*
 * Controller del flow docente.
 *
 * Pattern BCED del corso (esempio ControllerRimessaggio del professore):
 * classe stateless, metodi static. Il Boundary chiama questi metodi
 * passando dati grezzi / Entity, e riceve Entity (o primitivi) di
 * risposta. Tutti gli accessi al DB sono delegati ai facade in entity/
 * (GestoreRegistroDocente per lettura/persistenza, GestoreAggiornamentiRegistro
 * per la sola creazione delle Entity): il controller non conosce JPQL ne'
 * GestorePersistenza, ne' JFrame/JDialog/listener — quelli vivono nel
 * Boundary.
 *
 * Le guardie sugli input (campi vuoti, intervallo date invertito, studente
 * non selezionato, scadenza prima dell'assegnazione, ecc.) stanno lato
 * Boundary: questo controller assume input gia' validato e si limita alla
 * coordinazione operativa (crea via facade A, registra via facade B,
 * ritorna l'esito boolean).
 */
public class GestoreServiziDocente {

	// ============== Login ==============

	public static Docente cercaDocente(String nome, String cognome) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		return gr.cercaDocente(nome, cognome);
	}

	public static List<ClasseVirtuale> classiDi(Docente docente) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		return gr.classiDi(docente);
	}

	// ============== Registro — letture (Consulta + popolamento combo) ==============

	public static List<Studente> cercaStudenti(ClasseVirtuale classe) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		return gr.cercaStudenti(classe);
	}

	public static List<Lezione> visualizzaLezioni(ClasseVirtuale classe) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		return gr.visualizzaLezioni(classe);
	}

	public static List<Compito> visualizzaCompiti(ClasseVirtuale classe) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		return gr.visualizzaCompiti(classe);
	}

	public static List<Valutazione> visualizzaValutazioniConIntervallo(ClasseVirtuale classe, LocalDate da, LocalDate a) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		return gr.visualizzaValutazioniConIntervallo(classe, da, a);
	}

	public static double calcolaMediaClasse(List<Valutazione> valutazioni) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		return gr.calcolaMediaClasse(valutazioni);
	}

	// ============== Registro — scritture (Aggiorna) ==============

	/*
	 * Catena "crea (facade A) + registra (facade B)" per ogni tipo di
	 * aggiornamento. Il boolean di ritorno e' l'esito della persistenza:
	 * il Boundary lo usa per scegliere feedback verde/rosso.
	 */
	public static boolean registraLezione(ClasseVirtuale classe, LocalDate data, String argomento, String descrizione) {
		GestoreAggiornamentiRegistro ga = new GestoreAggiornamentiRegistro();
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		Lezione lezione = ga.creaLezione(data, argomento, descrizione, classe);
		return gr.registraLezione(lezione);
	}

	public static boolean registraCompito(ClasseVirtuale classe, String titolo, LocalDate dataAssegnazione, String descrizione, LocalDate dataScadenza) {
		GestoreAggiornamentiRegistro ga = new GestoreAggiornamentiRegistro();
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		Compito compito = ga.creaCompito(titolo, dataAssegnazione, descrizione, dataScadenza, classe);
		return gr.registraCompito(compito);
	}

	public static boolean registraValutazione(ClasseVirtuale classe, LocalDate data, double voto, String descrizione, Tipologia tipologia, Studente studente) {
		GestoreAggiornamentiRegistro ga = new GestoreAggiornamentiRegistro();
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		Valutazione valutazione = ga.creaValutazione(data, voto, descrizione, tipologia, classe, studente);
		return gr.registraValutazione(valutazione);
	}
}
