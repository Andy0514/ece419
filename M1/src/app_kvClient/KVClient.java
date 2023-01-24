package app_kvClient;

import client.Client;
import client.KVCommInterface;
import client.KVStore;
import logging.LogSetup;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import shared.messages.KVMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

public class KVClient implements IKVClient {

    KVCommInterface store = null;
    boolean stop = false;
    private static final String PROMPT = "KVClient> ";
    private BufferedReader stdin;
    private Logger logger = Logger.getRootLogger();

    @Override
    public void newConnection(String hostname, int port) throws Exception{
        store = new KVStore(hostname, port);
        store.connect();
    }

    @Override
    public KVCommInterface getStore() {
        return store;
    }

    private void run() {
        while(!stop) {
            stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(PROMPT);

            try {
                String cmdLine = stdin.readLine();
                this.handleCommand(cmdLine);
            } catch (IOException e) {
                stop = true;
                printError("CLI does not respond - Application terminated ");
            }
        }
    }

    private void handleCommand(String cmdLine) {
        String[] tokens = cmdLine.split("\\s+");

        if(tokens[0].equals("quit")) {
            stop = true;
            disconnect();
            System.out.println(PROMPT + "Application exit!");
        } else if (tokens[0].equals("connect")) {
            if (store != null) {
                logger.error("A connection is already established");
                printError("A connection is already established, please first disconnect");
            } else if(tokens.length == 3) {
                try{
                    String serverAddress = tokens[1];
                    int serverPort = Integer.parseInt(tokens[2]);
                    connect(serverAddress, serverPort);
                } catch(NumberFormatException nfe) {
                    printError("No valid address. Port must be a number!");
                    logger.info("Unable to parse argument <port>", nfe);
                } catch (UnknownHostException e) {
                    printError("Unknown Host!");
                    logger.info("Unknown Host!", e);
                } catch (IOException e) {
                    printError("Could not establish connection!");
                    logger.warn("Could not establish connection!", e);
                }
            } else {
                printError("Invalid number of parameters!");
            }

        } else if (tokens[0].equals("put")) {
            if(tokens.length >= 3) {
                if(store != null && store.isRunning()){
                    StringBuilder msg = new StringBuilder();
                    for(int i = 2; i < tokens.length; i++) {
                        msg.append(tokens[i]);
                        if (i != tokens.length -1 ) {
                            msg.append(" ");
                        }
                    }
                    try {
                        KVMessage result = store.put(tokens[1], msg.toString());
                        if (result.getStatus() == KVMessage.StatusType.PUT_ERROR) {
                            printError("Put failed on server-side");
                        } else if (result.getStatus() == KVMessage.StatusType.PUT_SUCCESS) {
                            printInfo("Put request succeeded, value inserted");
                        } else if (result.getStatus() == KVMessage.StatusType.PUT_UPDATE) {
                            printInfo("Put request succeeded, value updated");
                        }
                    } catch (Exception e) {
                        printError("Put failed");
                    }
                } else {
                    printError("Put failed - Not connected!");
                }
            } else {
                printError("Put failed - You must enter put command using 'put <key> <value>'");
            }
        } else if (tokens[0].equals("get")) {
            if (tokens.length == 2) {
                if (store != null && store.isRunning()) {
                    try {
                        KVMessage result = store.get(tokens[1]);
                        if (result.getStatus() == KVMessage.StatusType.GET_ERROR) {
                            printError("Get failed on server-side");
                        } else if (result.getStatus() == KVMessage.StatusType.GET_SUCCESS){
                            System.out.println(result.toString());
                        }
                    } catch (Exception e) {
                        printError("Get failed");
                    }
                } else {
                    printError("Get failed - Not connected!");
                }
            } else {
                printError("Get failed - you must enter get command using 'get <key>'");
            }
        } else if(tokens[0].equals("disconnect")) {
            if (store == null) {
                logger.error("Attempting to disconnect when you were never connected");
                printError("Disconnect failed - you were never connected");
            } else {
                store.disconnect();
                store = null;
            }
        } else if(tokens[0].equals("logLevel")) {
            if(tokens.length == 2) {
                String level = setLevel(tokens[1]);
                if(level.equals(LogSetup.UNKNOWN_LEVEL)) {
                    printError("No valid log level!");
                    printPossibleLogLevels();
                } else {
                    System.out.println(PROMPT +
                            "Log level changed to level " + level);
                }
            } else {
                printError("Invalid number of parameters!");
            }
        } else if(tokens[0].equals("help")) {
            printHelp();
        } else {
            printError("Unknown command");
            printHelp();
        }
    }

    private void connect(String address, int port) throws UnknownHostException, IOException {
        store = new KVStore(address, port);
        store.connect();
    }

    private void disconnect() {
        if(store != null) {
            store.disconnect();
            store = null;
        }
    }

    private void printError(String error){
        System.out.println(PROMPT + "Error! " +  error);
    }

    private void printInfo(String info){
        System.out.println(PROMPT + "Info: " +  info);
    }

    private String setLevel(String levelString) {

        if(levelString.equals(Level.ALL.toString())) {
            logger.setLevel(Level.ALL);
            return Level.ALL.toString();
        } else if(levelString.equals(Level.DEBUG.toString())) {
            logger.setLevel(Level.DEBUG);
            return Level.DEBUG.toString();
        } else if(levelString.equals(Level.INFO.toString())) {
            logger.setLevel(Level.INFO);
            return Level.INFO.toString();
        } else if(levelString.equals(Level.WARN.toString())) {
            logger.setLevel(Level.WARN);
            return Level.WARN.toString();
        } else if(levelString.equals(Level.ERROR.toString())) {
            logger.setLevel(Level.ERROR);
            return Level.ERROR.toString();
        } else if(levelString.equals(Level.FATAL.toString())) {
            logger.setLevel(Level.FATAL);
            return Level.FATAL.toString();
        } else if(levelString.equals(Level.OFF.toString())) {
            logger.setLevel(Level.OFF);
            return Level.OFF.toString();
        } else {
            return LogSetup.UNKNOWN_LEVEL;
        }
    }

    private void printHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append(PROMPT).append("ECHO CLIENT HELP (Usage):\n");
        sb.append(PROMPT);
        sb.append("::::::::::::::::::::::::::::::::");
        sb.append("::::::::::::::::::::::::::::::::\n");
        sb.append(PROMPT).append("connect <host> <port>");
        sb.append("\t establishes a connection to a server\n");
        sb.append(PROMPT).append("put <key> <value>");
        sb.append("\t\t inserts a key-value pair into the storage server \n");
        sb.append(PROMPT).append("get <key>");
        sb.append("\t\t\t\t retrieves the value corresponding to the key in the storage server \n");
        sb.append(PROMPT).append("disconnect");
        sb.append("\t\t\t disconnects from the server \n");

        sb.append(PROMPT).append("logLevel");
        sb.append("\t\t\t\t changes the logLevel. Choose one from the following: \n");
        sb.append(PROMPT).append("\t\t\t\t ");
        sb.append("ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF \n");

        sb.append(PROMPT).append("quit ");
        sb.append("\t\t\t\t\t exits the program");
        System.out.println(sb.toString());
    }

    private void printPossibleLogLevels() {
        System.out.println(PROMPT
                + "Possible log levels are:");
        System.out.println(PROMPT
                + "ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF");
    }

    public static void main(String[] args) {
        try {
            new LogSetup("logs/client.log", Level.ALL);
            KVClient client_app = new KVClient();
            client_app.run();
        } catch (IOException e) {
            System.out.println("Error! Unable to initialize logger!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
