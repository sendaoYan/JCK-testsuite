/*
 * @test
 * @summary JVMS 5.5 / JLS 12.4.2: if a class's static initializer completes
 *          abruptly by throwing, the first active use throws
 *          ExceptionInInitializerError wrapping the original exception, and the
 *          class is marked erroneous so subsequent uses throw
 *          NoClassDefFoundError.
 * @run main StaticInitFailureTest
 */

public class StaticInitFailureTest {
    static class Failing {
        static { if (true) throw new RuntimeException("boom"); }
    }

    public static void main(String[] args) {
        // First active use: ExceptionInInitializerError whose cause is the
        // exception thrown by the static initializer.
        try {
            new Failing();
            throw new AssertionError("first use did not throw");
        } catch (ExceptionInInitializerError e) {
            if (!(e.getCause() instanceof RuntimeException)) {
                throw new AssertionError("cause must be RuntimeException, got " + e.getCause());
            }
        }

        // Subsequent use: the class is now erroneous, so NoClassDefFoundError.
        try {
            new Failing();
            throw new AssertionError("second use did not throw");
        } catch (NoClassDefFoundError e) {
            // expected (JVMS 5.5)
        }

        System.out.println("PASS");
    }
}
