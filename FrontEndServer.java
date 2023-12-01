import java.util.List; 
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;     
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
  // 
  // Frontend --> decrypt key to add servers to regions
  // manages one main region, and a list of other FrontEndIPs

  //
  // some way to partition categories --> first we're starting off with 10 categories
  // assign database IP to categories
  // add method 
  // INTIAL dataload 
  
  // list machines to the genre they are in charge of
  private static boolean dataLoaded = false;
  private static List<String> categories = new ArrayList<String>();
  private static Map<String, ArrayList<String>> categoryToDb = new HashMap<String, ArrayList<String>>(); 
  private static ArrayList<String> databases = new ArrayList<>();
  private static ArrayList<String> otherFronteEnds = new ArrayList<>();

  // clients call this to send as small package/boolean
  public boolean heartbeat() {
    return true;
  }
  private void repartion(){
    return;
  }
  
  public void addDatabase(String ipAddress, String key){
    if(!databases.contains(ipAddress)){
      databases.add(ipAddress);
      // some sort if timer or something
      // repartition the keys based so 
      repartion();
    }
  }
  

  public boolean addCategory(){
    return true;
  }
  
  public boolean addBook(){
    return true;
  }

  public static void initialDataLoad() {
    // assign categories to databases
    // divide caterogies with the number of dbs 

    // randombly create ids and files for category, this is random access
    
    // send files to respective database for category --> ask barker 

    // send the data to each database in other regions, using the add categories (potentially)
    // and addbooks of other servers
  }

  private static void addCategories(){
    categories.add("Fantasy");
    categories.add("Mystery");
    categories.add("Action");
    categories.add("Self Help");
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

  /**
   * The main method
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Usage: [database 1 IP] [database 2 IP] ...");
      return;
    }
    // pass in other frontend servers
    otherFronteEnds.add("blah");
    otherFronteEnds.add("blue");

    // add database IPs to be added to frontend
    for (String databaseIp: args) {
      databases.add(databaseIp);
    }
    addCategories();
    
    

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

