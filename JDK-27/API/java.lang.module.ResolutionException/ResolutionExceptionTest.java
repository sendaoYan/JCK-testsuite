/*
 * @test
 * @summary java.lang.module.ResolutionException conformance: RuntimeException subclass and constructors.
 * @run main ResolutionExceptionTest
 */

import java.lang.module.ResolutionException;

public class ResolutionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(new ResolutionException() instanceof RuntimeException, "extends RuntimeException");

        ResolutionException e1 = new ResolutionException("boom");
        check("boom".equals(e1.getMessage()), "String ctor message");
        check(e1.getCause() == null, "String ctor no cause");

        RuntimeException cause = new RuntimeException("cause");
        ResolutionException e2 = new ResolutionException(cause);
        check(e2.getCause() == cause, "Throwable ctor cause");
        check(e2.getMessage() != null, "Throwable ctor message non-null");

        ResolutionException e3 = new ResolutionException("msg", cause);
        check("msg".equals(e3.getMessage()), "String+Throwable ctor message");
        check(e3.getCause() == cause, "String+Throwable ctor cause");

        check(new ResolutionException().getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
