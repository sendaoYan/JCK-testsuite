/*
 * @test
 * @summary java.lang.instrument.IllegalClassFormatException conformance: hierarchy and ctors.
 * @run main IllegalClassFormatExceptionTest
 */

import java.lang.instrument.IllegalClassFormatException;

public class IllegalClassFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalClassFormatException() {
        check(Exception.class.isAssignableFrom(IllegalClassFormatException.class),
              "extends Exception");
        IllegalClassFormatException e = new IllegalClassFormatException();
        check(e.getMessage() == null, "default message");
        IllegalClassFormatException e2 = new IllegalClassFormatException("bad format");
        check("bad format".equals(e2.getMessage()), "custom message");
    }

    public static void main(String[] args) {
        testIllegalClassFormatException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
