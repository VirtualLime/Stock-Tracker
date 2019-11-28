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

    private boolean first = true;
    private PrintWriter out;

    public static void main(String[] args) throws IOException {
        StockPriceClient main = new StockPriceClient();
        main.help();
        //Port number
        final int SERVERPORT = 1001;
        while(true) {
            try (Socket s = new Socket("localhost", SERVERPORT)) {
                //Getting input and output from the socket
                InputStream inStream = s.getInputStream();
                OutputStream outStream = s.getOutputStream();
                //Parsing streams
                Scanner in = new Scanner(inStream);
                main.out = new PrintWriter(outStream);
                //Parsing any commands
                Scanner keyScan = new Scanner(System.in);
                String command = keyScan.nextLine();


                while (!(command.equals("QUIT!"))) {
                    main.out.print(command + "\n");
                    main.out.flush();
                    String response = in.nextLine();
                    System.out.println("Receiving " + response);
                    command = keyScan.nextLine();
                }
                command.equals("QUIT!");
                System.out.println("Exiting program... " + command);
                main.out.print(command + "\n");
                main.out.flush();
                System.exit(1);

            } catch (NoSuchElementException e) {
                System.out.println("No string found, type HELP! for options");
            }
        }
    }




    public void help() {
        System.out.println("==============COMMANDS===============");
        System.out.println("USER username! creates a username");
        System.out.println("QUIT will quit and save client info to the database");
        System.out.println("PORTFOLIO!, view all of the stocks ");
        System.out.println("TRACK ticker! track a particular stock ");
        System.out.println("FORGET ticker! disables tracking of a ticker ");
        System.out.println("LISTUSERS! will list local users and users in memory ");
        System.out.println("LOGIN username will log into a username either in local memory or in the database ");
        System.out.println("=======================================");
    }











}
