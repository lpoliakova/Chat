import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by oradchykova on 5/9/17.
 */
public class Mail {
    public static void main(String[] args) throws IOException{
        //InetAddress adr = InetAddress.getLocalHost();
        try (Socket s = new Socket("aspmx.l.google.com", 25)) {
            PrintWriter out = new PrintWriter(s.getOutputStream());
            out.println("HELO gmail.com");
            out.println("MAIL FROM: lena.helena@gmail.com");
            out.println("RCPT TO: lena.helena@gmail.com");
            out.println("DATA");
            out.println("Subject: test");
            out.println();
            out.println("Hello!");
            out.println("");
            out.println("QUIT");
            System.out.println("Sent");
            Scanner scan = new Scanner(s.getInputStream());
            while (scan.hasNextLine()){
                System.out.println(scan.nextLine());
            }

        }
    }
}
