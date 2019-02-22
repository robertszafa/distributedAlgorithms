import java.util.stream.IntStream;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;  
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;



public class Simulation {
    private static final String USAGE = "\nUsage: java Simulation n a" + 
            "\n\tn - (> 1) max number of processors. Program will loop from 1 to n" + 
            "\n\ta - (> 1) multiplier of ids such that ids range from 1 to a*n" + 
            "\n\tf - flag for ordering of the ids:" + 
            "\n\t\t-a for clockwise ids\n\t\t-d for counterclockwise ids\n\t\tr - for random order";
    private static final int repeatSizeofN = 100;
    private static final String CLOCK_IDS_FLAG = "a"; // ascending ids
    private static final String COUNTER_IDS_FLAG = "d"; // descending ids
    private static final String RANDOM_IDS_FLAG = "r"; // descending ids
    private static String idOrderPlotTitle;
    private static int idOrder; // state variable for the BididerectionalRing() constructor
    private static int n; // number of processors
    private static int a; // alpha - multiplier for id range
    private static int errorCountLCR, errorCountHS; // count when an incorrect leader was elected
    private static BidirectionalRing ring;
    // int arrays to hold num of msgs and rounds. Will hold average case for random id assignment
    private static int[] msgCountLCR, roundCountLCR, msgCountHS, roundCountHS;
    // int arrays to hold num of msgs and rounds in best and worst cases for random id assignment
    private static int[] msgCountLCRBest, roundCountLCRBest, msgCountHSBest, roundCountHSBest;
    private static int[] msgCountLCRWorst, roundCountLCRWorst, msgCountHSWorst, roundCountHSWorst;
    private static int[] tempMsgCountLCR, tempRoundCountLCR, tempMsgCountHS, tempRoundCountHS;


    public static void main(String[] args) {
        try {
            // get n, a, idOrder
            processInput(args);
        } catch (Exception NumberFormatException) {
            System.out.println(USAGE);
            System.exit(1);
        }

        initializeCountArrays();
        errorCountLCR = 0;
        errorCountHS = 0;

        /* SIMULATION: loop through all ring sizes from 2 to n */
        for (int ringSize = 2; ringSize <= n; ringSize++) {
            // print progress for the user
            System.out.print("Progress " + (ringSize) + "/" + n + 
                                "  ||  LCR error rate " + errorCountLCR + "/" + n +
                                "  ||  HS error rate " + errorCountHS + "/" + n + "\r");

            if (idOrder == BidirectionalRing.RANDOM_IDS) {
                for (int i = 0; i < repeatSizeofN; i++) {
                    ring = new BidirectionalRing(ringSize, a, idOrder);
                    tempRoundCountLCR[i] = ring.LCR();
                    tempMsgCountLCR[i] = ring.getMsgCount();
                    errorCountLCR = (ring.hasCorrectLeader()) ? errorCountLCR + 1 : errorCountLCR;

                    ring.resetRing();
                    tempRoundCountHS[i] = ring.HS();
                    tempMsgCountHS[i] = ring.getMsgCount();
                    errorCountHS = (ring.hasCorrectLeader()) ? errorCountHS + 1 : errorCountHS;
                }
                // average case
                roundCountLCR[ringSize - 2] = IntStream.of(tempRoundCountLCR).sum() / repeatSizeofN;
                msgCountLCR[ringSize - 2] = IntStream.of(tempMsgCountLCR).sum() / repeatSizeofN;
                roundCountHS[ringSize - 2] = IntStream.of(tempRoundCountHS).sum() / repeatSizeofN;
                msgCountHS[ringSize - 2] = IntStream.of(tempMsgCountHS).sum() / repeatSizeofN;
                // worst case
                roundCountLCRWorst[ringSize - 2] = IntStream.of(tempRoundCountLCR).max().getAsInt();
                msgCountLCRWorst[ringSize - 2] = IntStream.of(tempMsgCountLCR).max().getAsInt();
                roundCountHSWorst[ringSize - 2] = IntStream.of(tempRoundCountHS).max().getAsInt();
                msgCountHSWorst[ringSize - 2] = IntStream.of(tempMsgCountHS).max().getAsInt();
                // best case
                roundCountLCRBest[ringSize - 2] = IntStream.of(tempRoundCountLCR).min().getAsInt();
                msgCountLCRBest[ringSize - 2] = IntStream.of(tempMsgCountLCR).min().getAsInt();
                roundCountHSBest[ringSize - 2] = IntStream.of(tempRoundCountHS).min().getAsInt();
                msgCountHSBest[ringSize - 2] = IntStream.of(tempMsgCountHS).min().getAsInt();
            }
            else {
                ring = new BidirectionalRing(ringSize, a, idOrder);
                roundCountLCR[ringSize - 2] = ring.LCR();
                msgCountLCR[ringSize - 2] = ring.getMsgCount();
                errorCountLCR = (ring.hasCorrectLeader()) ? errorCountLCR : errorCountLCR + 1;

                // clear all info stored by the nodes. Leave only their id's
                // nodes will be in the same state as when the ring was created the first time
                ring.resetRing();
                roundCountHS[ringSize - 2] = ring.HS();
                msgCountHS[ringSize - 2] = ring.getMsgCount();
                errorCountHS = (ring.hasCorrectLeader()) ? errorCountHS : errorCountHS + 1;
            }
        }

        /* ANALYSIS */
        // plot Time Complexity of LCR and HS
        JavaPlot p = new JavaPlot();
        p.setTitle("Time Complexity " + idOrderPlotTitle);
        if (idOrder == BidirectionalRing.RANDOM_IDS) {
            plotGraph(p, "Average Case LCR", "Rounds", roundCountLCR);
            plotGraph(p, "Worst Case LCR", "Rounds", roundCountLCRWorst);
            plotGraph(p, "Best Case LCR", "Rounds", roundCountLCRBest);
            plotGraph(p, "Average Case HS", "Rounds", roundCountHS);
            plotGraph(p, "Worst Case HS", "Rounds", roundCountHSWorst);
            plotGraph(p, "Best Case HS", "Rounds", roundCountHSBest);
        }
        else {
            plotGraph(p, "LCR", "Rounds", roundCountLCR);
            plotGraph(p, "HS", "Rounds", roundCountHS);
        }
        p.addPlot("x");
        p.plot();

        // plot communication complexity of LCR and HS
        p = new JavaPlot();
        p.setTitle("Communication Complexity " + idOrderPlotTitle);
        if (idOrder == BidirectionalRing.RANDOM_IDS) {
            plotGraph(p, "Average Case LCR", "Messages", msgCountLCR);
            plotGraph(p, "Worst Case LCR", "Messages", msgCountLCRWorst);
            plotGraph(p, "Best Case LCR", "Messages", msgCountLCRBest);
            plotGraph(p, "Average Case HS", "Messages", msgCountHS);
            plotGraph(p, "Worst Case HS", "Messages", msgCountHSWorst);
            plotGraph(p, "Best Case HS", "Messages", msgCountHSBest);
        }
        else {
            plotGraph(p, "LCR", "Messages", msgCountLCR);
            plotGraph(p, "HS", "Messages", msgCountHS);
        }
        p.addPlot("x * x");
        p.plot();


        System.out.println();
    }



