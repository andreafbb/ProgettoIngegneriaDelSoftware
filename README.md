# Sistema di Registro Elettronico di Classe

## Descrizione
Sistema software per la gestione di un **registro elettronico di classe**.

## Obiettivo
Fornire strumenti a docenti e studenti per:
- gestione lezioni;
- assegnazione e monitoraggio compiti;
- registrazione e consultazione valutazioni.
- ecc...

## Stack Tecnologico
- `Java`
- `Database relazionale con MySQL`
- `UML` con `Visual Paradigm`
- `Git` / `GitHub`

## Struttura del Repository
```text
Documentation/
VisualParadigm/
JavaProject/
```

## Attenzione

Per poter accedere correttamente allo schema nel databse MySQL, è necessario inserire la password nel template persistence.xml.template, del proprio server MySQL.

## Come verificare il funzionamento
- Avviare il main di setup MainSetupInsert.java per popolare il DB MySQL con i dati di esempio (1 Docente con 3 classi, 6 studenti per classi e vari elementi nel registro)
- Avviare l'app da MainAvviaApp.java

## Documentazione di progetto

Il documento di progetto, per problemi di spazio su Git (limite a 100MB) , si trova a questo link:
https://drive.google.com/file/d/1B7Rtoau_8cYPVXlS5XvgNwcvsvKUIeys/view?usp=sharing

## Team
Sviluppato da:
- Andrea Francesco Bruno
- Gaspare Tortora
- Luciano Meccariello
