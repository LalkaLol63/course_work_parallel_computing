package lohvin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private static final int PORT = 80;
    private ThreadPool threadPool;
    private ServerSocket serverSocket;

    public Server(int numThreads) {
        threadPool = new ThreadPool(numThreads);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер запущено на порті " + PORT);
            threadPool.start();
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        System.out.println("Сервер закінчив роботу.");
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        try {
            threadPool.shutdown();
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
