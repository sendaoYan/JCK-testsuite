/*
 * @test
 * @summary JLS 12.4.2 (Java SE 8, step 5): after a class initialization fails, the
 *          Class object is labeled erroneous and a subsequent attempt throws
 *          NoClassDefFoundError.
 * @run main ErroneousStateNoClassDefFoundError
 */

public class ErroneousStateNoClassDefFoundError {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Bad {
        static { if (System.nanoTime() >= 0) throw new RuntimeException("init failure"); }
    }

    public static void main(String[] args) {
        boolean first = false;
        try {
            new Bad();
        } catch (ExceptionInInitializerError expected) {
            first = true;
            check(expected.getCause() instanceof RuntimeException, "first failure wrapped cause");
        }
        check(first, "first initialization attempt threw ExceptionInInitializerError");

        boolean second = false;
        try {
            new Bad();
        } catch (NoClassDefFoundError expected) {
            second = true;
        }
        check(second, "subsequent attempt throws NoClassDefFoundError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
