/**
 * Robert Szafarczyk, February 2019, id: 201307211
 * 
 * Node class describes the local state of the node, its memory and methods to communicate with
 * its neighbours.
 */

import java.util.HashMap;


public class Node {
    // Node states
    public static final int LEADER_STATUS = -1;
    public static final int UNKNOWN_STATUS = 0;

    private boolean isTerminated;
    private int status;
    private int id;
    private int leaderId;
    private int phaseHS;
    private int msgCounter; // count all send messages
    private Node clockwiseNeighbour;
    private Node counterclockwiseNeighbour;
    private Message clockBuffMsg;
    private Message counterBuffMsg;
    private HashMap<Node, Message> receivedMsg;

    /**
     * 
     * @param id of the node
     */
    public Node (int id) {
        this.id = id;
        isTerminated = false;
        status = UNKNOWN_STATUS;
        msgCounter = 0;
        phaseHS = 0;
        receivedMsg = new HashMap<>();
    }

    /**
     * flush all memory, put into uknown state, node not terminated
     */
    public void resetNode() {
        isTerminated = false;
        status = UNKNOWN_STATUS;
        leaderId = 0;
        phaseHS = 0;
        msgCounter = 0;
        clockBuffMsg = null;
        counterBuffMsg = null;
        receivedMsg.clear();
    }

    /**
     * will only send if there is a message in the buffer to the clockwise neighbour
     */
    public void sendClock() {
        if (clockBuffMsg != null) {
            clockwiseNeighbour.receiveMsg(this, clockBuffMsg);
            msgCounter++;
        }
        clockBuffMsg = null;
    }

    /**
     * will only send if there is a message in the buffer to the counterclockwise neighbour
     */
    public void sendCounterclock() {
        if (counterBuffMsg != null) {
            counterclockwiseNeighbour.receiveMsg(this, counterBuffMsg);
            msgCounter++;
        }
        counterBuffMsg = null;
    }

    // this will override any message previously received fromNode
    public void receiveMsg(Node fromNode, Message msg) {
        // this will override the previous rcvd message -> 
        // a node will never have access to more than one message from each neighbour
        receivedMsg.put(fromNode, msg);
    }

    /**
     * @return the receivedMsg from clockwise neighbour
     */
    public Message getRcvdMsgFromClock() {
        return receivedMsg.get(clockwiseNeighbour);
    }

    /**
     * @return the receivedMsg from counter clockwise neighbour
     */
    public Message getRcvdMsgFromCounterclock() {
        return receivedMsg.get(counterclockwiseNeighbour);
    }

    /**
     * nullify the received msgs buffer
     */
    public void flushRcvdMsgs() {
        receivedMsg.clear();
    }

    /**
     * @return the current phaseHS
     */
    public int getPhase() {
        return phaseHS;
    }

    /**
     * increment phaseHS by 1
     */
    public void incPhase() {
        phaseHS++;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the leaderId
     */
    public int getLeaderId() {
        return leaderId;
    }

    /**
     * @return the msgCounter
     */
    public int getMsgCounter() {
        return msgCounter;
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

    public void terminate() {
        isTerminated = true;
    }

    /**
     * Message generation for clockwise neighbour
     * @param clockBuffMsg the clockBuffMsg to set
     */
    public void setClockBuffMsg(Message clockBuffMsg) {
        this.clockBuffMsg = clockBuffMsg;
    }

    /**
     * Message generation for clockwise neighbour
     * @param counterBuffMsg the counterBuffMsg to set
     */
    public void setCounterBuffMsg(Message counterBuffMsg) {
        this.counterBuffMsg = counterBuffMsg;
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

    @Override
    public boolean equals(Object o) { 
        // if the object is compared with itself then return true   
        if (o == this) { 
            return true; 
        } 
        // check if o is a Node
        if (!(o instanceof Node)) { 
            return false; 
        } 
        // typecast to Node 
        Node n = (Node) o; 
        // compare the ID's of the Nodes
        return this.id == n.getId(); 
    } 
} 