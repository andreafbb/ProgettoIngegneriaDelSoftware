package entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.ArrayList;
import java.util.List;


public class Docente extends Utente {

	//Lista vuota perché potrebbe anche avere 0 classi
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