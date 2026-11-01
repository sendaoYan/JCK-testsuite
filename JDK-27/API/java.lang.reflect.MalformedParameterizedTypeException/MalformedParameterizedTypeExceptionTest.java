/*
 * @test
 * @summary java.lang.reflect.MalformedParameterizedTypeException conformance.
 * @run main MalformedParameterizedTypeExceptionTest
 */

import java.lang.reflect.MalformedParameterizedTypeException;

public class MalformedParameterizedTypeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMalformedParameterizedTypeException() {
        MalformedParameterizedTypeException e = new MalformedParameterizedTypeException();
        check(e instanceof RuntimeException, "is RuntimeException");
    }

    public static void main(String[] args) {
        testMalformedParameterizedTypeException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
