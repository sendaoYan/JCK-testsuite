/*
 * @test
 * @summary java.lang.IllegalAccessException API conformance: ReflectiveOperationException
 *          hierarchy, checked status, and the no-arg/(String) constructors.
 * @run main IllegalAccessExceptionTest
 */

public class IllegalAccessExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        IllegalAccessException e = new IllegalAccessException();
        check(e instanceof ReflectiveOperationException, "IAE is a ReflectiveOperationException");
        check(e instanceof Exception, "IAE is an Exception");
        check(!((Object) e instanceof RuntimeException), "IAE is checked (not RuntimeException)");
    }

    static void testConstructors() {
        IllegalAccessException e1 = new IllegalAccessException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        IllegalAccessException e2 = new IllegalAccessException("m");
        check("m".equals(e2.getMessage()), "(String) ctor message");
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
