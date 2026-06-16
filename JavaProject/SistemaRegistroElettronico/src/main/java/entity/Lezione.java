package entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Lezione {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate data;
	private String argomentoTrattato;
	private String descrizione;

	@ManyToOne
	@JoinColumn(name = "classe_id")
	private ClasseVirtuale classeVirtuale;

	protected Lezione() {}

	public Lezione(LocalDate data, String argomentoTrattato, String descrizione, ClasseVirtuale classeVirtuale) {
		this.data = data;
		this.argomentoTrattato = argomentoTrattato;
		this.descrizione = descrizione;
		this.classeVirtuale = classeVirtuale;
	}

	/*
	Metodi utilizzati principalmente lato Boundary, per permettere una corretta visualizzazione
	degli oggetti con i relativi dati
	 */

	public LocalDate getData() {
		return data;
	}

	public String getArgomentoTrattato() {
		return argomentoTrattato;
	}

	public String getDescrizione() {
		return descrizione;
	}

}