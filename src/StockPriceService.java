import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Tyler Arsenault #143584
 *
 * Big class that parses commands and talks to the server in the JedisMaker class also extends the StockPricePricer
 * which uses an httpconnection to connect to the server. Implements runnable for every thread
 *
 */
public class StockPriceService implements Runnable {
    private StockPriceParser parser;

    //Boolean command to keep track if a user typed TRACK in the command line
    private boolean tracked = false;
    //To find a stock
    private boolean stock = false;
    //To keep track if user was switched
    private boolean switchUser;
    private ArrayList<String> userNames = new ArrayList<String>();
    private Map<String, List<String>> map = new HashMap<String, List<String>>();
    private List<String> tempList;

    //Private variables to keep track of the database
    private Jedis jedis;
    private JedisMaker jedisMake = new JedisMaker();
    //Keeps track of the current user that is currently logged in
    private String currentUser = "";
    private Socket s;
    private Scanner in;
    private PrintWriter out;


    /**
     * Constructor initializes a socket
     * @param methodSocket
     */
    public StockPriceService(Socket methodSocket) {
        s = methodSocket;
        init();
    }

    /**
     * Starts the database
     */
    public void init(){
        try{
            jedis = JedisMaker.make();
        }catch(IOException e){
            System.out.println("Exception " + e);
        }
    }

    /**
     * Getting streams and running service
     */
    public void run() {
        try {
            try {
                in = new Scanner(s.getInputStream());
                out = new PrintWriter(s.getOutputStream());
                doService();
            } finally {
                s.close();
            }
        } catch (IOException exception) {
            System.out.println("Exception " + exception);
        }
    }

    /**
     * Service method
     * @throws IOException
     */
    public void doService() throws IOException {
        while (true) {
            if (!in.hasNext()) {
                return;
            }
            String command = in.nextLine();
            executeCommand(command);
        }
    }

    /**
     * Executes different commands
     * @param command the command that is to be issued
     */
    public void executeCommand(String command) {
        //Calls on helper methods to try to spread the code out
        System.out.println("Received Command: " + command);
          if (command.equals("QUIT!")) {
            try {
                quit();
            }catch(IOException e){
                System.out.print("Exception " + e);
            }
        } else if (command.equals("HELP!")) {
            help();
        } else if (command.startsWith("USER")) {
            user(command);
        } else if (command.startsWith("LOGIN")) {
            login(command);
        } else if (command.equals("CURRENTUSER!")) {
            currentUser();
        } else if (command.equals("LISTUSERS!")) {
            listUsers();
        } else if (command.startsWith("TRACK")) {
           track(command);
        }
          else if (command.startsWith("FORGET")){
           forget(command);
        }
          else if(command.equals("PORTFOLIO!")){
            portfolio();
        }
        else if (!(command.equals("HELP!"))) {
            out.println("Invalid command, type HELP! for program options");
            out.flush();
        }
    }

    /**
     * Quits and saves the program
     * @throws IOException
     */
    public void quit()throws IOException {
        tracked = false;
        out.println("Exiting program...");
        System.out.println("Client has exited server");
        out.flush();
        jedisMake.setNames(userNames);
        jedisMake.setMap(map);
        jedisMake.fillMap(jedis);
        jedisMake.fillListUserNames(jedis);
    }

    /**
     * Prints a help message displaying commands
     */
    public void help() {
        out.append("USER username! creates a username||");
        out.append("QUIT will quit and save client info to the database||");
        out.append("PORTFOLIO!, view all of the stocks ||");
        out.append("TRACK ticker! track a particular stock ||");
        out.append("FORGET ticker! disables tracking of a ticker ||");
        out.append("LISTUSERS! will list local users and users in memory ||");
        out.append("LOGIN username will log into a username either in local memory or in the database ||");
        out.println("");
        out.flush();
    }

