package client;

import org.apache.log4j.Logger;
import shared.messages.KVMessage;

public class KVStore implements KVCommInterface {
	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	
	private Logger logger = Logger.getRootLogger();
	private Socket clientSocket;
	private OutputStream output;
	private InputStream input;

	private boolean running = false;


	 
	public KVStore(String address, int port) throws UnknownHostException, IOException {
		clientSocket = new Socket(address, port);
		logger.info("Connection established with server {}, port {}", address, port);
	}

	@Override
	public void connect() throws Exception {
		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
	}

	@Override
	public KVMessage put(String key, String value) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KVMessage get(String key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
