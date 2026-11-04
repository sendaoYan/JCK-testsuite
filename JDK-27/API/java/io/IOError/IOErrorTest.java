/*
 * @test
 * @summary java.io.IOError conformance: white-box structure (unchecked Error wrapping a
 *          Throwable cause) + black-box behavior (single constructor, cause retention,
 *          message derived from the cause, catch semantics, propagation as an Error).
 * @run main IOErrorTest
 */

import java.io.IOException;
import java.io.IOError;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class IOErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.IOError");
        check(c.getSuperclass() == Error.class, "extends Error");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) ctors++;
        check(ctors == 1 && c.getConstructor(Throwable.class) != null,
              "single public (Throwable) constructor");
        check(!RuntimeException.class.isAssignableFrom(c), "not a RuntimeException: an Error");
    }

    // ---- Part 2: black-box (behavioral) ----
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

        // 3. no message-only constructor exists (message always derives from the cause)
        try {
            IOError.class.getConstructor(String.class);
            check(false, "no (String) constructor should exist");
        } catch (NoSuchMethodException expected) {
            passed++;
        }

        // 4. unchecked: declared nowhere, escapes methods without throws clauses
        Runnable r = () -> { throw new IOError(cause); };
        try {
            r.run();
            check(false, "IOError must propagate unchecked");
        } catch (IOError expected) {
            passed++;
        }

        // 5. it is an Error, not an Exception (never caught by catch (IOException))
        Class<?> c = IOError.class;
        check(Error.class.isAssignableFrom(c) && !Exception.class.isAssignableFrom(c),
              "IOError is an Error, not an Exception");
        try {
            throw new IOError(cause);
        } catch (Throwable t) {
            check(t instanceof IOError, "caught as Throwable");
        }

        // 6. stack trace and toString behave like any Error
        IOError t = new IOError(cause);
        check(t.toString().startsWith("java.io.IOError"), "toString names the class");
        check(t.getStackTrace().length > 0, "stack trace captured");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
