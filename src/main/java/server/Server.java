package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by oradchykova on 5/9/17.
 */
public class Server {
    public static void main(String[] args) {
        Map<String, Socket> users = new ConcurrentHashMap<>();
        try (ServerSocket server = new ServerSocket(2049)){
            while (true){
                Socket income = server.accept();
                startNewConnection(income, users);
            }

        } catch (IOException ex){
            System.out.println("Connection error");
            ex.printStackTrace();
        }
    }

    static void startNewConnection(Socket socket, Map<String, Socket> users){
        Thread thread = new Thread(new MailingServerRunnable(socket, users));
        thread.start();
    }
}
