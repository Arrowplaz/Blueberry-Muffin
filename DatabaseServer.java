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

 private static Map<String, ArrayList<String>> database = new HashMap<String, ArrayList<String>>();
  
  /**
   * The port number being used
   * Standardized across our project
   */
  private static final int PORTNUMBER = 8412;

  /**
   * The location of the database's working directory
   * used for accessing the files
   */
  private static String workingDir;

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
      System.err.println("Database Exception: " + e);
    }
    return client;
  }


  // /**
  //  * A getter method to determine which categories (keys) this database contains
  //  * @return
  //  */
  // public static Set<String> getCategories(){
  //   System.out.println("Getting categories from Database");
  //   return database.keySet();
  // }
  
  // /**
  //  * 
  //  * @param genre
  //  * @return
  //  */
  // public ArrayList<Book> getCategory(String genre){
  //   if(database.keySet().contains(genre)){
  //     System.out.println("Successfully got, category!");
  //     return database.get(genre);
  //   }
  //   System.out.println("");

  //   else return null;
  // }

  /**
   * Using a category and fileName, converts the file to a string for sendin
   * 
   * @param category the category of the file
   * @param fileName the name of the file
   * 
   * @return the contents of a file as a String
   */
  public String getItem(String category, String fileName) {
    System.out.println("GETTING ITEM");
    String filePath = workingDir + "/Database/" + category + "/" + fileName;
    if(Files.exists(Paths.get(workingDir + "/Database/" + category + "/" + fileName))){
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
    else{
      return "";
    }
  }

  public boolean deleteFile(String category, String fileName){
    String filePath = workingDir + "/Database/" + category + "/" + fileName;
    if(Files.exists(Paths.get(workingDir + "/Database/" + category + "/" + fileName))){
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

    //If the category folder doesnt exist, make it
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
    final int PORTNUMBER = 8412;
    //Create client
    //Send over data
    //Recieve either a success or failure
    for (String fileName : database.get(category)) {
      XmlRpcClient client = createClient(databaseIp);
      List<String> params = new ArrayList<>();
      params.add(category);
      params.add(fileName);
      // change this to contents after
      params.add(fileName);
      try {
        Boolean result = (boolean) client.execute("Database.addItem", params.toArray());
      }
      catch(Exception e){
        System.err.println("Client exception: " + e);
        return false;
      }
    }
    // delete an entire folder after all has been sent
    return true; 
  }

  /**
   * A helper method to clean any previous database contents from previous runs
   * 
   * @param element The path of the database folder, recurses on everything inside the folder
   */
  public static void deleteFile(File element) {
    if(element.isDirectory()){
      String[] files = element.list();
      if(files.length != 0){
        for(String file: files){
          File subFile = new File(element.toString() + "/" + file);
          deleteFile(subFile);
        }
      }
    }
    element.delete();
  }

  /**
   * The main method
   */
  public static void main(String[] args) {
    workingDir = Paths.get("").toAbsolutePath().toString();
    File Database = new File(workingDir + "/Database");

    deleteFile(Database);

    Database.mkdir();
    
    if (args.length != 2) {
      System.out.println("USAGE: [Own Database IP] [FrontEnd entry point]");
      return;
    }

    if (joinDatabase(args[0], args[1])) {
      System.out.println("Successfully joined front-end with entry point: " + args[1]);
    }
    else {
      System.out.println("Database addition went wrong for some reason");
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