    /**
     * Creates a user
     * @param command command that is typed in
     */
    public void user(String command) {
        String userName = "";
        Scanner userScanner = new Scanner(command);
        userScanner.useDelimiter(" ");
        if (!(userScanner.hasNext())) {
            out.println("Invalid syntax, type HELP! for more options");
            out.flush();
            return;
        }
        while (userScanner.hasNext()) {
            String currentCommand = userScanner.next();
            if (!(currentCommand.equals("USER"))) {
                out.println("Invalid syntax, type HELP! for more options");
                out.flush();
                return;
            }
            userName = userScanner.next();

            //Calls on the database
            String databaseString = jedisMake.getListUserNames(jedis);
            System.out.println(databaseString);
            Scanner in = new Scanner(databaseString);
            in.useDelimiter(" ");
            while(in.hasNext()){
                String value = in.next();
                if(value.equalsIgnoreCase(userName.replaceAll("!", ""))){
                    out.println("User already exists in memory " );
                    out.flush();
                    return;
                }
            }
            in.close();



            if (!(userName.endsWith("!"))) {
                out.println("Invalid syntax, type HELP! for more options");
                out.flush();
                return;
            }
            userName = userName.replaceAll("!", "");
            for (int i = 0; i < userNames.size(); i++) {
                if (userNames.get(i).equalsIgnoreCase(userName)) {
                    out.println("User already created");
                    out.flush();
                    return;
                }
            }

            userNames.add(userName);

            System.out.println(userName);
            out.println(userName);
            out.flush();
        }
    }

    /**
     * Logs into a user
     * @param command command that is typed in
     */
    public void login(String command) {
        String userName = "";
        Scanner loginScanner = new Scanner(command);
        loginScanner.useDelimiter(" ");
        if (!(loginScanner.hasNext())) {
            out.println("Invalid syntax, type HELP! for options");
            out.flush();
            return;
        }
        while (loginScanner.hasNext()) {
            String currentCommand = loginScanner.next();
            if (!(currentCommand.equals("LOGIN"))) {
                out.println("Invalid syntax, type HELP! for options");
                out.flush();
                return;
            }
            userName = loginScanner.next();
            //Calls database again
            String databaseString = jedisMake.getListUserNames(jedis);
            List<String> tempString = new ArrayList<String>();
            Scanner in = new Scanner(databaseString);
            while(in.hasNext()){
                String value = in.next();
                if(value.equalsIgnoreCase(userName)){
                    currentUser = userName;
                    switchUser = true;
                    out.println("Logged in with database entry");
                    out.flush();
                    return;
                }
            }
            in.close();

            for (int i = 0; i < userNames.size(); i++) {
                if (userNames.get(i).equals(userName)) {
                    out.println("Logging in with user " + userName);
                    switchUser = true;
                    out.flush();
                    currentUser = userName;
                    return;
                }
            }
            out.println("User not found, create a user with USER");
            out.flush();
            return;
        }
    }

    /**
     * Tracks the current user
     */
    public void currentUser(){
        if (currentUser.equals("")) {
            out.println("No current user found, type USER username! to create a user");
            out.flush();
            return;
        }
        out.println(currentUser + " is the current user");
        out.flush();
    }

    /**
     * Lists all of the users
     */
    public void listUsers(){
        String concatList = "";
        for (int i = 0; i < userNames.size(); i++) {
            concatList += userNames.get(i) + "||";
        }
        //Gets the users
        String databaseUsers = jedisMake.getListUserNames(jedis);
        if (concatList.equals("") && databaseUsers.equals("")) {
            out.println("No users found");
            out.flush();
        }
        if(concatList.equals("")){
            concatList = "No user found ";
        }
        out.println("Local users:" + concatList + "Users in memory " + databaseUsers);
        out.flush();
    }

    /**
     * Tracks a stock
     * @param command command that is typed in
     */
    public void track(String command){
        double price = 0;
        if(currentUser.equals("")){
            out.println("No current user found, create a user with USER username! and login to that user with LOGIN username");
            out.flush();
            return;
        }
        Scanner trackerScanner = new Scanner(command);
        trackerScanner.useDelimiter(" ");
        if(!(trackerScanner.hasNext())){
            out.println("Invalid syntax, type HELP! for program options");
            out.flush();
            return;
        }
        String tracker = "";
        while(trackerScanner.hasNext()) {
            String currentCommand = trackerScanner.next();
            if (!(currentCommand.equals("TRACK"))) {
                out.println("Invalid syntax, type HELP! for more options");
                out.flush();
                return;
            }
            tracker = trackerScanner.next();
            if (!(tracker.endsWith("!"))) {
                out.println("Invalid syntax, type HELP! for more options");
                out.flush();
                return;
            }
            tracker = tracker.replaceAll("!", "");
            price = findStock(tracker);
            tracker += ":" +  "$" + price;
            tracker = tracker.toLowerCase();
            System.out.println("The boolean value is " + stock);
            if(!stock){
                out.println("Stock not found");
                out.flush();
                return;
            }
        }
        if(switchUser){
            tempList = new ArrayList<String>();
            switchUser = false;
        }
        if(tempList.contains(tracker)){
            tracker = tracker.trim().replaceAll("\\d", "").replaceAll("[:.,$]", "");
            out.println("Already tracking " + tracker);
            out.flush();
            return;
        }
        tracked = true;
        tempList.add(tracker);
        map.put(currentUser, tempList);
        out.println(map);
        out.flush();
    }

