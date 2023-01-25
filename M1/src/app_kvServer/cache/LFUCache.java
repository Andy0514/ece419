package app_kvServer.cache;
import java.util.HashMap;
import java.util.LinkedHashSet;

class LFUNode {
    LFUNode (String v, int c) {
        val = v;
        counts = c;
    }
    String val;
    int counts;
}

public class LFUCache extends ICache {
    HashMap<String, LFUNode> values;
    HashMap<Integer, LinkedHashSet<String>> countLists; //Counter and item list
    int min = -1;

    public LFUCache(int cap) {
        capacity = cap;
        currentSize = 0;
        values = new HashMap<>();
        countLists = new HashMap<>();
        countLists.put(1, new LinkedHashSet<>());
    }

    public String get(String key) {
        if (!values.containsKey(key))
            return null;

        // Update the frequency counter
        LFUNode relevant_node = values.get(key);
        int count = relevant_node.counts;
        relevant_node.counts += 1;
        // remove the element from the counter to linkedhashset
        countLists.get(count).remove(key);

        // keep track of the least-frequently-used element using the min counter, which
        // contains the use count of the least-frequently-used element. Because we just incremented
        // count, if min has no elements, then min+1 is the new min.
        if (count == min && countLists.get(count).size() == 0) {
            min++;
        }

        if (!countLists.containsKey(relevant_node.counts)) {
            countLists.put(relevant_node.counts, new LinkedHashSet<>());
        }

        countLists.get(relevant_node.counts).add(key);
        return relevant_node.val;
    }

    public void put(String key, String value) {

        boolean replacement = values.containsKey(key);

        if (replacement) {
            // If key exists, for LFU, we don't reset count to 1, and don't increase the count.
            // Setting the count to 1 is disadvantageous for performance.
            values.get(key).val = value;
            return;
        } else {
            if (currentSize >= capacity) {
                assert countLists.get(min).iterator().hasNext();
                String toBeEvicted = countLists.get(min).iterator().next();
                values.remove(toBeEvicted);
                countLists.get(min).remove(toBeEvicted);
                currentSize -= 1;
            }

            assert currentSize < capacity;
            values.put(key, new LFUNode(value, 1));
            min = 1;
            countLists.get(1).add(key);
            currentSize += 1;
        }
    }

    @Override
    public void clear() {
        values.clear();
        countLists.clear();
        min = -1;
    }

    @Override
    public boolean inCache(String key) {
        System.out.println(key + "  " + values.containsKey(key));
        return values.containsKey(key);
    }
}