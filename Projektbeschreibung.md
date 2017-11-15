# Projektrahmen
Im Praktikum werden Sie einen Client und Server für Videostreaming unter Nutzung des Real-Time-Streaming-Protokolls (RTSP) implementieren. Die eigentlichen Videodaten werden mit-tels Real-Time-Protokoll (RTP) übertragen. Ein großer Teil der Funktionalität ist bereits alsQuelltext vorhanden, so das RTSP-Protokoll im Server, das RTP-Handling im Client sowie dieVideoanzeige.
Ihre Aufgabe besteht im Wesentlichen aus der Ergänzung der Quellcodes in den Punkten:
* RTSP-Protokoll im Client 
* RTP-Protokoll im Server
* FEC in Client und Server

## Java-Klassen
Das Projekt besteht aus folgenden Java-Klassen:

Client: Funktionalität des Clients mit Benutzerschnittstelle zum Senden der RTSP-Kommandosund Anzeige des Videos.  
Server: Funktionalität des Servers zur Antwort auf die RTSP-Clientanfragen und Streaming desVideos.  
RTPpacket: Funktionalität zur Unterstützung von RTP-Paketen.  
VideoStream: Funktionalität zum Einlesen einer MJPEG-Datei auf der Serverseite

## Programmstart
Der Start des Servers erfolgt mittels `java Server RTSP-Port`. Der Standard-RTSP-Port ist 554, Sie werden aber im Praktikum einen Port > 1024 nutzen. Der Start des Clients erfolgt mittels `java Client server_name server_port video_file`. Am Client können RTSP-Kommandos angefordert werden. 
Eine Kommunikation läuft in derRegel folgendermaßen ab:  
1. Client sendet SETUP: Erzeugung der Session und der Transportparameter
2. Client sendet PLAY 
3. Client sendet u.U. PAUSE
4. Client sendet TEARDOWN: Terminierung der Session.
Der Server antwortet auf alle Clientrequests. Die Antwortcodes sind ähnlich zu HTTP. DerCode 200 bedeutet z.B. Erfolg. Die Codes finden Sie in [3]

## Client
Als ersten Schritt sollte das RTSP-Protokoll in den Handlern der Buttons der Benutzerinterfacesvervollständigt werden. Für die RTSP-Kommunikation mit dem Server wird der bereits geöffneteSocket verwendet. In jeden Request muss ein CSeq-Header eingefügt werden. Der Wert von CSeqerhöht sich bei jedem Senderequest.

### Setup
* Erzeugen eines Sockets für den Empfang der RTP-Pakete und setzen des Timeouts (5 ms)
* Senden des SETUP-Requests an den Server, Ergänzung des Transportheaders mit demgeöffneten RTP-Port.
* Einlesen der RTSP-Antwort vom Server und parsen des Sessionheaders für die Session-ID

### Play
* Senden des PLAY-Requests mit Sessionheader und Session-ID (kein Transportheader)
* Einlesen der RTSP-Antwort

### Pause
* Senden des PAUSE-Requests mit Sessionheader und Session-ID
* Einlesen der RTSP-Antwort

### Teardown
* Senden des TEARDOWN-Requests mit Sessionheader und Session-ID
* Einlesen der RTSP-Antwort

### Beispiel
Im Praktikum wird ein sehreinfacher Parserim Server verwendet, welcher die Daten in einerbestimmten Reihenfolge erwartet. Bitte orientieren Sie sich an den folgenden Beispiel (C-Client,S-Server). Insbesondere sind die Leerzeichen zu beachten (client_port) und die Request-URLdarf nur relativ sein (ohne rtsp://host).
```
C: OPTIONS movie.Mjpeg RTSP/1.0: 
   CSeq: 1

S: RTSP/1.0 200 OK
 : CSeq: 1
 : Public: DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE

C: SETUP movie.Mjpeg RTSP/1.0
 : CSeq: 1
 : Transport: RTP/UDP; client_port= 25000

S: RTSP/1.0 200 OK
 : CSeq: 1
 : Session: 123456

C: PLAY movie.Mjpeg RTSP/1.0
 : CSeq: 2
 : Session: 123456

S: RTSP/1.0 200 OK
 : CSeq: 2
 : Session: 123456

C: PAUSE movie.Mjpeg RTSP/1.0
 : CSeq: 3
 : Session: 123456

S: RTSP/1.0 200 OK
 : CSeq: 3
 : Session: 123456

C: TEARDOWN movie.Mjpeg RTSP/1.0
 : CSeq: 4
 : Session: 123456
```

### Zustände des Clients
Im RTSP-Protokoll hat jede Session einen bestimmten Zustand. Sie müssen den Zustand desClients entsprechend aktualisieren.

(rtp-state.png)

## Server
Auf Serverseite muss das Einbetten der Videodaten in die RTP-Pakete erfolgen. Die beinhaltetdas Erzeugen des Paketes, Setzen der Headerfelder und setzen der Payload.Für Informationen zur Bitmanipulation in Java siehe Vorlesungsfolien zu RTP.


(rtp-header.png)
