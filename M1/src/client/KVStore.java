package client;

import org.apache.log4j.Logger;
import shared.messages.KVMessage;
import shared.messages.KVMessageImpl;
import java.net.Socket;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import static shared.messages.KVMessage.StatusType.GET;

public class KVStore extends Thread implements KVCommInterface {
	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	
	private Logger logger = Logger.getRootLogger();
	private String address;
	private int port;
	private Socket clientSocket;
	private OutputStream output;
	private InputStream input;

	private boolean running = false;
	public enum SocketStatus {CONNECTED, DISCONNECTED, CONNECTION_LOST};
	private static final String prompt = "KVStore: ";

	private void printStatus(SocketStatus status) {
		if (status == SocketStatus.CONNECTION_LOST) {
			System.out.println(prompt + "connection lost");
		} else if (status == SocketStatus.DISCONNECTED) {
			System.out.println(prompt + "disconnected");
		}
	}

	public KVStore(String address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public void connect() throws UnknownHostException, IOException {
		clientSocket = new Socket(this.address, this.port);
		input = clientSocket.getInputStream();
		output = clientSocket.getOutputStream();
		running = true;
		logger.info(String.format("Connection established with server %s, port %d", this.address, this.port));
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	public void disconnect() {
		disconnect(SocketStatus.DISCONNECTED);
	}

	private void disconnect(SocketStatus reason) {
		logger.info("try to close connection ...");
		try {
			running = false;
			if (clientSocket != null) {
				clientSocket.close();
				clientSocket = null;
			}
			logger.info("socket has been closed");
			printStatus(reason);
		} catch (IOException e) {
			logger.error("Unable to close connection!");
		}
	}

	/**
	 * This method waits for the client to respond back with a message,
	 * and returns that message
	 */
	private KVMessage getServerResponse() throws Exception {
		Exception exc = null;
		try {
			KVMessage response = new KVMessageImpl(input);
			logger.info("Successfully received message from input stream");
			return response;
		} catch (IOException e) {
			logger.error("Error, I/O exception, could not receive message from input stream");
			exc = e;
			if (running) {
				disconnect(SocketStatus.CONNECTION_LOST);
			}
		} finally {
			if (exc != null) {
				throw exc;
			}
		}
		return null;
	}

	@Override
	public KVMessage put(String key, String value) throws Exception {
		// wait for KVMessage implementation
		KVMessageImpl newMessage = new KVMessageImpl(key, value, KVMessage.StatusType.PUT);
//		newMessage.setStatus(KVMessage.StatusType.PUT);
		byte[] messageBytes = newMessage.getMsgBytes();
		output.write(messageBytes, 0, messageBytes.length);
		output.flush();
		logger.info("Send PUT, key: '" + key + "', value: '" + value + "'");

		// Should return the server response message to the client
		return getServerResponse();
	}


	@Override
	public KVMessage get(String key) throws Exception {
		KVMessageImpl newMessage = new KVMessageImpl(key, "", KVMessage.StatusType.GET);
//		newMessage.setStatus(KVMessage.StatusType.GET);
		byte[] messageBytes = newMessage.getMsgBytes();
		output.write(messageBytes, 0, messageBytes.length);
		output.flush();
		logger.info("Send GET, key: '" + key + "'");

		// should return the server response message to the client
		return getServerResponse();
	}
}
