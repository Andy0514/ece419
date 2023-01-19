package client;

import shared.messages.KVMessage;

import java.io.IOException;
import java.net.UnknownHostException;

public interface KVCommInterface {

	public enum SocketStatus {CONNECTED, DISCONNECTED, CONNECTION_LOST};

	/**
	 * Establishes a connection to the KV Server.
	 *
	 * @throws Exception
	 *             if connection could not be established.
	 */
	public void connect() throws UnknownHostException, IOException;

	/**
	 * disconnects the client from the currently connected server.
	 */
	public void disconnect();


	/**
	 * Inserts a key-value pair into the KVServer.
	 *
	 * @param key
	 *            the key that identifies the given value.
	 * @param value
	 *            the value that is indexed by the given key.
	 * @return a message that confirms the insertion of the tuple or an error.
	 * @throws Exception
	 *             if put command cannot be executed (e.g. not connected to any
	 *             KV server).
	 */
	public KVMessage put(String key, String value) throws Exception;

	/**
	 * Retrieves the value for a given key from the KVServer.
	 *
	 * @param key
	 *            the key that identifies the value.
	 * @return the value, which is indexed by the given key.
	 * @throws Exception
	 *             if put command cannot be executed (e.g. not connected to any
	 *             KV server).
	 */
	public KVMessage get(String key) throws Exception;

	/**
	 * Retrieves the status of the communication module, whether it is connected and running or not
	 * @return the value
	 */
	public boolean isRunning();
}
