package setup;

import database.GestorePersistenza;
import database.JpaUtil;
import entity.ClasseVirtuale;
import entity.Docente;
import entity.Studente;

/*
Classe di prova per testare inizialmente se l'accesso al DB
tramite la classe fornita dal docente fosse corretto e coerente con
i dati inseriti dal MainSetupInsert
 */

public class MainTablesChek {

    public static void main(String[] args) {

        GestorePersistenza gestore = new GestorePersistenza();

        Docente docente = gestore.trovaPerId(Docente.class, 1L);
        ClasseVirtuale classe = gestore.trovaPerId(ClasseVirtuale.class, 1L);
        Studente s1 = gestore.trovaPerId(Studente.class, 1L);
        Studente s2 = gestore.trovaPerId(Studente.class, 2L);

        System.out.println("Docente: " + docente.getNome() + " " + docente.getCognome()
                + " (" + docente.getEmail() + ")");

        System.out.println("Classe associata al docente: " + classe.getNome());

        System.out.println("Studente 1: " + s1.getNome() + " " + s1.getCognome()
                + " (" + s1.getEmail() + ")");
        System.out.println("Studente 2: " + s2.getNome() + " " + s2.getCognome()
                + " (" + s2.getEmail() + ")");

        JpaUtil.getInstance().chiudi();
    }
}
