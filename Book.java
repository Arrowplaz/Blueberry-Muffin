public class Book {

    /**
     * The item number of the book
     */
    private int itemNumber;

    /**
     * The name of the book
     */
    private String bookName;

    /**
     * The number of copies
     */
    private int numCopies;

    /**
     * The topic of the book
     */
    private String topic;

    /**
     * A method to create the book
     * 
     * @param bookName the name
     * @param itemNumber the ID num
     * @param numCopies the number of copies
     * @param topic the topic of the book
     */
    public void createBook(String bookName, int itemNumber, int numCopies, String topic){
        this.itemNumber = itemNumber;
        this.bookName = bookName;
        this.numCopies = numCopies;
        this.topic = topic;
    }

    /**
     * A getter of the copies
     * 
     * @return the number of copies
     */
    public int getNumCopies(){
    return this.numCopies; 
    }

    /**
     * Returns all book details as a string
     * 
     * @return the details as a string
     */
    public String getBookDetails(){
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    sb.append(this.bookName);
    sb.append("\n");
    sb.append(Integer.toString(this.itemNumber));
    sb.append("\n");
    sb.append(this.topic);
    sb.append("\n");
    sb.append("Number of copies: ");
    sb.append(Integer.toString(this.numCopies));
    sb.append("\n");

    return sb.toString();
    }

    /**
     * A getter for the ID num
     * 
     * @return the ID num
     */
    public int getItemNumber(){
    return this.itemNumber;
    }

    /**
     * A getter for the topic
     * 
     * @return the topic as a string
     */
    public String getTopic(){
    return this.topic;
    }

    /**
     * A setter for the num of copies
     * 
     * @param setNum the num being set
     */
    public void setNumCopies(int setNum){
    this.numCopies += setNum;
    }
}