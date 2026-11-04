/*
 * @test
 * @summary JLS 10.3 / 15.10.1: in an array creation expression, the dimension
 *          expressions are evaluated left-to-right before any array is allocated.
 * @run main ArrayCreationDimensionOrder
 */

public class ArrayCreationDimensionOrder {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static StringBuilder order = new StringBuilder();
    static int dim(int v) {
        order.append(v);
        return v;
    }

    public static void main(String[] args) {
        int[][][] a = new int[dim(1)][dim(2)][dim(3)];
        check(order.toString().equals("123"), "dimension expressions evaluated left-to-right: " + order);
        check(a.length == 1 && a[0].length == 2 && a[0][0].length == 3, "dimensions applied");

        // The left-to-right order is observable even when a later dimension throws.
        order.setLength(0);
        try {
            int[][] tmp = new int[dim(1)][dim(-1)];
        } catch (NegativeArraySizeException expected) {
            // expected
        }
        check(order.toString().equals("1-1"), "all dimension expressions evaluated even on failure: " + order);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
