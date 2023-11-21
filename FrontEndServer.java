import java.util.List; 
import java.util.ArrayList; 
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

  private static ArrayList<String> Region1 = new ArrayList<>();
  private static ArrayList<String> Region2 = new ArrayList<>();
  private static ArrayList<String> Region3 = new ArrayList<>();
  
  private void addIP(String ipAddress){
    int region = self.calcRegion(ipAddress);
    this.Region1.add(ipAddress);
  }

  private void calcRegion(String ipAddress){
    //Need implementation to decide what group ipAddress goes into
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

