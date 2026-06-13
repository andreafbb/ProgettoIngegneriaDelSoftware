package entity;

public abstract class Utente {

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