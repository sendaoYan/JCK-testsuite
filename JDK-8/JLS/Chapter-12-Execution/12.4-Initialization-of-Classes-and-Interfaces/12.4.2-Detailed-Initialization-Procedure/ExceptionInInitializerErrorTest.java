/*
 * @test
 * @summary JLS 12.4.2 (Java SE 8): if a class initializer throws an exception that
 *          is not an Error, it is wrapped in an ExceptionInInitializerError with
 *          the original as its cause.
 * @run main ExceptionInInitializerErrorTest
 */

public class ExceptionInInitializerErrorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Bad {
        static { if (System.nanoTime() >= 0) throw new RuntimeException("init fail"); }
    }

    public static void main(String[] args) {
        try {
            new Bad();
            throw new AssertionError("expected ExceptionInInitializerError");
        } catch (ExceptionInInitializerError e) {
            check(e.getCause() instanceof RuntimeException, "cause is RuntimeException");
            check("init fail".equals(e.getCause().getMessage()), "cause message");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
