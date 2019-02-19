import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

public class BidirectionalRing {
    private ArrayList<Node> nodes;
    
    public BidirectionalRing(int n, int a) {
        nodes = createBiderectionalRing(n, a);
    }


    public void resetRing() {
        nodes.forEach(n -> n.resetNode());  
    }

    public boolean hasCorrectLeader() {
        int leaderId = nodes.get(0).getLeaderId();
        for (Node n : nodes) {
            if (n.getLeaderId() != leaderId || n.getId() > leaderId) {
                return false;
            }
        }

        return true;
    }


    /**
     * The LCR Message holds only the node id. LCRMessage = <id>
     * 
     * TERMINATION STAGE: after electing itself the leader, generate a special leader message and
     * transmit. Upon receiving a leader message, a non-leader node will store the leaderID, 
     * forward the message to it's clockwise neighbour and terminate. 
     * Upon receiving a leader message, a leader node will know that all non-leader nodes have 
     * terminated and that they know the leaderID and it will terminate.
     */
    public void lcrAlgorithm() {
        int round = 1;

        roundLoop:
        for (;;round++) {
            nodeLoop:
            for (Node n : nodes) {
                if (!n.isTerminated()) {
                    if (round == 1) {
                        // MESSAGE GENERATION -> each node generates a message with it's ID
                        n.setClockBuffMsg(new LCRMessage(n.getId()));
                        continue nodeLoop; // to next node
                    } 
                    // else round > 1

                    int myId = n.getId();
                    LCRMessage rcvdMsg = (LCRMessage) n.getRcvdMsgFromCounterclock();
                    int inId = rcvdMsg.getId();

                    if (inId > myId) { 
                        // upon receiving a larger ID -> generate a msg with inId to clockwise node
                        n.setClockBuffMsg(new LCRMessage(inId));
                    }
                    else if (inId == myId) {
                        // upon receiving an ID equal to myID -> elect yourself the leader
                        n.setStatus(Node.LEADER_STATUS);
                        n.setLeaderId(myId);
                        
                        // START TERMINATION STAGE -> leader node generates a leader message 
                        n.setClockBuffMsg(new LCRLeaderMessage(myId));
                    }
                    else if (rcvdMsg.isLeaderMsg() && n.getStatus() == Node.LEADER_STATUS) {
                        // END TERMINATION STAGE -> leader receives the leader message which it sent 
                        // after electing itself the leader. All nodes terminates, end algorithm
                        n.terminate();
                    }
                    else if (rcvdMsg.isLeaderMsg() && n.getStatus() != Node.LEADER_STATUS) {
                        // TERMINATION STAGE -> non-leader node receives leader message. Pass on the
                        // leader meessage to clockwise neighbour, store the leader ID and terminate
                        LCRLeaderMessage leaderMsg = (LCRLeaderMessage) n.getRcvdMsgFromCounterclock();
                        n.setLeaderId(leaderMsg.getLeaderId());
                        n.setClockBuffMsg(leaderMsg);
                        n.terminate();
                    }
                }
            }


            // MESSAGE TRANSMISSION -> each node sends the generated msg to its clockwise neighbour
            for (Node n : nodes) {
                n.sendClock();
            }
            
            // TERMINATION STAGE (for simulation) -> end if all nodes have terminated
            for (Node n : nodes) {
                if (!n.isTerminated()) {
                    continue roundLoop;
                }
            }
            System.out.println("Nodes terminated in " + round);
            break roundLoop;
        }
    }

