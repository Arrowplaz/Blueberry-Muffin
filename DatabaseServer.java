import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The class for the databases being used in the Giant Scale Service
 * Besides containing all our data, has the ability to send and recieve data to and from 
 * front end servers and other databases
 */
public class DatabaseServer { 
  /**
   * The primary data structure meant to the data
   */
  private static Map<String, ArrayList<Book>> database = new HashMap<String, ArrayList<Book>>();

  /**
   * A getter method to determine which categories (keys) this database contains
   * @return
   */
  private static Set<String> getCategories(){
    return database.keySet();
  }

  
  /**
   * 
   * @param genre
   * @return
   */
  private ArrayList<Book> GET(String genre){
    if(database.keySet().contains(genre)){
      return database.get(genre);
    }
    else return null;
  }


  public static boolean DELETE(String genre){
    if(database.keySet().contains(genre)){
      database.remove(genre);
      return true;
    }
    return false;
  }

  public static boolean ADD(String genre, Book newBook){
    if(database.keySet().contains(genre)){
      database.get(genre).add(newBook);
      return true;
    }
    return false;
  }

  private void sendData(String frontendIP){
    final int PORTNUMBER = 8412;
    //Create client
    //Send over data
    //Recieve either a success or failure
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    XmlRpcClient client = null;
    try {
      config.setServerURL(new URL("http://" + frontendIP + ":" + PORTNUMBER));
      client = new XmlRpcClient();
      client.setConfig(config);
    } catch (Exception e) {
      System.err.println("Client exception: " + e);
    }
    
    List<String> params = new ArrayList<>();
    params.add("PLACEHOLDER");

    try{
      Boolean result = client.execute("Database.recieveData", params.toArray());
    }
    catch(Exception e){
      System.err.println("Client exception: " + e);
    }
  }


  public void recieveData(String category, ArrayList<Book> incoming){
    database.put(category, incoming);
  }
  /**
   * The main method
   */
  public static void main(String[] args) {
    if (args.length != 0) {
      System.out.println("Usage: No arguments");
      return;
    }

    try {
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      XmlRpcServer xmlRpcServer;
      WebServer server = new WebServer(8412);
      xmlRpcServer = server.getXmlRpcServer();
      phm.addHandler("Database", DatabaseServer.class);
      xmlRpcServer.setHandlerMapping(phm);
      server.start();
      System.out.println("XML-RPC server started");
      
    } catch (Exception e) {
      System.err.println("Server exception: " + e);
    }
  }
}