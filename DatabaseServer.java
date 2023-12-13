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

//  private static Map<String, ArrayList<String>> database = new HashMap<String, ArrayList<String>>();
  
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

  private static Object objectLock = new Object();
    /**
   * The helper method to create a client
   * 
   * @param hostName: hostname of the client
   * @param portNum: the portnum of the client
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

  private String getFileContents(String filePath) {
    try{
      String fileContents = "";
      File currFile = new File(filePath);
      Scanner fileReader = new Scanner(currFile);
      while(fileReader.hasNextLine()){
        fileContents += fileReader.nextLine();
        fileContents += "\n";
      }
      fileReader.close();
      return fileContents; 
    }
    catch(Exception e){
      System.out.println("Database Error: " + e);
      return "";
    }
  }

  /**
   * Using a category and fileName, converts the file to a string for sendin
   * 
   * @param category the category of the file
   * @param fileName the name of the file
   * 
   * @return the contents of a file as a String
   */
  public String getItem(String category, String fileName) {
    String filePath = workingDir + "/Database/" + category + "/" + fileName;

    File file = new File(filePath);
    if(file.isFile()){
      return getFileContents(filePath);
    }
    else{
      return "";
    }
  }

  public boolean deleteFile(String category, String fileName){
    String filePath = workingDir + "/Database/" + category + "/" + fileName;
    if(Files.exists(Paths.get(workingDir))){
      File currFile = new File(filePath);
      if(currFile.delete()){
        return true;
      }
      else{
        return false;
      }
    }
    else{
      //If we couldnt find the file, should we assuming a success case?
      return true;
    }
  }

  public boolean addItem(String category, String fileName, String contents){
    String filePath = workingDir + "/Database/" + category;


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
          return true;
        }
        else{
          //File already exists
          return false;
        }
      }catch(Exception e){
        System.out.println("Database Error: " + e);
        return false;
      }
    }
  }

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


  public boolean sendCategory(String databaseIp, String category){
    String categoryPath = workingDir + "/Database/" + category;
    
    if(Files.exists(Paths.get(categoryPath)) && Files.isDirectory(Paths.get(categoryPath))){
      File categoryFile = new File(categoryPath);
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
      deleteFolder(categoryFile);
    }
    else{
      //DB doesnt have cat specified
      return false;
    }

    return true; 
  }

  /**
   * A helper method to clean a database
   * 
   * @param element The File object of a chosen directory (can either be a category or 
   *                the whole database)
   */
  public static void deleteFolder(File element) {
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