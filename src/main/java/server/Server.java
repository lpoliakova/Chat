package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

/**
 * Created by oradchykova on 5/9/17.
 */
public class Server {
    public static void main(String[] args) throws UnknownHostException{
        Logger logger = initLogging();
        System.out.println(InetAddress.getLocalHost());
        Map<String, Socket> users = new ConcurrentHashMap<>();
        try (ServerSocket server = new ServerSocket(2049)){
            while (true){
                Socket income = server.accept();
                startNewConnection(income, users, logger);
            }
        } catch (IOException ex){
            logger.log(Level.SEVERE, "Connection error", ex);
        }
    }

    private static void startNewConnection(Socket socket, Map<String, Socket> users, Logger logger){
        Thread thread = new Thread(new MailingServerRunnable(socket, users));
        logger.fine("start new connection");
        thread.start();
    }

    private static Logger initLogging(){
        Logger logger = Logger.getLogger("ChatServer");
        logger.setLevel(Level.FINE);
        //logger.setUseParentHandlers(false);
        try {
            FileHandler handler = new FileHandler("ChatServer.log");
            handler.setLevel(Level.FINE);
            logger.addHandler(handler);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Can't create log file handler", ex);
        }
        return logger;
    }
}