    /**
     * HSMessage = <id, direction, hopCount>.
     * 
     * TERMINATION STAGE: upon receiving an HSMessage with dir = OUT and id = myId, a node elcets 
     * itself the leader It will also know how many nodes are in the network by calculating 
     * 2^phase - hopCount. Knowing numOfNodes, we can send an HSLeaderMessage to clock- and 
     * counterclock neighbours with hopCount = numOfNodes/2. This means that each non-leader node 
     * will only receive one leader message from the direction closer to the elected leader node. 
     * Upon receiving an HSLeaderMessage a non-leader node will:
     *      if hopCount > 1 then forward msg to next neigbour with hopCount - 1 and terminate
     *      else if hopCount = 1 then terminate
     * 
     * End the algorithm once all nodes terminate
     */
    public void hsAlgorithm() {
        int IN_DIR = 1;
        int OUT_DIR = 0;
        int phase = 0;
        int round = 1;

        roundLoop:
        for (;;round++) {
            nodesLoop:
            for (Node n : nodes) {
                if (!n.isTerminated()) {
                    int myId = n.getId();

                    if (round == 1) {
                        // MESSAGE GENERATION -> generate <myIDi, out, 1> for clock- and counterclock
                        n.setClockBuffMsg(new HSMessage(myId, OUT_DIR, (int) Math.pow(2, phase)));
                        n.setCounterBuffMsg(new HSMessage(myId, OUT_DIR, (int) Math.pow(2, phase)));
                        continue nodesLoop; // to next node
                    }
                    // else round > 1

                    // incoming messages from neighbours
                    HSMessage fromCounter = (HSMessage) n.getRcvdMsgFromCounterclock();
                    HSMessage fromClock = (HSMessage) n.getRcvdMsgFromClock();

                    /** TERMINATION STAGE: UPON RECEIVING A LEADER MESSAGE */
                    // from clockwise neighbour
                    if (fromClock.isLeaderMsg() && fromClock.getHopCount() > 1) {
                        HSLeaderMessage fromClockLeader = (HSLeaderMessage) fromClock;
                        n.setLeaderId(fromClockLeader.getLeaderId());
                        n.setCounterBuffMsg(new HSLeaderMessage(fromClockLeader.getLeaderId(), 
                                                                    fromClock.getHopCount() - 1));
                        n.terminate();
                        continue nodesLoop;
                    }
                    else if (fromClock.isLeaderMsg() && fromClock.getHopCount() == 1) {
                        HSLeaderMessage fromClockLeader = (HSLeaderMessage) fromClock;
                        n.setLeaderId(fromClockLeader.getLeaderId());
                        n.terminate();
                        continue nodesLoop;
                    }
                    // from counterclockwise neighbour
                    if (fromCounter.isLeaderMsg() && fromCounter.getHopCount() > 1) {
                        HSLeaderMessage fromCounterLeader = (HSLeaderMessage) fromCounter;
                        n.setLeaderId(fromCounterLeader.getLeaderId());
                        n.setClockBuffMsg(new HSLeaderMessage(fromCounterLeader.getLeaderId(),
                                                                    fromClock.getHopCount() - 1));
                        n.terminate();
                        continue nodesLoop;
                    }
                    else if (fromCounter.isLeaderMsg() && fromCounter.getHopCount() == 1) {
                        HSLeaderMessage fromCounterLeader = (HSLeaderMessage) fromCounter;
                        n.setLeaderId(fromCounterLeader.getLeaderId());
                        n.terminate();
                        continue nodesLoop;
                    }
                    
                    /** UPON RECEIVING A MESSAGE FROM CLOCK- AND COUNTERCLOCK WITH 
                     * DIR = IN AND inId = myId AND hoCount = 1 -> START NEXT PHASE */
                    if (fromClock.getDir() == IN_DIR && fromCounter.getDir() == IN_DIR &&
                            fromClock.getId() == myId && fromCounter.getId() == myId &&
                            fromClock.getHopCount() == 1 && fromCounter.getHopCount() == 1) {
                        phase++;
                        n.setClockBuffMsg(new HSMessage(myId, OUT_DIR, (int) Math.pow(2, phase)));
                        n.setCounterBuffMsg(new HSMessage(myId, OUT_DIR, 
                                                        (int) Math.floor(Math.pow(2, phase)/2)));
                        continue nodesLoop; // to next node
                    }

                    /** UPON RECEIVING A MESSAGE FROM COUNTERCLOCKWISE NEIGHBOUR */
                    if (fromCounter.getDir() == OUT_DIR) {
                        if (fromCounter.getId() > myId && fromCounter.getHopCount() > 1) {
                            n.setClockBuffMsg(new HSMessage(fromCounter.getId(), OUT_DIR, 
                                                                    fromCounter.getHopCount() - 1));
                        }
                        else if (fromCounter.getId() > myId && fromCounter.getHopCount() == 1) {
                            n.setCounterBuffMsg(new HSMessage(fromCounter.getId(), IN_DIR, 1));
                        }
                        else if (fromCounter.getId() == myId) {
                            n.setStatus(Node.LEADER_STATUS);
                            n.setLeaderId(n.getId());
                            // TERMINATION STAGE: generate a leader message with 
                            // hopCount = (2^phase - getHopCount()) / 2
                            n.setClockBuffMsg(new HSLeaderMessage(myId, 
                                        (int) (Math.pow(2, phase) - fromCounter.getHopCount()) / 2));
                            n.terminate();
                        }
                    }
                    else if (fromCounter.getDir() == IN_DIR && myId != fromCounter.getId() && 
                                fromCounter.getHopCount() == 1) {
                        n.setClockBuffMsg(new HSMessage(fromCounter.getId(), IN_DIR, 1));
                    }
                    
                    /** UPON RECEIVING A MESSAGE FROM CLOCKWISE NEIGHBOUR */
                    if (fromClock.getDir() == OUT_DIR) {
                        if (fromClock.getId() > myId && fromClock.getHopCount() > 1) {
                            n.setCounterBuffMsg(new HSMessage(fromClock.getId(), OUT_DIR, 
                                                                    fromClock.getHopCount() - 1));
                        }
                        else if (fromClock.getId() > myId && fromClock.getHopCount() == 1) {
                            n.setClockBuffMsg(new HSMessage(fromClock.getId(), IN_DIR, 1));
                        }
                        else if (fromClock.getId() == myId) {
                            n.setStatus(Node.LEADER_STATUS);
                            n.setLeaderId(n.getId());
                            // TERMINATION STAGE: generate a leader message with 
                            // hopCount = (2^phase - getHopCount()) / 2
                            n.setCounterBuffMsg(new HSLeaderMessage(myId, 
                                        (int) (Math.pow(2, phase) - fromClock.getHopCount()) / 2));
                            n.terminate();
                        }
                    }
                    else if (fromClock.getDir() == IN_DIR && 
                                myId != fromClock.getId() && fromClock.getHopCount() == 1) {
                        n.setCounterBuffMsg(new HSMessage(fromClock.getId(), IN_DIR, 1));
                    }
                }
            }

            // MESSAGE TRANSMISSION -> each node sends the message to it's clockwise neighbour
            for (Node n : nodes) {
                n.sendClock();
                n.sendCounterclock();
            }

            // TERMINATION STAGE (for simulation) -> end if all nodes have terminated
            for (Node n : nodes) {
                if (!n.isTerminated()) {
                    continue roundLoop;
                }
            }
            System.out.println("Nodes terminated in " + round);
            break roundLoop;
        }
    }


