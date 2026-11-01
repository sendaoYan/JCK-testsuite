/*
 * @test
 * @summary java.lang.instrument.UnmodifiableClassException conformance: hierarchy and ctors.
 * @run main UnmodifiableClassExceptionTest
 */

import java.lang.instrument.UnmodifiableClassException;

public class UnmodifiableClassExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnmodifiableClassException() {
        check(Exception.class.isAssignableFrom(UnmodifiableClassException.class),
              "extends Exception");
        UnmodifiableClassException e = new UnmodifiableClassException();
        check(e.getMessage() == null, "default message");
        UnmodifiableClassException e2 = new UnmodifiableClassException("unmodifiable");
        check("unmodifiable".equals(e2.getMessage()), "custom message");
    }

    public static void main(String[] args) {
        testUnmodifiableClassException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
