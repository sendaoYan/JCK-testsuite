/*
 * @test
 * @summary java.lang.NoSuchFieldException API conformance: ReflectiveOperationException
 *          hierarchy, no-arg/(String) constructors, and a thrown instance from
 *          Class.getField of a non-existent field.
 * @run main NoSuchFieldExceptionTest
 */

public class NoSuchFieldExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        NoSuchFieldException e = new NoSuchFieldException();
        check(e instanceof ReflectiveOperationException, "NSFE is a ReflectiveOperationException");
        check(e instanceof Exception, "NSFE is an Exception");
        check(!((Object) e instanceof RuntimeException), "NSFE is checked (not RuntimeException)");
    }

    static void testConstructors() {
        NoSuchFieldException e1 = new NoSuchFieldException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        NoSuchFieldException e2 = new NoSuchFieldException("f");
        check("f".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() throws NoSuchFieldException {
        try {
            String.class.getField("definitelyNotARealField");
            throw new AssertionError("expected NoSuchFieldException");
        } catch (NoSuchFieldException e) {
            check(e instanceof NoSuchFieldException, "getField(nonexistent) throws NSFE");
        }
        check(String.class.getField("CASE_INSENSITIVE_ORDER") != null,
              "getField(existing) succeeds");
    }

    public static void main(String[] args) throws NoSuchFieldException {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
