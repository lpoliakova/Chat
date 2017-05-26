package client;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Created by oradchykova on 5/9/17.
 */
public class ClientInput implements Runnable{
    private Scanner in;

    ClientInput(InputStream inputStream){
        this.in = new Scanner(inputStream);
    }

    @Override
    public void run() {
        while (in.hasNextLine()) {
            printReceivedMessage(in.nextLine());
            if (Thread.interrupted()) {
                break;
            }
        }
    }

    static void printReceivedMessage(String message){
        System.out.println(message);
    }
}
