package entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/*
Utilizziamo l'approccio Table_per_class, cioè nel database andiamo
a creare una tabella per ogni sottoclasse, quindi Docente e Studente
avranno tabelle, mentre Utente no
 */
@MappedSuperclass
public abstract class Utente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nome;
	private String email;
	private String cognome;
	private String password;

	//Utile eventualmente per JPA
	protected Utente () {}

	public Utente(String nome, String email, String cognome, String password){
		this.nome = nome;
		this.email = email;
		this.cognome = cognome;
		this.password = password;
	}

	public abstract void cercaClasse();

	public String getNome() {
		return nome;
	}

	public String getEmail() {
		return email;
	}

	public String getCognome() {
		return cognome;
	}

	public String getPassword() {
		return password;
	}

}