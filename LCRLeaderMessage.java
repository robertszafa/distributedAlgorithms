public class LCRLeaderMessage extends LCRMessage {
    private int leaderId;

    /**
     * 
     * @param leaderId
     */
    public LCRLeaderMessage(int leaderId) {
        super(Node.LEADER_STATUS);
        this.leaderId = leaderId;
    }

    /**
     * @return the leaderId
     */
    public int getLeaderId() {
        return leaderId;
    }
}