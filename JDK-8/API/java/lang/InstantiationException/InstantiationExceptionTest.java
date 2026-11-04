/*
 * @test
 * @summary java.lang.InstantiationException API conformance: ReflectiveOperationException
 *          hierarchy, no-arg/(String) constructors, and a thrown instance from
 *          Constructor.newInstance on an abstract class.
 * @run main InstantiationExceptionTest
 */

public class InstantiationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        InstantiationException e = new InstantiationException();
        check(e instanceof ReflectiveOperationException, "IE is a ReflectiveOperationException");
        check(e instanceof Exception, "IE is an Exception");
        check(!((Object) e instanceof RuntimeException), "IE is checked (not RuntimeException)");
    }

    static void testConstructors() {
        InstantiationException e1 = new InstantiationException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        InstantiationException e2 = new InstantiationException("m");
        check("m".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() throws ReflectiveOperationException {
        java.lang.reflect.Constructor<Number> c = Number.class.getDeclaredConstructor();
        try {
            c.newInstance();
            throw new AssertionError("expected InstantiationException");
        } catch (InstantiationException e) {
            check(e instanceof InstantiationException,
                  "newInstance on abstract class throws InstantiationException");
        }
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
