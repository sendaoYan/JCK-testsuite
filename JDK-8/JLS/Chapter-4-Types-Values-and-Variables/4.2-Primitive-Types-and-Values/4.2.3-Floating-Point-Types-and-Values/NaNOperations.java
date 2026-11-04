/*
 * @test
 * @summary JLS 4.2.3 (Java SE 8): NaN is unordered. The numerical comparison
 *          operators <, <=, >, >= return false if either operand is NaN; ==
 *          returns false if either operand is NaN; != returns true if either
 *          operand is NaN. In particular, x != x is true iff x is NaN.
 * @run main NaNOperations
 */

public class NaNOperations {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        double nan = Double.NaN;
        float fnan = Float.NaN;

        check(!(nan < 1.0), "NaN < x is false");
        check(!(nan <= 1.0), "NaN <= x is false");
        check(!(nan > 1.0), "NaN > x is false");
        check(!(nan >= 1.0), "NaN >= x is false");
        check(!(1.0 < nan), "x < NaN is false");
        check(!(1.0 > nan), "x > NaN is false");

        check(!(nan == nan), "NaN == NaN is false");
        check(nan != nan, "NaN != NaN is true");
        check(nan != 1.0, "NaN != x is true");
        check(nan == nan == false, "NaN == NaN is exactly false");

        // x != x is true if and only if x is NaN.
        check(nan != nan, "NaN satisfies x != x");
        check(!(1.0 != 1.0), "ordinary values do not satisfy x != x");
        check(fnan != fnan, "Float.NaN satisfies x != x");

        // (x < y) == !(x >= y) is false when x or y is NaN.
        double x = nan, y = 1.0;
        check((x < y) != !(x >= y), "(x<y) == !(x>=y) is false when x is NaN");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
