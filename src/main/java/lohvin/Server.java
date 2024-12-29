package lohvin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 6666;
    private ThreadPool threadPool;
    private ServerSocket serverSocket;

    public Server(int numThreads) {
        threadPool = new ThreadPool(numThreads);
    }

    public void start() {
        try{
            serverSocket = new ServerSocket(PORT);
            while(!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
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
