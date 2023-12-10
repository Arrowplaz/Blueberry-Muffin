import java.util.List; 
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;     
import java.lang.Math;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.server.XmlRpcServer;
import java.util.Scanner;

/**
 * The Front End Server of the project
 */
public class FrontEndServer { 
  // The FrontEnd needs to
  // Concerns: what happens when we are adding two databases at the same time --> don't make it concurrent
  // 1. be able to add frontEnd servers, add FrontEnd to known IP flow (ip --> FE --> sends message to ip) - 
  // unsure what the flow should be 
  // 2. add databases to regions
  // 3. send files to databases
  // FrontEnd server information 
  // 4. repartition machines once there are more databases

  // Testing things in the FrontEnd: 
  // 1. how the hashfunction is/how evenly spread
  // 2. smart latency
  // 3. fault tolerance 
  // What do we want to see?
  
  // list machines to the genre they are in charge of
  private static List<String> categories = new ArrayList<String>();
  // instead have a hashfunction with the number of databases
  private static ArrayList<String> databases = new ArrayList<>();
  private static ArrayList<String> otherFrontEnds = new ArrayList<>();
  private static final int PORTNUMBER = 8412;


  /**
   * A method used to ping a frontend and recieve a package
   * Used for region smart selection
   * 
   * @return the string "Ping"
   */
  public String ping(){
    //A magic String, can be anything
    return "Ping";
  }

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

  // repartition? when? --> when new database joins
  // should this be static?
  private void repartion(){
    // how would repartitioning work 
    // hash with old values and send to different systems
    // hopefully it's uniform enough that it doesn't overload the system
    // Should this server be unavailable during repartitions? yes? --> ask barker 
    for (String category: categories) {
      int oldHash = hash(category, databases.size() - 1);
      int newHash = hash(category, databases.size());
      if (oldHash != newHash) {
        // DO RPC call for the old machine to send data to the new machine
        // and delete its key
        System.out.println(category + " remapped from " + Integer.toString(oldHash) + " to " 
        + Integer.toString(newHash));
        System.out.println("Find it there from now on!");
      }
    }
    return;
  }

  // hash function, is send string to hash ---> hash sends out 
  public int hash(String category, int numMachines){
    // need a hash that spits out 2 numbers modded over the number of machines

    int hash = hashCode();
    return hash%numMachines;
  }
  
  
  public Boolean addDatabase(String ipAddress){
    System.out.println(ipAddress + " wants to be added");
    // check if database already in 
    if (databases.contains(ipAddress)){
      // might want to just return true here
      return false;
    }
    System.out.println("about to add Database");
    databases.add(ipAddress); 
    System.out.println("after add size: " + databases.size());
    // do we need to notify the database at all or?
    // fault tolerance all the way
    repartion();
    System.out.println("Successfully added!");
    return true;
  }

  public void removeFrontEnds() {
    return;
  }
  
  public Boolean addItem(String category, String fileName, String contents){
    int index = hash(category, databases.size());
    List<String> deadFrontEnds = new ArrayList<String>();

    XmlRpcClient client = createClient(databases.get(index));
    List<String> params = new ArrayList<String>();
    params.add(category);
    params.add(fileName);
    params.add(contents);

    // in addition to this, send requests to all other frontEnds
    try {
      Boolean result = (Boolean) client.execute("Database.addItem", params);
      return result;
    } catch (Exception e) {
      System.out.println("(FrontEnd, addItem) Client Exception: " + e);
    }
    
    for (String frontEndIp: otherFrontEnds) {
      try {
        client.execute("FrontEnd.addItem", params);
      } catch (Exception e) {
        System.out.println("failed to addItem to Database to " + frontEndIp);
        // then remove the frontEnd from your list
        deadFrontEnds.add(frontEndIp);
      }
    }
    // remove
    for (String deadFrontEnd : deadFrontEnds) {
      otherFrontEnds.remove(deadFrontEnd);
    }
    // call an RPC call with databases[index]
    return true;
  }

