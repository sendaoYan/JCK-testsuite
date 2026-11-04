/*
 * @test
 * @summary java.lang.LinkageError API conformance: Error hierarchy, all four constructors,
 *          and that NoClassDefFoundError extends it.
 * @run main LinkageErrorTest
 */

public class LinkageErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        LinkageError e = new LinkageError();
        check(e instanceof Error, "LinkageError is an Error");
        check(new NoClassDefFoundError() instanceof LinkageError,
              "NoClassDefFoundError extends LinkageError");
    }

    static void testConstructors() {
        LinkageError e1 = new LinkageError();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        LinkageError e2 = new LinkageError("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        LinkageError cause = new LinkageError("cause");
        LinkageError e3 = new LinkageError("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
