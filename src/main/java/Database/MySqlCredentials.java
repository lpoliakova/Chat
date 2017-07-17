package Database;

import java.util.Scanner;

/**
 * Created by oradchykova on 7/5/17.
 */
public class MySqlCredentials {
    public static final String LOCAL_URL = "jdbc:mysql://localhost:3306/";
    private final String username;
    private final String password;

    private static MySqlCredentials credentials;

    private MySqlCredentials(String username, String password){
        this.username = username;
        this.password = password;
    }

    private static MySqlCredentials findOutCredentials(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("MySql username:");
        String username = scanner.nextLine();
        System.out.println("MySql password:");
        String password = scanner.nextLine();
        credentials = new MySqlCredentials(username, password);
        return credentials;
    }

    public static MySqlCredentials getInstance(){
        if (credentials == null) findOutCredentials();
        return credentials;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
