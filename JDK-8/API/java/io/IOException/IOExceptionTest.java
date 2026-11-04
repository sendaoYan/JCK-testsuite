/*
 * @test
 * @summary java.io.IOException API conformance: checked-exception hierarchy and the (), (String),
 *          and (String, Throwable) constructors.
 * @run main IOExceptionTest
 */

import java.io.IOException;

public class IOExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIOException() {
        IOException e = new IOException();
        check(e instanceof Exception, "is an Exception");
        check(!((Object) e instanceof RuntimeException), "is checked");
        IOException e2 = new IOException("msg");
        check("msg".equals(e2.getMessage()), "(String) constructor");
        IOException e3 = new IOException("msg", new Exception("cause"));
        check("msg".equals(e3.getMessage()) && e3.getCause() != null,
              "(String, Throwable) constructor");
    }

    public static void main(String[] args) {
        testIOException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
