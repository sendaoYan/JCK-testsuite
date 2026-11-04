/*
 * @test
 * @summary java.io.IOError API conformance: cause and inheritance.
 * @run main IOErrorTest
 */

import java.io.IOError;

public class IOErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIOError() {
        Throwable cause = new Throwable();
        IOError e = new IOError(cause);
        check(e instanceof Error, "extends Error");
        check(e.getCause() == cause, "getCause returns the wrapped cause");
    }

    public static void main(String[] args) {
        testIOError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
