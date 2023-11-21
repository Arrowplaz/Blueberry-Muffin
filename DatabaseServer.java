import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple example XML-RPC server program.
 */
public class DatabaseServer { 
  /**
   * The primary data structure meant to hold the books
   */
  private static Map<Integer, Book> bookshelf = new HashMap<Integer, Book>();

  /**
   * This funcition iterates through the map to get all books with a given topic
   * 
   * @param topic: the topic being searched for
   * 
   * @return An arrayList of strings with the ID numbers of all the books within the topic
   */
  private ArrayList<String> getByTopic(String topic){
    ArrayList<String> booksByTopic = new ArrayList<String>();
    for (Integer key : bookshelf.keySet()){
      Book book = bookshelf.get(key);
      if (book.getTopic().equals(topic)){
        booksByTopic.add(String.valueOf(book.getItemNumber()));
      }
    }
    return booksByTopic;
  }

  /**
   * A getter for a book via their ID number
   * 
   * @param itemNumber: the ID number of the book being looked for
   * 
   * @return The Book object with the given ID number or NULL if it doesnt exist
   */
  private Book getByItem(int itemNumber){
    return bookshelf.get(itemNumber);
  }

  /**
   * This method queries the bookshelf and returns the books for the argument
   * 
   * @param argType either topic or ID depeding on what we are querying by
   * @param arg the argument being used to query
   * 
   * @return the result of the query or a error message
   */
  public String query(String argType, String arg) throws InterruptedException{
    Thread.sleep(100);
    if (argType.equals("topic")) {
      ArrayList<String> booksByTopic = getByTopic(arg);

      if (booksByTopic.size() == 0) {
        return "No Books for topic: " + arg;
      }
      return "\n" + "Books in " + arg + ":" + "\n" + String.join("\n", booksByTopic) + "\n"; 
    }
    else{
      Book book = getByItem(Integer.valueOf(arg));
      if (book == null){
        return "No Book found for ID: " + arg;
      }
      else{
        return book.getBookDetails();
      }
    }
  }
  
  /**
   * A method to add a book to the bookshelf
   * 
   * @param bookName the name of the book
   * @param itemNumber the ID of the book
   * @param numCopies the number of copies
   * @param topic the topic of the book
   */
  private static void addBook(String bookName, int itemNumber, int numCopies, String topic) {
    Book newBook = new Book();
    newBook.createBook(bookName, itemNumber, numCopies, topic);
    synchronized(bookshelf){
      bookshelf.put(itemNumber, newBook);
    }
  }

  /**
   * A private method to the populate the bookshelf 
   */
  private static void populateBookshelf(){
    int initialStock = 10;
    addBook("AHHHHHHhh.", 1908,  initialStock, "horror");
    addBook("A Bad Grade", 2222,  initialStock, "horror");
    addBook("Nuance", 9080,  initialStock, "horror");
    addBook("Peter Pepper Picked the Wrong Answer", 9085,  initialStock, "fantasy");
    addBook("An Easy Barker Test", 9086,  initialStock, "fantasy");
    addBook("Fulfillment", 9087,  initialStock, "fantasy");
    addBook("The 1982 Bowdoin Fribee team", 1898,  initialStock, "non-fiction");
    addBook("A Mediocore Life",1899,  initialStock, "non-fiction");
    addBook("Tuesday Thursdays at 11:40", 1900,  initialStock, "non-fiction");
    addBook("Stare at the Sun", 2900,  initialStock, "self-help");
    addBook("Donate to a Cause: Me.", 2901,  initialStock, "self-help");
  } 

   /**
   * A method to update the bookshelf
   * 
   * @param itemNumber the ID of the book being updated
   * @param qty the amount being updated
   * 
   * @return a success or fail boolean
   */
  public String update(int itemNumber, int qty) throws InterruptedException{
    Book book = getByItem(itemNumber);
    if (book == null) {
      return "nonexistent";
    }
    synchronized(bookshelf){
      Thread.sleep(100);
      if (book.getNumCopies() + qty < 0){
        return "unsucessful";
      }
      book.setNumCopies(qty);
    }
    return "success";
  }

   /**
   * A getter for the copies of the number of book
   * 
   * @param itemNumber the ID of the book were getting copies of
   * 
   * @return the number of copies of the book
   */
  public int getCopies(int itemNumber){
    Book book = getByItem(itemNumber);
    return book.getNumCopies();
  }

  /**
   * The main method
   */
  public static void main(String[] args) {
    if (args.length != 0) {
      System.out.println("Usage: No arguments");
      return;
    }

    try {
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      XmlRpcServer xmlRpcServer;
      WebServer server = new WebServer(2384);
      xmlRpcServer = server.getXmlRpcServer();
      phm.addHandler("Catalog", CatalogServer.class);
      xmlRpcServer.setHandlerMapping(phm);
      server.start();
      System.out.println("XML-RPC server started");

      populateBookshelf();
      ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
      ScheduledFuture<?> result = executor.scheduleWithFixedDelay(() -> {
      synchronized(bookshelf){ 
        for (Integer key : bookshelf.keySet()){
          Book book = bookshelf.get(key);
          book.setNumCopies(1);
        }
      }
      }, Long.valueOf(30), Long.valueOf(30), TimeUnit.SECONDS);
      
    } catch (Exception e) {
      System.err.println("Server exception: " + e);
    }
  }
}