/*
 * @test
 * @summary java.lang.UnsupportedOperationException API conformance: RuntimeException
 *          hierarchy, all four constructors, and a thrown instance from mutating an
 *          unmodifiable collection.
 * @run main UnsupportedOperationExceptionTest
 */

public class UnsupportedOperationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        UnsupportedOperationException e = new UnsupportedOperationException();
        check(e instanceof RuntimeException, "UOE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "UOE is Exception/Throwable");
    }

    static void testConstructors() {
        UnsupportedOperationException e1 = new UnsupportedOperationException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        UnsupportedOperationException e2 = new UnsupportedOperationException("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        UnsupportedOperationException cause = new UnsupportedOperationException("cause");
        UnsupportedOperationException e3 = new UnsupportedOperationException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        UnsupportedOperationException e4 = new UnsupportedOperationException(cause);
        check(e4.getCause() == cause, "(Throwable) ctor sets cause");
    }

    static void testThrown() {
        java.util.List<String> l = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>());
        try {
            l.add("x");
            throw new AssertionError("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            check(e instanceof UnsupportedOperationException, "unmodifiable list add() throws UOE");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
