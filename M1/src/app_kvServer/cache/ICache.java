package app_kvServer.cache;


public abstract class ICache {

    public int capacity;  // number of key-value entries that can be cached
    public int currentSize;

    abstract public String get(String key);
    abstract public void put(String key, String value);
    abstract public void clear();
    abstract public boolean inCache(String key);
}
