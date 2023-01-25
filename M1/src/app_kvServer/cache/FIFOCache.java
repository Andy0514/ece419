package app_kvServer.cache;

import java.util.HashMap;

public class FIFOCache extends ICache {
    HashMap<String, String> values;
    linkedList insertionList;

    public FIFOCache(int cap) {
        values = new HashMap<>();
        insertionList = new linkedList();
        capacity = cap;
        currentSize = 0;
        assert capacity > 0;
    }

    @Override
    public String get(String key) {
        if (values.containsKey(key)) {
            return values.get(key);
        } else {
            return null;
        }
    }

    @Override
    public void put(String key, String value) {
        if (currentSize >= capacity) {
            // evict
            String toBeEvicted = insertionList.pop_back();
            values.remove(toBeEvicted);
            currentSize -= 1;
        }

        assert currentSize < capacity;
        insertionList.insert_front(key);
        values.put(key, value);
        currentSize += 1;
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
