package testing;

import client.KVStore;
import org.junit.Test;

import junit.framework.TestCase;
import shared.messages.KVMessage;
import shared.messages.KVMessageImpl;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

public class AdditionalTest extends TestCase {

	private KVStore kvClient1;
	private KVStore kvClient2;
	public void setUp() {
		// These tests assume you have a client set up at localhost 50000
		kvClient1 = new KVStore("localhost", 50000);
		kvClient2 = new KVStore("localhost", 50000);
		try {
			kvClient1.connect();
			kvClient2.connect();
		} catch (Exception e) {
		}
	}

	public void tearDown() {
		kvClient1.disconnect();
		kvClient2.disconnect();
	}


	@Test
	public void testCreateMessage() throws Exception {
		KVMessageImpl newMessage = new KVMessageImpl("key", "value", KVMessage.StatusType.PUT);
		assertTrue(newMessage.getKey() == "key");
		assertTrue(newMessage.getValue() == "value");
		assertTrue(newMessage.getStatus() == KVMessage.StatusType.PUT);
		System.out.println(newMessage.getMsgBytes());
	}

	@Test
	public void testCreateMessageFromStream() throws Exception {
		KVMessageImpl newMessage = new KVMessageImpl("key", "value", KVMessage.StatusType.PUT);
		byte[] byteArray = newMessage.getMsgBytes();
		InputStream targetStream = new ByteArrayInputStream(byteArray);
		KVMessageImpl secondMessage = new KVMessageImpl(targetStream);
		assertTrue(newMessage.getKey() == "key");
		assertTrue(newMessage.getValue() == "value");
		assertTrue(newMessage.getStatus() == KVMessage.StatusType.PUT);
		assertTrue(java.util.Arrays.equals(newMessage.getMsgBytes(), secondMessage.getMsgBytes()));
	}

	@Test
	public void testOversizeKey() throws Exception {
		Exception exc = null;
		try {
			KVMessageImpl newMessage = new KVMessageImpl("This key is very very oversized",
					"anyval", KVMessage.StatusType.GET);
		} catch (Exception e) {
			exc = e;
		}
		assertTrue(exc != null && exc.getMessage().equals("Exceed max key length"));
	}

	@Test
	public void testOversizeVal() throws Exception {
		StringBuilder str = new StringBuilder(130000);
		str.setLength(130000);
		for (int i = 0; i < 130000; i++) {
			str.setCharAt(i, 'a');
		}

		Exception exc = null;
		try {
			KVMessageImpl newMessage = new KVMessageImpl("key",
					str.toString(), KVMessage.StatusType.GET);
		} catch (Exception e) {
			exc = e;
		}
		assertTrue(exc != null && exc.getMessage().equals("Exceed max value length"));
	}

	@Test
	public void testMultiUpdateGet() throws Exception {
		String key = "updateTestValue";
		String initialValue = "initial";
		String updatedValue = "updated";
		String finalValue = "final";

		KVMessage response = null;
		Exception ex = null;

		try {
			kvClient1.put(key, initialValue);
			response = kvClient1.put(key, updatedValue);
			response = kvClient1.put(key, finalValue);

		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex == null && response.getStatus() == KVMessage.StatusType.PUT_UPDATE
				&& response.getValue().equals(finalValue));

		try {
			response = kvClient1.get(key);
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex == null && response.getStatus() == KVMessage.StatusType.GET_SUCCESS
				&& response.getValue().equals(finalValue));
	}

	@Test
	public void multiClientOneWriteOneRead() throws Exception {
		String key = "hello";
		String value = "world";

		KVMessage response = null;
		Exception ex = null;

		try {
			kvClient1.put(key, value);
			response = kvClient2.get(key);
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex == null && response.getStatus() == KVMessage.StatusType.GET_SUCCESS
				&& response.getValue().equals(value));
	}

	@Test
	public void multiClientOverwrite() throws Exception {
		String key = "hello";
		String valueOne = "world";
		String valueTwo = "universe";

		KVMessage response = null;
		Exception ex = null;

		try {
			kvClient1.put(key, valueOne);
			kvClient2.put(key, valueTwo);
			response = kvClient1.get(key);
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex == null && response.getStatus() == KVMessage.StatusType.GET_SUCCESS
				&& response.getValue().equals(valueTwo));
	}
}
