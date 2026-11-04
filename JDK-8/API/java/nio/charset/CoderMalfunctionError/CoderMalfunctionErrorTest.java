/*
 * @test
 * @summary java.nio.charset.CoderMalfunctionError conformance: wraps an Exception cause.
 * @run main CoderMalfunctionErrorTest
 */

import java.nio.charset.CoderMalfunctionError;

public class CoderMalfunctionErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCoderMalfunctionError() {
        Exception cause = new Exception("cause");
        CoderMalfunctionError e = new CoderMalfunctionError(cause);
        check(e instanceof Error, "is Error");
        check(e.getCause() == cause, "getCause");
    }

    public static void main(String[] args) {
        testCoderMalfunctionError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
