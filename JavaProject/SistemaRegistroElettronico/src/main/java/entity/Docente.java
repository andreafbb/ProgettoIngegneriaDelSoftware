package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Docente extends Utente {
	/*
	Niente Id perché e già presente in Utente
	 */

	//Lista vuota perché potrebbe anche avere 0 classi
	@OneToMany(mappedBy = "docenteReferente")
	private List<ClasseVirtuale> classiGestite = new ArrayList<>();

	//Utile per JPA
	protected Docente() { super(); }

	public Docente(String nome, String email, String cognome, String password){
		super(nome, email, cognome, password);
	}

	/*
	Costruttore di copia per i getters esterni, per evitare modifiche
	 */
	public Docente(Docente altroDocente) {
		super(altroDocente.getNome(),
				altroDocente.getEmail(),
				altroDocente.getCognome(),
				altroDocente.getPassword());
		this.classiGestite = new
				ArrayList<>(altroDocente.classiGestite);
	}

	/*
	Aggiunge una classe all'istanza corrente del Docente
	 */
	public void aggiungiClasse(ClasseVirtuale c){
		this.classiGestite.add(c);
	}

	/*
	Stesso criterio di Studente.equals (nome + cognome + email,
	case-insensitive): serve per confrontare un Docente caricato dalla
	query con il docenteReferente di una ClasseVirtuale caricata da
	un EntityManager diverso.
	 */
	@Override
	public boolean equals(Object altroDocente) {

		if (this == altroDocente) return true;

		return (altroDocente instanceof Docente d) &&
				this.getNome().equalsIgnoreCase(d.getNome()) &&
				this.getCognome().equalsIgnoreCase(d.getCognome()) &&
				this.getEmail().equalsIgnoreCase(d.getEmail());
	}


}