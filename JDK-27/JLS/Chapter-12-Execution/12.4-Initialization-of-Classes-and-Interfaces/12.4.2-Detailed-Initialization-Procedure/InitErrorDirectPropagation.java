/*
 * @test
 * @summary JLS 12.4.2: if a class initializer throws an Error, that Error is
 *          propagated directly and is not wrapped in ExceptionInInitializerError.
 * @run main InitErrorDirectPropagation
 */

public class InitErrorDirectPropagation {
    static class Bad {
        static {
            if (System.nanoTime() >= 0) throw new AssertionError("boom");
        }
    }

    public static void main(String[] args) {
        try {
            new Bad();
            throw new AssertionError("expected AssertionError from initializer");
        } catch (AssertionError e) {
            if (!"boom".equals(e.getMessage())) throw new AssertionError("message");
        }
        System.out.println("PASS");
    }
}
