/**
 * Robert Szafarczyk, February 2019, id: 201307211
 * 
 * BidirectionalRing is a wrapper class for all nodes that form a ring. This class has access to 
 * all the nodes, and initializes the LCR and HS algorithms. It has also methods to check 
 * correctness and performance of the algorithms.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class BidirectionalRing {
    public static final int RANDOM_IDS = 1;
    public static final int CLOCK_ORDERED_IDS = 2;
    public static final int COUNTER_ORDERED_IDS = 3;
    private int idAssignment;
    private ArrayList<Node> nodes;
    
    /**
     * 
     * @param n number of processors
     * @param a alpha, multiplyier for ids
     * @param idAssignment random, clockwise or counterclockwise id assignment
     */
    public BidirectionalRing(int n, int a, int idAssignment) {
        this.idAssignment = idAssignment;
        nodes = createBiderectionalRing(n, a, idAssignment);
    }


    /**
     * The LCR Message holds only the node id. LCRMessage = <id>
     * 
     * TERMINATION STAGE: after electing itself the leader, generate a special leader message and
     * transmit. Upon receiving a leader message, a non-leader node will store the leaderID, 
     * forward the message to it's clockwise neighbour and terminate. 
     * Upon receiving a leader message, a leader node will know that all non-leader nodes have 
     * terminated and that they know the leaderID and it will terminate.
     * 
     * @return number of rounds after which the algorithm terminates
     */
    public int LCR() {
        int round = 1;

        roundLoop:
        for (;;round++) {
            nodeLoop: // MESSAGE GENERATION
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
                    n.flushRcvdMsgs();

                    if (rcvdMsg == null) { // no received message -> do nothing
                        continue nodeLoop;
                    }

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
                        // after electing itself the leader. All nodes terminated, end algorithm
                        n.terminate();
                    }
                    else if (rcvdMsg.isLeaderMsg() && n.getStatus() != Node.LEADER_STATUS) {
                        // TERMINATION STAGE -> non-leader node receives leader message. Pass on the
                        // leader meessage to clockwise neighbour, store the leader ID and terminate
                        LCRLeaderMessage leaderMsg = (LCRLeaderMessage) rcvdMsg;
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
            break roundLoop;
        }

        return round - 1;
    }

    /**
     * HSMessage = <id, direction, hopCount>.
     * 
     * TERMINATION STAGE: upon receiving an HSMessage with dir = OUT and id = myId, a node elcets 
     * itself the leader. It will know how many nodes are in the network by calculating 
     * (2^phase - hopCount - 1). Knowing numOfNodes, we can send an HSLeaderMessage to clock- and 
     * counter clock neighbours with hopCount = numOfNodes/2. This means that each non-leader node 
     * will only receive one leader message from the direction closer to the elected leader node. 
     * Upon receiving an HSLeaderMessage a non-leader node will:
     *      if hopCount > 1 then forward msg to next neigbour with hopCount - 1 and terminate
     *      else if hopCount = 1 then terminate
     * 
     * @return number of rounds after which the algorithm terminates
     */
    public int HS() {
        int IN_DIR = HSMessage.IN_DIR;
        int OUT_DIR = HSMessage.OUT_DIR;
        int round = 1;

        roundLoop:
        for (;;round++) {
            nodesLoop: // MESSAGE GENERATION
            for (Node n : nodes) {
                if (!n.isTerminated()) {
                    int myId = n.getId();

                    if (round == 1) {
                        n.setClockBuffMsg(
                                    new HSMessage(myId, OUT_DIR, (int) Math.pow(2, n.getPhase())));
                        n.setCounterBuffMsg(
                                    new HSMessage(myId, OUT_DIR, (int) Math.pow(2, n.getPhase())));
                        continue nodesLoop; // to next node
                    }
                    // else round > 1

                    // incoming messages from neighbours. If a message is null, it is ignored.
                    HSMessage fromCounter = (HSMessage) n.getRcvdMsgFromCounterclock();
                    HSMessage fromClock = (HSMessage) n.getRcvdMsgFromClock();
                    n.flushRcvdMsgs(); // only consider the received msgs in this round


                     /** UPON RECEIVING A MESSAGE FROM COUNTERCLOCKWISE NEIGHBOUR */
                    if (fromCounter != null && fromCounter.getDir() == OUT_DIR) {
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
                            // hopCount = (2^phase - getHopCount() + 1) / 2
                            int numOfNodes = 
                                (int) (Math.pow(2, n.getPhase()) - fromClock.getHopCount() + 1);
                            n.setClockBuffMsg(new HSLeaderMessage(myId, (int) numOfNodes / 2));
                            n.setCounterBuffMsg(new HSLeaderMessage(myId, (int) numOfNodes / 2));
                            n.terminate();
                            continue nodesLoop; // to next node after this terminates
                        }
                    }
                    else if (fromCounter != null && fromCounter.getDir() == IN_DIR && 
                                fromCounter.getId() != myId && fromCounter.getHopCount() == 1) {
                        n.setClockBuffMsg(new HSMessage(fromCounter.getId(), IN_DIR, 1));
                    }
                    
                    /** UPON RECEIVING A MESSAGE FROM CLOCKWISE NEIGHBOUR */
                    if (fromClock != null && fromClock.getDir() == OUT_DIR) {
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
                            // hopCount = (2^phase - getHopCount() + 1) / 2
                            int numOfNodes = 
                                (int) (Math.pow(2, n.getPhase()) - fromCounter.getHopCount() + 1);
                            n.setClockBuffMsg(new HSLeaderMessage(myId, (int) numOfNodes / 2));
                            n.setCounterBuffMsg(new HSLeaderMessage(myId, (int) numOfNodes / 2));
                            n.terminate();
                            continue nodesLoop; // to next node after this terminates
                        }
                    }
                    else if (fromClock != null && fromClock.getDir() == IN_DIR && 
                                fromClock.getId() != myId && fromClock.getHopCount() == 1) {
                        n.setCounterBuffMsg(new HSMessage(fromClock.getId(), IN_DIR, 1));
                    }


                    /** TERMINATION STAGE: UPON RECEIVING A LEADER MESSAGE */
                    // from clockwise neighbour
                    if (fromClock != null && fromClock.isLeaderMsg() && 
                            fromClock.getHopCount() > 1) {
                        HSLeaderMessage fromClockLeader = (HSLeaderMessage) fromClock;
                        n.setLeaderId(fromClockLeader.getLeaderId());
                        n.setCounterBuffMsg(new HSLeaderMessage(fromClockLeader.getLeaderId(), 
                                                                    fromClock.getHopCount() - 1));
                        n.terminate();
                    }
                    else if (fromClock != null && fromClock.isLeaderMsg() && 
                            fromClock.getHopCount() == 1) {
                        HSLeaderMessage fromClockLeader = (HSLeaderMessage) fromClock;
                        n.setLeaderId(fromClockLeader.getLeaderId());
                        n.terminate();
                    }
                    // from counterclockwise neighbour
                    if (fromCounter != null && fromCounter.isLeaderMsg() && 
                            fromCounter.getHopCount() > 1) {
                        HSLeaderMessage fromCounterLeader = (HSLeaderMessage) fromCounter;
                        n.setLeaderId(fromCounterLeader.getLeaderId());
                        n.setClockBuffMsg(new HSLeaderMessage(fromCounterLeader.getLeaderId(),
                                                                    fromCounter.getHopCount() - 1));
                        n.terminate();
                    }
                    else if (fromCounter != null && fromCounter.isLeaderMsg() && 
                            fromCounter.getHopCount() == 1) {
                        HSLeaderMessage fromCounterLeader = (HSLeaderMessage) fromCounter;
                        n.setLeaderId(fromCounterLeader.getLeaderId());
                        n.terminate();
                    }
                    

                    /** PHASE++ STAGE: UPON RECEIVING A MESSAGE FROM CLOCK- AND COUNTERCLOCK WITH 
                     * DIR = IN AND inId = myId AND hoCount = 1 -> START NEXT PHASE */
                    if (fromClock != null && fromCounter != null && 
                            fromClock.getDir() == IN_DIR && fromCounter.getDir() == IN_DIR &&
                            fromClock.getId() == myId && fromCounter.getId() == myId &&
                            fromClock.getHopCount() == 1 && fromCounter.getHopCount() == 1) {
                        n.incPhase();
                        n.setClockBuffMsg(
                                    new HSMessage(myId, OUT_DIR, (int) Math.pow(2, n.getPhase())));
                        n.setCounterBuffMsg(
                                    new HSMessage(myId, OUT_DIR, (int) Math.pow(2, n.getPhase())));
                    }
                }
            }

            // MESSAGE TRANSMISSION -> each node sends the generated message to both neighbour
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
            break roundLoop;
        }


        return round - 1;
    }


    /**
     * @return true if all nodes elected the same, correct leader
     */
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
     * @return total number of messages transmitted by all nodes 
     */
    public int getMsgCount() {
        int msgCount = 0;
        for (Node n : nodes) {
            msgCount += n.getMsgCounter();
        }

        return msgCount;
    }

    public void resetRing() {
        nodes.forEach(n -> n.resetNode());  
    }


    /**
     * @param n
     * @param a
     * @return ArrayList<Node> of created, interconnected nodes that form a ring
     */
    private ArrayList<Node> createBiderectionalRing(int n, int a, int idAssignment) {
        ArrayList<Node> ring = new ArrayList<>();
        ArrayList<Integer> ids = getIds(n, n*a);

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
     * @return ArrayList of 'idAssignment' integers ranging from 1 to maxId
     */
    private ArrayList<Integer> getIds(int numberOfIds, int maxId) {
        Set<Integer> idsSet = new HashSet<>();
        Random random = new Random();
        int nextId;

        while(idsSet.size() < numberOfIds) {
            nextId = random.nextInt(maxId) + 1;
            idsSet.add(nextId);
        }
        ArrayList<Integer> ids = new ArrayList<Integer>(idsSet);

        // sort according to id assignment
        if (idAssignment == CLOCK_ORDERED_IDS) {
            ids.sort(null);
        }
        else if (idAssignment == COUNTER_ORDERED_IDS) {
            ids.sort(Collections.reverseOrder());		
        }
        // else random order, don't sort

        return ids;
    }
}