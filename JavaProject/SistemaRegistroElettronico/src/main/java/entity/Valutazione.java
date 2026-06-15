package entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Valutazione {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate data;
	private double voto;
	private String descrizione;

	@Enumerated(EnumType.STRING)
	private Tipologia tipologia;

	@ManyToOne
	@JoinColumn(name = "classe_id")
	private ClasseVirtuale classeVirtuale;

	@ManyToOne
	@JoinColumn(name = "studente_id")
	private Studente studenteValutato;

	protected Valutazione() {}

	public Valutazione(LocalDate data, double voto, String descrizione, Tipologia tipologia, ClasseVirtuale classeVirtuale, Studente studenteValutato) {
		this.data = data;
		this.voto = voto;
		this.descrizione = descrizione;
		this.tipologia = tipologia;
		this.classeVirtuale = classeVirtuale;
		this.studenteValutato = studenteValutato;
	}

	public LocalDate getData() {
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