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
	@OneToMany(mappedBy = "docente")
	private List<ClasseVirtuale> classiGestite = new ArrayList<>();

	//Utile per JPA
	protected Docente() { super(); }

	public Docente(String nome, String email, String cognome, String password){
		super(nome, email, cognome, password);
	}

	/*
	Aggiunge una classe all'istanza corrente del Docente
	 */
	public void aggiungiClasse(ClasseVirtuale c){
		this.classiGestite.add(c);
	}

	public List<ClasseVirtuale> getClassiGestite() {
		return new ArrayList<>(classiGestite);
	}

	public void cercaClasse() {
		// TODO - implement entity.Docente.cercaClasse
		throw new UnsupportedOperationException();
	}

}