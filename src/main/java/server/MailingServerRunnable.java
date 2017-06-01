package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by oradchykova on 5/9/17.
 */
public class MailingServerRunnable implements Runnable {
    private static final Logger logger = Logger.getLogger("ChatServer");
    private Socket internalSocket;
    private Scanner in;
    private PrintWriter out;
    private Map<String, Socket> users;
    private String username;

    MailingServerRunnable(Socket socket, Map<String, Socket> users){
        internalSocket = socket;
        try {
            in = new Scanner(internalSocket.getInputStream());
            out = new PrintWriter(internalSocket.getOutputStream());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Connection error", ex);
        }

        this.users = users;
    }

    @Override
    public void run() {
        try {
            sendGreeting();
            getUsername();

            while (true) {
                if (!receiveMessage()) break;
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Runtime exception", ex);
        } finally {
            quitFromChat();
        }
    }

    private void sendGreeting(){
        sendMessage("Hello! Send \"QUIT\" to exit");
    }

    private void getUsername(){
        sendMessage("Enter your username:");
        String name = in.nextLine();
        boolean wrongName = users.containsKey(name);

        while (wrongName){
            sendMessage("This username already exists. Enter another one:");
            name = in.nextLine();
            wrongName = users.containsKey(name);
        }

        username = name;
        users.put(username, internalSocket);
        sendMessage("Your username is " + username);
        logger.fine("Created new user. Username " + username);
    }

    private boolean receiveMessage() {
        String line;
        try {
            line = in.nextLine();
        } catch (NoSuchElementException ex){
            return false;
        }
        System.out.println(line);

        if (line.trim().toUpperCase().equals("QUIT")) {
            sendMessage("You left the chat!");
            return false;
        }

        if (line.startsWith("@")) {
            int endOfUsername = line.indexOf(" ");
            sendPrivateMessage(line.substring(1, endOfUsername), line.substring(endOfUsername + 1, line.length()));
        } else {
            sendBroadcastMessage(line);
        }
        return true;
    }

    private void sendMessage(String message){
        out.println(message);
        out.flush();
    }

    private void sendPrivateMessage(String username, String message){
        Socket user = users.get(username);
        if (user == null){
            sendMessage("From server: Such user does not exist");
            return;
        }

        try {
            PrintWriter userOut = new PrintWriter(user.getOutputStream());
            userOut.println(this.username + " privately: " + message);
            userOut.flush();
        } catch (IOException ex){
            logger.log(Level.SEVERE, "Connection error", ex);
        }
    }

    private void sendBroadcastMessage(String message){
        for (Map.Entry<String, Socket> user: users.entrySet()){
            if (user.getKey().equals(username)) continue;
            try {
                PrintWriter userOut = new PrintWriter(user.getValue().getOutputStream());
                userOut.println(username + ": " + message);
                userOut.flush();
            } catch (IOException ex){
                logger.log(Level.SEVERE, "Connection error", ex);
            }
        }
    }

    private void quitFromChat(){
        sendBroadcastMessage("has left chat");
        try {
            internalSocket.close();
        } catch (IOException ex){
            logger.log(Level.SEVERE, "Connection error", ex);
        }
        users.remove(username);
        logger.fine("User " + username + " left chat.");
    }

    private String getUsers(){
        StringBuilder info = new StringBuilder();
        for (Map.Entry<String, Socket> user: users.entrySet()){
            info.append("[")
                    .append(user.getKey())
                    .append(" ")
                    .append(user.getValue().isClosed() ? "not active" : "active" + "], ");
        }
        return info.length() == 0 ? "empty" : info.substring(0, info.length() - 2);
    }
}
