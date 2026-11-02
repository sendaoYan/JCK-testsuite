/*
 * @test
 * @summary java.lang.module.FindException conformance: RuntimeException subclass and constructors.
 * @run main FindExceptionTest
 */

import java.lang.module.FindException;

public class FindExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(new FindException() instanceof RuntimeException, "extends RuntimeException");

        FindException e1 = new FindException("boom");
        check("boom".equals(e1.getMessage()), "String ctor message");
        check(e1.getCause() == null, "String ctor no cause");

        RuntimeException cause = new RuntimeException("cause");
        FindException e2 = new FindException(cause);
        check(e2.getCause() == cause, "Throwable ctor cause");
        check(e2.getMessage() != null, "Throwable ctor message non-null");

        FindException e3 = new FindException("msg", cause);
        check("msg".equals(e3.getMessage()), "String+Throwable ctor message");
        check(e3.getCause() == cause, "String+Throwable ctor cause");

        check(new FindException().getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
