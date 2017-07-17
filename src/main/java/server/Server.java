package server;

import Database.DBConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Created by oradchykova on 5/9/17.
 */
public class Server {

    public static void main(String[] args) throws UnknownHostException{
        initLogging();

        final Map<String, Socket> users = new ConcurrentHashMap<>();
        final String dbName = "Chat";
        DBConnection.createDatabaseIfNotExists(dbName);
        try (ServerSocket server = new ServerSocket(2049)) {
            final ExecutorService executors = Executors.newCachedThreadPool();
            try {
                while (true) {
                    Socket socketConnection = server.accept();
                    executors.submit(new MailingServerRunnable(socketConnection, users, dbName));
                    Logger.getLogger("ChatServer").fine("start new connection");
                }
            } finally {
                executors.shutdownNow();
            }
        } catch (IOException ex){
            Logger.getLogger("ChatServer").log(Level.SEVERE, "Connection error", ex);
        }
    }

    private static void initLogging(){
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
    }
}
