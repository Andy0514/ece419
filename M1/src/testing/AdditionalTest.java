package testing;

import org.junit.Test;

import junit.framework.TestCase;
import shared.messages.KVMessage;
import shared.messages.KVMessageImpl;

public class AdditionalTest extends TestCase {
	
	// TODO add your test cases, at least 3
	
	@Test
	public void testStub() {
		assertTrue(true);
	}

	public void testCreateMessage() throws Exception {
		KVMessageImpl newMessage = new KVMessageImpl("key", "value", KVMessage.StatusType.PUT);
		assertTrue(newMessage.getKey() == "key");
		assertTrue(newMessage.getValue() == "value");
		assertTrue(newMessage.getStatus() == KVMessage.StatusType.PUT);
	}
}
