/*
 * @test
 * @summary JLS 11.3: if a finally clause completes abruptly, the reason for the
 *          abrupt completion of the try block is discarded and the new reason is
 *          propagated.
 * @run main FinallyOverridesException
 */

public class FinallyOverridesException {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        boolean threw = false;
        try {
            try {
                throw new IllegalArgumentException("original");
            } finally {
                throw new IllegalStateException("replacement");
            }
        } catch (IllegalStateException expected) {
            threw = true;
            check("replacement".equals(expected.getMessage()),
                  "finally exception replaces the try exception");
        }
        check(threw, "finally exception propagated");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
