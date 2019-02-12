import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

public class BidirectionalRing {
    private ArrayList<Node> nodes;
    private Node leader;
    
    public BidirectionalRing(int n, int a) {
        nodes = createBiderectionalRing(n, a);
        leader = null;
    }

    public void resetRing() {
        leader = null;
        nodes.forEach(n -> n.resetNode());  
    }

    /**
     * The LCR Message holds only the id that's being send
     * Message = <id>
     */
    public void lcrAlgorithm() {
        int round = 1;

        roundLoop:
        for (;;round++) {
            if (round == 1) {
                // message generation and sending
                nodes.forEach(
                    n -> n.sendClock(new Message(n.getId())));
            }
            else { // round > 1
                for (Node n : nodes) {
                    if (!n.isTerminated()) {
                        int myid = n.getId();
                        int inMsg = n.getReceivedMsgFromCounterclockwise().getId();

                        if (inMsg > myid) {
                            n.sendClock(new Message(inMsg));
                        }
                        else if (inMsg == myid) {
                            n.setStatus(Node.LEADER_STATUS);
                            leader = n;
                            n.sendClock(new Message(Node.LEADER_STATUS));
                        }
                        else if (inMsg == Node.LEADER_STATUS && 
                                    n.getStatus() == Node.LEADER_STATUS) {
                            n.setTerminated(true);
                            break roundLoop;
                        }
                        else if (inMsg == Node.LEADER_STATUS) {
                            n.setTerminated(true);;
                            n.sendClock(new Message(Node.LEADER_STATUS));
                        }
                    }
                }
            }
        }
    }

    /**
     * HS message <id, direction, hopCount>.
     */
    public void hsAlgorithm() {
        int IN_DIR = 1;
        int OUT_DIR = 0;
        int phase = 0;
        int round = 1;

        roundLoop:
        for (;;round++) {
            for (Node n : nodes) {
                if (!n.isTerminated()) {
                    int myId = n.getId();

                    if (round == 1) {
                        n.sendClock(new Message(myId, OUT_DIR, (int) Math.pow(2, phase)));
                        n.sendCounterclock(new Message(myId, OUT_DIR, (int) Math.pow(2, phase)));
                        continue; // to next node
                    }

                    // from countrerclockwise neighbour 
                    int inIdFromCounter = n.getReceivedMsgFromClockwise().getId();
                    int dirFromCounter = n.getReceivedMsgFromClockwise().getDir();
                    int hopCountFromCounter = n.getReceivedMsgFromClockwise().getHopCount();

                    // from clockwise neighbour 
                    int inIdFromClock = n.getReceivedMsgFromClockwise().getId();
                    int dirFromClock = n.getReceivedMsgFromClockwise().getDir();
                    int hopCountFromClock = n.getReceivedMsgFromClockwise().getHopCount();

                    // leader msg received by non leader node -> pass leader msg on
                    if (inIdFromClock == Node.LEADER_STATUS && 
                        inIdFromCounter == Node.LEADER_STATUS && 
                        n.getStatus() != Node.LEADER_STATUS) {
                            n.sendClock(
                                    new Message(Node.LEADER_STATUS, OUT_DIR, hopCountFromClock - 1));
                            n.sendCounterclock(
                                    new Message(Node.LEADER_STATUS, OUT_DIR, hopCountFromClock - 1));
                            n.setTerminated(true);
                            continue;
                    }
                    // leader msg received by leader node -> all nodes but leader have terminated ->
                    // terminate leader and end the algorithm
                    else if (inIdFromClock == Node.LEADER_STATUS && 
                                inIdFromCounter == Node.LEADER_STATUS && 
                                n.getStatus() == Node.LEADER_STATUS) {
                            n.setTerminated(true);
                            break roundLoop;
                    }

                    // message has returned to node -> initiate next phase
                    if (dirFromClock == IN_DIR && dirFromCounter == IN_DIR &&
                        inIdFromClock == myId && inIdFromCounter == myId &&
                        hopCountFromClock == 1 && hopCountFromCounter == 1) {
                            phase++;
                            n.sendClock(new Message(myId, OUT_DIR, (int) Math.pow(2, phase)));
                            n.sendCounterclock(new Message(myId, OUT_DIR, 
                                                    (int) Math.floor(Math.pow(2, phase)/2)));
                            continue; // to next node
                    }


                    // message from countrerclockwise neighbour
                    if (dirFromCounter == OUT_DIR) {
                        if (inIdFromCounter > myId && hopCountFromCounter > 1) {
                            n.sendClock(
                                new Message(inIdFromCounter, OUT_DIR, hopCountFromCounter - 1));
                        }
                        else if (inIdFromCounter > myId && hopCountFromCounter == 1) {
                            n.sendCounterclock(new Message(inIdFromCounter, IN_DIR, 1));
                        }
                        else if (inIdFromCounter == myId) {
                            n.setStatus(Node.LEADER_STATUS);
                            leader = n;
                            n.sendClock(
                                new Message(Node.LEADER_STATUS, OUT_DIR, 
                                                    (int) Math.floor(Math.pow(2, phase)/2)));
                        }
                    }
                    else if (dirFromCounter == IN_DIR && 
                                myId != inIdFromCounter && hopCountFromCounter == 1) {
                        n.sendClock(new Message(inIdFromCounter, IN_DIR, 1));
                    }
                    

                    // message from clockwise neighbour 
                    if (dirFromClock == OUT_DIR) {
                        if (inIdFromClock > myId && hopCountFromClock > 1) {
                            n.sendCounterclock(
                                new Message(inIdFromClock, OUT_DIR, hopCountFromCounter - 1));
                        }
                        else if (inIdFromClock > myId && hopCountFromCounter == 1) {
                            n.sendClock(new Message(inIdFromClock, IN_DIR, 1));
                        }
                        else if (inIdFromClock == myId) {
                            n.setStatus(Node.LEADER_STATUS);
                            leader = n;
                            n.sendCounterclock(
                                new Message(Node.LEADER_STATUS, OUT_DIR, (int) Math.pow(2, phase)));
                        }
                    }
                    else if (dirFromClock == IN_DIR && 
                                myId != inIdFromClock && hopCountFromClock == 1) {
                        n.sendCounterclock(new Message(inIdFromClock, IN_DIR, 1));
                    }
                }
            }
        }
    }

    /**
     * @return the leader
     */
    public Node getLeader() {
        return leader;
    }

    private ArrayList<Node> createBiderectionalRing(int n, int a) {
        Set<Integer> ids = getRandomIds(n, n*a);
        ArrayList<Node> ring = new ArrayList<>(n);

        for (int id : ids) {
            ring.add(new Node(id));
        }

        for (int i = 0; i < n; i++) {
            ring.get(i).setClockwiseNeighbour(ring.get((i + 1 + n) % n));
            ring.get(i).setCounterclockwiseNeighbour(ring.get((i - 1 + n) % n));
        }

        return ring;
    }

    private Set<Integer> getRandomIds(int numberOfIds, int maxId) {
        Set<Integer> ids = new HashSet<Integer>();		
        Random random = new Random();
        int nextId;

        while(ids.size() < numberOfIds){
            nextId = random.nextInt(maxId) + 1;
            ids.add(nextId);
        }

        return ids;
    }
}