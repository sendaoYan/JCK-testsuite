/*
 * @test
 * @summary JLS 12.4.2 (Java SE 8, step 3): a recursive request to initialize a
 *          class already being initialized by the current thread completes
 *          normally, observing default values rather than deadlocking.
 * @run main RecursiveInitializationOnce
 */

public class RecursiveInitializationOnce {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class A {
        static int x = init();
        static int y = 42;
        static int init() {
            return A.y;
        }
    }

    public static void main(String[] args) {
        check(A.x == 0, "re-entrant read observes the default value (0)");
        check(A.y == 42, "later initializer still executed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
