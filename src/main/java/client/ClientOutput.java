package client;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Created by oradchykova on 5/9/17.
 */
public class ClientOutput implements Runnable {
    private static Scanner console = new Scanner(System.in);
    private PrintWriter out;

    ClientOutput(OutputStream outputStream){
        this.out = new PrintWriter(outputStream);
    }

    @Override
    public void run() {
        while (true) {
            String line = console.nextLine();
            out.println(line);
            out.flush();
            if (line.trim().toUpperCase().equals("QUIT")) {
                break;
            }
        }
    }
}
