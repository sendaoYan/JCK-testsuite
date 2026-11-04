/*
 * @test
 * @summary JLS 10.6: if execution of a variable initializer completes abruptly,
 *          the array initializer completes abruptly for the same reason.
 * @run main ArrayInitializerAbruptCompletion
 */

public class ArrayInitializerAbruptCompletion {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int boom() {
        throw new IllegalStateException("boom");
    }

    public static void main(String[] args) {
        boolean threw = false;
        try {
            int[] a = { 1, boom(), 3 };
        } catch (IllegalStateException expected) {
            threw = true;
            check("boom".equals(expected.getMessage()), "same exception reason propagated");
        }
        check(threw, "array initializer completed abruptly");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
