package entity;

import java.time.LocalDate;

public class GestoreAggiornamentiRegistro {

	/*
	Metodi di creazione degli oggetti relativi all'aggiornamento del registro:

	Caso d'uso orchestrato dal controller GestoreServiziDocente e dal
	facade GestoreRegistroDocente

	 */

	public Lezione creaLezione(LocalDate data, String argomentoTrattato, String descrizione, ClasseVirtuale classeVirtuale) {

		return new Lezione(data, argomentoTrattato, descrizione, classeVirtuale);

	}

	public Compito creaCompito(String titolo, LocalDate dataDiAssegnazione, String descrizione, LocalDate dataDiScadenza, ClasseVirtuale classeVirtuale) {

		return new Compito(titolo, dataDiAssegnazione, descrizione, dataDiScadenza, classeVirtuale);

	}

	public Valutazione creaValutazione(LocalDate data, double voto, String descrizione, Tipologia tipologia, ClasseVirtuale classeVirtuale, Studente studenteValutato) {

		return new Valutazione(data, voto, descrizione, tipologia, classeVirtuale, studenteValutato);

	}

}
