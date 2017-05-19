package client;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Created by oradchykova on 5/9/17.
 */
public class Client {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 2049);

            CountDownLatch latch = new CountDownLatch(2);

            Thread threadIn = new Thread(new ClientInput(socket.getInputStream(), latch));
            threadIn.start();

            Thread threadOut = new Thread(new ClientOutput(socket.getOutputStream(), latch));
            threadOut.start();

            try {
                latch.await();
            } catch (InterruptedException ex) {

            } finally {
                socket.close();
            }

            /*try {
                sleep(2000);
            } catch (InterruptedException ex){

            }*/
        } catch (IOException ex) {
            System.out.println("Connection error");
            ex.printStackTrace();
        }
    }

}
