package controller;

import entity.ClasseVirtuale;
import entity.Compito;
import entity.GestoreVisualizzazione;
import entity.Lezione;
import entity.Studente;
import entity.Valutazione;

import java.util.List;

/*
 * Controller del flow studente.
 *
 * Pattern BCED del corso (esempio ControllerRimessaggio del professore):
 * classe stateless, metodi static. Il Boundary chiama questi metodi
 * passando dati grezzi / Entity, e riceve Entity (o primitivi) di
 * risposta. Tutti gli accessi al DB sono delegati al facade in entity/
 * (GestoreVisualizzazione): il controller non conosce JPQL ne'
 * GestorePersistenza, ne' JFrame/JDialog/listener — quelli vivono nel
 * Boundary.
 *
 * Le guardie sugli input (campo vuoto, studente non trovato, lista
 * vuota) stanno lato Boundary, dentro i listener: questo controller
 * assume input gia' validato e si limita alla coordinazione operativa.
 */
public class GestoreServiziStudente {

	public static Studente cercaStudente(String nome, String cognome) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		return gv.cercaStudente(nome, cognome);
	}

	public static List<ClasseVirtuale> classiDi(Studente studente) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		return gv.classiDi(studente);
	}

	public static List<Lezione> visualizzaLezioni(ClasseVirtuale classe) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		return gv.visualizzaLezioni(classe);
	}

	public static List<Compito> visualizzaCompiti(ClasseVirtuale classe) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		return gv.visualizzaCompiti(classe);
	}

	public static List<Valutazione> visualizzaValutazioni(Studente studente, ClasseVirtuale classe) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		return gv.visualizzaValutazioni(studente, classe);
	}

	public static double calcolaMediaStudente(List<Valutazione> valutazioni) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		return gv.calcolaMediaStudente(valutazioni);
	}
}
