public class HSLeaderMessage extends HSMessage {
    private int leaderId;

    /**
     * 
     * @param leaderId
     * @param hopCount
     */
    public HSLeaderMessage(int leaderId, int hopCount) {
        super(Node.LEADER_STATUS, hopCount);
        this.leaderId = leaderId;
    }


    /**
     * @return the leaderId
     */
    public int getLeaderId() {
        return leaderId;
    }
}