public class Simulation {
    private static final String USAGE = 
                "\nUsage: java Simulation n a\n\tn - number of processors (int larger than 1)" + 
                "\n\ta - multiplier of ids such that ids range from 1 to a*n (int larger than 1)" + 
                "\n\tf - (optional) flag for ordering of the ids. The default is random order:" + 
                "\n\t\t-a for clockwise ids\n\t\t-d for counterclockwise ids\n";
    private static final char CLOCK_IDS_FLAG = 'a'; // ascending ids
    private static final char COUNTER_IDS_FLAG = 'd'; // descending ids
    private static int n;
    private static int a;
    private static BidirectionalRing ring;

    public static void main(String[] args) {
        createRing(args);

        System.out.println("\nLCR finished after : " + ring.lcrAlgorithm());
        System.out.println("LCR transmitted : " + ring.getMsgCount());
        if (ring.hasCorrectLeader()) {
            System.out.println("Correct leader elected");
        } else {
            System.out.println("Error electing leader");
        }

        ring.resetRing();

        System.out.println("\nHS finished after : " + ring.hsAlgorithm());
        System.out.println("HS transmitted : " + ring.getMsgCount());
        if (ring.hasCorrectLeader()) {
            System.out.println("Correct leader elected");
        } else {
            System.out.println("Error electing leader");
        }
    }


    /**
     * Creates a Bidirectional ring given parameters from user's input
     * @param args
     */
    private static void createRing(String[] args) {
        if (args.length < 2) {
            System.out.println(USAGE);
            System.exit(1);
        }

        try {
            n = Integer.parseInt(args[0]);
            a = Integer.parseInt(args[1]);

            if (n < 1 || a < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println(USAGE);
            System.exit(1);
        }

        if (args.length > 2) {
            if (args[2].contains(CLOCK_IDS_FLAG + "")) {
                ring = new BidirectionalRing(n, a, BidirectionalRing.CLOCK_ORDERED_IDS);
            }
            else if (args[2].contains(COUNTER_IDS_FLAG + "")) {
                ring = new BidirectionalRing(n, a, BidirectionalRing.COUNTER_ORDERED_IDS);
            }
            else {
                System.out.println(USAGE);
                System.exit(1);
            }
        }
        else {
            ring = new BidirectionalRing(n, a, BidirectionalRing.RANDOM_IDS);
        }
    }
    
}