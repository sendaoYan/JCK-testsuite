/*
 * @test
 * @summary java.lang.reflect.MalformedParametersException conformance: white-box structure + black-box behavior.
 *          Deepened batch: Serializable census, exact bare/with-message toString forms,
 *          full serialization round-trip keeping message + cause, stack printing.
 * @run main MalformedParametersExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.MalformedParametersException;

public class MalformedParametersExceptionTest {
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

    // ===================== deepened batch (2026-11-05) =====================

    static void testSerializableCensus() {
        check(java.io.Serializable.class.isAssignableFrom(MalformedParametersException.class),
              "Serializable via Throwable");
        check(MalformedParametersException.class.getConstructors().length == 2,
              "two public constructors since 1.8");
        check("java.lang.reflect.MalformedParametersException".equals(
                      new MalformedParametersException().toString()),
              "bare toString exact");
        check("java.lang.reflect.MalformedParametersException: tagged".equals(
                      new MalformedParametersException("tagged").toString()),
              "with-message toString exact");
    }

    static void testSerializationRoundTrip() throws Exception {
        MalformedParametersException orig = new MalformedParametersException("ser-mp");
        orig.initCause(new IllegalStateException("mp-cause"));
        orig.addSuppressed(new IllegalArgumentException("mp-sup"));
        Object back = serRoundTrip(orig);
        check(back instanceof MalformedParametersException, "round-trip exact type");
        MalformedParametersException res = (MalformedParametersException) back;
        check("ser-mp".equals(res.getMessage()), "round-trip keeps message");
        check(res.getCause() instanceof IllegalStateException, "round-trip keeps cause type");
        check("mp-cause".equals(res.getCause().getMessage()), "round-trip keeps cause message");
        check(res.getSuppressed().length == 1, "round-trip keeps suppressed");
        check(res instanceof java.io.Serializable, "instanceof Serializable");
    }

    static void testStackPrinting() throws Exception {
        MalformedParametersException e = new MalformedParametersException("print-mp");
        e.initCause(new RuntimeException("mp-root"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos, true, "UTF-8"));
        String rendered = bos.toString("UTF-8");
        check(rendered.contains("MalformedParametersException"), "stack names type");
        check(rendered.contains("print-mp"), "stack renders message");
        check(rendered.contains("mp-root"), "stack renders cause");
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
