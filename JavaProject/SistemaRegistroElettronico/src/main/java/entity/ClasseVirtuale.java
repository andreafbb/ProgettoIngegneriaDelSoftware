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

	@ManyToMany(mappedBy = "classi")
	private List<Studente> studentiIscritti = new ArrayList<>();

	/*
	Liste degli elementi da aggiornare nel registro
	 */

	@OneToMany(mappedBy = "classeVirtuale")
	private List<Lezione> lezioni = new ArrayList<>();

	@OneToMany(mappedBy = "classeVirtuale")
	private List<Compito> compiti = new ArrayList<>();

	@OneToMany(mappedBy = "classeVirtuale")
	private List<Valutazione> valutazioni = new ArrayList<>();

	public ClasseVirtuale () {}

	public ClasseVirtuale( Docente d, String nome){

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

	public String getNome() { return this.nome; }

	public Docente getDocenteReferente() { return this.docenteReferente; }

	/*
	Due ClasseVirtuale caricate da EntityManager diversi sono oggetti Java
	distinti: per confrontarle uso l'id (chiave primaria del DB)
	La guardia su id != null evita falsi positivi tra entity non ancora
	persistite.
	 */
	@Override
	public boolean equals(Object altraClasse) {

		if (this == altraClasse) return true;

		return (altraClasse instanceof ClasseVirtuale c) &&
				this.id != null &&
				this.id.equals(c.id);
	}

	@Override
	public int hashCode() {
		return this.id == null ? 0 : this.id.hashCode();
	}

	@Override
	public String toString() {
		return this.getNome();
	}
}