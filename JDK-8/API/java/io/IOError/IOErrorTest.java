/*
 * @test
 * @summary java.io.IOError conformance (pure black-box): unchecked Error wrapping
 *          a Throwable cause; single constructor, cause retention, message derived
 *          from the cause, catch semantics, propagation as an Error.
 * @run main IOErrorTest
 */

import java.io.IOException;
import java.io.IOError;

public class IOErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlackBox() throws Exception {
        // 1. constructor: cause kept, message defaults to cause.toString()
        IOException cause = new IOException("fatal device failure");
        IOError e = new IOError(cause);
        check(e.getCause() == cause, "cause retained");
        check(cause.toString().equals(e.getMessage()), "message = cause.toString()");
        check(e.getMessage().contains("fatal device failure"), "message carries cause detail");

        // 2. wraps any Throwable, not only IOException
        RuntimeException rte = new RuntimeException("rt");
        IOError e2 = new IOError(rte);
        check(e2.getCause() == rte && rte.toString().equals(e2.getMessage()),
              "arbitrary Throwable cause");

        // 3. unchecked: declared nowhere, escapes methods without throws clauses
        Runnable r = () -> { throw new IOError(cause); };
        try {
            r.run();
            check(false, "IOError must propagate unchecked");
        } catch (IOError expected) {
            passed++;
        }

        // 4. it is an Error, not an Exception (never caught by catch (IOException))
        check(Error.class.isAssignableFrom(IOError.class)
                  && !Exception.class.isAssignableFrom(IOError.class),
              "IOError is an Error, not an Exception");
        try {
            throw new IOError(cause);
        } catch (Throwable t) {
            check(t instanceof IOError, "caught as Throwable");
        }

        // 5. stack trace and toString behave like any Error
        IOError t = new IOError(cause);
        check(t.toString().startsWith("java.io.IOError"), "toString names the class");
        check(t.getStackTrace().length > 0, "stack trace captured");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
