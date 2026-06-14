package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ClasseVirtuale {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nome;

	@ManyToOne
	@JoinColumn(name = "docente_id")
	private Docente docenteReferente;

	@ManyToMany(mappedBy = "studenti")
	private List<Studente> studentiIscritti = new ArrayList<>();

	/*
	Liste degli elementi da aggiornare nel registro
	 */
	private List<Lezione> lezioni = new ArrayList<>();
	private List<Compito> compiti = new ArrayList<>();
	private List<Valutazione> valutazioni = new ArrayList<>();

	public ClasseVirtuale () {}

	public ClasseVirtuale(Long id, Docente d, String nome){
		this.id = id;
		this.docenteReferente = d;
		this.nome = nome;
	}

	/*
	Uso una guardia per mantenere la coerenza tra le due liste
	 */
	public void aggiungiStudente(Studente s) {
		if (this.studentiIscritti.contains(s)) return;
		this.studentiIscritti.add(s);
		s.aggiungiClasse(this);
	}

	public void aggiungiLezione(Lezione l) {
		this.lezioni.add(l);
	}

	public void aggiungiCompito(Compito c) {
		this.compiti.add(c);
	}

	public void aggiungiValutazione(Valutazione v) {
		this.valutazioni.add(v);
	}

	public void cercaStudente() {
		// TODO - implement entity.ClasseVirtuale.cercaStudente
		throw new UnsupportedOperationException();
	}

	public List<Compito> getElencoCompiti() {
		return new ArrayList<>(this.compiti);
	}

	public List<Lezione> getElencoLezioni() {
		return new ArrayList<>(this.lezioni);
	}

	/*
	Fa una ricerca filtrata solo sullo Studente passato in input
	 */
	public List<Valutazione> getValutazioniStudente(Studente s) {

		List<Valutazione> valutazioniStudente = new ArrayList<>();

		for (Valutazione valutazione : valutazioni){
			if(valutazione.getStudenteValutato().equals(s)){
				valutazioniStudente.add(valutazione);
			}
		}

		return  valutazioniStudente;
	}

	public List<Valutazione> getValutazioniClasse() {
		return new ArrayList<>(this.valutazioni);
	}

}