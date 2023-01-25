package app_kvServer.cache;

public class linkedList {
    LLNode head = null;
    LLNode tail = null;

    public LLNode insert_front (String value) {
        if (head == null) {
            assert tail == null;

            head = new LLNode(value);
            tail = head;
        } else {
            assert tail != null;

            LLNode new_node = new LLNode(value);
            new_node.next = head;
            head.prev = new_node;
            head = new_node;
        }
        return head;
    }

    public void delete (LLNode node) {
        if (node.prev == null && node.next == null) {
            // this is the only node in the linked list
            head = null;
            tail = null;
        } else if (node.prev == null) {
            head = node.next;
            node.next.prev = null;
        } else if (node.next == null) {
            tail = node.prev;
            node.prev.next = null;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
    }

    public void moveFront (LLNode node) {
        if (node.prev == null) {
            // This is already the head node. Do nothing.
            return;
        } else if (node.next == null) {
            // This is the tail node
            node.prev.next = null;
            tail = node.prev;
            node.next = head;
            node.prev = null;
            head.prev = node;
            head = node;
        } else {
            // This node is somewhere in the middle of the linked list
            node.prev.next = node.next;
            node.next.prev = node.prev;
            node.next = head;
            head.prev = node;
            head = node;
            node.prev = null;
        }
    }

    public String pop_back () {
        assert tail != null;
        String result = tail.value;
        tail.prev.next = null;
        tail = tail.prev;
        return result;
    }
}
