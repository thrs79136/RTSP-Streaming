# RTSP-Streaming
Beleg Videostreaming für das Modul Internettechnologien 2

## Aufgaben

### 1. Client
Ergänzen Sie die Klasse Client entsprechend der in der Projektbeschreibung und den Kommentaren im Quelltext gegebenen Hinweisen.

### 2. Server
Ergänzen Sie die Klasse RTPpacket entsprechend der in der Projektbeschreibung und den Kommentaren im Quelltext gegebenen Hinweisen.

### 3. RTSP-Methoden
Ergänzen Sie die RTSP-Methoden OPTIONS und DESCRIBE anhand der Beispiele aus RFC 2326, RFC 2327 und RFC 5109. Es ist ausreichend, sich bei DESCRIBE auf das Beispielvideo zu beziehen.

### 4. Simulation von Paketverlusten
Simulieren Sie Paketverluste und eine variable Verzögerung im Netz, indem Sie am Sender eine wahlweise Unterdrückung von zu sendenden Paketen vornehmen. Diese Unterdrückung von Paketen sollte zufällig und mit einstellbarer Paketverlustwahrscheinlichkeit über das GUI erfolgen. Beispiel: der Wert 0,1 bedeutet, es werden im Mittel 10% der zu übertragenen Pakete unterdrückt.

### 5. Anzeige von Statistiken am Client
Um die simulierten Netzwerkeigenschaften prüfen zu können und die Leistungsfähigkeit der später zu integrierenden Fehlerschutzcodes einschätzen zu können, ist eine Statistikanzeige notwendig.
Folgende Werte sollten mindestens am Client angezeigt werden:
1. Anzahl erhaltener/verlorener Medienpakete + prozentuale Angabe
2. Anzahl korrigierter/unkorrigierbarer Medienpakete
3. Die Anzeige sollte bis zum Ende des Videos sekündlich aktualisiert werden und dann auf dem Gesamtstand stehen bleiben.

Mit dem ersten Punkt kann die Qualität der Verbindung eingeschätzt werden und mit dem zweiten Punkt die Leistungsfähigkeit des FEC-Verfahrens.
Machen Sie sich Gedanken über weitere zu überwachende Parameter.


### 6. Implementierung des FEC-Schutzes
t.b.d.
