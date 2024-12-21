package lohvin;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ThreadPool {
    private final Queue<Runnable> queue = new LinkedList<>();
    private final List<Worker> workers = new ArrayList<>();
    private final int numThreads;
    private boolean isStopped = false;

    public ThreadPool(int numThreads) {
        this.numThreads = numThreads;
        for (int j = 0; j < numThreads; j++) {
            workers.add(new Worker());
        }
    }

    public synchronized void submit(Runnable task) throws IllegalStateException {
        if (isStopped) throw new IllegalStateException("ThreadPool is stopped");
        synchronized (queue) {
            queue.offer(task);
            queue.notify();
        }
    }

    public synchronized void start() {
        for (Worker t : workers) {
            t.start();
        }
    }

    public synchronized void shutdown() {
        isStopped = true;
        for (Worker t : workers) {
            t.interrupt();
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Runnable task;
                    synchronized (queue) {
                        while (queue.isEmpty()) {
                            queue.wait();
                        }
                        task = queue.poll();
                    }
                    task.run();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
