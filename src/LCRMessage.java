public class LCRMessage extends Message{

    /**
     * LCR algorithm message
     * @param id
     */
    public LCRMessage(int id) {
        super(id);
    }

    /**
     * @return the id
     */
    public int getId() {
        return getData();
    }
}