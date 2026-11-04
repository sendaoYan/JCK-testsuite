/*
 * @test
 * @summary java.io.UncheckedIOException conformance: white-box structure + black-box
 *          behavior (both constructors require an IOException cause; getCause typing;
 *          real trigger: BufferedReader.lines() wrapping an IOException raised mid-stream;
 *          the IOException cause is retrievable for recovery).
 * @run main UncheckedIOExceptionTest
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class UncheckedIOExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Emits one line, then the underlying device fails. */
    static class FailingReader extends Reader {
        int calls;
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (calls++ == 0) {
                cbuf[off] = 'a'; cbuf[off + 1] = '\n';
                return 2;
            }
            throw new IOException("device gone");
        }
        public void close() {}
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.UncheckedIOException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        int two = 0, one = 0;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 2 && p[0] == String.class && p[1] == java.io.IOException.class) two++;
            if (p.length == 1 && p[0] == java.io.IOException.class) one++;
        }
        check(two == 1 && one == 1, "two public constructors");
        boolean typedCause = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (m.getName().equals("getCause")
                && m.getReturnType() == java.io.IOException.class)
                typedCause = true;
        check(typedCause, "getCause declared to return IOException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. (IOException) constructor: message defaults to cause.toString()
        IOException cause = new IOException("io failure");
        UncheckedIOException u = new UncheckedIOException(cause);
        check(u.getCause() == cause && cause.toString().equals(u.getMessage()),
              "cause kept, message = cause.toString()");

        // 2. (String, IOException) constructor
        UncheckedIOException u2 = new UncheckedIOException("wrapper message", cause);
        check("wrapper message".equals(u2.getMessage()) && u2.getCause() == cause,
              "message + cause kept");

        // 3. null cause is refused
        try {
            new UncheckedIOException((IOException) null);
            check(false, "null cause must throw");
        } catch (NullPointerException expected) {
            passed++;
        }

        // 4. unchecked: no throws clause needed
        Runnable r = () -> {
            throw new UncheckedIOException(new IOException("x"));
        };
        try {
            r.run();
        } catch (UncheckedIOException expected) {
            passed++;
        }

        // 5. real trigger: BufferedReader.lines() wraps mid-stream IOExceptions
        try {
            new BufferedReader(new FailingReader()).lines().count();
            check(false, "lines() must wrap the mid-stream IOException");
        } catch (UncheckedIOException wrapped) {
            check(wrapped.getCause() instanceof IOException
                      && "device gone".equals(wrapped.getCause().getMessage()),
                  "cause carries the original IOException");
        }

        // 6. recovery pattern: catch UncheckedIOException, unwrap, rethrow checked
        try {
            new BufferedReader(new FailingReader()).lines().count();
        } catch (UncheckedIOException wrapped) {
            IOException io = wrapped.getCause();
            check(io != null, "caller can unwrap the checked cause");
            throw io;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        // black-box part rethrows an IOException by design: assert it escapes main's
        // first run through a direct call
        try {
            testBlackBox();
            check(false, "scenario 6 must rethrow the unwrapped IOException");
        } catch (IOException io) {
            check("device gone".equals(io.getMessage()),
                  "unwrapped checked cause propagates for recovery");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
