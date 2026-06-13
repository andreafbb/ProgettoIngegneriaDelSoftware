package entity;

public class Compito {

	private String titolo;
	private String dataDiAssegnazione;
	private String descrizione;
	private String dataDiScadenza;
	private ClasseVirtuale classeVirtuale;

	protected Compito() {}

	public Compito(String titolo, String dataDiAssegnazione, String descrizione, String dataDiScadenza, ClasseVirtuale classeVirtuale) {
		this.titolo = titolo;
		this.dataDiAssegnazione = dataDiAssegnazione;
		this.descrizione = descrizione;
		this.dataDiScadenza = dataDiScadenza;
		this.classeVirtuale = classeVirtuale;
	}

	public String getTitolo() {
		return titolo;
	}

	public String getDataDiAssegnazione() {
		return dataDiAssegnazione;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public String getDataDiScadenza() {
		return dataDiScadenza;
	}

	public ClasseVirtuale getClasseVirtuale() {
		return classeVirtuale;
	}

}