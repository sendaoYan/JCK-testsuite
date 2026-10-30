/*
 * @test
 * @summary java.lang.ArrayStoreException API conformance: RuntimeException hierarchy, no-arg
 *          and (String) constructors, and a thrown instance from storing an incompatible
 *          element into an array.
 * @run main ArrayStoreExceptionTest
 */

public class ArrayStoreExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        ArrayStoreException e = new ArrayStoreException();
        check(e instanceof RuntimeException, "ASE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "ASE is Exception/Throwable");
    }

    static void testConstructors() {
        ArrayStoreException e1 = new ArrayStoreException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        ArrayStoreException e2 = new ArrayStoreException("store");
        check("store".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        Object[] arr = new String[1];
        try {
            arr[0] = Integer.valueOf(1);
            throw new AssertionError("expected ArrayStoreException");
        } catch (ArrayStoreException e) {
            check(e instanceof ArrayStoreException, "storing Integer into String[] throws ASE");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
