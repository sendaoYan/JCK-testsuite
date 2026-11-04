/*
 * @test
 * @summary javax.management.remote.JMXServerErrorException conformance: white-box structure + black-box behavior.
 * @run main JMXServerErrorExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.management.remote.JMXServerErrorException;

public class JMXServerErrorExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXServerErrorException");
        check(IOException.class.isAssignableFrom(c), "extends IOException");
        Constructor<?> k = c.getConstructor(String.class, Error.class);
        check(k != null, "(String,Error) ctor");
        check(c.getMethod("getCause") != null, "getCause");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Error err = new Error("server boom");
        JMXServerErrorException e = new JMXServerErrorException("msg", err);
        check(e.getMessage().equals("msg"), "message");
        check(e.getCause() == err, "cause preserved");
        check(e.getCause() instanceof Error, "cause is Error");
        check(e instanceof IOException, "is IOException");

        // The Error argument may be null per the specification.
        JMXServerErrorException nullErr = new JMXServerErrorException("m", null);
        check(nullErr.getMessage().equals("m"), "null-err message");
        check(nullErr.getCause() == null, "null-err cause null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
