/*
 * @test
 * @summary JLS 11.3: during propagation of an uncaught exception, the finally
 *          clause is executed even if no matching catch clause is found.
 * @run main FinallyRunsWithoutCatch
 */

public class FinallyRunsWithoutCatch {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean ran;

    public static void main(String[] args) {
        ran = false;
        try {
            try {
                throw new Error("boom");
            } finally {
                ran = true;
            }
        } catch (Error expected) {
            // expected, caught here
        }
        check(ran, "finally ran during propagation before the exception was caught");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
