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
 * A java Test client to Test Blueberry muffin for scabillity,
 * smart select, and fault tolerance. 
 */
public class TestClient {
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
   * Converting form nano to miliseconds
   */
  private static int nanoSecondsInMili = 1000000;

   /**
    * hardedcoded categories for testing. Allows for adds to be adding accross the whole system,
    * not just directed at one (or a few) databases
    */
  private static String[] categories = new String[]{"third", "first", "classics", "BIG",
    "gutenberg", "fantasy", "BIG", "news", "horror", "thriller", "mystery", "random"};

  private static Random rand = new Random();
  
  private static String selectType;

  /**
   * Used to compare smart select vs random (or dumb select). chooses
   * a front end randomly
   */
  private static void regionDumbSelect(){
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

    optimalFrontEnd = frontEnds.get(rand.nextInt(frontEnds.size()));
    secondOptimalFrontEnd = frontEnds.get(rand.nextInt(frontEnds.size()));
    return;
  }

  /**
   * The region smart selection to choose the Frontend
   * with the best latency to the client
   */
  private static void regionSmartSelect(){
    //Create a client to the entry point
    //System.out.println("STARTING SMART SELECT");
    XmlRpcClient entryClient = createClient(entryPoint);
    List<String> params = new ArrayList<String>();

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
        System.out.println(frontEnd);
        long startTime = System.nanoTime();
        String result = (String) entryClient.execute("FrontEnd.ping", params);
        System.out.println(result);
        long totalTime = System.nanoTime() - startTime;
        System.out.println(totalTime/nanoSecondsInMili);
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
   * The client side method to start the adding file process, altered
   * to test how long it takes for RPC calls to addFile
   * @param category The category the file belongs to
   * @param outgoingFile The file itself
   */
  private static void addFile(String category, String outgoingFile){
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
    // start timing here, when the data has been loaded
    List<String> params = new ArrayList<String>();

    params.add(category); //Category of the file
    params.add(outgoingFile); //Name of the file
    params.add(dataToBeSent.toString()); //Contents of the file
    params.add("YES"); 
    
    
    long startTime = System.nanoTime();
    try{
        // System.out.println("about to execute");
        Boolean result = (Boolean) client.execute("FrontEnd.addItem", params.toArray());
        if(!result){
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
            addFile(category, "gatsby");
        }
    }
    long totalTime = System.nanoTime() - startTime;
    // might move this to elsewhere later;
    System.out.println(totalTime/nanoSecondsInMili + "\n");
 }


  /**
   * Used for fault tolerance of getting rid of a frontEnd, and then taking out optimal
   * frontEnd. Ideally, we should see a small spike in latency.
   * 
   * @param category The category the file belongs to
   * @param fileName The file itself
   */

  private static void continuedLookup(String category, String fileName){
    XmlRpcClient client = createClient(optimalFrontEnd);
    List<String> params = new ArrayList<>();
    params.add(category);
    params.add(fileName);

    while (true) {
        long startTime = System.nanoTime();
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
        File recievedFile = new File(fileName);

        //If an outdated version exists, delete it
        long totalTime = System.nanoTime() - startTime;
        Thread.sleep(1000);
        System.out.println(totalTime/nanoSecondsInMili);
        //   System.out.println(totalTime/nanoSecondsInMili + "\n");
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
        continuedLookup(category, fileName);
      }
    }
    }
  }

  
    /**
   * Used for testing fault tolerance when both databases a machine goes to 
   * are down. This test was done manually. We started with 3 databases, 
   * and a frontEnd. We kept adding, and saw which two databases "classics"
   * mapped to, and then terminated them to see the spike in latency. 
   * 
   * @param category The category the file belongs to
   * @param fileName The file itself
   */

  private static void continuedAdd(String category, String fileName) {
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
    // start timing here, when the data has been loaded
    List<String> params = new ArrayList<String>();

    params.add(category); //Category of the file
    params.add(fileName); //Name of the file
    params.add(dataToBeSent.toString()); //Contents of the file
    params.add("YES"); 
    
    while(true) {
        long startTime = System.nanoTime();
        try{
            // System.out.println("about to execute");
            Boolean result = (Boolean) client.execute("FrontEnd.addItem", params.toArray());
            if(!result){
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
                addFile(category, "gatsby");
            }
        }

        long totalTime = System.nanoTime() - startTime;
        System.out.println(totalTime/nanoSecondsInMili + "\n");
        // might move this to elsewhere later;
    }
}
  /**
   * The clientside method to start the lookup process
   * Prints the results in the console to be used in analysis
   * 
   * @param Category The category being looked up
   * @param @fileName 
   */
  private static void lookupFile(String category, String fileName){
    XmlRpcClient client = createClient(optimalFrontEnd);
    List<String> params = new ArrayList<>();
    params.add(category);
    params.add(fileName);

    // time the time it takes to lookup a file, will be called concurrently to test
    long startTime = System.nanoTime();
    try{
      String fileContents =  (String) client.execute("FrontEnd.getItem", params.toArray());
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
      
      long totalTime = System.nanoTime() - startTime;
      System.out.println(totalTime/nanoSecondsInMili + "\n");

      //Create the File
      recievedFile.createNewFile();

      //Write the contents to the file
      FileWriter fileWriter = new FileWriter(fileName);
    //   System.out.println((int)fileContents.charAt(1));
      fileWriter.write(fileContents.substring(1));
      fileWriter.close();
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
        lookupFile(category, fileName);
      }
    }
  }
  
  /**
   * The main method
   * @param args
   */
  public static void main(String[] args) {
    //Grab the initial Front end IP from terminal 
    if (args.length != 3) {
      System.out.println("Usage: [Entry Point] [Test Type] [Select Type]");
      return;
    }

    entryPoint = args[0];
    selectType = args[2];
    //Identify the best Frontend for this user
    if (selectType.equals("smart")) {
        System.out.println("IN SMART");
        regionSmartSelect();
    }
    else {
        regionDumbSelect();
    }
    
    int index = rand.nextInt(categories.length);
    String category = categories[index];
    switch(args[1]){
        case "lookup":
          lookupFile(category, "gatsby.txt");
          break;
        
        case "continuedLookup":
            continuedLookup("classics", "gatsby.txt");
            break;

        case "add":
          // test adds for a 399 kb file
          // category
          addFile(category, "gatsby.txt");
          break;
        
        case "continuedAdd":
            continuedAdd("classics", "gatsby.txt");
            break;
        default:
          System.out.println("Invalid function name");
          System.out.println("Functions: lookup, addFile, deleteFile");
          System.out.println("Please try again");
      }
  }
}

