import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.ArrayList;
import java.io.File;
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
   * The entry point into the System defined in startup
   */
  private static String entryPoint = null;

  /**
   * The IP address of the optimal front end
   * as defined by our region smart selection
   */
  private static String optimalFrontEnd = null;


  /**
   * The region smart selection to choose the Frontend
   * with the best latency to the client
   */
  private static void regionSmartSelect(){
    //Create a client to the entry point
    XmlRpcClient entryClient = createClient(entryPoint);
    List<String> params = new ArrayList<>();


    //Retrieve the list of all frontends
    ArrayList<String> frontEnds;
    try{
      Object result = (Object) entryClient.execute("FrontEnd.getFrontEnd", params);
      
      frontEnds = (ArrayList<String>) result;
    }
    catch(Exception e){
      System.out.println("Could not get Frontends from entry point, ensure entry point is online");
      return;
    }

    //Keep track of the frontend with the best latency
    String bestFrontEnd = "";
    Long bestTime = Long.MAX_VALUE;

    //Iterate over all frontends
    for(String frontEnd: frontEnds){
      XmlRpcClient FEClient = createClient(frontEnd);
      try{
        //Start Time
        long startTime = System.nanoTime();
        String result = (String) entryClient.execute("FrontEnd.ping", params);
        long endTime = System.nanoTime() - startTime;
        if(endTime < bestTime){ //Compare total time to best
          bestTime = endTime;
          bestFrontEnd = frontEnd;
        }
      }
      catch(Exception e){
        System.out.println("Could not ping Front end: " + frontEnd);
      }
    }
    optimalFrontEnd = bestFrontEnd;
  }

  /**
   * An overload method to create a client using the optimal IP found
   * using our region checking
   * @return The client created using the IP of the best Frontend
   */
  public static XmlRpcClient createClient(){
    return createClient(entryPoint);
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
    String dataToBeSent = "";
    try{
      File file = new File(outgoingFile);
      Scanner fileReader = new Scanner(file);
      while(fileReader.hasNextLine()){
      dataToBeSent += fileReader.nextLine();
      }
    }
    catch(Exception e){
      System.out.println("Could not open and/or read Filepath: " + outgoingFile);
      return;
    }

      System.out.println(category);
      System.out.println(outgoingFile);
      System.out.println(dataToBeSent);

      XmlRpcClient client = createClient();
      List<String> params = new ArrayList<>();
      params.add(category);
      params.add(outgoingFile);
      params.add(dataToBeSent);
      params.add("YES");

      try{
        Boolean result = (Boolean) client.execute("FrontEnd.addItem", params.toArray());
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
  private static void lookupCategory(String Category, String Filename){
    System.out.println("STARTING LOOKUP");
    XmlRpcClient client = createClient();
    List<String> params = new ArrayList<>();
    params.add(Category);
    params.add(Filename);

    System.out.println(Category);
    System.out.println(Filename);

    try{
      System.out.println("Executing");
      Object result =  (Object) client.execute("FrontEnd.getItem", params.toArray());
      result = (String) result;
      System.out.println("SUCCESS");
      if(result != null){
        System.out.println("WE HAVE RESULT");
      }
      else{
        System.out.println("Could not get data");
      }
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
      Boolean result = (Boolean) client.execute("FrontEnd.addDatabase", params.toArray());
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
      System.out.println("Usage: [Entry Point]");
      return;
    }

    entryPoint = args[0];
    
    //Identify the best Frontend for this user
    //regionSmartSelect();

    //Take in commands from the user
     while(true){
      Scanner Scanner = new Scanner(System.in);  // Create a Scanner object
      System.out.println("Enter Function and Parameters");

      String[] cmdLineParse = Scanner.nextLine().split(" ");
      if(cmdLineParse.length < 2 || cmdLineParse.length > 3){
        System.out.println("Usage: [Function] [Params]");
        continue;
      }
      else{
          switch(cmdLineParse[0]){
        case "lookup":
          lookupCategory(cmdLineParse[1], cmdLineParse[2]);
          break;

        case "addFile":
          if(cmdLineParse.length != 3){
            System.out.println("addBook Usage: [Category] [File]");
            break;
          }
          addFile(cmdLineParse[1], cmdLineParse[2]);
          break;
        
        case "addDatabase":
          if(cmdLineParse.length != 3){
            System.out.println("addDatabase Usage: [Frontend IP] [Database IP]");
          }
          addDatabase(cmdLineParse[1], cmdLineParse[2]);
          break;

        default:
          System.out.println("Invalid function name");
          System.out.println("Functions: lookup, addBook, addDatabase");
          System.out.println("Please try again");
          // removed return here, we might want it back
      }
      }
      
    }


    
  }
}

