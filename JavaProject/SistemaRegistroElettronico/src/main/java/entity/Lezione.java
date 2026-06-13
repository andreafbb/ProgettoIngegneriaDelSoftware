package entity;

public class Lezione {

	private String data;
	private String argomentoTrattato;
	private String descrizione;
	private ClasseVirtuale classeVirtuale;

	protected Lezione() {}

	public Lezione(String data, String argomentoTrattato, String descrizione, ClasseVirtuale classeVirtuale) {
		this.data = data;
		this.argomentoTrattato = argomentoTrattato;
		this.descrizione = descrizione;
		this.classeVirtuale = classeVirtuale;
	}

	public String getData() {
		return data;
	}

	public String getArgomentoTrattato() {
		return argomentoTrattato;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public ClasseVirtuale getClasseVirtuale() {
		return classeVirtuale;
	}

}