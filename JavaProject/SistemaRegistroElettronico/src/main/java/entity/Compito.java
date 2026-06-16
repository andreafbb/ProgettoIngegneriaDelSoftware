package entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Compito {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String titolo;
	private LocalDate dataDiAssegnazione;
	private String descrizione;
	private LocalDate dataDiScadenza;

	@ManyToOne
	@JoinColumn(name = "classe_id")
	private ClasseVirtuale classeVirtuale;

	protected Compito() {}

	public Compito(String titolo, LocalDate dataDiAssegnazione, String descrizione, LocalDate dataDiScadenza, ClasseVirtuale classeVirtuale) {
		this.titolo = titolo;
		this.dataDiAssegnazione = dataDiAssegnazione;
		this.descrizione = descrizione;
		this.dataDiScadenza = dataDiScadenza;
		this.classeVirtuale = classeVirtuale;
	}

	/*
	Metodi utilizzati principalmente lato Boundary, per permettere una corretta visualizzazione
	degli oggetti con i relativi dati
	 */

	public String getTitolo() {
		return titolo;
	}

	public LocalDate getDataDiAssegnazione() {
		return dataDiAssegnazione;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public LocalDate getDataDiScadenza() {
		return dataDiScadenza;
	}

}