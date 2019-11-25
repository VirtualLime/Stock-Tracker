import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Tyler Arsenault #143584
 *
 * This is a simple client class that connects to a server on port 1001
 *
 *
 *
 */
public class StockPriceClient {

    public static void main(String[] args) throws IOException {
        //Port number
        final int SERVERPORT = 1001;
        while(true) {
            try (Socket s = new Socket("localhost", SERVERPORT)) {
                //Getting input and output from the socket
                InputStream inStream = s.getInputStream();
                OutputStream outStream = s.getOutputStream();
                //Parsing streams
                Scanner in = new Scanner(inStream);
                PrintWriter out = new PrintWriter(outStream);
                //Parsing any commands
                Scanner keyScan = new Scanner(System.in);
                String command = keyScan.nextLine();

                while (!(command.equals("QUIT!"))) {
                    out.print(command + "\n");
                    out.flush();
                    String response = in.nextLine();
                    System.out.println("Receiving " + response);
                    command = keyScan.nextLine();
                }
                command.equals("QUIT!");
                System.out.println("Exiting program... " + command);
                out.print(command + "\n");
                out.flush();
                System.exit(1);

            } catch (NoSuchElementException e) {
                System.out.println("No string found, type HELP! for options");
            }
        }
    }











}
