package setup;

import database.GestorePersistenza;
import database.JpaUtil;
import entity.ClasseVirtuale;
import entity.Compito;
import entity.Docente;
import entity.Lezione;
import entity.Studente;
import entity.Tipologia;
import entity.Valutazione;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MainSetupInsert {

    public static void main(String[] args) {

        /*
        Apriamo una EntityManagerFactory "una tantum" SOLO per questo main,
        forzando hibernate.hbm2ddl.auto = create.
        Questo droppa e ricrea tutte le tabelle da zero, in modo che il
        successivo salvaTutti parta da un database pulito.
        Il persistence.xml resta su "update", quindi gli altri main NON
        toccano lo schema.
         */
        EntityManagerFactory bootstrap = Persistence.createEntityManagerFactory(
                "registroPU",
                Map.of("hibernate.hbm2ddl.auto", "create")
        );
        bootstrap.close();

        GestorePersistenza gestore = new GestorePersistenza();

        /*
        Andiamo a popolare il database con dati di esempio, principalmente 1 docente
        e vari studenti tutti iscritti ad 1 classe di prova associata al docente

        Proveremo poi a creare altre classi con altri studenti per un solo Docente

        Esempio

        5A Informatica, 4A Informatica, 3A Informatica

         */

        Docente docente = new Docente("Alessandro", "alessandro.senatore@liceolamura.edu.it", "Senatore", "Sandriello05");

        /*
        L'unico Docente nel Database avrà 3 classi, con 6 studenti ognuna
         */
        ClasseVirtuale classe1 = new ClasseVirtuale(docente, "5A Informatica");
        ClasseVirtuale classe2 = new ClasseVirtuale(docente, "4A Informatica");
        ClasseVirtuale classe3 = new ClasseVirtuale(docente, "3A Informatica");

        docente.aggiungiClasse(classe1);
        docente.aggiungiClasse(classe2);
        docente.aggiungiClasse(classe3);

        /*
        ========== STUDENTI 5A INFORMATICA ==========
         */
        Studente s1  = new Studente("Andrea Francesco", "andreaf.bruno@liceolamura.edu.it", "Bruno", "Andreone05!");
        Studente s2  = new Studente("Mattia", "mattia.pactiello@liceolamura.edu.it", "Pactiello", "SanGiovanni24");
        Studente s3  = new Studente("Umberto", "umberto.scarpato@liceolamura.edu.it", "Scarpato", "Scarpetta07!");
        Studente s4  = new Studente("Emilio", "emilio.manzo@liceolamura.edu.it", "Manzo", "Manzetto99");
        Studente s5  = new Studente("Luciano", "luciano.meccariello@liceolamura.edu.it", "Meccariello", "LucaMecca01");
        Studente s6  = new Studente("Gaspare", "gaspare.tortora@liceolamura.edu.it", "Tortora", "Gasparino25!");

        classe1.aggiungiStudente(s1);
        classe1.aggiungiStudente(s2);
        classe1.aggiungiStudente(s3);
        classe1.aggiungiStudente(s4);
        classe1.aggiungiStudente(s5);
        classe1.aggiungiStudente(s6);

        /*
        ========== LEZIONI 5A INFORMATICA ==========
        Quindici lezioni distribuite sull'anno scolastico 2025/2026, sul
        programma standard di Informatica della quinta superiore.
         */
        Lezione[] lezioni5A = {
                new Lezione(LocalDate.of(2025, 10,  6), "Introduzione a Java",          "Storia del linguaggio, JVM, primo programma Hello World",        classe1),
                new Lezione(LocalDate.of(2025, 10, 13), "Variabili e tipi primitivi",   "int, double, char, boolean: dichiarazione, casting, overflow",  classe1),
                new Lezione(LocalDate.of(2025, 10, 20), "Operatori e espressioni",      "Aritmetici, relazionali, logici, bit a bit; precedenza",         classe1),
                new Lezione(LocalDate.of(2025, 10, 27), "Strutture di controllo",       "if/else, switch e operatore ternario con esempi guidati",        classe1),
                new Lezione(LocalDate.of(2025, 11,  3), "Cicli iterativi",              "for, while e do-while; uso di break e continue",                 classe1),
                new Lezione(LocalDate.of(2025, 11, 10), "Array monodimensionali",       "Dichiarazione, accesso, iterazione, lunghezza, copia",           classe1),
                new Lezione(LocalDate.of(2025, 11, 24), "Stringhe in Java",             "Classe String, metodi principali, immutabilita'",                classe1),
                new Lezione(LocalDate.of(2025, 12,  1), "Classi e oggetti",             "Definizione di classe, costruttori, istanziazione, riferimenti", classe1),
                new Lezione(LocalDate.of(2025, 12, 15), "Incapsulamento",               "Modificatori di accesso, getter e setter, motivazioni",          classe1),
                new Lezione(LocalDate.of(2026,  1, 12), "Ereditarieta'",                "extends, super, override di metodi",                              classe1),
                new Lezione(LocalDate.of(2026,  1, 26), "Polimorfismo",                 "Binding dinamico, upcasting e downcasting",                       classe1),
                new Lezione(LocalDate.of(2026,  2,  9), "Interfacce e classi astratte", "Differenze, contratti e use case tipici",                         classe1),
                new Lezione(LocalDate.of(2026,  2, 23), "Gestione delle eccezioni",     "try/catch, throw, throws ed eccezioni custom",                    classe1),
                new Lezione(LocalDate.of(2026,  3,  9), "Collezioni Java",              "List, Set, Map: gerarchia e implementazioni principali",         classe1),
                new Lezione(LocalDate.of(2026,  4, 20), "Introduzione a SQL",           "DDL e DML, query SELECT, JOIN e operatori di confronto",         classe1)
        };
        for (Lezione l : lezioni5A) classe1.aggiungiLezione(l);

        /*
        ========== COMPITI 5A INFORMATICA ==========
        Quindici compiti agganciati alle lezioni: data di assegnazione il
        giorno della lezione, scadenza tipicamente una settimana dopo.
         */
        Compito[] compiti5A = {
                new Compito("Esercizi su variabili e operatori",  LocalDate.of(2025, 10, 13), "Quattro esercizi sui tipi primitivi e sugli operatori",                LocalDate.of(2025, 10, 20), classe1),
                new Compito("Esercizi su strutture di controllo", LocalDate.of(2025, 10, 27), "Risolvere tre problemi usando if/else e switch",                       LocalDate.of(2025, 11,  3), classe1),
                new Compito("Esercizi sui cicli",                 LocalDate.of(2025, 11,  3), "Stampare pattern numerici e calcolare somme con for/while",            LocalDate.of(2025, 11, 10), classe1),
                new Compito("Manipolazione di array",             LocalDate.of(2025, 11, 10), "Ricerca min/max, inversione e conteggi su array di interi",            LocalDate.of(2025, 11, 17), classe1),
                new Compito("Manipolazione di stringhe",          LocalDate.of(2025, 11, 24), "Inversione, palindromi, conteggio occorrenze",                         LocalDate.of(2025, 12,  1), classe1),
                new Compito("Prima classe OOP",                   LocalDate.of(2025, 12,  1), "Implementare la classe ContoCorrente con saldo e operazioni",          LocalDate.of(2025, 12,  8), classe1),
                new Compito("Incapsulamento e getter/setter",     LocalDate.of(2025, 12, 15), "Refactor di ContoCorrente: campi privati e accessor pubblici",         LocalDate.of(2025, 12, 22), classe1),
                new Compito("Gerarchia di classi",                LocalDate.of(2026,  1, 12), "Veicolo, Auto e AutoElettrica: ereditarieta' e costruttori",            LocalDate.of(2026,  1, 19), classe1),
                new Compito("Polimorfismo: figure geometriche",   LocalDate.of(2026,  1, 26), "Forma astratta + Cerchio/Quadrato/Triangolo, area() polimorfica",      LocalDate.of(2026,  2,  2), classe1),
                new Compito("Interfaccia Confrontabile",          LocalDate.of(2026,  2,  9), "Definire Confrontabile<T> e applicarla a Punto e Studente",            LocalDate.of(2026,  2, 16), classe1),
                new Compito("Eccezioni personalizzate",           LocalDate.of(2026,  2, 23), "Implementare SaldoInsufficienteException per ContoCorrente",            LocalDate.of(2026,  3,  2), classe1),
                new Compito("ArrayList e HashMap",                LocalDate.of(2026,  3,  9), "Catalogo libri: aggiunta, ricerca per autore, conteggio",               LocalDate.of(2026,  3, 16), classe1),
                new Compito("Algoritmi di ordinamento",           LocalDate.of(2026,  3, 16), "Implementare BubbleSort e SelectionSort su array di interi",            LocalDate.of(2026,  3, 23), classe1),
                new Compito("Query SQL di base",                  LocalDate.of(2026,  4, 20), "Cinque query SELECT con WHERE e ORDER BY su schema fornito",            LocalDate.of(2026,  4, 27), classe1),
                new Compito("Ripasso finale",                     LocalDate.of(2026,  5,  4), "Esercizio integrato: gestione biblioteca con persistenza su file",      LocalDate.of(2026,  5, 15), classe1)
        };
        for (Compito c : compiti5A) classe1.aggiungiCompito(c);

        /*
        ========== VALUTAZIONI 5A INFORMATICA ==========
        Quattro valutazioni per studente:
          - 2 PROVA_SCRITTA con stessa data per tutti (compito in classe);
          - 2 PROVA_ORALE con date scaglionate (uno studente al giorno).
        Voti variati cosi' le medie non vengono tutte identiche.
         */
        LocalDate dataScritta1 = LocalDate.of(2025, 12,  9);
        LocalDate dataScritta2 = LocalDate.of(2026,  4, 13);

        Valutazione[] valutazioni5A = {
                // s1 — Andrea Francesco Bruno
                new Valutazione(dataScritta1,              7.5, "Verifica scritta del primo quadrimestre",      Tipologia.PROVA_SCRITTA, classe1, s1),
                new Valutazione(LocalDate.of(2026, 1, 19), 8.0, "Interrogazione: ereditarieta' e polimorfismo", Tipologia.PROVA_ORALE,   classe1, s1),
                new Valutazione(dataScritta2,              7.0, "Verifica scritta del secondo quadrimestre",    Tipologia.PROVA_SCRITTA, classe1, s1),
                new Valutazione(LocalDate.of(2026, 5,  4), 8.5, "Interrogazione: collezioni ed eccezioni",      Tipologia.PROVA_ORALE,   classe1, s1),

                // s2 — Mattia Pactiello
                new Valutazione(dataScritta1,              6.0, "Verifica scritta del primo quadrimestre",      Tipologia.PROVA_SCRITTA, classe1, s2),
                new Valutazione(LocalDate.of(2026, 1, 20), 6.5, "Interrogazione: ereditarieta' e polimorfismo", Tipologia.PROVA_ORALE,   classe1, s2),
                new Valutazione(dataScritta2,              7.0, "Verifica scritta del secondo quadrimestre",    Tipologia.PROVA_SCRITTA, classe1, s2),
                new Valutazione(LocalDate.of(2026, 5,  5), 7.0, "Interrogazione: collezioni ed eccezioni",      Tipologia.PROVA_ORALE,   classe1, s2),

                // s3 — Umberto Scarpato
                new Valutazione(dataScritta1,              8.0, "Verifica scritta del primo quadrimestre",      Tipologia.PROVA_SCRITTA, classe1, s3),
                new Valutazione(LocalDate.of(2026, 1, 21), 8.5, "Interrogazione: ereditarieta' e polimorfismo", Tipologia.PROVA_ORALE,   classe1, s3),
                new Valutazione(dataScritta2,              9.0, "Verifica scritta del secondo quadrimestre",    Tipologia.PROVA_SCRITTA, classe1, s3),
                new Valutazione(LocalDate.of(2026, 5,  6), 8.5, "Interrogazione: collezioni ed eccezioni",      Tipologia.PROVA_ORALE,   classe1, s3),

                // s4 — Emilio Manzo
                new Valutazione(dataScritta1,              5.5, "Verifica scritta del primo quadrimestre",      Tipologia.PROVA_SCRITTA, classe1, s4),
                new Valutazione(LocalDate.of(2026, 1, 22), 6.0, "Interrogazione: ereditarieta' e polimorfismo", Tipologia.PROVA_ORALE,   classe1, s4),
                new Valutazione(dataScritta2,              5.0, "Verifica scritta del secondo quadrimestre",    Tipologia.PROVA_SCRITTA, classe1, s4),
                new Valutazione(LocalDate.of(2026, 5,  7), 6.5, "Interrogazione: collezioni ed eccezioni",      Tipologia.PROVA_ORALE,   classe1, s4),

                // s5 — Luciano Meccariello
                new Valutazione(dataScritta1,              6.5, "Verifica scritta del primo quadrimestre",      Tipologia.PROVA_SCRITTA, classe1, s5),
                new Valutazione(LocalDate.of(2026, 1, 23), 7.5, "Interrogazione: ereditarieta' e polimorfismo", Tipologia.PROVA_ORALE,   classe1, s5),
                new Valutazione(dataScritta2,              6.0, "Verifica scritta del secondo quadrimestre",    Tipologia.PROVA_SCRITTA, classe1, s5),
                new Valutazione(LocalDate.of(2026, 5,  8), 7.0, "Interrogazione: collezioni ed eccezioni",      Tipologia.PROVA_ORALE,   classe1, s5),

                // s6 — Gaspare Tortora
                new Valutazione(dataScritta1,              9.0, "Verifica scritta del primo quadrimestre",      Tipologia.PROVA_SCRITTA, classe1, s6),
                new Valutazione(LocalDate.of(2026, 1, 26), 9.5, "Interrogazione: ereditarieta' e polimorfismo", Tipologia.PROVA_ORALE,   classe1, s6),
                new Valutazione(dataScritta2,              8.5, "Verifica scritta del secondo quadrimestre",    Tipologia.PROVA_SCRITTA, classe1, s6),
                new Valutazione(LocalDate.of(2026, 5, 11), 9.5, "Interrogazione: collezioni ed eccezioni",      Tipologia.PROVA_ORALE,   classe1, s6)
        };
        for (Valutazione v : valutazioni5A) classe1.aggiungiValutazione(v);

        /*
        ========== STUDENTI 4A INFORMATICA ==========
         */
        Studente s7  = new Studente("Salvatore", "salvatore.pignataro@liceolamura.edu.it", "Pignataro", "Pigna2008");
        Studente s8  = new Studente("Armando", "armando.esposito@liceolamura.edu.it", "Esposito", "Espo10!");
        Studente s9  = new Studente("Andrea", "andrea.dapice@liceolamura.edu.it", "D'Apice", "Apice07");
        Studente s10 = new Studente("Pio", "pio.balsamo@liceolamura.edu.it", "Balsamo", "PioBalsa11!");
        Studente s11 = new Studente("Giovanni", "giovanni.handanovic@liceolamura.edu.it", "Handanovic", "Inter1908");
        Studente s12 = new Studente("Lorenzo", "lorenzo.insigne@liceolamura.edu.it", "Insigne", "Capitano24!");

        classe2.aggiungiStudente(s7);
        classe2.aggiungiStudente(s8);
        classe2.aggiungiStudente(s9);
        classe2.aggiungiStudente(s10);
        classe2.aggiungiStudente(s11);
        classe2.aggiungiStudente(s12);

        /*
        ========== STUDENTI 3A INFORMATICA ==========
         */
        Studente s13 = new Studente("Federica", "federica.sassoli@liceolamura.edu.it", "Sassoli", "FedeSas09");
        Studente s14 = new Studente("Serena", "serena.brancale@liceolamura.edu.it", "Brancale", "Brancalona!");
        Studente s15 = new Studente("Rosario", "rosario.miraggio@liceolamura.edu.it", "Miraggio", "RosarioMir99");
        Studente s16 = new Studente("Antonio", "antonio.vergara@liceolamura.edu.it", "Vergara", "TonyVer06!");
        Studente s17 = new Studente("Fabrizio", "fabrizio.corona@liceolamura.edu.it", "Corona", "KingCorona25");
        Studente s18 = new Studente("Patrizio", "patrizio.chianese@liceolamura.edu.it", "Chianese", "Patry01!");

        classe3.aggiungiStudente(s13);
        classe3.aggiungiStudente(s14);
        classe3.aggiungiStudente(s15);
        classe3.aggiungiStudente(s16);
        classe3.aggiungiStudente(s17);
        classe3.aggiungiStudente(s18);

        /*
        La chiamata ad un metodo del gestore apre una factory JpaUtil.
        Aggrego tutte le entita' in un unico ArrayList<Object> e lo passo
        a salvaTutti (varargs Object...): cosi' la chiamata resta leggibile
        anche con 60+ entita' fra studenti, lezioni, compiti e valutazioni.
         */
        ArrayList<Object> entitaDaSalvare = new ArrayList<>();
        Collections.addAll(entitaDaSalvare,
                docente,
                classe1, classe2, classe3,
                s1, s2, s3, s4, s5, s6,
                s7, s8, s9, s10, s11, s12,
                s13, s14, s15, s16, s17, s18
        );
        Collections.addAll(entitaDaSalvare, lezioni5A);
        Collections.addAll(entitaDaSalvare, compiti5A);
        Collections.addAll(entitaDaSalvare, valutazioni5A);

        boolean esito = gestore.salvaTutti(entitaDaSalvare.toArray());
        System.out.println("Setup avviato con esito: " + esito);
        JpaUtil.getInstance().chiudi();


    }

}
