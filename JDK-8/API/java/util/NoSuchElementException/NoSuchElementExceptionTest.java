/*
 * @test
 * @summary java.util.NoSuchElementException API conformance: hierarchy and the (), (String),
 *          and (String, Throwable) constructors.
 * @run main NoSuchElementExceptionTest
 */

import java.util.NoSuchElementException;

public class NoSuchElementExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConstructors() {
        NoSuchElementException e = new NoSuchElementException();
        check(e instanceof RuntimeException, "is a RuntimeException");
        NoSuchElementException e2 = new NoSuchElementException("msg");
        check("msg".equals(e2.getMessage()), "(String) constructor");
        try {
            java.lang.reflect.Constructor<NoSuchElementException> c =
                NoSuchElementException.class.getConstructor(String.class, Throwable.class);
            NoSuchElementException e3 = c.newInstance("msg", new Exception("cause"));
            check("msg".equals(e3.getMessage()) && e3.getCause() != null,
                  "(String, Throwable) constructor [Java 9+]");
        } catch (NoSuchMethodException ex) {
            // JDK 8: the (String, Throwable) constructor does not exist.
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    public static void main(String[] args) {
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
