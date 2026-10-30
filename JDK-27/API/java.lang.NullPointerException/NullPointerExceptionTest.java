/*
 * @test
 * @summary java.lang.NullPointerException API conformance: RuntimeException hierarchy,
 *          no-arg and (String) constructors, and a null dereference throwing NPE with a
 *          null message.
 * @run main NullPointerExceptionTest
 */

public class NullPointerExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        NullPointerException e = new NullPointerException();
        check(e instanceof RuntimeException, "NPE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "NPE is Exception/Throwable");
    }

    static void testConstructors() {
        NullPointerException e1 = new NullPointerException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        NullPointerException e2 = new NullPointerException("npe");
        check("npe".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        String s = null;
        try {
            s.length();
            throw new AssertionError("expected NullPointerException");
        } catch (NullPointerException e) {
            check(e instanceof RuntimeException, "JVM-thrown NPE is a RuntimeException");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
