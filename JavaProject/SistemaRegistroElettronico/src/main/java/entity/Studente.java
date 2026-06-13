package entity;

import java.util.ArrayList;
import java.util.List;

public class Studente extends Utente {

	private List<ClasseVirtuale> classi = new ArrayList<>();

	protected Studente() {super();}

	public Studente(String nome, String email, String cognome, String password){
		super(nome, email, cognome, password);
	}

	/*
	Uso una guardia per mantenere la coerenza tra le due liste
	 */
	public void aggiungiClasse(ClasseVirtuale c){
		if (this.classi.contains(c)) return;
		this.classi.add(c);
		c.aggiungiStudente(this);
	}

	public List<ClasseVirtuale> getClassi() {
		return new ArrayList<>(classi);
	}

	public void cercaClasse() {
		// TODO - implement entity.Studente.cercaClasse
		throw new UnsupportedOperationException();
	}

}