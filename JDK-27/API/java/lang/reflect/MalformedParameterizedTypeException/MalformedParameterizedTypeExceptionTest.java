/*
 * @test
 * @summary java.lang.reflect.MalformedParameterizedTypeException conformance: white-box structure + black-box behavior.
 *          Deepened batch: Serializable census (constructor count 2 here vs 1 on JDK-8),
 *          full serialization round-trip with cause, bare toString exact, stack printing.
 * @run main MalformedParameterizedTypeExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.MalformedParameterizedTypeException;

public class MalformedParameterizedTypeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Object serRoundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
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

    // ===================== deepened batch (2026-11-05) =====================

    static void testSerializableCensus() {
        check(java.io.Serializable.class.isAssignableFrom(MalformedParameterizedTypeException.class),
              "Serializable via Throwable");
        // version difference: JDK-8 exposes exactly one constructor; the String
        // constructor arrived in JDK 21
        check(MalformedParameterizedTypeException.class.getConstructors().length == 2,
              "two public constructors here (one on JDK-8)");
        check("java.lang.reflect.MalformedParameterizedTypeException".equals(
                      new MalformedParameterizedTypeException().toString()),
              "bare toString exact");
    }

    static void testSerializationRoundTrip() throws Exception {
        MalformedParameterizedTypeException orig = new MalformedParameterizedTypeException("ser-mz");
        orig.initCause(new IllegalStateException("mz-cause"));
        orig.addSuppressed(new IllegalArgumentException("mz-sup"));
        Object back = serRoundTrip(orig);
        check(back instanceof MalformedParameterizedTypeException, "round-trip exact type");
        MalformedParameterizedTypeException res = (MalformedParameterizedTypeException) back;
        check("ser-mz".equals(res.getMessage()), "round-trip keeps message");
        check(res.getCause() instanceof IllegalStateException, "round-trip keeps cause type");
        check("mz-cause".equals(res.getCause().getMessage()), "round-trip keeps cause message");
        check(res.getSuppressed().length == 1, "round-trip keeps suppressed");
        check(res instanceof java.io.Serializable, "instanceof Serializable");
    }

    static void testStackPrinting() throws Exception {
        MalformedParameterizedTypeException e =
                new MalformedParameterizedTypeException("print-me");
        e.initCause(new RuntimeException("printable-root"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos, true, "UTF-8"));
        String rendered = bos.toString("UTF-8");
        check(rendered.contains("MalformedParameterizedTypeException"), "stack names type");
        check(rendered.contains("print-me"), "stack renders message");
        check(rendered.contains("printable-root"), "stack renders cause");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerializableCensus();
        testSerializationRoundTrip();
        testStackPrinting();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
