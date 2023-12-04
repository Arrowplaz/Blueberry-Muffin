import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.ArrayList; 
import java.net.URL;     
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
/**
 * A Java client to the online bookstore.
 */
public class Client {

  /*
   * The List of frontend servers
   */
  private static ArrayList<String> frontendServers;

  /**
   * A magic number for the port number being used
   * Standardized across all our classes
   */
  private final static int PORTNUMBER = 8412;

  /**
   * The cached frontend the client will be interacting with
   */
  private static String frontendIp = null;

  private static void intializeFrontEnd(){
    frontendServers = new ArrayList<>();
    //RPC call to master frontend to get all the front end servers
    //Then do a loop to figure out best latency 
    //Set the instance variable to that frontend
  }

  private static void findFrontEnd(){
    //Create clock
    //Do RPC call to each front end server
    //Check time on clock
    //Keep track of best time/server
    frontendIp = frontendServers.get(0);
  }

  /**
   * An overload method to create a client using the optimal IP found
   * using our region checking
   * @return The client created using the IP of the best Frontend
   */
  public static XmlRpcClient createClient(){
    return createClient(frontendIp);
  }

  /**
   * The helper method to create a client
   * 
   * @param IPaddress: The ip address of the client being created
   * 
   * @return the client that was built
   */
  public static XmlRpcClient createClient(String IPaddress) {
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    XmlRpcClient client = null;
    try {
      config.setServerURL(new URL("http://" + IPaddress + ":" + PORTNUMBER));
      client = new XmlRpcClient();
      client.setConfig(config);
    } catch (Exception e) {
      System.err.println("Client exception: " + e);
    }
    return client;
  }


  /**
   * The client side method to start the adding file process
   * @param category The category the file belongs to
   * @param outgoingFile The file itself
   */
  private static void addFile(String category, String outgoingFile){
      XmlRpcClient client = createClient();
      List<String> params = new ArrayList<>();
      params.add(category);
      params.add(outgoingFile);

      try{
        Boolean result = client.execute("FrontEnd.addBook", params.toArray());
        if(result){
          System.out.println("File: " + outgoingFile + " was Sucessfully added to"); 
        }
        else{
          System.out.println("Failed to add");
        }
      }
      catch(Exception e){
        System.err.println("Client exception: " + e);
      }
    }

  /**
   * The clientside method to start the lookup process
   * Prints the results in the console
   * @param Category The category being looked up
   */
  private static void lookupCategory(String Category){
    XmlRpcClient client = createClient();
    List<String> params = new ArrayList<>();
    params.add(Category);

    try{
      //FIX 
      ArrayList<String> result = client.execute("FrontEnd.addBook", params.toArray());
    }
    catch(Exception e){
      System.err.println("Client exception: " + e);
    }
  }

  /**
   * A "Admin" method that will add a database to a specifc region
   * 
   * @param frontendIP the IP of the frontend for the region we are adding the DB to
   * @param databaseIP the IP of the database being added
   */
  private static void addDatabase(String frontendIP, String databaseIP){
    XmlRpcClient client = createClient(frontendIP);
    List<String> params = new ArrayList<>();
    params.add(databaseIP);
    
    try{
      Boolean result = client.execute("FrontEnd.addDatabase", params.toArray());
      if(result){
        System.out.println("Database was successfully added to region: " + frontendIP );
      }
      else{
        System.out.println("Failed to add Database");
      }
    }
    catch(Exception e){
      System.err.println("Client exception: " + e);
    }
  }

  
  /**
   * The main method
   * @param args
   */
  public static void main(String[] args) {

    //Grab the initial Front end IP from terminal 
    if (args.length != 1) {
      System.out.println("Usage: [Master FrontEnd IP]");
      return;
    }

    frontendIp = args[0];
    
    //Identify the best Frontend for this user
    intializeFrontEnd();
    findFrontEnd();


     while(true){
      Scanner Scanner = new Scanner(System.in);  // Create a Scanner object
      System.out.println("Enter Function and Parameters");

      String[] cmdLineParse = Scanner.nextLine().split(" ");
      if(cmdLineParse.length != 3){
        System.out.println("Usage: [Function] [Param]");
        continue;
      }
      else{
          switch(cmdLineParse[0]){
        case "lookupCategory":
          lookupCategory(cmdLineParse[1]);
          break;

        case "addBook":
          addFile("genre", cmdLineParse[1]);
          break;
        
        case "addDatabase":
          addDatabase(cmdLineParse[1], cmdLineParse[2]);
          break;

        default:
          System.out.println("Invalid function name");
          System.out.println("Functions: lookupCategory, addBook, addDatabase");
          return;
      }
      }
      
    }


    
  }
}

