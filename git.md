# Schritte zum eigenen Repository

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
`git commit`

entferntes Repro mit lokalem Repro abgleichen  
`git push origin`

## Hilfe
`git help`

`git help clone`

