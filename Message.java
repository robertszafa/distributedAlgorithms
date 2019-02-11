import java.util.ArrayList;

public class Message {
    private int id;
    private int dir;
    private int hopCount;


    /**
     * HS algorithm messsage
     * @param id
     * @param dir
     * @param hopCount
     */
    public Message(int id, int dir, int hopCount) {
        this.id = id;
        this.dir = dir;
        this.hopCount = hopCount;
    }

    /**
     * LCR algorithm message
     * @param id
     */
    public Message(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the dir
     */
    public int getDir() {
        return dir;
    }

    /**
     * @return the hopCount
     */
    public int getHopCount() {
        return hopCount;
    }
}