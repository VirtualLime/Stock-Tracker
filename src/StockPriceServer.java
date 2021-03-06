import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Tyler Arsenault #143584
 *
 * Simple server that creates threads for each user that connects to it
 *
 *
 */
public class StockPriceServer {

    private boolean first = false;

    public static void main(String[] args) throws IOException {
        final int SERVERPORT = 1001;

        StockPriceServer main = new StockPriceServer();

        ServerSocket server = new ServerSocket(SERVERPORT);
        System.out.println("Waiting for clients to connect");

        while(true)
        {
            Socket s = server.accept();
            System.out.println("Client connected");
            StockPriceService service = new StockPriceService(s);
            //Creates the threads
            Thread t = new Thread(service);
            t.start();
        }
    }
}
