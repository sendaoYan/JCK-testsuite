/*
 * @test
 * @summary JLS 12.6: unlike constructors, a finalizer does not automatically
 *          invoke the superclass finalizer; such an invocation must be explicit.
 * @run main FinalizeDoesNotAutoCallSuper
 */

public class FinalizeDoesNotAutoCallSuper {
    static final StringBuilder log = new StringBuilder();

    static class Base {
        protected void finalize() throws Throwable { log.append("Base.finalize "); }
    }

    static class Sub extends Base {
        protected void finalize() throws Throwable { log.append("Sub.finalize "); }
        // deliberately does not call super.finalize()
    }

    public static void main(String[] args) throws Throwable {
        new Sub().finalize();
        if (!"Sub.finalize ".equals(log.toString()))
            throw new AssertionError("super.finalize should not be auto-called: " + log);
        System.out.println("PASS");
    }
}
