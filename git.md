# Schritte zum eigenen Repository


## eigenes privates Repository erstellen
Erzeugen Sie auf HTWDD-RN ein eigenes privates Repository mit einem Namen entsprechend Ihrer S-NUmmer in der Form `sXXXX-it2-beleg`. Fügen Sie als Kommentar Ihren Namen hinzu, um die Zuordnung zu erleichtern.

## eigenes Repository verknüpfen

Repository lokal klonen  
`git clone https://github.com/HTWDD-RN/RTSP-Streaming`

Wechsel in das angelegte lokale Repository  
`cd RTSP-Streaming`

Umbenennen des Alias des originalen Repositories     
`git remote rename origin htw-upstream`

Anlegen der Verknüpfung (origin) mit dem eigenen entfernten Repository  
`git remote add origin https://github.com/HTWDD-RN/sxxxxx-it2-beleg`

Aktualisierung des eigenen entfernten Repositories  
`git push origin master`


## Aktualisierung 

alle Dateien im Arbeitsverzeichnis mit lokalem Repr. verknüpfen  
`git add .`

Änderungen ins lokale Repr. übergeben  
`git commit`  oder  `git commit -m "kurze Angabe der Änderungen"`

entferntes Repro mit lokalem Repro abgleichen  
`git push origin`

## Arbeitserleichterung
Die globale Konfigurationsdatei der ignorierten Dateien befindet sich in `~/.gitignore`.
Hier können alle unerwünschten Dateiendungen wie z.B. `*.class *.*~ *.out` aufgelistet werden.

Die globale Konfiguration befindet sich in ~/.gitconfig

Konfiguration auflisten  
`git config -l`

Git-Usernamen  
`git config --global url."https://yourusername@github.com".insteadOf "https://github.com"`

Git-Passwort cachen  
`git config --global credential.helper 'cache –timeout=5400'`


## GUI
Manchmal ist es sinnvoll, eventuelle Verzweigungen grafisch darzustellen. Hierfür werden zwei Tools bereitgestellt: `gitk` und `smartgit`.

## Hilfe
`git help`

`git help clone`

Git-Übersicht (https://jan-krueger.net/wordpress/wp-content/uploads/2007/09/git-cheat-sheet.pdf)

Das Git-Buch (kostenlos)  (http://gitbu.ch/)

