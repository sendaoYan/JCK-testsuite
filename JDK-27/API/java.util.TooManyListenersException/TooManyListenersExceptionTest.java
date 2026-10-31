/*
 * @test
 * @summary java.util.TooManyListenersException API conformance: constructors and inheritance.
 * @run main TooManyListenersExceptionTest
 */

import java.util.TooManyListenersException;

public class TooManyListenersExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTooManyListenersException() {
        TooManyListenersException e = new TooManyListenersException("msg");
        check(e instanceof Exception, "extends Exception");
        check("msg".equals(e.getMessage()), "message from (String) ctor");
        check(new TooManyListenersException().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testTooManyListenersException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
