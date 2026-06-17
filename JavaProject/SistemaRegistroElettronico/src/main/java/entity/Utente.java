package entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/*
Per la traduzione della gen-spec utilizziamo la soluzione 2 vista a lezione,
cioè nel database andiamo a creare una tabella per ogni sottoclasse,
quindi Docente e Studente avranno tabelle, mentre Utente no.
L'annotazione @MappedSuperclass serve proprio a definire una superclasse
che non andrà ad essere tradotta in entità nel DB.
 */
@MappedSuperclass
public class Utente {

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

	/*
	Metodi getters che non verranno Overridati, dato che i dati dell'Utente sono
	praticamente gli unici utilizzabili da Docente e Studente
	 */

	public String getNome() {
		return nome;
	}

	public String getEmail() {
		return email;
	}

	public String getCognome() {
		return cognome;
	}

}