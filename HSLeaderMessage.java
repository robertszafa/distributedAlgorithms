public class HSLeaderMessage extends HSMessage {
    private int leaderId;

    public HSLeaderMessage(int leaderId, int dir, int hopCount) {
        super(Node.LEADER_STATUS, dir, hopCount);
        this.leaderId = leaderId;
    }


    /**
     * @return the leaderId
     */
    public int getLeaderId() {
        return leaderId;
    }
}