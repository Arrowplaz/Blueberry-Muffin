import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
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
  private static final int PORTNUMBER = 8412;

    /**
   * The helper method to create a client
   * 
   * @param hostName: hostname of the client
   * @param portNum: the portnum of the client
   * 
   * @return the client
   */
  public static XmlRpcClient createClient(String ip) {
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    XmlRpcClient client = null;
    try {
      config.setServerURL(new URL("http://" + ip + ":" + PORTNUMBER));
      client = new XmlRpcClient();
      client.setConfig(config);
    } catch (Exception e) {
      System.err.println("Client exception: " + e);
    }
    return client;
  }


  /**
   * A getter method to determine which categories (keys) this database contains
   * @return
   */
  public static Set<String> getCategories(){
    return database.keySet();
  }
  
  /**
   * 
   * @param genre
   * @return
   */
  public ArrayList<Book> getCategory(String genre){
    if(database.keySet().contains(genre)){
      return database.get(genre);
    }
    else return null;
  }


  public static boolean deleteFile(String genre, String fileName){
    if(database.keySet().contains(genre)){
      database.remove(genre);
      return true;
    }
    return false;
  }

  public static boolean add(String genre, Book newBook){
    if(database.keySet().contains(genre)){
      database.get(genre).add(newBook);
      return true;
    }
    return false;
  }

  private static boolean joinDatabase(String databaseIp, String entryPoint) {
    XmlRpcClient client = createClient(entryPoint);
    List<String> params = new ArrayList<String>();
    params.add(databaseIp);

    try {
      Boolean result = (boolean) client.execute("FrontEnd.addDatabase", params.toArray());
      return true;
    }
    catch(Exception e){
      System.err.println("Client exception: " + e);
      return false;
    }
  }

  // eman note: I suspect that this will be, fileName, and a byte array
  // of the contents
  public void sendData(String frontendIP){
    final int PORTNUMBER = 8412;
    //Create client
    //Send over data
    //Recieve either a success or failure
    XmlRpcClient client = createClient(frontendIP);
    List<String> params = new ArrayList<>();
    params.add("PLACEHOLDER");

    try{
      Boolean result = (boolean) client.execute("Database.recieveData", params.toArray());
    }
    catch(Exception e){
      System.err.println("Client exception: " + e);
    }
  }

  // do we need to lock in repartitioning?
  public void sendToDatabase(String category, String databaseIp) {
    // send the files in the category to the database IP
  }

  // ArrayList<Book> will be a byte array for files
  public boolean recieveData(String category, ArrayList<Book> incoming){
    database.put(category, incoming);
    return true;
  }
  /**
   * The main method
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("USAGE: [Own Database IP] [FrontEnd entry point]");
      return;
    }

    if (joinDatabase(args[0], args[1])) {
      System.out.println("Successfully joined front-end with entry point: " + args[1]);
    }
    else {
      System.out.println("Database addition went wrong for some reason");
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