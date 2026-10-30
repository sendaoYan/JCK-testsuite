/*
 * @test
 * @summary java.lang.ArrayIndexOutOfBoundsException API conformance: IndexOutOfBoundsException
 *          hierarchy, no-arg and (String) constructors, a thrown instance from an out-of-range
 *          array access, and the Java 9+ (int index) constructor (via reflection).
 * @run main ArrayIndexOutOfBoundsExceptionTest
 */

public class ArrayIndexOutOfBoundsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException();
        check(e instanceof IndexOutOfBoundsException, "AIOOBE is an IndexOutOfBoundsException");
        check(e instanceof RuntimeException, "AIOOBE is a RuntimeException");
    }

    static void testConstructors() {
        ArrayIndexOutOfBoundsException e1 = new ArrayIndexOutOfBoundsException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        ArrayIndexOutOfBoundsException e2 = new ArrayIndexOutOfBoundsException("idx");
        check("idx".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        int[] a = new int[3];
        try {
            a[3] = 1;
            throw new AssertionError("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            check(e instanceof ArrayIndexOutOfBoundsException, "a[3] on length-3 array throws AIOOBE");
        }
    }

    static void testIntCtor() {
        try {
            java.lang.reflect.Constructor<ArrayIndexOutOfBoundsException> c =
                ArrayIndexOutOfBoundsException.class.getConstructor(int.class);
            ArrayIndexOutOfBoundsException e = c.newInstance(5);
            check("Array index out of range: 5".equals(e.getMessage()),
                  "(int) ctor message [Java 9+]");
        } catch (NoSuchMethodException e) {
            // JDK 8: the (int index) constructor does not exist.
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        testIntCtor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
