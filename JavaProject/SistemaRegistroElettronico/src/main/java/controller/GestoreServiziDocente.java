package controller;

import entity.ClasseVirtuale;
import entity.Compito;
import entity.Docente;
import entity.GestoreAggiornamentiRegistro;
import entity.GestoreRegistroDocente;
import entity.Lezione;
import entity.Studente;
import entity.Tipologia;
import entity.Utente;
import entity.Valutazione;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * Controller del flow docente.
 *
 * Pattern BCED: classe stateless, metodi static. Il Boundary chiama questi metodi
 * passando dati grezzi / Map<String,String>, e riceve String / Map / primitivi
 * di risposta (mai Entity). Tutti gli accessi al DB sono delegati ai facade in
 * entity/ (GestoreRegistroDocente per lettura/persistenza,
 * GestoreAggiornamentiRegistro per la sola creazione delle Entity): il controller
 * risale dalle identita' (nome+cognome, nomeClasse) alle Entity tramite il
 * facade, non conosce JPQL ne' GestorePersistenza, ne' JFrame/JDialog/listener
 * — quelli vivono nel Boundary.
 *
 * Le guardie sugli input (campi vuoti, intervallo date invertito, studente
 * non selezionato, scadenza prima dell'assegnazione, ecc.) stanno lato
 * Boundary: questo controller assume input gia' validato e si limita alla
 * coordinazione operativa (crea via facade A, registra via facade B,
 * ritorna l'esito boolean).
 *
 * Nel controller abbiamo quindi solo metodi di salvataggio di input già validati
 * oppure di ricerca di dati persistenti per fornirli al Boundary, sempre
 * passando dagli adapter cosi' boundary/ non importa mai da entity/.
 */
public class GestoreServiziDocente {

	/*
	 * Formato unico delle date scambiate verso/da il Boundary. Gli input
	 * arrivano come String "dd/MM/yyyy" (formato spinner) e vengono parsati
	 * a LocalDate internamente prima di chiamare i facade.
	 */
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	/*
	Metodi per la gestione dell'accesso del Docente nel sistema
	 */

	public static Map<String, String> cercaDocente(String nome, String cognome) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		Docente d = gr.cercaDocente(nome, cognome);
		if (d == null) return null;
		return adaptUtente(d);
	}

	public static List<String> classiDi(Map<String, String> docente) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		Docente d = gr.cercaDocente(docente.get("nome"), docente.get("cognome"));
		if (d == null) return List.of();
		List<String> nomi = new ArrayList<>();
		for (ClasseVirtuale c : gr.classiDi(d)) {
			nomi.add(adaptClasseVirtuale(c));
		}
		return nomi;
	}



	public static List<Map<String, String>> cercaStudenti(String nomeClasse) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		ClasseVirtuale classe = gr.cercaClassePerNome(nomeClasse);
		if (classe == null) return List.of();
		List<Map<String, String>> out = new ArrayList<>();
		for (Studente s : gr.cercaStudenti(classe)) {
			out.add(adaptUtente(s));
		}
		return out;
	}

	/*
	Espone al Boundary le tipologie disponibili come label leggibili
	("prova scritta", "prova orale"), cosi' la combo Tipologia di
	FormAggiornaRegistro non conosce l'enum. La label e' la stessa che
	adaptValutazioneClasse mette nelle Map: round-trip consistente con
	registraValutazione che la riconverte in enum.
	 */
	public static List<String> tipologieDisponibili() {
		List<String> out = new ArrayList<>();
		for (Tipologia t : Tipologia.values()) {
			out.add(t.name().toLowerCase().replace('_', ' '));
		}
		return out;
	}

	/*
	Metodi per la gestione del Caso D'Uso di Consultazione del Registro
	 */

	public static List<Map<String, String>> visualizzaLezioni(String nomeClasse) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		ClasseVirtuale classe = gr.cercaClassePerNome(nomeClasse);
		if (classe == null) return List.of();
		List<Map<String, String>> out = new ArrayList<>();
		for (Lezione l : gr.visualizzaLezioni(classe)) {
			out.add(adaptLezione(l));
		}
		return out;
	}

	public static List<Map<String, String>> visualizzaCompiti(String nomeClasse) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		ClasseVirtuale classe = gr.cercaClassePerNome(nomeClasse);
		if (classe == null) return List.of();
		List<Map<String, String>> out = new ArrayList<>();
		for (Compito c : gr.visualizzaCompiti(classe)) {
			out.add(adaptCompito(c));
		}
		return out;
	}

	public static List<Map<String, String>> visualizzaValutazioniConIntervallo(String nomeClasse, String daStr, String aStr) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		ClasseVirtuale classe = gr.cercaClassePerNome(nomeClasse);
		if (classe == null) return List.of();
		LocalDate da = LocalDate.parse(daStr, FMT);
		LocalDate a = LocalDate.parse(aStr, FMT);
		List<Map<String, String>> out = new ArrayList<>();
		for (Valutazione v : gr.visualizzaValutazioniConIntervallo(classe, da, a)) {
			out.add(adaptValutazioneClasse(v));
		}
		return out;
	}

	/*
	Riceve la lista di valutazioni gia' adattate (Map) e calcola la media
	dai voti String parsati: stessa lista che il Boundary mostra, niente
	doppio fetch al DB.
	 */
	public static double calcolaMediaClasse(List<Map<String, String>> valutazioni) {
		if (valutazioni.isEmpty()) return 0.0;
		double somma = 0;
		for (Map<String, String> v : valutazioni) {
			somma += Double.parseDouble(v.get("voto"));
		}
		return somma / valutazioni.size();
	}

	/*
	Metodi per la gestione del Caso D'Uso di Aggiornamento del Registro.
	Ricevono solo tipi neutri; date come "dd/MM/yyyy", tipologia come label
	leggibile, studente come Map<nome,cognome,email>.
	 */

	public static boolean registraLezione(String nomeClasse, String dataStr, String argomento, String descrizione) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		ClasseVirtuale classe = gr.cercaClassePerNome(nomeClasse);
		if (classe == null) return false;
		LocalDate data = LocalDate.parse(dataStr, FMT);
		GestoreAggiornamentiRegistro ga = new GestoreAggiornamentiRegistro();
		Lezione lezione = ga.creaLezione(data, argomento, descrizione, classe);
		return gr.registraLezione(lezione);
	}

	public static boolean registraCompito(String nomeClasse, String titolo, String dataAssStr, String descrizione, String dataScadStr) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		ClasseVirtuale classe = gr.cercaClassePerNome(nomeClasse);
		if (classe == null) return false;
		LocalDate dataAss = LocalDate.parse(dataAssStr, FMT);
		LocalDate dataScad = LocalDate.parse(dataScadStr, FMT);
		GestoreAggiornamentiRegistro ga = new GestoreAggiornamentiRegistro();
		Compito compito = ga.creaCompito(titolo, dataAss, descrizione, dataScad, classe);
		return gr.registraCompito(compito);
	}

	public static boolean registraValutazione(String nomeClasse, String dataStr, double voto, String descrizione, String tipologiaLabel, Map<String, String> studente) {
		GestoreRegistroDocente gr = new GestoreRegistroDocente();
		ClasseVirtuale classe = gr.cercaClassePerNome(nomeClasse);
		if (classe == null) return false;
		Studente s = null;
		for (Studente candidato : gr.cercaStudenti(classe)) {
			if (candidato.getNome().equalsIgnoreCase(studente.get("nome"))
					&& candidato.getCognome().equalsIgnoreCase(studente.get("cognome"))) {
				s = candidato;
				break;
			}
		}
		if (s == null) return false;
		LocalDate data = LocalDate.parse(dataStr, FMT);
		Tipologia tipologia = Tipologia.valueOf(tipologiaLabel.toUpperCase().replace(' ', '_'));
		GestoreAggiornamentiRegistro ga = new GestoreAggiornamentiRegistro();
		Valutazione valutazione = ga.creaValutazione(data, voto, descrizione, tipologia, classe, s);
		return gr.registraValutazione(valutazione);
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

	public static Map<String, String> adaptValutazioneClasse(Valutazione v) {
		return Map.of(
				"studenteValutato", v.getStudenteValutato().toString(),
				"voto", String.format(Locale.US, "%.1f", v.getVoto()),
				"tipologia", v.getTipologia().name().toLowerCase().replace('_', ' '),
				"data", v.getData().format(FMT),
				"descrizione", v.getDescrizione()
		);
	}

}
