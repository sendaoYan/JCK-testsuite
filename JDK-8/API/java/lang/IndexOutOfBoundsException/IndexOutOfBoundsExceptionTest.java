/*
 * @test
 * @summary java.lang.IndexOutOfBoundsException API conformance: RuntimeException hierarchy,
 *          no-arg and (String) constructors, and the Java 9+ (int index) constructor
 *          (via reflection, since it is absent on JDK 8).
 * @run main IndexOutOfBoundsExceptionTest
 */

public class IndexOutOfBoundsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        IndexOutOfBoundsException e = new IndexOutOfBoundsException();
        check(e instanceof RuntimeException, "IOOBE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "IOOBE is Exception/Throwable");
    }

    static void testConstructors() {
        IndexOutOfBoundsException e1 = new IndexOutOfBoundsException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        IndexOutOfBoundsException e2 = new IndexOutOfBoundsException("range");
        check("range".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testIntCtor() {
        try {
            java.lang.reflect.Constructor<IndexOutOfBoundsException> c =
                IndexOutOfBoundsException.class.getConstructor(int.class);
            IndexOutOfBoundsException e = c.newInstance(5);
            check("Index out of range: 5".equals(e.getMessage()), "(int) ctor message [Java 9+]");
        } catch (NoSuchMethodException e) {
            // JDK 8: the (int index) constructor does not exist.
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testIntCtor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
