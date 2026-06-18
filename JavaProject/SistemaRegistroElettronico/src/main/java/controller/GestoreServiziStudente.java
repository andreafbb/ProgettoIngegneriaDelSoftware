package controller;

import entity.ClasseVirtuale;
import entity.Compito;
import entity.GestoreVisualizzazione;
import entity.Lezione;
import entity.Studente;
import entity.Utente;
import entity.Valutazione;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * Controller del flow studente: classe stateless, metodi static.
 * Il Boundary chiama questi metodi passando dati grezzi / Map<String,String>,
 * e riceve String / Map / primitivi di risposta (mai Entity). Tutti gli
 * accessi al DB sono delegati al facade in entity/ (GestoreVisualizzazione):
 * il controller risale dalle identita' (nome+cognome, nomeClasse) alle Entity
 * tramite il facade, non conosce JPQL ne' GestorePersistenza, ne'
 * JFrame/JDialog/listener — quelli vivono nel Boundary.
 *
 * Le guardie sugli input (campo vuoto, studente non trovato, lista
 * vuota) stanno lato Boundary, dentro i listener: questo controller
 * assume input gia' validato e si limita alla coordinazione operativa.
 *
 * Nel controller abbiamo quindi solo metodi di salvataggio di input già validati
 * oppure di ricerca di dati persistenti per fornirli al Boundary, sempre
 * passando dagli adapter cosi' boundary/ non importa mai da entity/.
 */
public class GestoreServiziStudente {

	/*
	 * Formato unico delle date scambiate verso il Boundary, coerente con
	 * lo "dd/MM/yyyy" degli spinner lato docente.
	 */
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public static Map<String, String> cercaStudente(String nome, String cognome) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		Studente s = gv.cercaStudente(nome, cognome);
		if (s == null) return null;
		return adaptUtente(s);
	}

	public static List<String> classiDi(Map<String, String> studente) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		Studente s = gv.cercaStudente(studente.get("nome"), studente.get("cognome"));
		if (s == null) return List.of();
		List<String> nomi = new ArrayList<>();
		for (ClasseVirtuale c : gv.classiDi(s)) {
			nomi.add(adaptClasseVirtuale(c));
		}
		return nomi;
	}

	public static List<Map<String, String>> visualizzaLezioni(String nomeClasse) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		ClasseVirtuale classe = gv.cercaClassePerNome(nomeClasse);
		if (classe == null) return List.of();
		List<Map<String, String>> out = new ArrayList<>();
		for (Lezione l : gv.visualizzaLezioni(classe)) {
			out.add(adaptLezione(l));
		}
		return out;
	}

	public static List<Map<String, String>> visualizzaCompiti(String nomeClasse) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		ClasseVirtuale classe = gv.cercaClassePerNome(nomeClasse);
		if (classe == null) return List.of();
		List<Map<String, String>> out = new ArrayList<>();
		for (Compito c : gv.visualizzaCompiti(classe)) {
			out.add(adaptCompito(c));
		}
		return out;
	}

	public static List<Map<String, String>> visualizzaValutazioni(Map<String, String> studente, String nomeClasse) {
		GestoreVisualizzazione gv = new GestoreVisualizzazione();
		Studente s = gv.cercaStudente(studente.get("nome"), studente.get("cognome"));
		ClasseVirtuale classe = gv.cercaClassePerNome(nomeClasse);
		if (s == null || classe == null) return List.of();
		List<Map<String, String>> out = new ArrayList<>();
		for (Valutazione v : gv.visualizzaValutazioni(s, classe)) {
			out.add(adaptValutazioneStudente(v));
		}
		return out;
	}

	/*
	 * Riceve la lista di valutazioni gia' adattate (Map) e calcola la media
	 * dai voti String parsati: e' la stessa lista che il Boundary mostra,
	 * quindi nessun doppio fetch al DB. Locale.US in adaptValutazioneStudente
	 * garantisce il punto decimale, parseDouble e' deterministico.
	 */
	public static double calcolaMediaStudente(List<Map<String, String>> valutazioni) {
		if (valutazioni.isEmpty()) return 0.0;
		double somma = 0;
		for (Map<String, String> v : valutazioni) {
			somma += Double.parseDouble(v.get("voto"));
		}
		return somma / valutazioni.size();
	}

	/*
	Classi Adapter per separare l'Entity dal Boundary, passando solo String e Mappe di String.
	adaptUtente vale per Studente e Docente (entrambe estendono Utente).
	 */

	public static Map<String, String> adaptUtente(Utente u) {
		return Map.of(
				"nome", u.getNome(),
				"cognome", u.getCognome(),
				"email", u.getEmail()
		);
	}

	public static String adaptClasseVirtuale(ClasseVirtuale c) {
		return c.getNome();
	}

	public static Map<String, String> adaptLezione(Lezione l) {
		return Map.of(
				"argomentoTrattato", l.getArgomentoTrattato(),
				"data", l.getData().format(FMT),
				"descrizione", l.getDescrizione()
		);
	}

	public static Map<String, String> adaptCompito(Compito c) {
		return Map.of(
				"titolo", c.getTitolo(),
				"dataDiAssegnazione", c.getDataDiAssegnazione().format(FMT),
				"descrizione", c.getDescrizione(),
				"dataDiScadenza", c.getDataDiScadenza().format(FMT)
		);
	}

	public static Map<String, String> adaptValutazioneStudente(Valutazione v) {
		return Map.of(
				"voto", String.format(Locale.US, "%.1f", v.getVoto()),
				"tipologia", v.getTipologia().name().toLowerCase().replace('_', ' '),
				"data", v.getData().format(FMT),
				"descrizione", v.getDescrizione()
		);
	}

}
