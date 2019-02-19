public class Simulation {
    private static final String USAGE = 
                "Usage: java Simulation n a\n\tn - number of processors (int larger than 1)" + 
                "\n\ta - multiplier of ids such that ids range from 1 to a*n (int larger than 1)";
    private static int n;
    private static int a;
    private static BidirectionalRing ring;

    public static void main(String[] args) {
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

        ring = new BidirectionalRing(n, a);
        ring.lcrAlgorithm();
        ring.resetRing();
        ring.hsAlgorithm();
    }
    
}