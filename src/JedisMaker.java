import com.sun.tools.javac.Main;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * Creates the database and sets up database information that reads from a file called resources.
 */
public class JedisMaker {

    private int counter;

    private ArrayList<String> names;
    private Map<String, List<String>> map;

    public JedisMaker(){

    }

    public void init() {
        try {
            main();
        } catch (IOException e) {
            System.out.print("Exception " + e);
        }
    }

    //Makes a new jedis and connects to the server
    public static Jedis make() throws IOException {
        JedisMaker jm = new JedisMaker();

        // assemble the directory name
        String slash = File.separator;
        String filename = "resources" + slash + "redis_url.txt";
        URL fileURL = JedisMaker.class.getClassLoader().getResource(filename);
        String filepath = URLDecoder.decode(fileURL.getFile(), "UTF-8");

        // open the file
        StringBuilder sb = new StringBuilder();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(filepath));
        } catch (FileNotFoundException e1) {
            System.out.println("File not found: " + filename);
            return null;
        }

        // read the file
        while (true) {
            String line = br.readLine();
            if (line == null) break;
            sb.append(line);
        }
        br.close();

        // parse the URL
        URI uri;
        try {
            uri = new URI(sb.toString());
        } catch (URISyntaxException e) {
            System.out.println("Reading file: " + filename);
            System.out.println("It looks like this file does not contain a valid URI.");
            return null;
        }
        String host = uri.getHost();
        int port = uri.getPort();

        String[] array = uri.getAuthority().split("[:@]");
        String auth = array[1];

        // connect to the server
        Jedis jedis = new Jedis(host, port);

        try {
            jedis.auth(auth);
        } catch (Exception e) {
            System.out.println("Trying to connect to " + host);
            System.out.println("on port " + port);
            System.out.println("with authcode " + auth);
            System.out.println("Got exception " + e);
            return null;
        }
        return jedis;
    }

    /**
     * The main method to instantiate a jedis connection and filling it with information
     * @throws IOException
     */
    public void main() throws IOException {
        Jedis jedis = make();

        fillListUserNames(jedis);
        fillMap(jedis);

        jedis.close();
    }

    /**
     * Fills the data base with user names
     * @param jedis the jedis connection
     */
    public void fillListUserNames(Jedis jedis) {
        //Removes duplicates
        Set<String> duplicateRemove = new LinkedHashSet<String>();
        duplicateRemove.addAll(names);

        Iterator<String> iterator = duplicateRemove.iterator();
        List<String> newNames = new ArrayList<String>();
        while(iterator.hasNext()){
            newNames.add(iterator.next());
        }

        String combinedNames = "";
        for (int i = 0; i < newNames.size(); i++) {
            combinedNames += newNames.get(i) + " ";
        }

        jedis.rpush("Users", combinedNames);
    }

    /**
     * Test method to remove keys
     * @param jedis jedis connection
     */
    public void deleteListUserNames(Jedis jedis){
        jedis.del("Users");
    }

    /**
     * Gets the user nmames
     * @param jedis jedis connection
     * @return returns the string with information
     */
    public String getListUserNames(Jedis jedis){
        List<String> value = jedis.lrange("Users", 0, -1);

        Set<String> duplicateRemove = new LinkedHashSet<String>();

        String concatString = "";
        for(int i = 0; i < value.size(); i++){
            concatString += value.get(i);
        }

        Scanner scan = new Scanner(concatString);
        scan.useDelimiter(" ");
        while(scan.hasNext()){
            duplicateRemove.add(scan.next());
        }
        scan.close();


        Iterator<String> iterator = duplicateRemove.iterator();
        String iterString = "";
        while(iterator.hasNext()){
            iterString += iterator.next() + " ";
        }
        return iterString;
    }

    /**
     * Fills the map
     * @param jedis
     */
    public void fillMap(Jedis jedis) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String name = entry.getKey();
            List<String> stocks = entry.getValue();
            Set<String> duplicateRemove = new LinkedHashSet<String>();
            duplicateRemove.addAll(stocks);
            Iterator<String> iterator = duplicateRemove.iterator();
            List<String> newStocks = new ArrayList<String>();
            while(iterator.hasNext()){
                newStocks.add(iterator.next());
            }
            String combinedStocks = "";
            for (int i = 0; i < stocks.size(); i++) {
                combinedStocks += newStocks.get(i) + "-";
            }
            jedis.rpush(name, combinedStocks);
             }
        }

    /**
     * Test method to delete the map
     * @param jedis jedis connection
     * @param name gets the key
     */
    public void deleteMap(Jedis jedis, String name){
        jedis.del(name);
    }

    /**
     * Removes the item
     * @param jedis gets a jedis connection
     * @param name gets a name
     * @param removeValue the value to be removed
     */
    public void removeItem(Jedis jedis, String name, String removeValue){
        List<String> combinedStocks = jedis.lrange(name, 0, -1);
        jedis.del(name);
        System.out.println("These are the stocks coming in " + combinedStocks);
        String concatString = "";
        for(int i = 0; i < combinedStocks.size(); i++){
                concatString += combinedStocks.get(i);
        }
        Scanner scan = new Scanner(concatString);
        scan.useDelimiter("-");
        String newConcatString = "";
        while(scan.hasNext()){
            String value = scan.next();
            if(value.contains(removeValue)){

            }
            else{
                newConcatString += value + "-";
            }
        }
        scan.close();
        jedis.rpush(name, newConcatString);
    }

    /**
     * Checks the database if a value exists
     * @param jedis gets a jedis connection
     * @param name the key to be checked
     * @return
     */
    public boolean checkDatabases(Jedis jedis, String name){
        if(jedis.exists(name)){
            return true;
        }
        else{return false;}
    }

    /**
     * Gets the map and returns it in a string
     * @param jedis gets the jedis connection
     * @param name gets the key
     * @return returns information in a string
     */
    public String getMap(Jedis jedis, String name) {
        {
            List<String> value = jedis.lrange(name, 0, -1);

            Set<String> duplicateRemove = new LinkedHashSet<String>();

            String concatString = "";
            for(int i = 0; i < value.size(); i++){
                concatString += value.get(i);
            }

            Scanner scan = new Scanner(concatString);
            scan.useDelimiter("-");
            while(scan.hasNext()){
                duplicateRemove.add(scan.next());
            }
            scan.close();


            Iterator<String> iterator = duplicateRemove.iterator();
            String iterString = name+": ";
            while(iterator.hasNext()){
                iterString += iterator.next() + " ";
            }



            if(iterString.equals("")){
                System.out.println("it equals nothing" + iterString);
            }
            return iterString;


        }
    }

    /**
     * Sets the map
     * @param map the map to be set
     */
    public void setMap(Map<String, List<String>> map){ {
            this.map = map;
        }
    }

    /**
     * Sets the names
     * @param names the names to be set
     */
    public void setNames(ArrayList<String> names){
        this.names = names;
    }
  }

