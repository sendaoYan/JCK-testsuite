/*
 * @test
 * @summary java.util.ConcurrentModificationException API conformance: hierarchy and the (),
 *          (String), and (String, Throwable) constructors.
 * @run main ConcurrentModificationExceptionTest
 */

import java.util.ConcurrentModificationException;

public class ConcurrentModificationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConstructors() {
        ConcurrentModificationException e = new ConcurrentModificationException();
        check(e instanceof RuntimeException, "is a RuntimeException");
        ConcurrentModificationException e2 = new ConcurrentModificationException("msg");
        check("msg".equals(e2.getMessage()), "(String) constructor");
        ConcurrentModificationException e3 =
            new ConcurrentModificationException("msg", new Exception("cause"));
        check("msg".equals(e3.getMessage()) && e3.getCause() != null,
              "(String, Throwable) constructor");
    }

    public static void main(String[] args) {
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
