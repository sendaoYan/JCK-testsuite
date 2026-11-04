/*
 * @test
 * @summary java.lang.reflect.MalformedParameterizedTypeException conformance: white-box structure + black-box behavior.
 * @run main MalformedParameterizedTypeExceptionTest
 */

import java.lang.reflect.MalformedParameterizedTypeException;

public class MalformedParameterizedTypeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        MalformedParameterizedTypeException e = new MalformedParameterizedTypeException();
        check(e instanceof RuntimeException, "is RuntimeException");
        check(MalformedParameterizedTypeException.class.getConstructors().length == 2,
              "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. no-arg: null message/cause
        MalformedParameterizedTypeException bare = new MalformedParameterizedTypeException();
        check(bare.getMessage() == null, "bare null message");
        check(bare.getCause() == null, "bare null cause");
        check(bare.toString().equals("java.lang.reflect.MalformedParameterizedTypeException"),
              "bare toString exact");

        // 2. message constructor (added in JDK 21) round-trips
        MalformedParameterizedTypeException msg =
                new MalformedParameterizedTypeException("signature invalid");
        check("signature invalid".equals(msg.getMessage()), "message kept");
        check(msg.toString().contains("signature invalid"), "toString includes message");
        check(msg.getLocalizedMessage().equals("signature invalid"), "localized defaults to message");

        // 3. cause wiring and suppression
        RuntimeException root = new RuntimeException("root");
        MalformedParameterizedTypeException wired = new MalformedParameterizedTypeException("w");
        wired.initCause(root);
        check(wired.getCause() == root, "initCause wiring");
        check(wired.getCause().getMessage().equals("root"), "cause message");
        wired.addSuppressed(new IllegalArgumentException("sup"));
        check(wired.getSuppressed().length == 1
              && wired.getSuppressed()[0] instanceof IllegalArgumentException,
              "suppressed attachable");

        // 4. unchecked throw/catch
        boolean caught = false;
        try {
            throw new MalformedParameterizedTypeException("thrown");
        } catch (RuntimeException e) {
            caught = e instanceof MalformedParameterizedTypeException;
        }
        check(caught, "catch as RuntimeException");

        // 5. hierarchy and stack capture
        check(MalformedParameterizedTypeException.class.getSuperclass() == RuntimeException.class,
              "direct RuntimeException subclass");
        MalformedParameterizedTypeException s = new MalformedParameterizedTypeException("s");
        check(s.getStackTrace().length > 0, "stack captured at construction");
        check(s.fillInStackTrace() == s, "fillInStackTrace returns this");
        s.setStackTrace(s.getStackTrace());
        s.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("Cls", "mth", "File.java", 17)});
        check(s.getStackTrace().length == 1 && s.getStackTrace()[0].getLineNumber() == 17,
              "programmatic stack accepted");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
