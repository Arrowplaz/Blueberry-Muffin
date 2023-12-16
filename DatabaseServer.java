import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.file.Path;
import java.io.File;
import java.util.Scanner;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The class for the databases being used in the Giant Scale Service
 * Besides containing all our data, has the ability to send and recieve data to and from 
 * front end servers and other databases
 */
public class DatabaseServer { 
  /**
   * The port number being used
   * Standardized across our project
   */
  private static final int PORTNUMBER = 8413;

  /**
   * The location of the database's working directory
   * used for accessing the files
   */
  private static String workingDir;

  /**
   * A dummy object used to run object locks
   */
  private static Object objectLock = new Object();

  /**
   * A helper method to create a client
   * 
   * @param ip: The ip Address of the client
   * 
   * @return the client
   */
  public static XmlRpcClient createClient(String ip) {
    System.out.println("http://" + ip + ":" + PORTNUMBER);
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
   * A helper method to open a file and get its contents
   * 
   * @param filePath the file being opened
   * 
   * @return the contents as a string
   */
  private String getFileContents(String filePath) {
    try{
      StringBuilder fileContents = new StringBuilder();
      File currFile = new File(filePath);
      Scanner fileReader = new Scanner(currFile);
      while(fileReader.hasNextLine()){
        fileContents.append(fileReader.nextLine());
        fileContents.append("\r\n");
      }
      fileReader.close();
      return fileContents.toString(); 
    }
    
    catch(Exception e){
      System.out.println("Database Error: " + e);
      return "";
    }
  }

  /**
   * Using a category and fileName, converts the file to a string for sending
   * 
   * @param category the category of the file
   * @param fileName the name of the file
   * 
   * @return 0 + the contents of a file as a String or 1 for failure
   */
  public String getItem(String category, String fileName) {
    String filePath = workingDir + "/Database/" + category + "/" + fileName;

    File file = new File(filePath);
    if(file.isFile()){
      return "0" + getFileContents(filePath);
    }
    else{
      return "1";
    }
  }

  /**
   * The method to add an Item to the system
   * 
   * @param category the category of the file
   * @param fileName the name of the file
   * @param contents the contents of the file
   * 
   * @return true for success false for failure
   */
  public boolean addItem(String category, String fileName, String contents){
    String filePath = workingDir + "/Database/" + category;

    // synrchronize on an object lock
    synchronized(objectLock) {    
      if(!Files.exists(Paths.get(workingDir + "/Database/" + category)) || 
      !Files.isDirectory(Paths.get(workingDir + "/Database/" + category))){
        File categoryDir = new File(filePath);
        categoryDir.mkdir();
      }

    try{
        File newFile = new File(filePath + "/" + fileName);
        if(newFile.createNewFile()){
          FileWriter writer = new FileWriter(filePath + "/" + fileName);
          writer.write(contents);
          writer.close();
        }
      }catch(Exception e){
        System.out.println("Database Error: " + e);
        return false;
      }
    }
    return true;
  }

  /**
   * A helper method during startup to join a frontend
   * 
   * @param databaseIP the machines own IP
   * @param entryPoint the frontend IP
   * 
   * @return true for success false for failure
   */
  private static boolean joinDatabase(String databaseIp, String entryPoint) {
    XmlRpcClient client = createClient(entryPoint);
    List<String> params = new ArrayList<String>();
    params.add(databaseIp);

    try {
      Boolean result = (boolean) client.execute("FrontEnd.addDatabase", params.toArray());
      return true;
    }
    catch(Exception e){
      System.err.println("Database Exception: " + e);
      return false;
    }
  }


  /**
   * This method sends all data related to a category to another IP
   * 
   * @param databaseIp the IP of where the data is being sent
   * @param category the category of data being sent
   * @param delete the string "YES" or "NO" indicating whether or not the data should be deleted
   * 
   * @return true for success false for failure
   */
  public boolean sendCategory(String databaseIp, String category, String delete){
    File categoryFile = new File(workingDir + "/Database/" + category);
    Path categoryPath = categoryFile.toPath();
    
    if(Files.isDirectory(categoryPath)){
      System.out.println(categoryPath.toString());
      
      String[] files = categoryFile.list();
      for (String file: files) {
        // get the file path and send it over to getfile contents,
        String fileContents = getFileContents(categoryPath + "/" + file);
        System.out.println("Here are the file contents: " + fileContents);
        XmlRpcClient client = createClient(databaseIp);
        List<String> params = new ArrayList<String>();
        params.add(category);
        params.add(file);
        // change this to contents after
        params.add(fileContents);

        try {
          client.execute("Database.addItem", params);
        }
        catch(Exception e){
          System.out.println("Failed to send elements to other db: |" + databaseIp + "|");
          System.err.println("Client exception: " + e);
          return false;
        }
      }
      // delete an entire folder after all has been sent
    }
    else{
      //DB doesnt have cat specified
      return false;
    }
    if (delete.equals("YES")) {
      System.out.println("STARTING TO DELETE CAT");
      deleteFolder(categoryFile);
    }
    return true; 
  }

  /**
   * A method to delete an item from the databasea
   * 
   * @param category the category of the file being deleted
   * @param fileName the file's name
   * 
   * @return true or false is successful or not
   */
  public String deleteItem(String category, String fileName){
    System.out.println("DELETING FILE");
    String categoryPath = workingDir + "/Database/" + category;

    //Checks to see if the Cat is a dir
    if(Files.isDirectory(Paths.get(categoryPath))){ //Or this
      File toBeDeleted = new File(categoryPath + "/" + fileName);
      Path filePath = toBeDeleted.toPath();
      if(!Files.isRegularFile(filePath)) return "false"; //This is not working
      synchronized(objectLock) { 
        //Is inconsequential to delete something that doesnt exist however
        toBeDeleted.delete();
        }
      
      File categoryFile = new File(categoryPath);
      if(categoryFile.list().length == 0){
        synchronized(objectLock){
          categoryFile.delete();
        }
        // if it's the last file of that category, tell the frontEndt
        return "delete";
      }
      
      return "true";
    }
    else{
      return "false";
    }
  }

  /**
   * A helper method to clean a database
   * 
   * @param element The File object of a chosen directory (can either be a category or 
   *                the whole database)
   */
  public static void deleteFolder(File element) {
    System.out.println("DELETING FOLDER");
    if(element.isDirectory()){
      String[] files = element.list();
      if(files.length != 0){
        for(String file: files){
          File subFile = new File(element.toString() + "/" + file);
          deleteFolder(subFile);
        }
      }
    }
    synchronized(objectLock) { 
      element.delete();
    }
  }

  /**
   * The main method
   */
  public static void main(String[] args) {
    workingDir = Paths.get("").toAbsolutePath().toString();
    File Database = new File(workingDir + "/Database");

    deleteFolder(Database);

    Database.mkdir();
    
    if (args.length != 2) {
      System.out.println("USAGE: [Own Database IP] [FrontEnd entry point]");
      return;
    }

    try {
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      XmlRpcServer xmlRpcServer;
      WebServer server = new WebServer(PORTNUMBER);
      xmlRpcServer = server.getXmlRpcServer();
      phm.addHandler("Database", DatabaseServer.class);
      xmlRpcServer.setHandlerMapping(phm);
      server.start();
      System.out.println("XML-RPC server started");
      
    } catch (Exception e) {
      System.err.println("Server exception: " + e);
    }

    if (joinDatabase(args[0], args[1])) {
      System.out.println("Successfully joined front-end with entry point: " + args[1]);
    }
    else {
      System.out.println("Database addition went wrong for some reason");
      return;
    }
  }
}