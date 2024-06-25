package pl.bic;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            Socket socket = context.createSocket(SocketType.PULL);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr();
                System.out.println("Received message: " + message);

                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
