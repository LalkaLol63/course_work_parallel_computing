package lohvin;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ThreadPool {
    private final Queue<Runnable> queue = new LinkedList<>();
    private final List<Worker> workers = new ArrayList<>();
    private final int numThreads;
    private volatile boolean isStopped = false;
    private volatile boolean isStarted = false;

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
        if(isStarted) throw new IllegalStateException("ThreadPool is already started");
        isStarted = true;
        for (Worker t : workers) {
            t.start();
        }
    }

    public synchronized void shutdown() {
        isStopped = true;
        for (Worker t : workers) {
            t.interrupt();
        }
        synchronized (queue) {
            queue.notifyAll();
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            while (!isStopped) {
                try {
                    Runnable task;
                    synchronized (queue) {
                        while (queue.isEmpty() && !isStopped) {
                            try {
                                queue.wait();
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                        if(isStopped) {
                            return;
                        }
                        task = queue.poll();
                    }
                    task.run();
                } catch (Exception e) {
                    System.out.println("Помилка при виконанні задачі" + e.getMessage());
                }
            }
        }
    }
}
