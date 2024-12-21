package lohvin;

import java.util.ArrayList;
import java.util.LinkedList;

public class ConcurrentHashTable<K, V>{
    ArrayList<LinkedList<Entry<K, V>>> buckets = new ArrayList<>();
    private int numEntries = 0;
    private int numBuckets = 10;
    private static final double loadFactorLimit = 0.75;

    public ConcurrentHashTable() {
        for (int i = 0; i < numBuckets; i++) {
            buckets.add(new LinkedList<>());
        }
    }

    private boolean checkLoadFactor() {
        return (double)numEntries / numBuckets >= loadFactorLimit;
    }
    static class Entry<K, V> {
        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }
}