    /**
     * @param n
     * @param a
     * @return ArrayList<Node> of created, interconnected nodes that form a ring
     */
    private ArrayList<Node> createBiderectionalRing(int n, int a) {
        // set of IDs to choose from
        Set<Integer> ids = getRandomIds(n, n*a);
        ArrayList<Node> ring = new ArrayList<>(n);

        // create nodes
        for (int id : ids) {
            ring.add(new Node(id));
        }

        // connect nodes biderectionally
        for (int i = 0; i < n; i++) {
            // use the modulo operator to connect the last node to the first one and vice versa
            ring.get(i).setClockwiseNeighbour(ring.get((i + 1 + n) % n));
            ring.get(i).setCounterclockwiseNeighbour(ring.get((i - 1 + n) % n));
        }

        return ring;
    }

    /**
     * @param numberOfIds
     * @param maxId
     * @return set of random integers ranging from 1 to maxId
     */
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

    /**
     * @param id
     * @return node from nodes with id
     */
    private Node getNode(int id) {
        for (Node n : nodes) {
            if (n.getId() == id) {
                return n;
            }
        }

        return null;
    }


    /**
     * @return leader node from nodes
     */
    private Node getNode() {
        for (Node n : nodes) {
            if (n.getStatus() == Node.LEADER_STATUS) {
                return n;
            }
        }

        return null;
    }
}