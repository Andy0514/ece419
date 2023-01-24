package app_kvServer;

import shared.messages.TextMessage;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Represents a connection and point for a particular client that is
 * connected to the server. This class is responsible for message reception and sending.
 * **/
public class ClientConnection implements Runnable {
    private static final Logger logger = Logger.getRootLogger();
    private static final int BUFFER_SIZE = 1024;
    private static final int DROP_SIZE = 1024 * BUFFER_SIZE;

    private boolean isOpen;
    private KVServer server;
    private Socket clientSocket;
    private InputStream input;
    private OutputStream output;

    /**
     * Constructs a new ClientConnection object for a given TCP socket.
     *
     * @param clientSocket the Socket object for the client connection.
     * @param server the KV server to connect.
     **/
    public ClientConnection(Socket clientSocket, KVServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.isOpen = true;
    }

    public void run() {
        try {
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();

            while (isOpen) {
                try {
                    TextMessage message = receiveMessage();

                    String[] msg = null;
                    String send_msg = "";
                    msg = message.getMsg().trim().split("\\s+");

                    // TODO: right now msg[0] = key, msg[1] = value, msg[2] = status,
                    // TODO: can we switch to make status to be the first one?
                    if (msg[0].equals("PUT")) {
                        logger.info("Received a PUT request.");
                        send_msg = put_kv_to_server(msg);
                        sendMessage(new TextMessage(send_msg));
                    }

                    if (msg[0].equals("GET")) {
                        logger.info("Received a GET request.");
                        send_msg = get_kv_from_server(msg);
                        sendMessage(new TextMessage(send_msg));

                    }
                } catch (IOException e) {
                    logger.error("Error! Connection lost!");
                    isOpen = false;
                }
            }
        } catch (IOException e) {
            logger.error("Error! Connection cannot be established.");
        } finally {
            stop();
        }
    }

    public void stop() {
        isOpen = false;
        try {
            input.close();
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            logger.error("Error! Unable to close the connection successfully!");
        }
    }

    /**
     * Preprocess msg, and then get the value by key from the database
     **/

    private String get_kv_from_server(String[] msg) {
        String send_msg = "";

        if (msg.length < 2) {
            send_msg = "GET_ERROR";
            logger.error("Missing the key. The GET operation fails because of invalid input argument number.");
            return send_msg;
        }

        String value = "";
        try {
            value = server.getKV(msg[1]);
        } catch (Exception e) {
            send_msg = "GET_ERROR";
            logger.error("Server GET function fails! Send message " + send_msg);
        }

        send_msg = send_msg + ", " + value;
        return send_msg;
    }

    /**
     * Preprocess msg, and then put key, value into the database
     **/
    private String put_kv_to_server(String[] msg) {
        String send_msg = "";
        // Case of invalid request.
        if (msg.length < 2) {
            send_msg = "PUT_ERROR";
            logger.error("Missing the key. The PUT operation fails because of invalid input argument number.");
            return send_msg;
        }

        // Check whether the key is in storage
        if (server.inStorage(msg[1])) {
            if (msg.length == 2) {
                send_msg = "DELETE_SUCCESS";
            } else {
                send_msg = "PUT_UPDATE";
            }
        } else {
            if (msg.length == 2) {
                send_msg = "DELETE_ERROR";
            } else {
                send_msg = "PUT_SUCCESS";
            }
        }

        // Get the value string from received message.
        String value = "";
        for (int i = 2; i < msg.length; i++) {
            value += msg[i] + " ";
        }
        value.trim();

        // Call server put function, record the error if it fails
        try {
            server.putKV(msg[1], value);
            logger.info("Server PUT function succeeds. Send message " + send_msg);
        } catch (Exception e) {
            send_msg = "PUT_ERROR";
            logger.error("Server PUT function fails! Send message " + send_msg) ;
        }

        return send_msg;
    }

    /**
     * Method sends a TextMessage using this socket.
     *
     * @param msg the message that is to be sent.
     * @thros IOException some I/O error regarding the output stream.
     **/
    private void sendMessage(TextMessage msg) throws IOException {
        byte[] msgBytes = msg.getMsgBytes();
        output.write(msgBytes, 0, msgBytes.length);
        output.flush();
        logger.info("SEND \t <"
                + clientSocket.getInetAddress().getHostAddress() + ":"
                + clientSocket.getPort() + ">: '"
                + msg.getMsg() + "'"
        );
    }

    private TextMessage receiveMessage() throws IOException {
        int index = 0;
        byte[] msgBytes = null, tmp = null;
        byte[] bufferBytes = new byte[BUFFER_SIZE];

        // read first char from stream
        byte read = (byte) input.read();
        boolean reading = true;

        while (read != 10 && read != -1 && reading) {
            // if buffer filled, copy to msg array
            if (index == BUFFER_SIZE) {
                if (msgBytes == null) {
                    tmp = new byte[BUFFER_SIZE];
                    System.arraycopy(bufferBytes, 0, tmp, 0, BUFFER_SIZE);
                } else {
                    tmp = new byte[msgBytes.length + BUFFER_SIZE];
                    System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
                    System.arraycopy(bufferBytes, 0, tmp, msgBytes.length, BUFFER_SIZE);
                }

                msgBytes = tmp;
                bufferBytes = new byte[BUFFER_SIZE];
                index = 0;
            }

            // only read valid characters, i.e. letter and constants
            bufferBytes[index] = read;
            index++;

            // stop reading if DROP_SIZE is reached
            if (msgBytes != null && msgBytes.length + index >= DROP_SIZE) {
                reading = false;
            }

            // read next char from stream
            read = (byte) input.read();
        }

        if (msgBytes == null) {
            tmp = new byte[index];
            System.arraycopy(bufferBytes, 0, tmp, 0, index);
        } else {
            tmp = new byte[msgBytes.length + index];
            System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
            System.arraycopy(bufferBytes, 0, tmp, msgBytes.length, index);
        }

        msgBytes = tmp;

        // build final String
        TextMessage msg = new TextMessage(msgBytes);
        logger.info("RECEIVE \t<"
                + clientSocket.getInetAddress().getHostAddress() + ":"
                + clientSocket.getPort() + ">: '"
                + msg.getMsg().trim() + "'"
        );

        return msg;
    }
}