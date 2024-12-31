package lohvin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InvertedIndex {
    LinkedList<Entry>[] buckets;
    private AtomicInteger numEntries = new AtomicInteger(0);
    private int numBucketLocks = 8;
    private int capacity = 10;
    private static final double loadFactorLimit = 0.75;
    private final ArrayList<ReentrantReadWriteLock> bucketLocks;
    private final ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock();


    public InvertedIndex() {
        bucketLocks = new ArrayList<>();
        for (int i = 0; i < numBucketLocks; i++) {
            bucketLocks.add(new ReentrantReadWriteLock());
        }
        buckets = (LinkedList<Entry>[]) new LinkedList[capacity];
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new LinkedList<>();
        }
    }

    public void put(String key, DocWordPositions value) {
        globalLock.readLock().lock();
        try {
            if (checkLoadFactor()) {
                globalLock.readLock().unlock();
                globalLock.writeLock().lock();
                try {
                    if (checkLoadFactor()) {
                        updateMemory();
                    }
                } finally {
                    globalLock.writeLock().unlock();
                    globalLock.readLock().lock();
                }
            }

            int index = Math.abs(key.hashCode()) % capacity;
            ReentrantReadWriteLock bucketLock = getBucketLock(index);
            bucketLock.writeLock().lock();
            try {
                Entry entry = null;
                for (Entry e : buckets[index]) {
                    if (e.getKey().equals(key)) {
                        entry = e;
                    }
                }
                if (entry != null) {
                    entry.getValue().add(value);
                } else {
                    Set<DocWordPositions> newSet = new HashSet<>();
                    newSet.add(value);
                    buckets[index].add(new Entry(key, newSet));
                    numEntries.incrementAndGet();
                }
            } finally {
                bucketLock.writeLock().unlock();
            }
        } finally {
            globalLock.readLock().unlock();
        }

    }


    public Set<DocWordPositions> get(String key) {
        LinkedList<Entry>[] currentBuckets = buckets;
        int index = Math.abs(key.hashCode()) % currentBuckets.length;
        ReentrantReadWriteLock bucketLock = getBucketLock(index);
        bucketLock.readLock().lock();
        try {
            Entry entry = null;
            for (Entry e : currentBuckets[index]) {
                if (e.getKey().equals(key)) {
                    entry = e;
                }
            }
            if (entry == null) return null;
            return entry.getValue();
        } finally {
            bucketLock.readLock().unlock();
        }
    }

    private ReentrantReadWriteLock getBucketLock(int bucketIndex) {
        return bucketLocks.get(bucketIndex % numBucketLocks);
    }

    private void updateMemory() {
        int newCapacity = capacity * 2;
        LinkedList<Entry>[] newBuckets = (LinkedList<Entry>[]) new LinkedList[newCapacity];

        for (int i = 0; i < newCapacity; i++) {
            newBuckets[i] = new LinkedList<>();
        }

        for (LinkedList<Entry> bucket : buckets) {
            for (Entry entry : bucket) {
                int newIndex = Math.abs(entry.getKey().hashCode()) % newCapacity;
                newBuckets[newIndex].add(entry);
            }
        }
        buckets = newBuckets;
        capacity = newCapacity;
    }

    private boolean checkLoadFactor() {
        return (double) numEntries.get() / capacity >= loadFactorLimit;
    }

    static class Entry {
        private String key;
        private Set<DocWordPositions> value;

        public Entry(String key, Set<DocWordPositions> value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Set<DocWordPositions> getValue() {
            return value;
        }

        public void setValue(Set<DocWordPositions> value) {
            this.value = value;
        }
    }
}
