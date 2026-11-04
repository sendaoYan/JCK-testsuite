/*
 * @test
 * @summary java.lang.reflect.MalformedParametersException conformance.
 * @run main MalformedParametersExceptionTest
 */

import java.lang.reflect.MalformedParametersException;

public class MalformedParametersExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMalformedParametersException() {
        MalformedParametersException e = new MalformedParametersException();
        check(e instanceof RuntimeException, "is RuntimeException");
        MalformedParametersException e2 = new MalformedParametersException("msg");
        check("msg".equals(e2.getMessage()), "message");
    }

    public static void main(String[] args) {
        testMalformedParametersException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
