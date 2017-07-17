package server;

import Database.DBConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private final Socket socket;
    private final Map<String, Socket> users;
    private final String dbName;
    private Scanner in;
    private PrintWriter out;

    MailingServerRunnable(Socket socket, Map<String, Socket> users, String database){
        this.socket = socket;
        this.users = users;
        this.dbName = database;
    }

    private void setStreams(InputStream input, OutputStream output){
        in = new Scanner(input);
        out = new PrintWriter(output);
    }

    @Override
    public void run() {
        String username = "";
        try {
            setStreams(socket.getInputStream(), socket.getOutputStream());

            sendGreeting();
            username = getUsername();
            addUserToDatabase(username);

            while (true) {
                if (!receiveMessage(username)) break;
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Connection error", ex);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Runtime exception", ex);
        } finally {
            if (!username.isEmpty()) quitFromChat(username);
        }
    }

    private void sendGreeting(){
        sendMessage("Hello! Send \"QUIT\" to exit");
    }

    private String getUsername(){
        sendMessage("Enter your username:");
        String name = in.nextLine();
        boolean wrongName = users.containsKey(name);

        while (wrongName){
            sendMessage("This username already exists. Enter another one:");
            name = in.nextLine();
            wrongName = users.containsKey(name);
        }

        users.put(name, socket);
        sendMessage("Your username is " + name);
        logger.fine("Created new user. Username " + name);
        return name;
    }

    private void addUserToDatabase(String username){
        try (DBConnection connection = new DBConnection(dbName)) {
            connection.updateUserEntered(username);
        }
    }

    private boolean receiveMessage(String username) {
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
            sendPrivateMessage(line, username);
        } else {
            sendBroadcastMessage(line, username);
        }
        return true;
    }

    private void sendMessage(String message){
        out.println(message);
        out.flush();
    }

    private void sendPrivateMessage(String line, String username){
        int endOfUsername = line.indexOf(" ");
        final String toUser = line.substring(1, endOfUsername);
        final String message = line.substring(endOfUsername + 1, line.length());

        Socket user = users.get(toUser);
        if (user == null){
            sendMessage("From server: Such user does not exist");
            return;
        }

        try (DBConnection connection = new DBConnection(dbName)) {
            connection.updateUserSentMessage(username, message, toUser);
        }

        try {
            PrintWriter userOut = new PrintWriter(user.getOutputStream());
            userOut.println(username + " privately: " + message);
            userOut.flush();
        } catch (IOException ex){
            logger.log(Level.SEVERE, "Connection error", ex);
        }
    }

    private void sendBroadcastMessage(String message, String username){
        try (DBConnection connection = new DBConnection(dbName)) {
            connection.updateUserSentMessage(username, message, null);
        }

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

    private void quitFromChat(String username){
        sendBroadcastMessage("has left chat", username);
        try {
            socket.close();
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
