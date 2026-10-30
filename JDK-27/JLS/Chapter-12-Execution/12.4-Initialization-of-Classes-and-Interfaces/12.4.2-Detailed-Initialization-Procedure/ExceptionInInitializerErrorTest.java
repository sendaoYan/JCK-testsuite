/*
 * @test
 * @summary JLS 12.4.2: if a class initializer throws an exception that is not an
 *          Error, it is wrapped in an ExceptionInInitializerError with the
 *          original as its cause.
 * @run main ExceptionInInitializerErrorTest
 */

public class ExceptionInInitializerErrorTest {
    static class Bad {
        static {
            if (System.nanoTime() >= 0) throw new RuntimeException("init fail");
        }
    }

    public static void main(String[] args) {
        try {
            new Bad();
            throw new AssertionError("expected ExceptionInInitializerError");
        } catch (ExceptionInInitializerError e) {
            if (!(e.getCause() instanceof RuntimeException))
                throw new AssertionError("cause should be RuntimeException");
            if (!"init fail".equals(e.getCause().getMessage()))
                throw new AssertionError("cause message");
        }
        System.out.println("PASS");
    }
}