  /*
   * this function may not be needed
   */
  public String getItem(String category, String fileName) {
    System.out.println("Size of database: " + databases.size());
    int index = hash(category, databases.size());
    XmlRpcClient client = createClient(databases.get(index));
    List<String> params = new ArrayList<String>();
    params.add(category);
    params.add(fileName);


    System.out.println("This is the category: " + category);
    System.out.println("This is the fileName: " + fileName);
    System.out.println("This is the database ip: " + databases.get(index));

    try{
      String result = (String) client.execute("Database.getItem", params.toArray());
      return result;
    }
    catch (Exception e){
      System.out.println("Failed to get file from Database");
      // remove database from databases since it's not accesible
      databases.remove(databases.get(index));
      return "";
    } 
  }

  public List<String> getFrontEnds(){
    return otherFrontEnds;
  }

  public Boolean addFrontEnd(String ipAddress) {
    // failure case: when the ipAddress already exists?
    otherFrontEnds.add(ipAddress);
    return true;
  }

  public List<String> acceptFrontEnd(String newFrontEndIp) {
    // may lead to an issue, may need to copy instead of merely assign
    System.out.println("inside acceptFrontEnd");
    List<String> frontEnds = otherFrontEnds;

    for (String frontEnd : otherFrontEnds) {
      XmlRpcClient client = createClient(frontEnd);
      List<String> params = new ArrayList<String>();
      try{
        // we can incorporate a logging thing to make debugging easier, but
        // this shouldn't happen 
        Boolean result = (Boolean) client.execute("FrontEnd.addFrontEnd", params);
        if (result) {
          System.out.println("Successfully added new FrontEnd to " + frontEnd);
        } 
        else{
          // theoretically this code block should never be accessed
          System.out.println("Unsuccessfully tried to add new IP to FrontEnd " + frontEnd);
        }
      }
      catch(Exception e){
        // remove the FrontEnd from frontEnds (eventually other frontEnds will know that one
        // is down) --> how? --> only when adding a File, perhaps we should send it to 
        // 
       otherFrontEnds.remove(frontEnd);
      }
     // Lastly, add to own list first
      otherFrontEnds.add(newFrontEndIp);

    } 
    // return the arraylist without the newIp's own IP
    return frontEnds;
  }

  public static Boolean joinFrontEnd(String frontEndIp, String entryPoint) {
    // RPC call to accept frontEnds
    // needs to send its IP and the entryPoint IP
    XmlRpcClient client = createClient(entryPoint);
    List<String> params = new ArrayList<String>();
    params.add(frontEndIp);

    try{
      Object[] otherFE = (Object[]) client.execute("FrontEnd.acceptFrontEnd", params);
      if (otherFE != null) {
        // add the entry point to your frontEnds
        System.out.println("before loop");
        for (Object frontEnd : otherFE) {
          System.out.println(frontEnd);
          otherFrontEnds.add(frontEnd.toString());
        }
        
      return true;
      }
      return false;
    }
    catch(Exception e){
      System.out.println("Exception: " + e);
      System.out.println("Failed to get front ends from: " + entryPoint);
      return false;
    } 
  }


  public String lookupCategory(String category){
    System.out.println("STARTING LOOKUP");
    int index = hash(category, databases.size());
    XmlRpcClient client = createClient(databases.get(index));
    List<String> params = new ArrayList<>();
    params.add(category);

    try{
      System.out.println("Executing");
      String result = (String) client.execute("Database.getCategories", params);
      System.out.println("Success");
      if(result != null){
        return result;
      }
      else{
        return null;
      }
    }
    catch(Exception e){
      System.err.println("Front end failure" + e);
      return null;
    }
  }


  /**
   * The main method
   */
  public static void main(String[] args) {
    if (args.length != 0 && args.length != 2) {
      System.out.println("USAGE: [Own front-end Ip] [Other FrontEnd Server]");
      return;
    }

    if (args.length != 0) {
      if (joinFrontEnd(args[0], args[1])) {
        System.out.println("Successfully joined front-end with entry point: " + args[1]);
        System.out.println("Please add Database[s]");
      }
      else {
        // we have to deal with this failure, let's say
        // some of the frontEnds get added the new frontEnd but not all of the
        System.out.println("FrontEnd addition went wrong for some reason");
      }
    }

    try {
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      XmlRpcServer xmlRpcServer;
      WebServer server = new WebServer(PORTNUMBER);
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
