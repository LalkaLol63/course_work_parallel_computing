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
        queue.offer(task);
        notify();
    }

    public synchronized void start() {
        if(isStarted) {
            throw new IllegalStateException("ThreadPool is already started");
        }
        if (isStopped) {
            throw new IllegalStateException("ThreadPool is stopped");
        }
        isStarted = true;
        for (Worker t : workers) {
            t.start();
        }
    }

    public synchronized void shutdown() {
        if(!isStarted) {
            throw new IllegalStateException("ThreadPool isn`t started");
        }
        isStopped = true;
        notifyAll();

        for (Worker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            while (!isStopped) {
                try {
                    Runnable task;
                    synchronized (ThreadPool.this) {
                        while (queue.isEmpty() && !isStopped) {
                            ThreadPool.this.wait();
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
