import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.ArrayList;
import java.io.File;
import java.net.URL;     
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.io.FileWriter;
import java.util.*; 
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.*; 

/**
 * A Java client for THE Blueberry Muffin :) 
 */
public class Client {
  /**
   * A magic number for the port number being used
   * Standardized across all our classes
   */
  private final static int PORTNUMBER = 8413;

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
   * The IP address of the 2nd optimal front end
   * as defined by our region smart selection
   */
  private static String secondOptimalFrontEnd = null;

  /**
   * The time in seconds between RSS calls
   */
  private static final int RSSTimeout = 300;


  /**
   * The region smart selection to choose the Frontend
   * with the best latency to the client
   */
  private static void regionSmartSelect(){
    //Create a client to the entry point
    //System.out.println("STARTING SMART SELECT");
    XmlRpcClient entryClient = createClient(entryPoint);
    List<String> params = new ArrayList<>();

    List<String> frontEnds = new ArrayList<String>();
    //Retrieve the list of all frontends

    try{
      Object[] frontEndsObj = (Object[]) entryClient.execute("FrontEnd.getFrontEnds", params);
      for (Object frontEnd : frontEndsObj){
        frontEnds.add(frontEnd.toString());
      }
    }
    catch(Exception e){
      System.out.println("Could not get Frontends from entry point, ensure entry point is online");
      System.out.println(e);
      return;
    }

    frontEnds.add(entryPoint);

    //Keep track of the 2 frontend with the best latency
    String bestFrontEnd = "";
    String secondBestFrontEnd = "";
    Long bestTime = Long.MAX_VALUE;
    Long secondBestTime = Long.MAX_VALUE;

    //Iterate over all frontends
    for(String frontEnd: frontEnds){
      XmlRpcClient FEClient = createClient(frontEnd.toString());
      try{
        //Start Time
        long startTime = System.nanoTime();
        String result = (String) entryClient.execute("FrontEnd.ping", params);
        long totalTime = System.nanoTime() - startTime;
        if(totalTime < bestTime){ //Compare total time to best
          //Make the current best the new second best
          secondBestFrontEnd = bestFrontEnd;
          secondBestTime = bestTime;

          //Store the best
          bestTime = totalTime;
          bestFrontEnd = frontEnd.toString();
        }

        else if(totalTime < secondBestTime){
          //Just replace the second best
          secondBestTime = totalTime;
          secondBestFrontEnd = frontEnd;
        }
      }
      catch(Exception e){
        System.out.println("Could not ping Front End: " + frontEnd);
      }
    }
    System.out.println("OPTIMAL FE CHOSEN: " + bestFrontEnd);
    System.out.println("SECOND OPTIMAL FE CHOSEN: " + secondBestFrontEnd);
    optimalFrontEnd = bestFrontEnd;
    secondOptimalFrontEnd = secondBestFrontEnd;
  }

  /**
   * The helper method to create a client
   * 
   * @param IPaddress: The ip address of the client being created
   * 
   * @return the client that was built
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
   * The client side method to start the adding file process
   * @param category The category the file belongs to
   * @param fileName The path of the file
   */
  private static void addItem(String category, String fileName){

    //Opens the given file and reads the content
    // perhaps use a string builder instead
    StringBuilder dataToBeSent = new StringBuilder();
    try{
      File file = new File(fileName);
      Scanner fileReader = new Scanner(file); 
      while(fileReader.hasNextLine()){
        dataToBeSent.append(fileReader.nextLine());
        dataToBeSent.append("\r\n");
      }
    }
    catch(Exception e){
      //If we couldnt read the file, stop the command
      System.out.println("Could not open and/or read Filepath: " + fileName);
      return;
    }

    if(dataToBeSent.length() == 0){
      System.out.println("Specified File has no contents. Aborting");
      return;
    }

      XmlRpcClient client = createClient(optimalFrontEnd);
      List<String> params = new ArrayList<String>();
      params.add(category); //Category of the file
      params.add(fileName); //Name of the file
      params.add(dataToBeSent.toString()); //Contents of the file
      params.add("YES"); //The 'leader' frontEnd

      try{
        Boolean result = (Boolean) client.execute("FrontEnd.addItem", params.toArray());
        if(result){
          System.out.println("File: " + fileName + " was Sucessfully added to System"); 
        }
        else{
          System.out.println("Failed to add");
        }
      }
      catch(Exception e){
        if(optimalFrontEnd == null && secondOptimalFrontEnd == null){
          System.err.println("Client exception: " + e);
          return;
        }
        else{
          optimalFrontEnd = secondOptimalFrontEnd;
          secondOptimalFrontEnd = null;
          addItem(category, fileName);
        }
      }
    }

