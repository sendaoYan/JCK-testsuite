/*
 * @test
 * @summary JLS 15.21.1 (Java SE 8): numeric equality operators treat NaN as
 *          never equal to anything (so NaN != NaN is true) and treat -0.0 as
 *          equal to 0.0, even though the two zeros have distinct signs.
 * @run main NaNEqualityAndSignedZero
 */

public class NaNEqualityAndSignedZero {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        double nan = Double.NaN;
        check(nan != nan, "NaN != NaN is true");
        check(!(nan == nan), "NaN == NaN is false");
        check(Double.NaN != Double.NaN, "Double.NaN != Double.NaN");
        check(Double.isNaN(nan), "Double.isNaN(nan)");

        float fnan = Float.NaN;
        check(fnan != fnan, "float NaN != NaN");

        check(0.0 == -0.0, "0.0 == -0.0 is true");
        check(!(0.0 != -0.0), "0.0 != -0.0 is false");
        check(1.0 / -0.0 == Double.NEGATIVE_INFINITY, "1.0/-0.0 == -Inf");
        check(1.0 / 0.0 == Double.POSITIVE_INFINITY, "1.0/0.0 == +Inf");
        check(-0.0f == 0.0f, "float -0.0 == 0.0");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
