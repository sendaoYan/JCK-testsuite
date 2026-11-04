/*
 * @test
 * @summary java.lang.NoSuchMethodException API conformance: ReflectiveOperationException
 *          hierarchy, no-arg and (String) constructors, and a thrown instance from
 *          Class.getMethod of a non-existent method.
 * @run main NoSuchMethodExceptionTest
 */

public class NoSuchMethodExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        NoSuchMethodException e = new NoSuchMethodException();
        check(e instanceof ReflectiveOperationException, "NSME is a ReflectiveOperationException");
        check(e instanceof Exception, "NSME is an Exception");
        check(!((Object) e instanceof RuntimeException), "NSME is checked (not RuntimeException)");
    }

    static void testConstructors() {
        NoSuchMethodException e1 = new NoSuchMethodException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        NoSuchMethodException e2 = new NoSuchMethodException("m");
        check("m".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() throws NoSuchMethodException {
        try {
            String.class.getMethod("definitelyNotARealMethod");
            throw new AssertionError("expected NoSuchMethodException");
        } catch (NoSuchMethodException e) {
            check(e instanceof NoSuchMethodException, "getMethod(nonexistent) throws NSME");
        }
        check(String.class.getMethod("length") != null, "getMethod(length) succeeds");
    }

    public static void main(String[] args) throws NoSuchMethodException {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
