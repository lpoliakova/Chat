package Database;

import java.io.Closeable;
import java.security.InvalidParameterException;
import java.sql.*;
import java.util.Scanner;

/**
 * Created by oradchykova on 6/4/17.
 */
public class DBConnection implements Closeable{
    private Connection connection;

    public static void main(String[] args){
        createDatabaseIfNotExists("Chat");
        try (DBConnection connection = new DBConnection("Chat")) {
            connection.updateUserSentMessage("sasha", "Hello", null);
            connection.updateUserSentMessage("sasha", "Hello, Lena!", "lena");
        }
    }

    public static void createDatabaseIfNotExists(String dbName){
        MySqlCredentials credentials = MySqlCredentials.getInstance();
        createDatabase(dbName, credentials);
        createTables(dbName, credentials);
    }

    private static void createDatabase(String dbName, MySqlCredentials credentials){
        try (Connection conn = DriverManager.getConnection(MySqlCredentials.LOCAL_URL, credentials.getUsername(), credentials.getPassword())){
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void createTables(String dbName, MySqlCredentials credentials){
        try (Connection conn = DriverManager.getConnection(MySqlCredentials.LOCAL_URL + dbName, credentials.getUsername(), credentials.getPassword())){
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Users " +
                    "(ID int NOT NULL AUTO_INCREMENT, Username VARCHAR(100) UNIQUE NOT NULL, Used int NOT NULL DEFAULT 1, MessagesCount int NOT NULL DEFAULT 0, PRIMARY KEY(ID))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Messages " +
                    "(ID int NOT NULL AUTO_INCREMENT, UserID int NOT NULL, Message VARCHAR(200) NOT NULL, ToUserID int, PRIMARY KEY(ID), " +
                    "FOREIGN KEY (UserID) REFERENCES Users(ID), FOREIGN KEY (ToUserID) REFERENCES Users(ID))");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public DBConnection(String dbName){
        MySqlCredentials credentials = MySqlCredentials.getInstance();
        try {
            connection = DriverManager.getConnection(MySqlCredentials.LOCAL_URL + dbName, credentials.getUsername(), credentials.getPassword());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateUserEntered(String username){
        try (Statement statement = connection.createStatement()){
            ResultSet user = statement.executeQuery("SELECT Users.ID, Users.Used FROM Users WHERE Users.Username = '" + username + "'");
            if (user.next()) {
                Integer id = user.getInt(1);
                Integer used = user.getInt(2);
                statement.executeUpdate("UPDATE Users SET Users.Used = " + (used + 1) + " WHERE Users.ID = " + id);
            } else {
                statement.executeUpdate("INSERT INTO Users (Username) VALUES ('" + username + "')");
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void updateUserSentMessage(String username, String message, String toUsername){
        try (Statement statement = connection.createStatement()){
            ResultSet user = statement.executeQuery("SELECT Users.ID, Users.MessagesCount FROM Users WHERE Users.Username = '" + username + "'");
            user.next();
            Integer id = user.getInt(1);
            Integer messagesCount = user.getInt(2);
            Integer otherId = -1;
            if (toUsername != null){
                ResultSet otherUser = statement.executeQuery("SELECT Users.ID FROM Users WHERE Users.Username = '" + toUsername + "'");
                otherUser.next();
                otherId = otherUser.getInt(1);
            }
            Savepoint savepoint = connection.setSavepoint();
            try {
                statement.addBatch("UPDATE Users SET Users.MessagesCount = " + (messagesCount + 1) + " WHERE Users.ID = " + id);
                if (toUsername == null){
                    statement.addBatch("INSERT INTO Messages (UserID, Message) VALUES (" + id + ", '" + message + "')");
                } else {
                    statement.addBatch("INSERT INTO Messages (UserID, Message, ToUserID) VALUES (" + id + ", '" + message + "', " + otherId + ")");
                }
                statement.executeBatch();
            } catch (SQLException ex){
                connection.rollback(savepoint);
                ex.printStackTrace();
            }
            connection.releaseSavepoint(savepoint);
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void close(){
        try {
            connection.close();
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }


}
