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
  /**
   * The list of categories in the region
   */
  private static ArrayList<String> categories = new ArrayList<String>();

  /**
   * The list of databases withing the region
   */
  private static ArrayList<String> databases = new ArrayList<>();

  /**
   * A list of the other frontends in the system
   */
  private static ArrayList<String> otherFrontEnds = new ArrayList<>();

  /**
   * A boolean to tell if repartioning is needed
   */
  private static Boolean repartitionNeeded = false;
  
  /**
   * The magic number for the port; standardized across the project
   */
  private static final int PORTNUMBER = 8413;


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
   * This method is a getter for the other front ends list
   * Used by client to get frontends from the entrypoint
   * 
   * Is non inclusive of current frontend to prevent optimal front end 
   * and entry point from being the same
   * 
   * @return the List of other frontends
   */
  public ArrayList<String> getFrontEnds(){
    return otherFrontEnds;
  }

  /**
   * The helper method to create a client
   * 
   * @param ip The ip address for the client
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
   *  Helper for remove repartioning, which happens on addFiles
   * 
   * @param newDatabases the list of new Databases
   * @param oldHash the old database hash
   * @param newHash the new has number
   * @param category the category being moved
   * @param delete a string whether or not to delete, "Yes" or "No"
   */
  private void repartitionHelper(List<String> newDatabases, int oldHash, int newHash, String category, String delete) {
    String oldDb = databases.get(oldHash);
    String newDb = newDatabases.get(newHash);

    try {
      XmlRpcClient client = createClient(oldDb);
      List<String> params = new ArrayList<String>();
      params.add(newDb);
      params.add(category);
      params.add(delete);

      client.execute("Database.sendCategory", params);
      System.out.println(category + " remapped from " + Integer.toString(oldHash) + ": " +
      oldDb + " to " + Integer.toString(newHash) + ": " + newDb);
      System.out.println("Find it there from now on!");
    }
    catch(Exception e) {
      System.out.println("(FronEnd, repartition) Client exception" + e);
    }

  }

  /**
   * A helper to repartion when both databases responsible for a category
   * fails
   * 
   * @param hashesToRemove an array of hashes that need to be removed
   */
  private void removeRepartition(int[] hashesToRemove) {
    ArrayList<String> newDatabases = new ArrayList<String>(databases);
    ArrayList<String> liveCategories = new ArrayList<String>();
    // remove bad machines
    newDatabases.remove(databases.get(hashesToRemove[0]));
    newDatabases.remove(databases.get(hashesToRemove[1]));
    
    synchronized(repartitionNeeded) {
      repartitionNeeded = false;
    }
    
    if (newDatabases.size() == 0) {
      synchronized (categories) {
        categories = new ArrayList<String>();
      }
      synchronized(databases) {
        databases = new ArrayList<String>();
      }
      System.out.println("No databases left! Everything cleared.");
      return;
    }

    for (String category : categories) {
      int[] prevHashes = hash(category, databases.size());
      int[] newHashes = hash(category, databases.size() - hashesToRemove.length);
      System.out.println("Looking at this cateogry: " + category);
      System.out.println("Old hash: (" + prevHashes[0] + ", " + prevHashes[1] +")");
      System.out.println("New Hash: (" + newHashes[0] + ", " + newHashes[1] +")");

      // if category doesn't exist, don't add it to live categories
      if (prevHashes[0] == hashesToRemove[0] && prevHashes[1] == hashesToRemove[1]) {
        continue;
      }
      // the category exists on at least one machine
      liveCategories.add(category);
     
      if (newDatabases.size() == 1){
        // no need to send data anywhere if there's only one db
        continue;
      }
       // if the first machine is down
      if(prevHashes[0] == hashesToRemove[0] || prevHashes[0] == hashesToRemove[1]) {
        // second machine, send categories to these new machines
        // if the second machine is down, give up, can't take care of all failures
        // only delete the category on your second send
        if (prevHashes[1] != newHashes[0]) {
          repartitionHelper(newDatabases, prevHashes[1], newHashes[0], category, "NO"); 
        }
        if (prevHashes[1] != newHashes[1]){         
          repartitionHelper(newDatabases, prevHashes[1], newHashes[1], category, "YES");
        }

      }
      // if the second machine was down
      if(prevHashes[1] == hashesToRemove[0] || prevHashes[1] == prevHashes[1]) {
        // first machine you send categories to these new machines
        if (prevHashes[0] != newHashes[0]) {
          repartitionHelper(newDatabases, prevHashes[0], newHashes[0], category, "NO"); 
        }

        if (prevHashes[0] != newHashes[1]) {
          repartitionHelper(newDatabases, prevHashes[0], newHashes[1], category, "YES");
        }
      }
      else {
        // else, both machines send your categories to the new hash and delete your folders
        // to make capacity
        if (prevHashes[0] != newHashes[0]) {
          repartitionHelper(newDatabases, prevHashes[0], newHashes[0], category, "YES"); 
        }
        if (prevHashes[1] != newHashes[1]) {
          repartitionHelper(newDatabases, prevHashes[1], newHashes[1], category, "YES");
        }
      }
    }
    System.out.println("printing new categories list...");
    synchronized(categories) {
      categories = new ArrayList<String>(liveCategories);
    }
    System.out.println(categories.toString());
    synchronized(databases) {
      databases = new ArrayList<String>(newDatabases);
    }   
  }

  /**
   * A method to repartion when a DB joins
   */
  private void addRepartion(){
    // go through categories
    for (int i = 0; i < categories.size(); i++) {
      String category = categories.get(i);
      System.out.println("Looking at new category: " + category);

      int[] oldHashes = hash(category, databases.size() - 1);
      int[] newHashes = hash(category, databases.size());

      if (oldHashes.equals(newHashes)) {
        continue; 
      }

      for (int j = 0; j < oldHashes.length; j++) {
        // if the first two aren't the same
        int oldHash = oldHashes[j];
        int newHash = newHashes[j];
        // if this hash is one of the new hashes, no need to delete the category folder
        if (oldHash == newHashes[0] || oldHash == newHashes[1]) {
          // send over if it's different
          if (oldHash != newHash) {
            repartitionHelper(databases, oldHash, newHash, category, "NO");
          }
        }
        // this hash wasn't one of new hashes, go ahead and delete it
        else if (oldHash != newHash){
          repartitionHelper(databases, oldHash, newHash, category, "YES");
        }
      }
    }
    return;
  }

  // hash function, is send string to hash ---> hash sends out 
  public int[] hash(String category, int numMachines){
    // need a hash that spits out 2 numbers modded over the number of machines
    int hash1 = Math.abs(category.hashCode());
    int hash2 = hash1 + 1;
    int[] hashes = {hash1%numMachines, hash2%numMachines};

    return hashes;
  }
  
  /**
   * A method called by a database when joining the region
   * 
   * @param ipAddress the IP of the database joining
   */
  public Boolean addDatabase(String ipAddress){
    System.out.println(ipAddress + " wants to be added");
    // check if database already in 
    if (databases.contains(ipAddress)){
      return false;
    }
    System.out.println("about to add Database");
    databases.add(ipAddress); 
    System.out.println("after add size: " + databases.size());
    // do we need to notify the database at all or?
    // fault tolerance all the way
    addRepartion();
    System.out.println("Successfully added!");
    return true;
  }

  /**
   * The method to delete an item from the region
   * 
   * @param category the category of the item
   * @param fileName the filenam
   * @param leader a string indicating whether or not this is a leader, "YES" or "NO"
   * 
   * @return true if successful or false if not
   */
  public Boolean deleteItem(String category, String fileName, String leader){
    if (databases.size() == 0){
      // or a string saying add a database... ?
      return false; 
    }
    ArrayList<String> liveFrontEnds = new ArrayList<String>();
    System.out.println("Deleting item...");

    List<String> params = new ArrayList<String>();
    params.add(category);
    params.add(fileName);
    String delResult = addDeleteHelper("delete", category, fileName, params); 
    if (delResult.equals("false")){
      return false;
    } 
    // By this point either deleting was successful, or the databases in charge of
    // the category are down, either way forward the requests to the other frontEnds

    if (leader.equals("NO")) {
      System.out.println("Not coordinator, not deleting from other FrontEnds...");
      return true;
    }

    System.out.println("About to delete items from other FrontEnds");
    System.out.println("otherFrontEnds size: " + otherFrontEnds.size());
    //leading to concurrency modication errors, so placing synchronized
    // synchronized(otherFrontEnds) {
    params.add("NO");
    for (int i = 0; i < otherFrontEnds.size(); i ++) {
      String frontEndIp = otherFrontEnds.get(i);
      System.out.println("Deleting item from FE: + " + frontEndIp);
      try {
        XmlRpcClient frontEndClient = createClient(frontEndIp);
        // use the same params
        frontEndClient.execute("FrontEnd.deleteItem", params);
        liveFrontEnds.add(frontEndIp);
        System.out.println("successfully deleted from frontEnd above");
      } catch (Exception e) {
        System.out.println("failed to addItem to " + frontEndIp);
        System.out.println("(FrontEnd, deleteItem) " + e);
      }
    }
    // update the value of otherFrontEnds to only include live 
    // front ends
    synchronized(otherFrontEnds) {
      otherFrontEnds = new ArrayList<String>(liveFrontEnds);
    }
    System.out.println("number of alive frontEnds " + otherFrontEnds.size());
    return true;
  }
  

  /**
   * A helper that does both adds and deletes, checks if both replications are down to repartition
   * 
   * 
   * @param method the method occuring
   * @param category the category of the file
   * @param fileName the name of the file
   * @param params the params for the method being called
   * 
   * @return "true" or "false" or "repartition" depending on success, failure, or repartition needed
   */
  private String addDeleteHelper(String method, String category, String fileName, List<String> params) {
    int[] hashes = hash(category, databases.size());
    int index1 = hashes[0];
    int index2 = hashes[1];
    String[] dbs = {databases.get(index1), databases.get(index2)};
    Boolean firstOffline = false;
    // loop though databases
    for (int i = 0; i < dbs.length; i++) {
      // if you only have one database for some reason, then only add once
      // since hash will spit out (0, 0)
      if (i == 1 && index1 == index2 && !firstOffline) {
        return "true";
      }
      // if your one database is offline, return false
      else if (i == 1 && index1 == index2 && firstOffline) {
        // there should be nothing in categories or databases
        // also synchronize categories?
        synchronized(databases) {
          databases = new ArrayList<String>();
          categories = new ArrayList<String>();
        }
        // offline
        return "false";
      }
      String db = dbs[i];
      System.out.println("sending to db: " + db);
      XmlRpcClient client = createClient(db);

      try {
        if (method.equals("delete")) {
          String result = (String) client.execute("Database.deleteItem", params.toArray());
          if(result.equals("false")){
            System.out.println("Unable to delete from DB");
            return "false";
          }
          else if(result.equals("delete")){
            System.out.println("Category: " + category + " is empty, deleting it");
            synchronized(categories) {
              categories.remove(category);
            }
          }
        }
        else {
          Boolean result = (Boolean) client.execute("Database.addItem", params);
          // database addition went wrong for some reason
          if (!result) {
            return "false";
          }
          if (!categories.contains(category)) {
            categories.add(category);
          }
        }

      } catch (Exception e) {
        if (i == 0) {
          firstOffline = true;
        }
        // if both machines are down
        if (i == 1 && firstOffline) {
          System.out.println("Database unaccessible: " + e);
          synchronized (repartitionNeeded){
            repartitionNeeded = true;
          }
          // synchronize such that only one process can come to this at
          // a time
          // to avoid race conditions where two clients try to
          // reparition at the same tike
          synchronized (repartitionNeeded) {
            if (repartitionNeeded) {
              removeRepartition(hashes);
            }
          }
          return "repartition";
        }
      }
    }
    return "true";
  }

  /**
   * This method adds an item into the region
   * 
   * @param category the category of the file
   * @param fileName the name of the file
   * @param contents the contents of the file
   * @param leader "Yes" or "No" depending on if it is a leader
   */
  public Boolean addItem(String category, String fileName, String contents, String leader){
    if (databases.size() == 0){
      return false; 
    }
    
    System.out.println("Adding item...");
    // params for adding to Databases and, later, other frontEnds
    List<String> progressParams = new ArrayList<String>();
    List<String> params = new ArrayList<String>();
    params.add(category);
    params.add(fileName);
    params.add(contents);

    String addResult = addDeleteHelper("add", category, fileName, params);
    if (addResult.equals("repartition")){
      addItem(category, fileName, contents, leader);
    }
    // fails for some reason 
    else if (addResult.equals("false")){
      return false;
    }

    ArrayList<String> liveFrontEnds = new ArrayList<String>();
    // to avoid a recursive add where they all add to each other
    if (leader.equals("NO")) {
      System.out.println("Not coordinator, not adding to other FrontEnds...");
      
      return true;
    }

    System.out.println("About to add items to other FrontEnds");
    System.out.println("otherFrontEnds size: " + otherFrontEnds.size());

    params.add("NO");
    // using for int i loop to avoid iterator concurrency issues
    for (int i = 0; i < otherFrontEnds.size(); i ++) {
      String frontEndIp = otherFrontEnds.get(i);
      System.out.println("Adding item to FE: + " + frontEndIp);

      try {
        XmlRpcClient frontEndClient = createClient(frontEndIp);
        // use the same params
        frontEndClient.execute("FrontEnd.addItem", params);
        liveFrontEnds.add(frontEndIp);
        System.out.println("successfully added to frontEnd above: " + frontEndIp);
      } catch (Exception e) {
        System.out.println("failed to addItem to " + frontEndIp);
        System.out.println("(FrontEnd, addItem) " + e);
      }
    }
    // update the value of otherFrontEnds to only include live 
    // front ends
    synchronized(otherFrontEnds) {
      otherFrontEnds = new ArrayList<String>(liveFrontEnds);
    }
    System.out.println("number of alive frontEnds " + otherFrontEnds.size());
    return true;
  }

  /**
   * This method returns a file from the database
   * 
   * @param category the category of the file
   * @param fileName the name of the file
   * 
   * @return the contens of the file as a string
   */
  public String getItem(String category, String fileName) {
    System.out.println("Size of database: " + databases.size());
    int[] hashes = hash(category, databases.size());
    String database1 = databases.get(hashes[0]);
    String database2 = databases.get(hashes[1]); 

    XmlRpcClient client1 = createClient(database1);
    XmlRpcClient client2 = createClient(database2);

    List<String> params = new ArrayList<String>();
    params.add(category);
    params.add(fileName);

    System.out.println("This is the category: " + category);
    System.out.println("This is the fileName: " + fileName);
    System.out.println("This is the database ip: " + database1);

    try{
      String result = (String) client1.execute("Database.getItem", params);
      return result;
    }
    catch (Exception e){
      System.out.println("Failed to get file from Database1: " + e);
      try {
        String result = (String) client2.execute("Database.getItem", params);
        return result;  
      }
      catch(Exception err) {
        System.out.println("Failed to get file from Database2 too: " + err);
      }
      return "";
    } 
  }

  /**
   * A method by a frontEnd to add other frontEnds. Called by acceptFrontEnd
   * to update every other machine of a new frontEnd joiing.
   * 
   * @param ipAddress the address of the new frontend
   * 
   * @return true if successful or false if not
   */
  public Boolean addFrontEnd(String ipAddress) {
    otherFrontEnds.add(ipAddress);
    System.out.println("(add FrontEnd, Amount of frontEnds now: " + otherFrontEnds.size());
    return true;
  }

  public List<String> acceptFrontEnd(String newFrontEndIp) {
    System.out.println("inside acceptFrontEnd");
    List<String> frontEnds = new ArrayList<String>(otherFrontEnds);
    // same params 
    List<String> params = new ArrayList<String>();
    params.add(newFrontEndIp);
    // synchronized(otherFrontEnds) {
    for (int i = 0; i < otherFrontEnds.size(); i ++) {
      String frontEnd = otherFrontEnds.get(i);
      XmlRpcClient client = createClient(frontEnd);

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
        System.out.println("Cannot contact frontEnd: " + frontEnd);
        System.out.println(e);
        otherFrontEnds.remove(frontEnd);
      }
    } 
    // Lastly, add to own list first
    otherFrontEnds.add(newFrontEndIp);
    System.out.println("(Accept frontEnd) Amount of frontEnds now: " + otherFrontEnds.size());
    // return the arraylist without the newIp's own IP
    return frontEnds;
  }
  // }

  /**
   * A helper method called at startup to join the system from an existing frontEnd
   * 
   * @param froneEndIp my own IP address
   * @param entryPoint the entry point IP address
   * 
   * @return true if successful or false if not
   */
  private static Boolean joinFrontEnd(String frontEndIp, String entryPoint) {
    XmlRpcClient client = createClient(entryPoint);
    List<String> params = new ArrayList<String>();
    params.add(frontEndIp);

    try{
      // gets list of other frontEnds from FrontEnd
      Object[] otherFE = (Object[]) client.execute("FrontEnd.acceptFrontEnd", params);
      if (otherFE != null) {
        // add frontEnds to your frontEnds
        System.out.println("before loop");
        for (Object frontEnd : otherFE) {
          System.out.println(frontEnd);
          otherFrontEnds.add(frontEnd.toString());
        }
      // then add the IP to your own frontEndb
      otherFrontEnds.add(entryPoint);
      }
      return true;
    }
    catch(Exception e){
      System.out.println("Exception: " + e);
      System.out.println("Failed to get front ends from: " + entryPoint);
      return false;
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
        return;
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