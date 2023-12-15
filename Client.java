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
import java.util.stream.*; 
/**
 * A Java client to the online bookstore.
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
   * The region smart selection to choose the Frontend
   * with the best latency to the client
   */
  private static void regionSmartSelect(){
    //Create a client to the entry point
    System.out.println("STARTING SMART SELECT");
    XmlRpcClient entryClient = createClient(entryPoint);
    List<String> params = new ArrayList<>();

    Object[] frontEnds;
    //Retrieve the list of all frontends
    try{
      frontEnds = (Object[]) entryClient.execute("FrontEnd.getFrontEnds", params);
      Object entryAsFE = entryPoint;
    }
    catch(Exception e){
      System.out.println("Could not get Frontends from entry point, ensure entry point is online");
      System.out.println(e);
      return;
    }

    List<Object> allFrontEnds = Arrays.asList(frontEnds);
    allFrontEnds.add(entryPoint);

    //Keep track of the 2 frontend with the best latency
    String bestFrontEnd = "";
    String secondBestFrontEnd = "";
    Long bestTime = Long.MAX_VALUE;
    Long secondBestTime = Long.MAX_VALUE;

    //Iterate over all frontends
    for(Object frontEnd: frontEnds){
      XmlRpcClient FEClient = createClient(frontEnd.toString());
      try{
        //Start Time
        long startTime = System.nanoTime();
        String result = (String) entryClient.execute("FrontEnd.ping", params);
        long endTime = System.nanoTime() - startTime;
        if(endTime < bestTime){ //Compare total time to best
          //Make the current best the new second best
          secondBestFrontEnd = bestFrontEnd;
          secondBestTime = bestTime;

          //Store the best
          bestTime = endTime;
          bestFrontEnd = frontEnd.toString();
        }

        else if(endTime < secondBestTime){
          //Just replace the second best
          secondBestTime = endTime;
          secondBestFrontEnd = frontEnd.toString();
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

    //Opens the given file and reads the content
    // perhaps use a string builder instead
    StringBuilder dataToBeSent = new StringBuilder();
    try{
      File file = new File(outgoingFile);
      Scanner fileReader = new Scanner(file); 
      while(fileReader.hasNextLine()){
        dataToBeSent.append(fileReader.nextLine());
        dataToBeSent.append("\r\n");
      }
    }
    catch(Exception e){
      //If we couldnt read the file, stop the command
      System.out.println("Could not open and/or read Filepath: " + outgoingFile);
      return;
    }

    if(dataToBeSent.length() == 0){
      System.out.println("Specified File has no contents. Aborting");
      return;
    }

     

      XmlRpcClient client = createClient(optimalFrontEnd);
      List<String> params = new ArrayList<String>();
      params.add(category); //Category of the file
      params.add(outgoingFile); //Name of the file
      params.add(dataToBeSent.toString()); //Contents of the file
      params.add(optimalFrontEnd); //The 'leader' frontEnd

      try{
        Boolean result = (Boolean) client.execute("FrontEnd.addItem", params.toArray());
        if(result){
          System.out.println("File: " + outgoingFile + " was Sucessfully added to System"); 
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
          addFile(category, outgoingFile);
        }
      }
    }

  /**
   * The clientside method to start the lookup process
   * Prints the results in the console
   * @param Category The category being looked up
   */
  private static void lookupFile(String Category, String Filename){
    XmlRpcClient client = createClient(optimalFrontEnd);
    List<String> params = new ArrayList<>();
    params.add(Category);
    params.add(Filename);

    // System.out.println(Category);
    // System.out.println(Filename);

    try{
      String fileContents =  (String) client.execute("FrontEnd.getItem", params.toArray());
      //We are going to assume that a request for a nonexistent file will never come in
      //DOUBLE CHECK THIS
      // this only 
      if(fileContents.length() == 0){
        //This should cause the drop into the catch block, which will make the db go to another region
        throw new Error("Did not recieve file");
      }
      else if (fileContents.length() == 1) {
        System.out.println("File does not exist");
        return;
      }
      //Makes a file object using the given name
      File recievedFile = new File(Filename);

      //If an outdated version exists, delete it
      if(recievedFile.exists()){
        recievedFile.delete();
      }

      //Create the File
      recievedFile.createNewFile();

      //Write the contents to the file
      FileWriter fileWriter = new FileWriter(Filename);
      System.out.println((int)fileContents.charAt(1));
      fileWriter.write(fileContents.substring(1));
      fileWriter.close();

      System.out.println("Successfully retrieved: " + Filename);
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
        lookupFile(Category, Filename);
      }
    }
  }


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
    
    //Identify the best Frontend for this user
    regionSmartSelect();
    //Track time of last region smart select
    long lastRSS = System.currentTimeMillis();
    
    //Take in commands from the user
     while(true){
      long currentTime = System.currentTimeMillis();
      //See if 5 minutes have passed last RSS
      if((currentTime - lastRSS) / 1000.0 > 300){ //The divide by 1000 converts MS to seconds, then compare to 300
        lastRSS = System.currentTimeMillis();
        regionSmartSelect();
      }

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
          lookupFile(cmdLineParse[1], cmdLineParse[2]);
          break;

        case "addFile":
          if(cmdLineParse.length != 3){
            System.out.println("addFile Usage: [Category] [File]");
            break;
          }
          addFile(cmdLineParse[1], cmdLineParse[2]);
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
          System.out.println("Functions: lookup, addFile, deleteFile");
          System.out.println("Please try again");
          // removed return here, we might want it back
      }
      }
      
    }


    
  }
}

