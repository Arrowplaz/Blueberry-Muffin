import java.util.List; 
import java.util.ArrayList; 
import java.net.URL;     
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
/**
 * A Java client to the online bookstore.
 */

/*
Add book
Lookup Book
Add
*/


public class Client {
  private static ArrayList<String> frontendServers;
  private final int PORTNUMBER = 8412;
  private static String frontendIP = "0";

  private static void intializeFrontEnd(){
    frontendServers = new ArrayList<>();
    frontendServers.add("123");
  }

  /**
   * The helper method to create a client
   * 
   * @param hostName: hostname of the client
   * @param portNum: the portnum of the client
   * 
   * @return the client
   */
  public XmlRpcClient createClient() {
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    XmlRpcClient client = null;
    try {
      config.setServerURL(new URL("http://" + frontendIP + ":" + PORTNUMBER));
      client = new XmlRpcClient();
      client.setConfig(config);
    } catch (Exception e) {
      System.err.println("Client exception: " + e);
    }
    return client;
  }


  private static void findFrontEnd(){
  }

  private static void addBook(String genre){
    System.out.println("Not developed yet");
    return;
  }

  private static void lookupCategory(String Category){
  }

  private static void addDatabase(String ipAddress){
    
  }

  public static void main(String[] args) {
    intializeFrontEnd();

    if (args.length != 2) {
      System.out.println("Usage: [function name] [param]");
      return;
    }
    
    String functionName = args[0];
    
    switch(functionName){
      case "lookupCategory":
        lookupCategory(args[1]);
        break;

      case "addBook":
        addBook(args[1]);
        break;
      
      case "addDatabase":
        addDatabase(args[1]);
        break;

      default:
        System.out.println("Invalid function name");
        System.out.println("Functions: lookupCategory, addBook, addDatabase");
        return;
    }
  }
}

