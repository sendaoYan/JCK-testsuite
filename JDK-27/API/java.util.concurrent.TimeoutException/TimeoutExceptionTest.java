/*
 * @test
 * @summary java.util.concurrent.TimeoutException API conformance: constructors and inheritance.
 * @run main TimeoutExceptionTest
 */

import java.util.concurrent.TimeoutException;

public class TimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTimeoutException() {
        TimeoutException e = new TimeoutException("m");
        check(e instanceof Exception, "extends Exception");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new TimeoutException().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testTimeoutException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
