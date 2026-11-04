/*
 * @test
 * @summary javax.management.remote.JMXProviderException conformance: white-box structure + black-box behavior.
 * @run main JMXProviderExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.management.remote.JMXProviderException;

public class JMXProviderExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXProviderException");
        check(IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getConstructor() != null, "() ctor");
        check(c.getConstructor(String.class) != null, "(String) ctor");
        check(c.getConstructor(String.class, Throwable.class) != null, "(String,Throwable) ctor");
        check(c.getMethod("getCause") != null, "getCause");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Exception cause = new Exception("root");
        JMXProviderException e = new JMXProviderException("msg", cause);
        check(e.getMessage().equals("msg"), "message");
        check(e.getCause() == cause, "cause preserved");
        check(e instanceof IOException, "is IOException");

        JMXProviderException noCause = new JMXProviderException("only");
        check(noCause.getMessage().equals("only"), "string-only message");
        check(noCause.getCause() == null, "string-only cause null");

        JMXProviderException empty = new JMXProviderException();
        check(empty.getMessage() == null, "no-arg message null");
        check(empty.getCause() == null, "no-arg cause null");

        // Standard Throwable behavior: suppressed exception machinery is usable.
        JMXProviderException ex = new JMXProviderException("m");
        ex.addSuppressed(new RuntimeException("s"));
        check(ex.getSuppressed().length == 1, "suppressed exceptions work");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
