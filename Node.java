import java.util.HashMap;

public class Node {
    // states
    public static final int LEADER_STATUS = -1;
    public static final int UNKNOWN_STATUS = 0;

    private boolean isTerminated;
    private int status;
    private int id;
    private int leaderId;
    private HashMap<Node, Message> receivedMsg;
    private Node clockwiseNeighbour;
    private Node counterclockwiseNeighbour;

    public Node (int id) {
        this.id = id;
        status = UNKNOWN_STATUS;
        receivedMsg = new HashMap<>();
    }

    public void resetNode() {
        isTerminated = false;
        status = UNKNOWN_STATUS;
        leaderId = 0;
        receivedMsg = new HashMap<>();
    }

    public void sendClock(Message msg) {
        clockwiseNeighbour.receiveMsg(this, msg);
    }

    public void sendCounterclock(Message msg) {
        counterclockwiseNeighbour.receiveMsg(this, msg);
    }

    // this will override any message previously received fromNode
    public void receiveMsg(Node fromNode, Message msg) {
        receivedMsg.put(fromNode, msg);
    }

    /**
     * @return the receivedMsg
     */
    public Message getRcvdMsgFromClock() {
        return receivedMsg.get(clockwiseNeighbour);
    }

    /**
     * @return the receivedMsg
     */
    public Message getRcvdMsgFromCounterclock() {
        return receivedMsg.get(counterclockwiseNeighbour);
    }
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }


    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * @return the clockwiseNeighbour
     */
    public Node getClockwiseNeighbour() {
        return clockwiseNeighbour;
    }

    /**
     * @return the counterclockwiseNeighbour
     */
    public Node getCounterclockwiseNeighbour() {
        return counterclockwiseNeighbour;
    }

    /**
     * @param isTerminated the isTerminated to set
     */
    public void setTerminated(boolean isTerminated) {
        this.isTerminated = isTerminated;
    }

    /**
     * @param leaderId the leaderId to set
     */
    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    /**
     * @return the isTerminated
     */
    public boolean isTerminated() {
        return isTerminated;
    }

    /**
     * @param clockwiseNeighbour the clockwiseNeighbour to set
     */
    public void setClockwiseNeighbour(Node clockwiseNeighbour) {
        this.clockwiseNeighbour = clockwiseNeighbour;
    }

    /**
     * @param counterclockwiseNeighbour the counterclockwiseNeighbour to set
     */
    public void setCounterclockwiseNeighbour(Node counterclockwiseNeighbour) {
        this.counterclockwiseNeighbour = counterclockwiseNeighbour;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

}