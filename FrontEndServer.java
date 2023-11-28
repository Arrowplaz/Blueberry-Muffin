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
  // some way to partition genres --> first we're starting off with 10 genres
  // assign database IP to genres
  // add method 
  // INTIAL dataload 
  
  // list machines to the genre they are in charge of
  private Map<String, ArrayList<String>> dbToGenre = new HashMap<String, ArrayList<String>>(); 
  private static ArrayList<String> databases = new ArrayList<>();
  private static ArrayList<String> otherFronteEnds = new ArrayList<>();

  private void repartion(){
    return;
  }
  
  private void addDatabase(String ipAddress, String key){
    if(!databases.contains(ipAddress)){
      databases.add(ipAddress);
      // some sort if timer or something
      repartion();
    }
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
    if (args.length != 2) {
      System.out.println("Usage: [catalog server host name] [order server host name]");
      return;
    }
    // get ip address of first database 
    
    // pass in other frontend servers

    // pass in databases

    // 
    catalogHostName = args[0];
    orderHostName = args[1];

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

