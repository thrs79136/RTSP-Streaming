#!/bin/bash

# es wird die Verzeichnisstruktur von IntelliJ IDEA angenommen
# für Eclipse sind die Verzeichnisse entsprechend anzupassen

port=3333
src=src
bin=out/production/RTSP-Streaming


# Kompilierung
javac -cp src src/Server.java  -d out/production/RTSP-Streaming
javac -cp src src/Client.java  -d out/production/RTSP-Streaming

# Start
java -cp out/production/RTSP-Streaming  Server $port &
sleep 1s
java -cp out/production/RTSP-Streaming  Client localhost $port videos/htw.mjpeg &
