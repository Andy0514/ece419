package app_kvServer;

import logger.LogSetup;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import server.ClientConnection;
import server.Server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

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
	private String strategy;

	/**
	 * @param logger logger for the server.
	 * @param running boolean value assigned during the sever initialization, which represents whether the server is running or not.
	 * @param severSocket the server side of a socket connection    .
	 *
	 * **/
	private static Logger logger = Logger.getRootLogger();
	private boolean running;
	private ServerSocket serverSocket;


	public KVServer(int port, int cacheSize, String strategy) {
		// TODO Auto-generated method stub
		this.port = port;
		this.cacheSize = cacheSize;
		this.strategy = strategy;
	}

	@Override
	public int getPort(){
		// TODO Auto-generated method stub
		return this.port;
//		return -1;
	}

	@Override
    public String getHostname(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public CacheStrategy getCacheStrategy(){
		// TODO Auto-generated method stub
		return IKVServer.CacheStrategy.None;
	}

	@Override
    public int getCacheSize(){
		// TODO Auto-generated method stub
		return this.cacheSize;
//		return -1;
	}

	@Override
    public boolean inStorage(String key){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
    public boolean inCache(String key){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
    public String getKV(String key) throws Exception{
		// TODO Auto-generated method stub
		return "";
	}

	@Override
    public void putKV(String key, String value) throws Exception{
		// TODO Auto-generated method stub
	}

	@Override
    public void clearCache(){
		// TODO Auto-generated method stub
	}

	@Override
    public void clearStorage(){
		// TODO Auto-generated method stub
	}

	private boolean initializeServer() {
		logger.info("Initialize server ...");
		try {
			serverSocket = new ServerSocket(this.port);
			logger.info("Server listening on the port: " + serverSocket.getLocalPort());
			return true;
		} catch (IOException e) {
			logger.error("Error! Cannot open server socket");
			if (e instanceof BindException) {
				logger.error("Port " + port + " is already bound!");
			}
			return false;
		}
	}

	private boolean isRunning() { return this.running;}

	@Override
    public void run(){
		// TODO Auto-generated method stub
		running = initializeServer();

		if (serverSocket != null) {
			while (isRunning()) {
				try {
					Socket client = serverSocket.accept();
					ClientConnection connection = new ClientConnection(client);
					new Thread(connection).start();

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

	}

	@Override
    public void close(){
		// TODO Auto-generated method stub
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
