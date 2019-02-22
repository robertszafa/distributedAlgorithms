import java.io.Console;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map.Entry;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;  
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.*;
import com.panayotis.gnuplot.GNUPlotParameters;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.FileDataSet;
import com.panayotis.gnuplot.layout.StripeLayout;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.NamedPlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.swing.JPlot;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;
import com.panayotis.gnuplot.utils.Debug;



public class Simulation {
    private static final String CLOCK_IDS_FLAG = "a"; // ascending ids
    private static final String COUNTER_IDS_FLAG = "d"; // descending ids
    private static final String RANDOM_IDS_FLAG = "r"; // descending ids
    private static final String USAGE = "\nUsage: java Simulation n a" + 
            "\n\tn - (> 1) max number of processors. Program will loop from 1 to n" + 
            "\n\ta - (> 1) multiplier of ids such that ids range from 1 to a*n" + 
            "\n\tf - flag for ordering of the ids:" + 
            "\n\t\t-a for clockwise ids\n\t\t-d for counterclockwise ids\n\t\tr - for random order";
    private static String idOrderPlotTitle;
    private static BidirectionalRing ring;
    private static int n; // number of processors
    private static int a; // alpha - multiplier for id range
    private static int idOrder; // state variable for the BididerectionalRing() constructor
    /* int arrays to hold num of msgs and rounds for LCR and HS */
    private static int[] msgCountLCR;
    private static int[] roundCountLCR;
    private static int[] msgCountHS;
    private static int[] roundCountHS;


    public static void main(String[] args) {
        try {
            // get n, a, idOrder
            processInput(args);
        } catch (Exception NumberFormatException) {
            System.out.println(USAGE);
            System.exit(1);
        }

        msgCountLCR = new int[n];
        roundCountLCR = new int[n];
        msgCountHS = new int[n];
        roundCountHS = new int[n];

        /* SIMULATION: loop through all ring sizes from 2 to n */
        for (int ringSize = 2; ringSize <= n; ringSize++) {
            // print progress for the user
            System.out.print("Progress " + (ringSize - 1) + " / " + n + "\r");

            ring = new BidirectionalRing(ringSize, a, idOrder);
            roundCountLCR[ringSize - 2] = ring.LCR();
            msgCountLCR[ringSize - 2] = ring.getMsgCount();

            // clear all info stored by the nodes. Leave only their id's
            // nodes will be in the same state as when the ring was created the first time
            ring.resetRing();
            roundCountHS[ringSize - 2] = ring.HS();
            msgCountHS[ringSize - 2] = ring.getMsgCount();
        }

        /* ANALYSIS */
        // plot time complexity of LCR and HS
        JavaPlot p = new JavaPlot();
        p.setTitle("Time Complexity " + idOrderPlotTitle);
        plotGraph(p, "LCR", "Rounds", roundCountLCR);
        plotGraph(p, "HS", "Rounds", roundCountHS);
        p.addPlot("x");
        p.plot();

        // plot communication complexity of LCR and HS
        p = new JavaPlot();
        p.setTitle("Communication Complexity " + idOrderPlotTitle);
        plotGraph(p, "LCR", "Messages", msgCountLCR);
        plotGraph(p, "HS", "Messages", msgCountHS);
        p.addPlot("x * x");
        p.plot();
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

}