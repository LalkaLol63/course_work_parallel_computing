package lohvin;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class IndexStorage{
    LinkedList<Entry>[] buckets;
    private int numEntries = 0;
    private int capacity = 10;
    private static final double loadFactorLimit = 0.75;

    public IndexStorage() {
        buckets = (LinkedList<Entry>[]) new LinkedList[capacity];
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new LinkedList<>();
        }
    }

    public void put(String key, Set<String> value) {
        if(checkLoadFactor()) {
            updateMemory();
        }
        Entry entry = getEntry(key);
        if(entry != null) {
            Set<String> combinedSet = new HashSet<>(value);
            combinedSet.addAll(entry.getValue());
            entry.setValue(combinedSet);
        } else {
            int index = getIndex(key);
            buckets[index].add(new Entry(key, value));
            numEntries++;
        }
    }

    public Set<String> searchByQuery(String query) {
        String[] words = query.split(" ");
        Set<String> files = get(words[0]);
        if(files == null) {
            return null;
        }
        Set<String> result = new HashSet<>(files);
        for (int i = 1; i < words.length; i++) {
            files = get(words[i]);
            if(files == null) {
                return null;
            }
            result.retainAll(files);
        }
        return result;
    }

    public Set<String> get(String key) {
        Entry entry = getEntry(key);
        if(entry != null) {
            return entry.getValue();
        }
        return null;
    }

    private Entry getEntry(String key) {
        int index = getIndex(key);
        for (Entry entry : buckets[index]) {
            if(entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }
    private void updateMemory() {
        capacity *= 2;
        LinkedList<Entry>[] newBuckets = (LinkedList<Entry>[]) new LinkedList[capacity];

        for (int i = 0; i < capacity; i++) {
            newBuckets[i] = new LinkedList<>();
        }

        for (LinkedList<Entry> bucket : buckets) {
            for (Entry entry : bucket) {
                int newIndex = getIndex(entry.getKey());
                newBuckets[newIndex].add(entry);
            }
        }
        buckets = newBuckets;
    }

    private int getIndex(String key) {
        return Math.abs(key.hashCode()) % capacity;
    }

    private boolean checkLoadFactor() {
        return (double)numEntries / capacity >= loadFactorLimit;
    }

    static class Entry {
        private String key;
        private Set<String> value;

        public Entry(String key, Set<String> value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Set<String> getValue() {
            return Collections.unmodifiableSet(value);
        }

        public void setValue(Set<String> value) {
            this.value = value;
        }
    }
}
