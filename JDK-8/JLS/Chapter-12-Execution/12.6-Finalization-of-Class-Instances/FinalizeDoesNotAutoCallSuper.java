/*
 * @test
 * @summary JLS 12.6 (Java SE 8): unlike constructors, a finalizer does not
 *          automatically invoke the superclass finalizer; such an invocation must
 *          be explicit.
 * @run main FinalizeDoesNotAutoCallSuper
 */

public class FinalizeDoesNotAutoCallSuper {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static StringBuilder log = new StringBuilder();

    static class Base {
        protected void finalize() throws Throwable { log.append("Base.finalize "); }
    }

    static class Sub extends Base {
        protected void finalize() throws Throwable { log.append("Sub.finalize "); }
    }

    public static void main(String[] args) throws Throwable {
        new Sub().finalize();
        check("Sub.finalize ".equals(log.toString()), "super.finalize should not be auto-called: " + log);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
