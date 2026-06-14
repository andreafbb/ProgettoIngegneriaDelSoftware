package setup;

import database.GestorePersistenza;
import database.JpaUtil;
import entity.ClasseVirtuale;
import entity.Docente;
import entity.Studente;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

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
        La chiamata ad un metodo del gestore apre una factory JpaUtil
         */
        boolean esito = gestore.salvaTutti(
                docente,
                classe1, classe2, classe3,
                s1, s2, s3, s4, s5, s6,
                s7, s8, s9, s10, s11, s12,
                s13, s14, s15, s16, s17, s18
        );
        System.out.println("Setup avviato con esito: " + esito);
        JpaUtil.getInstance().chiudi();


    }

}
