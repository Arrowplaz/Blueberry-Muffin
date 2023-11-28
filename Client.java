import java.util.List; 
import java.util.ArrayList; 
import java.net.URL;     
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
/**
 * A Java client to the online bookstore.
 */
public class Client {
  private static String frontEndHostName;
  private static int frontEndPortNum = 9182;

  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: [front-end server host name] [function name] [param]");
      return;
    }
    
    frontEndHostName = args[0];
    String functionName = args[1];
    String param = args[2];

    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    XmlRpcClient client = null;
    try {
      config.setServerURL(new URL("http://" + frontEndHostName + ":" + frontEndPortNum));
      client = new XmlRpcClient();
      client.setConfig(config);
    } catch (Exception e) {
      System.err.println("Client exception: " + e);
    }

    List<String> params = new ArrayList<String>();
    params.add(functionName);
    params.add(param);

    try {
      String result = (String) client.execute("FrontEnd.takeInCommand", params);//What does the handler do
      System.out.println(result);
    } catch (Exception e) {
      System.err.println("Client exception: " + e);
    }  
  }
}

