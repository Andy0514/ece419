package testing;

import app_kvServer.cache.LRUCache;
import app_kvServer.cache.FIFOCache;
import app_kvServer.cache.LFUCache;
import app_kvServer.cache.ICache;
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
		assertTrue(newMessage.getKey().equals("key"));
		assertTrue(newMessage.getValue().equals("value"));
		assertTrue(newMessage.getStatus() == KVMessage.StatusType.PUT);
		System.out.println(newMessage.getMsgBytes());
	}

	@Test
	public void testCreateMessageFromStream() throws Exception {
		KVMessageImpl newMessage = new KVMessageImpl("key", "value", KVMessage.StatusType.PUT);
//		byte[] keyBytes = "key".getBytes();
//		System.out.println("key: ");
//		for(byte b:keyBytes){
//			System.out.print(b + " ");
//		}
//		System.out.println("\nvalue: ");
//		byte[] valueBytes = "value".getBytes();
//		for(byte b:valueBytes){
//			System.out.print(b + " ");
//		}
		byte[] byteArray = newMessage.getMsgBytes();
//		System.out.println("\nmsg: ");
//		for(byte b:byteArray){
//			System.out.print(b + " ");
//		}
		InputStream targetStream = new ByteArrayInputStream(byteArray);
		KVMessageImpl secondMessage = new KVMessageImpl(targetStream);
//		byte[] byteArray2 = secondMessage.getMsgBytes();
//		System.out.println("\nmsg2: ");
//		for(byte b:byteArray2){
//			System.out.print(b + " ");
//		}
		assertTrue(newMessage.getKey().equals("key"));
		assertTrue(newMessage.getValue().equals("value"));
		assertTrue(newMessage.getStatus() == KVMessage.StatusType.PUT);
		assertTrue(secondMessage.getKey().equals("key"));
		assertTrue(secondMessage.getValue().equals("value"));
		assertTrue(secondMessage.getStatus() == KVMessage.StatusType.PUT);
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

	@Test
	public void testLRUCache() throws Exception {
		ICache test = new LRUCache(3);
		test.put("asdf", "fdsa");
		assertTrue(test.get("asdf") == "fdsa");

		test.put("a", "aa");
		test.put("b", "bb");
		assertFalse(test.inCache("c"));
		assertTrue(test.inCache("asdf") && test.inCache("a") && test.inCache("b"));

		test.get("a");
		test.put("d", "dd");
		test.put("e", "ee");
		assertTrue(test.inCache("a") && test.inCache("d") && test.inCache("e"));
		assertFalse(test.inCache("asdf") || test.inCache("b"));

		assertTrue(test.get("d") == "dd");
		test.put("d", "ee");
		assertTrue(test.get("d") == "ee");

		// No eviction should have taken place by an update operation
		assertTrue(test.inCache("a") && test.inCache("d") && test.inCache("e"));
	}

	@Test
	public void testFIFOCache() throws Exception {
		ICache test = new FIFOCache(3);
		test.put("asdf", "fdsa");
		assertTrue(test.get("asdf") == "fdsa");

		test.put("a", "aa");
		test.put("b", "bb");
		assertFalse(test.inCache("c"));
		assertTrue(test.inCache("asdf") && test.inCache("a") && test.inCache("b"));

		test.get("a");
		test.put("d", "dd");
		test.put("e", "ee");
		assertTrue(test.inCache("b") && test.inCache("d") && test.inCache("e"));
		assertFalse(test.inCache("asdf") || test.inCache("a"));

		assertTrue(test.get("d") == "dd");
		test.put("d", "ee");
		assertTrue(test.get("d") == "ee");

		// No eviction should have taken place by an update operation
		assertTrue(test.inCache("b") && test.inCache("d") && test.inCache("e"));
	}

	@Test
	public void testLFUCache() throws Exception {
		ICache test = new LFUCache(3);
		test.put("asdf", "fdsa");
		assertTrue(test.get("asdf") == "fdsa");

		test.put("a", "aa");
		test.put("b", "bb");
		assertFalse(test.inCache("c"));
		assertTrue(test.inCache("asdf") && test.inCache("a") && test.inCache("b"));

		test.get("asdf");
		test.get("asdf");
		test.get("a");

		// "asdf" has use count 3
		// "a" has use count 2
		// "b" has use count 1
		test.put("d", "dd"); // evict "b", but d's use count is 1
		test.put("e", "ee"); // evict "d"
		assertTrue(test.inCache("asdf") && test.inCache("e") && test.inCache("a"));
		assertFalse(test.inCache("b") || test.inCache("d"));

		assertTrue(test.get("e") == "ee");
		test.put("e", "ff");
		assertTrue(test.get("e") == "ff");

		// No eviction should have taken place by an update operation
		assertTrue(test.inCache("asdf") && test.inCache("e") && test.inCache("a"));
	}
}
