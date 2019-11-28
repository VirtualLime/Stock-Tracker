import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class to parse a json response containing a stock price
 * Dependant on the json-simple package
 * (add as library the json-simple-1.1.1.jar file)
 *
 * This class came with the assignment, I use it to parse the json strings coming from the website
 */
public class StockPriceParser {

    /**
     * Parse out the price from a json string
     * @param jsonString the string containing price : value
     * @return the price
     */
    public static double parsePrice(String jsonString)  {
        double price = 0.0;
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            price = (Double) (jsonObject.get("price"));
        }catch(ParseException e) {
            System.out.println("Invalid JSON to parse");
            e.printStackTrace();
            return 0.0;
        }catch(NullPointerException e) {
            System.out.println("No JSON string passed in");
            e.printStackTrace();
            return -1;
        }
        return price;
    }
}
