public class Simulation {
    private static int n;
    private static int a;
    private static BidirectionalRing ring;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Simulation n a\n\tn - number of processors (integer)" + 
                                "\n\ta - range of ids from 1 to an (integer)");
            System.exit(1);
        }

        try {
            n = Integer.parseInt(args[0]);
            a = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Usage: java Simulation n a\n\tn - number of processors (integer)" + 
                                "\n\ta - range of ids from 1 to an (integer)");
            System.exit(1);
        }

        ring = new BidirectionalRing(n, a);
        ring.lcrAlgorithm();
        // System.out.println(ring.getLeader().getId());

        // ring.resetRing();
        // ring.hsAlgorithm();
        // System.out.println(ring.getLeader().getId());

    }
    
}