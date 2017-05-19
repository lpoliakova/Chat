package Client;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Created by oradchykova on 5/9/17.
 */
public class ClientInput implements Runnable{
    Scanner in;
    CountDownLatch latch;

    ClientInput(InputStream inputStream, CountDownLatch latch){
        this.in = new Scanner(inputStream);
        this.latch = latch;
    }

    @Override
    public void run() {
        while (in.hasNextLine()) {
            printReceivedMessage(in.nextLine());
            if (Thread.interrupted()) {
                break;
            }
        }
        latch.countDown();

    }

    static void printReceivedMessage(String message){
        System.out.println(message);
    }
}
