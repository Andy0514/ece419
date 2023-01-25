package app_kvServer.cache;

public class LLNode {
    public LLNode(String v) {
        value = v;
    }
    String value;
    LLNode prev = null;
    LLNode next = null;
}