    /**
     * Finds the stock either in the database or in local memory
     * @param stockName the stock name
     * @return a price
     */
    public double findStock(String stockName){
        try {
            String url = "";
            url += "https://financialmodelingprep.com/api/v3/stock/real-time-price/" + stockName;
            System.out.println(url);
            URL u = new URL(url);
            URLConnection connection = u.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int code = httpConnection.getResponseCode();
            String message = httpConnection.getResponseMessage();
            System.out.println(code + " " + message);
            if (code != HttpURLConnection.HTTP_OK) {
                return 0;
            }
            InputStream inStream = connection.getInputStream();
            Scanner in = new Scanner(inStream);

            String inputString = "";
            while (in.hasNextLine()) {
                String input = in.nextLine();
                if(input.equals("{ }")){
                    stock = false;
                }
                else{
                    stock = true;
                    inputString += input;
                }
            }
            System.out.print("Price was obtained " + parser.parsePrice(inputString));
            double price = parser.parsePrice(inputString);
            return price;
        }catch(IOException e){
            System.out.println("Exception " + e);
        }
        return 0;
    }

    /**
     * Forgets a stock
     * @param command the command that is typed in
     */
    public void forget(String command){
        String ticker = "";
        Scanner forgetScanner = new Scanner(command);
        forgetScanner.useDelimiter(" ");
        //If forgetscanner does not have anything next
        if(!(forgetScanner.hasNext())){
            out.println("Invalid syntax, type HELP! for program options");
            out.flush();
            return;
        }
        while(forgetScanner.hasNext()){
            String currentCommand = forgetScanner.next();
            if(!(currentCommand.equals("FORGET"))){
                out.println("Invalid commands, type HELP! for program options");
                out.flush();
                return;
            }
            //Gets the ticker
            ticker = forgetScanner.next();
            if(!(ticker.endsWith("!"))){
                out.println("Invalid syntax, type HELP! for program options");
                out.flush();
                return;
            }
            //Replaces the ! with nothing
            ticker = ticker.replaceAll("!", "");
        }
            //Gets database information
            jedisMake.removeItem(jedis, currentUser, ticker);
            Iterator<Map.Entry<String, List<String>>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<String>> entry = iterator.next();
                if (entry.getKey().equals(currentUser)) {
                    List<String> tempValues = entry.getValue();
                    for (int i = 0; i < tempValues.size(); i++) {
                        if (tempValues.get(i).contains(ticker)) {
                            entry.getValue().remove(tempValues.get(i));
                        }
                    }
                }
            }
        out.println(map);
        out.flush();
    }

    /**
     * Gest the stock names and prices
     */
    public void portfolio(){
        if(currentUser == "" || currentUser == null){
            out.println("You must log in to a user before checking their portfolio, use LOGIN username to log in");
            out.flush();
            return;
        }

        if(tracked){
            out.println(jedisMake.getMap(jedis, currentUser) + "" + map.get(currentUser));
            out.flush();
            return;


        }



        if (jedisMake.checkDatabases(jedis, currentUser) && jedisMake.getMap(jedis, currentUser).contains("$")){
            out.println(jedisMake.getMap(jedis, currentUser));
            out.flush();
            return;
        }
        else {
            if(map.get(currentUser) == null || map.get(currentUser).equals("[]")){
                out.println("Nothing was found");
                out.flush();
                return;
            }
            out.println(map.get(currentUser));
            out.flush();
        }



    }




}



