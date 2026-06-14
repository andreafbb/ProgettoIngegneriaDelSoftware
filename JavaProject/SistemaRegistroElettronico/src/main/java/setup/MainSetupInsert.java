package setup;

import database.GestorePersistenza;
import database.JpaUtil;
import entity.ClasseVirtuale;
import entity.Docente;
import entity.Studente;

public class MainSetupInsert {

    public static void main(String[] args) {


        GestorePersistenza gestore = new GestorePersistenza();

        /*
        Andiamo a popolare il database con dati di esempio, principalmente 1 docente
        e vari studenti tutti iscritti ad 1 classe di prova associata al docente

        Proveremo poi a creare altre classi con altri studenti per un solo Docente

        Esempio

        5A Informatica, 4A Informatica, 3A Informatica

         */

        Docente docente = new Docente("Alessandro", "alessandro.senatore@liceolamura.edu.it", "Senatore", "Sandriello05");
        ClasseVirtuale classe = new ClasseVirtuale(docente, "5A Informatica");

        Studente s1 = new Studente("Andrea Francesco", "andreaf.bruno@liceolamura.edu.it", "Bruno", "Andreone05!");
        Studente s2 = new Studente("Mattia", "mattia.pactiello@liceolamura.edu.it", "Pactiello", "SanGiovanni24");

        /*
        Adesso colleghiamo gli elementi tra di loro
         */

        classe.aggiungiStudente(s1);
        classe.aggiungiStudente(s2);
        docente.aggiungiClasse(classe);

        /*
        La chiamata ad un metodo del gestore apre una factory JpaUtil
         */
        boolean esito = gestore.salvaTutti(docente, classe, s1, s2);
        System.out.println("Setup avviato con esito: "+esito);
        JpaUtil.getInstance().chiudi();


    }

}
