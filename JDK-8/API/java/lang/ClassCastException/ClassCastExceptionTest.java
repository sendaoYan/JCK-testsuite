/*
 * @test
 * @summary java.lang.ClassCastException API conformance: RuntimeException hierarchy, no-arg
 *          and (String) constructors, and a thrown instance from an invalid reference cast.
 * @run main ClassCastExceptionTest
 */

public class ClassCastExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        ClassCastException e = new ClassCastException();
        check(e instanceof RuntimeException, "CCE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "CCE is Exception/Throwable");
    }

    static void testConstructors() {
        ClassCastException e1 = new ClassCastException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        ClassCastException e2 = new ClassCastException("cast");
        check("cast".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        Object o = Integer.valueOf(1);
        try {
            ((String) o).length();
            throw new AssertionError("expected ClassCastException");
        } catch (ClassCastException e) {
            check(e instanceof ClassCastException, "invalid reference cast throws CCE");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
