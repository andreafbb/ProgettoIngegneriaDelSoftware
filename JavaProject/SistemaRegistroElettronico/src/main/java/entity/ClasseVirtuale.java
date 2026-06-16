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

	public String getNome() {
		return this.nome;
	}

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

	/*
	Come funzione di Hash utilizzo proprio l'id relativo alla tabella del
	DB, in modo da mantenere una corrispondenza e confrontare oggetti in maniera
	corretta
	 */
	@Override
	public int hashCode() {
		return this.id == null ? 0 : this.id.hashCode();
	}

	/*
	Il toString mi serve principalmente per la corretta visualizzazione delle
	Classi nel boundary (se un giorno volessi cambiare come viene visualizzata una classe
	in qualsiasi punto dell'interfaccia venga chiamata, vado a modificare solo qui)
	 */
	@Override
	public String toString() {
		return this.getNome();
	}
}