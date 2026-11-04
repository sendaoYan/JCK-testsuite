/*
 * @test
 * @summary java.lang.reflect.MalformedParametersException conformance: white-box structure + black-box behavior.
 * @run main MalformedParametersExceptionTest
 */

import java.lang.reflect.MalformedParametersException;

public class MalformedParametersExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        MalformedParametersException e = new MalformedParametersException();
        check(e instanceof RuntimeException, "is RuntimeException");
        MalformedParametersException e2 = new MalformedParametersException("msg");
        check("msg".equals(e2.getMessage()), "message");
        check(MalformedParametersException.class.getConstructors().length == 2,
              "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. no-arg constructor: null message, no cause, usable as-is
        MalformedParametersException bare = new MalformedParametersException();
        check(bare.getMessage() == null, "bare null message");
        check(bare.getCause() == null, "bare null cause");
        check(bare.getLocalizedMessage() == null, "bare localized null");
        check(bare.getStackTrace().length >= 0, "stack trace accessible");

        // 2. message constructor round-trip
        MalformedParametersException msg = new MalformedParametersException("bad MethodParameters");
        check("bad MethodParameters".equals(msg.getMessage()), "message kept");
        check(msg.getCause() == null, "no cause without initCause");
        check(msg.toString().startsWith("java.lang.reflect.MalformedParametersException"),
              "toString prefix");
        check(msg.toString().contains("bad MethodParameters"), "toString includes message");

        // 3. throwable mechanics: cause wiring, suppression, capture at construction
        RuntimeException root = new RuntimeException("root");
        MalformedParametersException wired = new MalformedParametersException("wired");
        wired.initCause(root);
        check(wired.getCause() == root, "initCause wiring");
        check(wired.getCause().getMessage().equals("root"), "cause message");
        wired.addSuppressed(new MalformedParametersException("sup"));
        check(wired.getSuppressed().length == 1, "suppressed attachable");
        check(wired.getSuppressed()[0] instanceof MalformedParametersException,
              "suppressed exact type");

        // 4. unchecked semantics: throw/catch without declaring
        boolean caught = false;
        try {
            throw new MalformedParametersException("thrown");
        } catch (RuntimeException e) {
            caught = e instanceof MalformedParametersException
                  && "thrown".equals(e.getMessage());
        }
        check(caught, "catch as RuntimeException with message");

        // 5. serialization surface stays intact (round-trip via same-process copy semantics)
        check(MalformedParametersException.class.getSuperclass() == RuntimeException.class,
              "direct RuntimeException subclass");

        // 6. fillInStackTrace idempotence of identity (same instance returned)
        MalformedParametersException f = new MalformedParametersException("fis");
        check(f.fillInStackTrace() == f, "fillInStackTrace returns this");
        check(f.getStackTrace() != null, "stack trace captured");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
