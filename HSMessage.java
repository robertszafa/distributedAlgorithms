public class HSMessage extends Message {
    public static final int IN_DIR = 1;
    public static final int OUT_DIR = 0;
    private int dir;
    private int hopCount;

    /**
     * HS algorithm messsage
     * @param id
     * @param dir
     * @param hopCount
     */
    public HSMessage(int id, int dir, int hopCount) {
        super(id);
        this.dir = dir;
        this.hopCount = hopCount;
    }

    /**
     * 
     * @param id
     * @param hopCount
     */
    public HSMessage(int id, int hopCount) {
        super(id);
        this.hopCount = hopCount;
    }


    /**
     * @return the id
     */
    public int getId() {
        return getData();
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