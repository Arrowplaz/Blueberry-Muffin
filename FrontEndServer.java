import java.util.List; 
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;     
import java.lang.Math
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.server.XmlRpcServer;

/**
 * The Front End Server of the project
 */
public class FrontEndServer { 
  // Plan of action --> today we need to have multiple distributed machines (so two machines)

  // Frontend --> decrypt key to add servers to regions
  // manages one main region, and a list of other FrontEndIPs

  // some way to partition categories --> first we're starting off with 10 categories
  // assign database IP to categories
  // add method 
  // INTIAL dataload 
  
  // list machines to the genre they are in charge of
  private static boolean dataLoaded = false;
  private static List<String> categories = new ArrayList<String>();
  // instead have a hashfunction with the number of databases
  private static ArrayList<String> databases = new ArrayList<>();
  private static ArrayList<String> otherFronteEnds = new ArrayList<>();
  private final int PORTNUMBER = 8412;



  /**
   * The helper method to create a client
   * 
   * @param hostName: hostname of the client
   * @param portNum: the portnum of the client
   * 
   * @return the client
   */
  public XmlRpcClient createClient(String databaseIp) {
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    XmlRpcClient client = null;
    try {
      config.setServerURL(new URL("http://" + databaseIp + ":" + PORTNUMBER));
      client = new XmlRpcClient();
      client.setConfig(config);
    } catch (Exception e) {
      System.err.println("Client exception: " + e);
    }
    return client;
  }

  // hash function even work 
  // add 
  // clients call this to send as small package/boolean
  public boolean heartbeat() {
    return true;
  }

  private void repartion(){
    // how would repartitioning work 
    // hash with old vslue
    for (String category: categories) {
      int oldHash = hash(category, databases.size() - 1);
      int newHash = hash(category, databases.size());
      if (oldHash != newHash) {
        // RPC call for the old machine to send data to the new machine
        // and delete its key
      }
    }
    return;
  }

  // hash function, is send string to hash ---> hash sends out 
  public int hash(String category, int numMachines){
    // doing hashing
    // taken from https://stackoverflow.com/questions/16521148/string-to-unique-integer-hashing
    int result = 0;
    for (int i = 0; i < category.length(); i++) {
      // 250 is a magic number, it's somehow MAX_NUMBER from that above link
      result += Math.pow(27, 250 - i - 1)*(1 + category.charAt(i) - 'a');
    }
    return result%numMachines;
  }
  
  public void addDatabase(String ipAddress, String key){
    // check if database already in stuff?
    databases.add(ipAddress); 
    repartion();
    return;
  }
  
  public boolean addItem(String category, String contents){
    int index = hash(category, databases.size());
    XmlRpcClient client = createClient(databases.get(index));
    List<String> params = new ArrayList<String>();
    params.add(category);
    params.add(contents);

    try {
      String result = (String) client.execute("Database.addItem", params);//
      return result;
    } catch (Exception e) {
      return "(FrontEnd, search) Client Exception: " + e;
    }
    // call an RPC call with databases[index]
    return true;
  }

  public static void addCategories(String category){
    // make category a hashset?
    categories.add(category);
    return;
  }

  /**
   * The helper method to create a client
   * 
   * @param hostName: hostname of the client
   * @param portNum: the portnum of the client
   * 
   * @return the client
   */
  public XmlRpcClient createClient(String hostName, String portNum) {
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    XmlRpcClient client = null;
    try {
      config.setServerURL(new URL("http://" + hostName + ":" + portNum));
      client = new XmlRpcClient();
      client.setConfig(config);
    } catch (Exception e) {
      System.err.println("Client exception: " + e);
    }
    return client;
  }

  public boolean addFrontEnd(String ipAddress){
    otherFronteEnds.add(ipAddress);
    return true;
  }

  /**
   * The main method
   */
  public static void main(String[] args) {
    if (args.length != 0) {
      System.out.println("NO INPUTS");
      return;
    }

    
    try {
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      XmlRpcServer xmlRpcServer;
      WebServer server = new WebServer(9182);
      xmlRpcServer = server.getXmlRpcServer();
      phm.addHandler("FrontEnd", FrontEndServer.class);
      xmlRpcServer.setHandlerMapping(phm);
      server.start();
      System.out.println("FrontEnd server started");
    } catch (Exception e) {
      System.err.println("Server exception: " + e);
    }
  }
}
