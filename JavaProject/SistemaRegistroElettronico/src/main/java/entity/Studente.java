package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Studente extends Utente {
	/*
	Niente Id perché e già presente in Utente
	 */

	/*
	Creo una tabella associativa "iscrizione" che andrebbe creata anche nel modello di dominio
	e nelle entità, ma dato che in implementazione non verranno trattati i
	casi d'uso relativi all'iscrizione di uno studente è irrilevante a livello
	di Entity, ma nelle tabelle bisogna inserirla
	 */

	/*
	Questo per permettere la ricerca filtrata per classe e studente
	 */
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "iscrizione",
			joinColumns = @JoinColumn(name = "studente_id"),
			inverseJoinColumns = @JoinColumn(name = "classevirtuale_id")
	)
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

	/*
	Metodo utilizzato da ClasseVirtuale (contains) per ottenere la lista di valutazioni
	per quel preciso studente, uso come discriminanti solo
	nome, cognome ed email (in caso di omonimia l'email discrimina)
	 */
	@Override
	public boolean equals(Object altroStudente) {

		if (this == altroStudente) return true;

		return (altroStudente instanceof Studente s) &&
				this.getNome().equalsIgnoreCase(s.getNome()) &&
				this.getCognome().equalsIgnoreCase(s.getCognome()) &&
				this.getEmail().equalsIgnoreCase(s.getEmail());
	}

	/*
	Metodo utile ad impostare la corretta visualizzazione dell'oggetto Studente
	mediante i suoi dati
	 */
	@Override
	public String toString() {
		return this.getNome()+" "+this.getCognome();
	}
}