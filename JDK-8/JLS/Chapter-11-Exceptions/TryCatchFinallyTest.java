/*
 * @test
 * @summary JLS 11.3 (Java SE 8): when an exception is thrown, the matching catch
 *          block runs, and finally always runs whether or not an exception occurs.
 * @run main TryCatchFinallyTest
 */

public class TryCatchFinallyTest {
    static int trace;

    static void m(boolean fail) {
        try {
            trace = 1;
            if (fail) throw new IllegalArgumentException("x");
            trace = 2;
        } catch (IllegalArgumentException e) {
            trace = 3;
        } finally {
            trace += 10;
        }
    }

    public static void main(String[] args) {
        m(false);
        if (trace != 12) throw new AssertionError("try then finally");
        m(true);
        if (trace != 13) throw new AssertionError("catch then finally");
        System.out.println("PASS");
    }
}
