package app_kvServer.cache;
import java.util.*;

class LRUNode {
    LRUNode(String v, LLNode ref) {
        val = v;
        refToInsertionNode = ref;
    }
    String val;
    LLNode refToInsertionNode;
}


public class LRUCache extends ICache {

    HashMap<String, LRUNode> values;
    linkedList insertionList;

    public LRUCache(int cap) {
        values = new HashMap<>();
        insertionList = new linkedList();
        capacity = cap;
        currentSize = 0;
        assert capacity > 0;
    }

    @Override
    public String get(String key) {
        if (values.containsKey(key)) {
            LRUNode node = values.get(key);
            insertionList.moveFront(node.refToInsertionNode);
            return node.val;
        } else {
            return null;
        }
    }

    @Override
    public void put(String key, String value) {
        boolean replacement = values.containsKey(key);

        if (replacement) {
            LRUNode node = values.get(key);
            insertionList.moveFront(node.refToInsertionNode);
            node.val = value;
        } else {
            if (currentSize == capacity) {
                // evict
                String toBeEvicted = insertionList.pop_back();
                values.remove(toBeEvicted);
                currentSize -= 1;
            }

            values.put(key, new LRUNode(value, insertionList.insert_front(key)));
            currentSize += 1;
        }
    }

    @Override
    public void clear() {
        values.clear();
        insertionList = new linkedList();
    }

    @Override
    public boolean inCache(String key) {
        return values.containsKey(key);
    }
}
