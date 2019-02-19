public class Message {
    int data;
    
    public Message(int data) {
        this.data = data;
    }

    /**
     * @return the data
     */
    public int getData() {
        return data;
    }

    public boolean isLeaderMsg() {
        return data == Node.LEADER_STATUS;
    }
}