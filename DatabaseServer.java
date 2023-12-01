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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple example XML-RPC server program.
 */
public class DatabaseServer { 
  /**
   * The primary data structure meant to the data
   */
  private static Map<String, ArrayList<Book>> database = new HashMap<String, ArrayList<Book>>();

  private static Set<String> getGenres(){
    return database.keySet();
  }

  
  private ArrayList<Book> GET(String genre){
    if(database.keySet().contains(genre)){
      return database.get(genre);
    }
    else return null;
  }


  public static void DELETE(String genre){
    if(database.keySet().contains(genre)){
      database.remove(genre);
    }
  }

  public static void ADD(String genre, Book newBook){
    if(database.keySet().contains(genre)){
      database.get(genre).add(newBook);
    }
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