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

            Thread threadIn = new Thread(new ClientInput(socket.getInputStream()));
            threadIn.start();

            Thread threadOut = new Thread(new ClientOutput(socket.getOutputStream()));
            threadOut.start();


            try {
                threadIn.join();
                threadOut.join();
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
