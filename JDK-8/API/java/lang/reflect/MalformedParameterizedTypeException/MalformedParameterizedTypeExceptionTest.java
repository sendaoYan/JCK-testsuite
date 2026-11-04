/*
 * @test
 * @summary java.lang.reflect.MalformedParameterizedTypeException conformance.
 *          Deepened batch (JDK-8 shape): only the no-arg constructor exists here (the
 *          message constructor was added in JDK 21), RuntimeException + Serializable
 *          hierarchy, exact bare toString, unchecked throw/catch without declaring,
 *          cause wiring via initCause, suppression, fillInStackTrace/setStackTrace
 *          mechanics, and a serialization round-trip.
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

    static void testMalformedParameterizedTypeException() {
        MalformedParameterizedTypeException e = new MalformedParameterizedTypeException();
        check(e instanceof RuntimeException, "is RuntimeException");
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testHierarchyCensus() {
        check(MalformedParameterizedTypeException.class.getSuperclass() == RuntimeException.class,
                "direct RuntimeException subclass");
        check(RuntimeException.class.isAssignableFrom(MalformedParameterizedTypeException.class),
                "RuntimeException assignable");
        check(Exception.class.isAssignableFrom(MalformedParameterizedTypeException.class),
                "Exception assignable");
        check(java.io.Serializable.class.isAssignableFrom(MalformedParameterizedTypeException.class),
                "Serializable via Throwable");
        // version difference: JDK-8 has exactly one public constructor; JDK 21+ adds
        // MalformedParameterizedTypeException(String)
        check(MalformedParameterizedTypeException.class.getConstructors().length == 1,
                "single public constructor on JDK-8 (no message ctor until 21)");
    }

    static void testBareState() {
        MalformedParameterizedTypeException bare = new MalformedParameterizedTypeException();
        check(bare.getMessage() == null, "bare message null");
        check(bare.getLocalizedMessage() == null, "bare localized message null");
        check(bare.getCause() == null, "bare cause null");
        check("java.lang.reflect.MalformedParameterizedTypeException".equals(bare.toString()),
                "bare toString exact");
    }

    static void testUncheckedThrow() {
        boolean caught = false;
        try {
            throwBare();
        } catch (RuntimeException e) {
            caught = e instanceof MalformedParameterizedTypeException;
        }
        check(caught, "catch as RuntimeException without declaring");
        boolean caughtExact = false;
        try {
            throwBare();
        } catch (MalformedParameterizedTypeException e) {
            caughtExact = true;
        }
        check(caughtExact, "catch exact type");
    }

    static void throwBare() {
        // no throws clause: unchecked by construction
        throw new MalformedParameterizedTypeException();
    }

    static void testThrowableMechanics() {
        MalformedParameterizedTypeException e = new MalformedParameterizedTypeException();
        RuntimeException root = new RuntimeException("root");
        e.initCause(root);
        check(e.getCause() == root, "initCause wiring");
        check("root".equals(e.getCause().getMessage()), "cause message");
        check(e.toString().contains("MalformedParameterizedTypeException"), "toString names type");
        e.addSuppressed(new IllegalArgumentException("sup"));
        check(e.getSuppressed().length == 1
                && e.getSuppressed()[0] instanceof IllegalArgumentException,
                "suppressed attachable");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack captured at construction");
        e.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("Declaring", "method", "File.java", 42)});
        check(e.getStackTrace().length == 1
                && e.getStackTrace()[0].getLineNumber() == 42,
                "programmatic stack accepted");
    }

    static void testSerialization() throws Exception {
        MalformedParameterizedTypeException orig = new MalformedParameterizedTypeException();
        orig.initCause(new IllegalStateException("mz"));
        Object back = serRoundTrip(orig);
        check(back instanceof MalformedParameterizedTypeException, "round-trip exact type");
        MalformedParameterizedTypeException res = (MalformedParameterizedTypeException) back;
        check(res.getMessage() == null, "round-trip keeps null message");
        check(res.getCause() instanceof IllegalStateException, "round-trip keeps cause type");
        check("mz".equals(res.getCause().getMessage()), "round-trip keeps cause message");
        check(res instanceof java.io.Serializable, "instanceof Serializable");
        check("java.lang.reflect.MalformedParameterizedTypeException".equals(res.toString()),
                "round-trip keeps exact toString");
    }

    static void testPrinting() throws Exception {
        MalformedParameterizedTypeException e = new MalformedParameterizedTypeException();
        e.initCause(new RuntimeException("printable"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos, true, "UTF-8"));
        String rendered = bos.toString("UTF-8");
        check(rendered.contains("MalformedParameterizedTypeException"), "stack names type");
        check(rendered.contains("printable"), "stack renders cause");
    }

    public static void main(String[] args) throws Exception {
        testMalformedParameterizedTypeException();
        testHierarchyCensus();
        testBareState();
        testUncheckedThrow();
        testThrowableMechanics();
        testSerialization();
        testPrinting();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
