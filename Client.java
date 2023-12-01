import java.util.List;
import java.util.Vector;
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
  private static String frontendIp = "0";

  private static void intializeFrontEnd(){
    frontendServers = new ArrayList<>();
    frontendServers.add("54.241.136.136"); //California
    frontendServers.add("15.161.56.73"); //Milan
    frontendServers.add("13.245.150.107"); //Capetown
    frontendServers.add("54.180.122.185"); //Seoul
  }

  /**
   * The helper method to create a client
   * 
   * @param hostName: hostname of the client
   * @param portNum: the portnum of the client
   * 
   * @return the client
   */
  public static XmlRpcClient createClient(String frontendIP) {
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
    frontendIp = frontendServers.get(0);
  }

  private static void addBook(String genre, String bookName){
    for(String frontEnd: frontendServers){
      XmlRpcClient client = createClient(frontEnd);
      List<String> params = new ArrayList<>();
      params.add(genre);
      params.add(bookName);

      try{
        Boolean result = (Boolean)client.execute("FrontEnd.addBook", params.toArray());
        if(result){
          System.out.println("Book: " + bookName + " was Sucessfully added to " + frontEnd); 
        }
        else{
          System.out.println("Failed to add to " + frontEnd);
        }
      }
      catch(Exception e){
        System.err.println("Client exception: " + e);
      }
    }
  }

  private static void lookupCategory(String Category){
    if(frontendIp.equals("0")) findFrontEnd();
  }

  private static void addDatabase(String ipAddress){
    if(frontendIp.equals("0")) findFrontEnd();
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

