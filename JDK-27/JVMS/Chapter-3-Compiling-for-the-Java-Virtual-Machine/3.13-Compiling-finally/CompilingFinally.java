/*
 * @test
 * @summary JVMS 3.13: a finally clause is duplicated at every exit point, so it
 *          runs on both normal return and abrupt (throw) completion.
 * @run main CompilingFinally
 */

public class CompilingFinally {
    static int[] log = new int[2];
    static int idx = 0;

    static void record(int v) { log[idx++] = v; }

    static int returnsNormally() {
        try {
            return 1;
        } finally {
            record(1);
        }
    }

    static int throwsAndCatches() {
        try {
            try {
                throw new RuntimeException();
            } finally {
                record(2);
            }
        } catch (RuntimeException e) {
            return 3;
        }
    }

    public static void main(String[] args) {
        if (returnsNormally() != 1) throw new AssertionError("normal return");
        if (throwsAndCatches() != 3) throw new AssertionError("abrupt then catch");

        if (idx != 2 || log[0] != 1 || log[1] != 2)
            throw new AssertionError("finally not executed on all exits");

        System.out.println("PASS");
    }
}
