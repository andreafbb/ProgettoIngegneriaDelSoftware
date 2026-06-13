package entity;

public class Valutazione {

	private String data;
	private double voto;
	private String descrizione;
	private Tipologia tipologia;
	private ClasseVirtuale classeVirtuale;
	private Studente studenteValutato;

	protected Valutazione() {}

	public Valutazione(String data, double voto, String descrizione, Tipologia tipologia, ClasseVirtuale classeVirtuale, Studente studenteValutato) {
		this.data = data;
		this.voto = voto;
		this.descrizione = descrizione;
		this.tipologia = tipologia;
		this.classeVirtuale = classeVirtuale;
		this.studenteValutato = studenteValutato;
	}

	public String getData() {
		return data;
	}

	public double getVoto() {
		return voto;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public Tipologia getTipologia() {
		return tipologia;
	}

	public ClasseVirtuale getClasseVirtuale() {
		return classeVirtuale;
	}

	public Studente getStudenteValutato() {
		return studenteValutato;
	}

}