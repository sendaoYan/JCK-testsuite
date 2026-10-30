/*
 * @test
 * @summary java.lang.StringIndexOutOfBoundsException API conformance: IndexOutOfBoundsException
 *          hierarchy, no-arg and (String) constructors, a thrown instance from String.charAt,
 *          and the Java 9+ (int index) constructor (via reflection).
 * @run main StringIndexOutOfBoundsExceptionTest
 */

public class StringIndexOutOfBoundsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        StringIndexOutOfBoundsException e = new StringIndexOutOfBoundsException();
        check(e instanceof IndexOutOfBoundsException, "SIOOBE is an IndexOutOfBoundsException");
        check(e instanceof RuntimeException, "SIOOBE is a RuntimeException");
    }

    static void testConstructors() {
        StringIndexOutOfBoundsException e1 = new StringIndexOutOfBoundsException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        StringIndexOutOfBoundsException e2 = new StringIndexOutOfBoundsException("idx");
        check("idx".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        try {
            "abc".charAt(5);
            throw new AssertionError("expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException e) {
            check(e instanceof StringIndexOutOfBoundsException, "\"abc\".charAt(5) throws SIOOBE");
        }
    }

    static void testIntCtor() {
        try {
            java.lang.reflect.Constructor<StringIndexOutOfBoundsException> c =
                StringIndexOutOfBoundsException.class.getConstructor(int.class);
            StringIndexOutOfBoundsException e = c.newInstance(5);
            check("String index out of range: 5".equals(e.getMessage()),
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
