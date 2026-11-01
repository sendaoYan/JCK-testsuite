/*
 * @test
 * @summary java.util.concurrent.BrokenBarrierException API conformance: constructors and inheritance.
 * @run main BrokenBarrierExceptionTest
 */

import java.util.concurrent.BrokenBarrierException;

public class BrokenBarrierExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBrokenBarrierException() {
        BrokenBarrierException e = new BrokenBarrierException("m");
        check(e instanceof Exception, "extends Exception");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new BrokenBarrierException().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testBrokenBarrierException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