  /**
   * The clientside method to start the lookup process
   * Prints the results in the console
   * @param category The category being looked up
   */
  private static void lookup(String category, String fileName){
    XmlRpcClient client = createClient(optimalFrontEnd);
    List<String> params = new ArrayList<>();
    params.add(category);
    params.add(fileName);

    // System.out.println(category);
    // System.out.println(fileName);

    try{
      String fileContents =  (String) client.execute("FrontEnd.getItem", params.toArray());
      // fileContents are prepended with an error code
      if(fileContents.length() == 0){
        //This should cause the drop into the catch block, which will make the db go to another region
        throw new Error("Did not recieve file");
      }
      else if (fileContents.length() == 1) {
        System.out.println("File does not exist");
        return;
      }
      //Makes a file object using the given name
      File recievedFile = new File(fileName);

      //If an outdated version exists, delete it
      if(recievedFile.exists()){
        recievedFile.delete();
      }

      //Create the File
      recievedFile.createNewFile();

      //Write the contents to the file
      FileWriter fileWriter = new FileWriter(fileName);
      fileWriter.write(fileContents.substring(1));
      fileWriter.close();

      System.out.println("Successfully retrieved: " + fileName);
    }
    catch(Exception e){
      //If there is no backup frontEnd, fail
      if(secondOptimalFrontEnd == null){
          System.err.println("Both regions down, try another entry point..." + e);
          return;
        }
      //If there is, replace optimal with backup and try again
      else{
        //This block can be hit by either a FE error or a DB error
        optimalFrontEnd = secondOptimalFrontEnd;
        secondOptimalFrontEnd = null;
        System.out.println("CHANGING FRONTEND");
        lookup(category, fileName);
      }
    }
  }

  /**
   * This method starts the deleteFile process which deletes a file from the system
   * 
   * @param category the category of the file
   * @param fileName the name of the file
   * 
   */
  private static void deleteFile(String category, String fileName){
    XmlRpcClient client = createClient(optimalFrontEnd);
    List<String> params = new ArrayList<>();
    params.add(category);
    params.add(fileName);
    params.add("YES");

    try{
      Boolean result = (Boolean) client.execute("FrontEnd.deleteItem", params.toArray());
      if(result){
        System.out.println("File: " + fileName + " was Sucessfully deleted from System"); 
      }
    }
    catch(Exception e){
      if(optimalFrontEnd == null && secondOptimalFrontEnd == null){
        System.err.println("Client exception: " + e);
        return;
      }
      else{
        optimalFrontEnd = secondOptimalFrontEnd;
        secondOptimalFrontEnd = null;
        deleteFile(category, fileName);
      }
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
    
    /**
     * A runnable to perform Region Smart Select Recurringly
     */
    Runnable RSSRunnable = new Runnable() {
    public void run() {
        regionSmartSelect();
      }
    };
    
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    executor.scheduleAtFixedRate(RSSRunnable, 0, RSSTimeout, TimeUnit.SECONDS);
    
    //Take in commands from the user
     while(true){
      Scanner Scanner = new Scanner(System.in);  // Create a Scanner object
      System.out.println("Enter Function and Parameters");

      String[] cmdLineParse = Scanner.nextLine().split(" ");
      if(cmdLineParse.length <= 2 || cmdLineParse.length > 3){
        System.out.println("Usage: [Function] [Category] [Filename]");
        continue;
      }
      else{
          switch(cmdLineParse[0]){
        case "lookup":
          if(cmdLineParse.length != 3){
            System.out.println("lookup Usage: [Category] [Filename]");
          }
          lookup(cmdLineParse[1], cmdLineParse[2]);
          break;

        case "addItem":
          if(cmdLineParse.length != 3){
            System.out.println("addItem Usage: [Category] [File]");
            break;
          }
          addItem(cmdLineParse[1], cmdLineParse[2]);
          break;
        
        case "deleteFile":
          if(cmdLineParse.length != 3){
            System.out.println("deleteFile Usage: [Category] [File]");
            break;
          }
          deleteFile(cmdLineParse[1], cmdLineParse[2]);
          break;

        default:
          System.out.println("Invalid function name");
          System.out.println("Functions: lookup, addItem, deleteFile");
          System.out.println("Please try again");
          // removed return here, we might want it back
        }
      }      
    }
  }
}

