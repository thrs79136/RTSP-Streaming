/* ------------------
Server
usage: java Server [RTSP listening port]
---------------------- */

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Server extends JFrame implements ActionListener, ChangeListener {

  // RTP variables:
  // ----------------
  DatagramSocket RTPsocket; // socket to be used to send and receive UDP packets
  DatagramPacket senddp; // UDP packet containing the video frames
  InetAddress ClientIPAddr; // Client IP address
  int RTP_dest_port = 0; // destination port for RTP packets  (given by the RTSP Client)
  int FEC_dest_port = 0; // destination port for RTP-FEC packets  (RTP or RTP+2)
  final static int startGroupSize = 2;
  FecHandler fec;
  //FecHandler fec = new FecHandler(startGroupSize);
  // Channel errors
  private double lossRate = 0.0;
  Random random = new Random(123456); // fixed seed for debugging
  int dropCounter; // Nr. of dropped media packets

  // GUI:
  // ----------------
  JLabel label;
  static JLabel stateLabel;

  // Video variables:
  // ----------------
  static int imagenb = 0; // image nb of the image currently transmitted
  VideoReader video; // VideoStream object used to access video frames
  static int MJPEG_TYPE = 26; // RTP payload type for MJPEG video
  static int FRAME_PERIOD = 40; // Frame period of the video to stream, in ms

  Timer timer; // timer used to send the images at the video frame rate
  // byte[] buf; // buffer used to store the images to send to the client

  // RTSP variables
  // ----------------
  // rtsp states
  static final int INIT = 0;
  static final int READY = 1;
  static final int PLAYING = 2;
  // rtsp message types
  static final int SETUP = 3;
  static final int PLAY = 4;
  static final int PAUSE = 5;
  static final int TEARDOWN = 6;
  static final int OPTIONS = 7;
  static final int DESCRIBE = 8;

  static int state; // RTSP Server state == INIT or READY or PLAY
  Socket RTSPsocket; // socket used to send/receive RTSP messages
  // input and output stream filters
  static BufferedReader RTSPBufferedReader;
  static BufferedWriter RTSPBufferedWriter;
  static String VideoFileName; // video file requested from the client
  static String VideoDir = "videos/";
  static int RTSP_ID = 123456; // ID of the RTSP session
  int RTSPSeqNb = 0; // Sequence number of RTSP messages within the session

  static final String CRLF = "\r\n";



  public Server() {
    super("Server"); // init Frame

    // init Timer
    timer = new Timer(FRAME_PERIOD, this);
    timer.setInitialDelay(0);
    timer.setCoalesce(false); // Coalesce can lead to buffer underflow in client

    // Handler to close the main window
    addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            // stop the timer and exit
            timer.stop();
            System.exit(0);
          }
        });

    // GUI:
    label = new JLabel("Send frame #        ", JLabel.CENTER);
    stateLabel = new JLabel("State:         ",JLabel.CENTER);
    getContentPane().add(label, BorderLayout.NORTH);
    getContentPane().add(stateLabel, BorderLayout.SOUTH);
    // Error Slider
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new GridLayout(2,0));
    JSlider dropRate = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
    dropRate.addChangeListener(this);
    dropRate.setMajorTickSpacing(10);
    dropRate.setMinorTickSpacing(5);
    dropRate.setPaintTicks(true);
    dropRate.setPaintLabels(true);
    dropRate.setName("p");
    JSlider groupSize = new JSlider(JSlider.HORIZONTAL, 2, 48, startGroupSize);
    groupSize.addChangeListener(this::stateChanged);
    groupSize.setMajorTickSpacing(4);
    groupSize.setMinorTickSpacing(1);
    groupSize.setPaintLabels(true);
    groupSize.setPaintTicks(true);
    groupSize.setName("k");
    mainPanel.add(groupSize);
    mainPanel.add(dropRate);
    getContentPane().add(mainPanel, BorderLayout.CENTER);
  }

  /**
   * Handler for Channel error Slider
   *
   * @param e Change Event
   */
  public void stateChanged(ChangeEvent e) {
    JSlider source = (JSlider) e.getSource();
    if (!source.getValueIsAdjusting()) {
      if (source.getName().equals("k")) {
        int k = source.getValue();
        fec.setFecGroupSize(k);
        System.out.println("New Group size: " + k);
      } else {
        lossRate = source.getValue();
        lossRate = lossRate / 100;
        System.out.println("New packet error rate: " + lossRate);
      }
    }
  }

  // ------------------------------------
  // main
  // ------------------------------------
  public static void main(String[] argv) throws Exception {
    // create a Server object
    Server theServer = new Server();
    theServer.setSize(500, 200);
    theServer.setVisible(true);

    // get RTSP socket port from the command line
    int RTSPport = Integer.parseInt(argv[0]);

    // Initiate TCP connection with the client for the RTSP session
    ServerSocket listenSocket = new ServerSocket(RTSPport);
    theServer.RTSPsocket = listenSocket.accept();
    listenSocket.close();

    // Get Client IP address
    theServer.ClientIPAddr = theServer.RTSPsocket.getInetAddress();

    // Initiate RTSPstate
    state = INIT;
    stateLabel.setText("INIT");


    // Set input and output stream filters:
    RTSPBufferedReader =
        new BufferedReader(new InputStreamReader(theServer.RTSPsocket.getInputStream()));
    RTSPBufferedWriter =
        new BufferedWriter(new OutputStreamWriter(theServer.RTSPsocket.getOutputStream()));


    int request_type;

    // loop to handle RTSP requests
    while (true) {
      // parse the request
      request_type = theServer.parse_RTSP_request(); // blocking

      switch (request_type) {
        case SETUP:
          // Wait for the SETUP message from the client
          state = READY;
          stateLabel.setText("READY");
          System.out.println("New RTSP state: READY");

          // Send response
          theServer.send_RTSP_response(SETUP);

          // init the VideoStream object:
          theServer.video = new VideoReader(VideoFileName);
          imagenb = 0;

          // init RTP socket and FEC
          theServer.RTPsocket = new DatagramSocket();
          theServer.fec = new FecHandler(startGroupSize);
          break;

        case PLAY:
          if (state == READY) {
            // send back response
            theServer.send_RTSP_response(PLAY);
            // start timer
            theServer.timer.start();
            // update state
            state = PLAYING;
            stateLabel.setText("PLAY");
            System.out.println("New RTSP state: PLAYING");
          }
          break;

        case PAUSE:
          if (state == PLAYING) {
            // send back response
            theServer.send_RTSP_response(PAUSE);
            // stop timer
            theServer.timer.stop();
            // update state
            state = READY;
            stateLabel.setText("READY");
            System.out.println("New RTSP state: READY");
          }
          break;

        case TEARDOWN:
          state = INIT;
          stateLabel.setText("INIT");
          // send back response
          theServer.send_RTSP_response(TEARDOWN);
          // stop timer
          theServer.timer.stop();
          // close sockets
          //theServer.RTSPsocket.close();
          theServer.RTPsocket.close();
          break;

        case OPTIONS:
          System.out.println("Options request");
          theServer.send_RTSP_response(OPTIONS);
          break;

        case DESCRIBE:
          System.out.println("DESCRIBE Request");
          theServer.send_RTSP_response(DESCRIBE);
          break;

        default:
          System.out.println("Wrong request");
      }
    }
  }

  /**
   * Hander for timer
   *
   * @param e ActionEvent
   */
  public void actionPerformed(ActionEvent e) {
    imagenb++; // image counter
    byte[] packet_bits;

    try {
      byte[] frame = video.readNextImage(); // get next frame
      if (frame != null) {
        System.out.println("Frame size: " + frame.length);

        // Build RTP-JPEG RFC 2435
        JpegFrame jpegFrame = JpegFrame.getFromJpegBytes(frame);
        frame = jpegFrame.getAsRfc2435Bytes();

        // Builds an RTPpacket object containing the frame
        RTPpacket rtp_packet =
            new RTPpacket(MJPEG_TYPE, imagenb, imagenb * FRAME_PERIOD, frame, frame.length);

        // retrieve the packet bitstream as array of bytes
        packet_bits = rtp_packet.getpacket();
        rtp_packet.printheader(); // Show header of bitstream if necessary
        rtp_packet.printpayload(8);

        // send the packet as a DatagramPacket over the UDP socket
        senddp = new DatagramPacket(packet_bits, packet_bits.length, ClientIPAddr, RTP_dest_port);

        sendPacketWithError(senddp, false); // Send with packet loss

        // FEC handling
        fec.setRtp(rtp_packet);
        if (fec.isReady()) {
          System.out.println("FEC-Encoder ready...");
          packet_bits = fec.getPacket();  // print Header
          // fec.printHeaders();
          // send to the FEC dest_port
          senddp = new DatagramPacket(packet_bits, packet_bits.length, ClientIPAddr, FEC_dest_port);
          sendPacketWithError(senddp, true);
        }

        // update GUI
        label.setText("Send frame #" + imagenb);
      } else timer.stop();
    } catch (Exception ex) {
      System.out.println("Exception caught: " + ex);
      ex.printStackTrace();
      System.exit(0);
    }
  }

  /**
   * @param senddp Datagram to send
   * @throws Exception Throws all
   */
  private void sendPacketWithError(DatagramPacket senddp, boolean fec) throws Exception {
    String label;
    if (fec) label = " fec ";
    else label = " media ";
    // TASK_F correct the if-instruction to work properly
    if (random.nextDouble() > lossRate) {
      System.out.println("Send frame: " + imagenb + label);
      RTPsocket.send(senddp);
    } else {
      System.err.println("Dropped frame: " + imagenb + label);
      if (!fec) dropCounter++;
    }
    // System.out.println("Drop count media packets: " +  dropCounter);
  }

  /**
   * Parse RTSP-Request
   *
   * @return RTSP-Request Type (SETUP, PLAY, etc.)
   */
  private int parse_RTSP_request() {
    int request_type = -1;
    try {
      System.out.println("*** wait for RTSP-Request ***");
      // parse request line and extract the request_type:
      String RequestLine = RTSPBufferedReader.readLine();
      // System.out.println("RTSP Server - Received from Client:");
      System.out.println(RequestLine);

      StringTokenizer tokens = new StringTokenizer(RequestLine);
      String request_type_string = tokens.nextToken();

      // convert to request_type structure:
      switch ((request_type_string)) {
        case "SETUP":
          request_type = SETUP;
          break;
        case "PLAY":
          request_type = PLAY;
          break;
        case "PAUSE":
          request_type = PAUSE;
          break;
        case "TEARDOWN":
          request_type = TEARDOWN;
          break;
        case "OPTIONS":
          request_type = OPTIONS;
          break;
        case "DESCRIBE":
          request_type = DESCRIBE;
          break;
      }

      if (request_type == SETUP) {
        // extract VideoFileName from RequestLine
        String dir = tokens.nextToken();
        //String[] tok = dir.split(".+?/(?=[^/]+$)");
        String[] tok = dir.split("/");
        //VideoFileName = VideoDir + tok[1];
        VideoFileName = VideoDir + tok[3];
        System.out.println("File: " + VideoFileName);
      }

      // parse the SeqNumLine and extract CSeq field
      String SeqNumLine = RTSPBufferedReader.readLine();
      System.out.println(SeqNumLine);
      tokens = new StringTokenizer(SeqNumLine);
      tokens.nextToken();
      RTSPSeqNb = Integer.parseInt(tokens.nextToken());

      // get LastLine
      String LastLine = RTSPBufferedReader.readLine();
      System.out.println(LastLine);

      if (request_type == SETUP) {
        // extract RTP_dest_port after Char "="
        RTP_dest_port = Integer.parseInt( LastLine.split("=")[1].split("-")[0] );
        FEC_dest_port = RTP_dest_port + 0;
        System.out.println("Client-Port: " + RTP_dest_port);
      }
      // else LastLine will be the SessionId line ... do not check for now.

      // Read until end of request (empty line)
      while (!LastLine.equals("")) {

        System.out.println(LastLine);
        LastLine = RTSPBufferedReader.readLine();

      }
      System.out.println("*** End of Request ***\n");

    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("Exception caught: " + ex);
      System.exit(0);
    }
    return (request_type);
  }

  /**
   * Send RTSP Response
   *
   * @param method RTSP-Method
   */
  private void send_RTSP_response(int method) {
    System.out.println("*** send RTSP-Response ***");
    try {
      RTSPBufferedWriter.write("RTSP/1.0 200 OK" + CRLF);
      RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);

      // 3th line depends on Request
      if (method == OPTIONS) {
        RTSPBufferedWriter.write(options() );
      } else if (method == DESCRIBE) {
        RTSPBufferedWriter.write(describe() );

      } else {
        RTSPBufferedWriter.write("Session: " + RTSP_ID + CRLF);
      }

      // Send end of response
      if (method != DESCRIBE) RTSPBufferedWriter.write(CRLF);
      RTSPBufferedWriter.flush();
      System.out.println("*** RTSP-Server - Sent response to Client ***");

    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("Exception caught: " + ex);
      System.exit(0);
    }
  }

  /** Creates a OPTIONS response string
   * @return  Options string, starting with: Public: ...
   */
  //TASK_F Complete the OPTIONS response
  private String options() {
    return "Public: DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE" + CRLF;
  }


  /** Creates a DESCRIBE response string in SDP format for current media */
  //TASK_F Complete the DESCRIBE response
  private String describe() {
    StringWriter rtspHeader = new StringWriter();
    StringWriter rtspBody = new StringWriter();

    // Write the body first so we can get the size later
    rtspBody.write("v=0" + CRLF);
    rtspBody.write("a=control:streamid=" + RTSP_ID + CRLF);
    rtspBody.write("m=video 0 RTP/AVP 96" + CRLF);
    rtspBody.write("m=audio 0 RTP/AVP 97" + CRLF);

    rtspHeader.write("Content-Base: " + RTSPsocket.getLocalAddress() + ":" + RTSPsocket.getLocalPort() + CRLF);
    rtspHeader.write("Content-Type: " + "application/sdp" + CRLF);
    rtspHeader.write("Content-Length: " + rtspBody.getBuffer().length() + CRLF);
    rtspHeader.write(CRLF);

    return rtspHeader.toString() + rtspBody.toString();
  }
}
