package app_kvServer;

import app_kvServer.cache.FIFOCache;
import app_kvServer.cache.ICache;
import app_kvServer.cache.LFUCache;
import app_kvServer.cache.LRUCache;
import client.KVStore;
import logger.LogSetup;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.*;
import java.util.ArrayList;

public class KVServer extends Thread implements IKVServer {
	/**
	 * Start KV Server at given port
	 * @param port given port for storage server to operate
	 * @param cacheSize specifies how many key-value pairs the server is allowed
	 *           to keep in-memory
	 * @param strategy specifies the cache replacement strategy in case the cache
	 *           is full and there is a GET- or PUT-request on a key that is
	 *           currently not contained in the cache. Options are "FIFO", "LRU",
	 *           and "LFU".
	 */
	private int port;
	private int cacheSize;
	private CacheStrategy cacheStrategy;

	/**
	 * @param logger logger for the server.
	 * @param running boolean value assigned during the sever initialization, which represents whether the server is running or not.
	 * @param severSocket the server side of a socket connection    .
	 * @param connections the array to store all connected client.
	 * **/
	private static Logger logger = Logger.getRootLogger();
	private boolean running;
	private ServerSocket serverSocket;
	private ArrayList<ClientConnection> connections;

	private ICache cache;


	public KVServer(int port, int cacheSize, String strategy) {
		// TODO Auto-generated method stub
		this.port = port;
		this.cacheSize = cacheSize;
		try {
			this.cacheStrategy = CacheStrategy.valueOf(strategy);
			logger.info("The cache strategy is " + strategy);
		} catch (IllegalArgumentException e) {
			logger.info("The specified cache strategy doesn't exist, defaulting to using no cache.");
			this.cacheStrategy = CacheStrategy.None;
		}
		// There are four cache strategy in IKVServer.java,
		// None, LRU, LFU, FIFO
		switch (cacheStrategy) {
			case LRU:
				cache = new LRUCache(cacheSize);
				break;
			case FIFO:
				cache = new FIFOCache(cacheSize);
			case LFU:
				cache = new LFUCache(cacheSize);
			default:
				cache = null;
		}
	}

	@Override
	public int getPort(){
		// TODO Auto-generated method stub
		return this.port;
	}

	@Override
	public String getHostname(){
		// TODO Auto-generated method stub
		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			return hostName;
		} catch (UnknownHostException e) {
			logger.error("Local host name cannot be resolved into an address!");
			return null;
		}
	}

	@Override
	public CacheStrategy getCacheStrategy(){
		// TODO Auto-generated method stub
		return this.cacheStrategy;
//		return this.cacheStrategy;
	}

	@Override
	public int getCacheSize(){
		// TODO Auto-generated method stub
		return this.cacheSize;
	}

	@Override
	public boolean inStorage(String key){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean inCache(String key){
		// cache.inCache does not count as "usage" in LRU
		if (cache != null) {
			return cache.inCache(key);
		} else {
			return false;
		}
	}

	@Override
	public String getKV(String key) throws Exception {
		if (cache != null) {
			String result = cache.get(key);
			if (result != null) {
				return result;
			}
		}

		// TODO Not found in cache, search in storage
		return "";
	}

	@Override
	public void putKV(String key, String value) throws Exception {
		// TODO Put in file

		if (cache != null) {
			cache.put(key, value);
		}
	}

	@Override
	public void clearCache(){
		if (cache != null) {
			cache.clear();
		}
	}

	@Override
	public void clearStorage(){
		// TODO Auto-generated method stub
	}

	private boolean initializeServer() {
		logger.info("Initialize server ...");
		connections = new ArrayList<>();

		try {
			serverSocket = new ServerSocket(this.port);
			logger.info("Server listening on the port: " + serverSocket.getLocalPort());
		} catch (IOException e) {
			logger.error("Error! Cannot open server socket");
			if (e instanceof BindException) {
				logger.error("Port " + port + " is already bound!");
			}
			return false;
		}

		return true;
	}

	private boolean isRunning() { return this.running; }

	@Override
	public void run(){
		// TODO Auto-generated method stub
		running = initializeServer();

		if (serverSocket != null) {
			while (isRunning()) {
				try {
					Socket client = serverSocket.accept();
					ClientConnection connection = new ClientConnection(client, this);
					new Thread(connection).start();

					connections.add(connection);

					logger.info("Connected to "
							+ client.getInetAddress().getHostName()
							+ " on port " + client.getPort());
				} catch (IOException e) {
					logger.error("Error! " + "Unable to establish connection. \n ", e);
				}
			}
		}

		logger.info("Server stopped.");
	}

	@Override
	public void kill(){
		// TODO Auto-generated method stub
		running = false;
		try {
			logger.info("Killing the server...");
			for (ClientConnection client : connections) {
				client.stop();
			}
			serverSocket.close();
		} catch (IOException e) {
			logger.error("Error! Unable to close the server on port " + port);
		}
	}

	@Override
	public void close(){
		// TODO Auto-generated method stub
		running = false;
		try {
			logger.info("Killing the server...");
			for (ClientConnection client : connections) {
				client.stop();
			}
			serverSocket.close();
		} catch (IOException e) {
			logger.error("Error! Unable to close the server on port " + port);
		}
	}

	public static void main(String[] args) {
		try {
			new LogSetup("logs/server.log", Level.ALL);
			if (args.length != 3) {
				System.out.println("Error! Invalid number of arguments!");
				System.out.println("Usage: Server <port> <cacheSize> <strategy>");
			} else {
				int port = Integer.parseInt(args[0]);
				int cacheSize = Integer.parseInt(args[1]);
				String strategy = args[2];
				new KVServer(port, cacheSize, strategy).start();
			}
		} catch (IOException e) {
			System.out.println("Error! Unable to initialize logger!");
			e.printStackTrace();
			System.exit(1);
		} catch (NumberFormatException nfe) {
			System.out.println("Error! Invalid argument <port> or <cacheSize>! Not a number!");
			System.out.println("Usage: Server <port> <cacheSize> <strategy>");
			System.exit(1);
		}
	}
}