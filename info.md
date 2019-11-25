
Weiterentwicklung
=================


Anpassen
--------
- neue Quelltexte auskommentieren und beschreiben
- Bibliothek für JPEG mitgeben und beschreiben
- Pakete fragmentiert senden ???

Probleme
--------
- Last vom Server JPEG einlesen
- DatagramChannel nutzen beim Client
- empfangene Pakete nach Timestamp sortieren bei VLC


- UDP-Puffer im Netzwerkstack  ( SO_RCVBUF )
  - Anzeige: DatagramSocket.getReceiveBufferSize()
  - Setzen:   setReceiveBufferSize(int size)
  - Füllung prüfen mit: netstat -uantp

Weiterentwicklung
------------------
- RTCP implementieren?


RTP Payloadformat for JPEG-Video  (RFC 2435)
--------------------------------------------

- definiert JPEG-Header nach RTP-Header, 8 Byte main header
- Restart Header, wenn Typ 64-127
- MJPEGpacketize: sun.awt.image.ImageFormatException: Quantization table 0x01 was not defined


FEC  (RFC 5109)
---------------

- Entweder Separater FEC-Stream oder redundant Encoding
- Konfiguration mittels SDP (RFC 2327)

Für Praktikum
- Nutzung nur von Level 0
- Nutzung des separaten Streams mit PT 127


- Einfachste Variante ist XOR über Header + Payload - das kürzere Paket hat nach der Wiederherstellung dann Nullen am Ende


aktueller Stand
---------------
- Videos
  - movie.mjpeg 500 frames -> 20s (384x288) 
       JpegFrame.getFromJpegBytes IllegalStateException: Nur 8-bit Präzesion wird unterstützt
  - htw.mjpeg 2812 frames -> 112s  (640x360)
  - htw2.mjpeg  
  - htw3.mjpeg 
  - maulwurf.mjpeg 7908 frames -> 5:16 min, (720x576 ->  368x294) yuv420p, 25 Hz
  - PAL (720x576)
- Test-Player
  - vlc
  - mpv
- Server sendet RTP-Pakete mit ganzem JPEG
- Empfänger kann Fragmente zusammensetzen
- Restart für Client und Server möglich