    /**
     * 
     * @param p
     * @param title of what the data represents
     * @param ylabel
     * @param data
     */
    private static void plotGraph(JavaPlot p, String title, String ylabel, int[] data) {
        // convert data to 2D array
        int[][] data2D = new int[data.length + 1][1];
        for (int i = 0; i < data.length; i++) {
            data2D[i + 1] = new int[1];
            data2D[i + 1][0] = data[i];
        }

        p.getAxis("x").setBoundaries(0, n);
        p.getAxis("x").setLabel("Ring Size");
        p.getAxis("y").setLabel(ylabel);
        p.setKey(JavaPlot.Key.TOP_LEFT);

        PlotStyle myPlotStyle = new PlotStyle();
        myPlotStyle.setStyle(Style.LINES);
            
        DataSetPlot dataPlot = new DataSetPlot(data2D);
        dataPlot.setPlotStyle(myPlotStyle);
        dataPlot.setTitle(title);
        p.addPlot(dataPlot);
    }

    /**
     * Creates a Bidirectional ring given parameters from user's input.
     * 
     * @param args
     */
    private static void processInput(String[] args) throws NumberFormatException {
        if (args.length < 3) {
            throw new NumberFormatException();
        }

        n = Integer.parseInt(args[0]);
        a = Integer.parseInt(args[1]);

        if (n < 1 || a < 1) {
            throw new NumberFormatException();
        }

        if (args[2].contains(CLOCK_IDS_FLAG)) {
            idOrder = BidirectionalRing.CLOCK_ORDERED_IDS;
            idOrderPlotTitle = "(clockwise ids)";
        } else if (args[2].contains(COUNTER_IDS_FLAG)) {
            idOrder = BidirectionalRing.COUNTER_ORDERED_IDS;
            idOrderPlotTitle = "(counterclockwise ids)";
        } else if (args[2].contains(RANDOM_IDS_FLAG)) {
            idOrder = BidirectionalRing.RANDOM_IDS;
            idOrderPlotTitle = "(random ids)";
        }
        else {
            throw new NumberFormatException();
        }
    }

    private static void initializeCountArrays() {
        msgCountLCR = new int[n];
        roundCountLCR = new int[n];
        msgCountHS = new int[n];
        roundCountHS = new int[n];
        if (idOrder == BidirectionalRing.RANDOM_IDS) {
            msgCountLCRBest = new int[n];
            roundCountLCRBest = new int[n];
            msgCountHSBest = new int[n];
            roundCountHSBest = new int[n];
            msgCountLCRWorst = new int[n];
            roundCountLCRWorst = new int[n];
            msgCountHSWorst = new int[n];
            roundCountHSWorst = new int[n];
            tempMsgCountLCR = new int[repeatSizeofN];
            tempRoundCountLCR = new int[repeatSizeofN];
            tempMsgCountHS = new int[repeatSizeofN];
            tempRoundCountHS = new int[repeatSizeofN];
        }
    }

}