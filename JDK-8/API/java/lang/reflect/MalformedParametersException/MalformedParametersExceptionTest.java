/*
 * @test
 * @summary java.lang.reflect.MalformedParametersException conformance.
 *          Deepened batch: both constructors exist since 1.8, RuntimeException +
 *          Serializable hierarchy, exact bare/with-message toString forms, unchecked
 *          throw/catch with message, cause wiring, suppression, fillInStackTrace/
 *          setStackTrace mechanics, and a serialization round-trip keeping the message.
 * @run main MalformedParametersExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.MalformedParametersException;
import java.lang.reflect.Method;

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

    static class Holder {
        void withParams(int a, String b) {
        }
    }

    static void testMalformedParametersException() {
        MalformedParametersException e = new MalformedParametersException();
        check(e instanceof RuntimeException, "is RuntimeException");
        MalformedParametersException e2 = new MalformedParametersException("msg");
        check("msg".equals(e2.getMessage()), "message");
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testHierarchyCensus() {
        check(MalformedParametersException.class.getSuperclass() == RuntimeException.class,
                "direct RuntimeException subclass");
        check(Exception.class.isAssignableFrom(MalformedParametersException.class),
                "Exception assignable");
        check(java.io.Serializable.class.isAssignableFrom(MalformedParametersException.class),
                "Serializable via Throwable");
        check(MalformedParametersException.class.getConstructors().length == 2,
                "two public constructors since 1.8");
    }

    static void testBareState() {
        MalformedParametersException bare = new MalformedParametersException();
        check(bare.getMessage() == null, "bare message null");
        check(bare.getLocalizedMessage() == null, "bare localized null");
        check(bare.getCause() == null, "bare cause null");
        check("java.lang.reflect.MalformedParametersException".equals(bare.toString()),
                "bare toString exact");
        MalformedParametersException with = new MalformedParametersException("bad");
        check("java.lang.reflect.MalformedParametersException: bad".equals(with.toString()),
                "with-message toString exact");
        check("bad".equals(with.getLocalizedMessage()), "localized defaults to message");
    }

    static void testUncheckedThrow() {
        boolean caught = false;
        try {
            throw new MalformedParametersException("thrown");
        } catch (RuntimeException e) {
            caught = e instanceof MalformedParametersException
                    && "thrown".equals(e.getMessage());
        }
        check(caught, "catch as RuntimeException with message");
        boolean exact = false;
        try {
            throw new MalformedParametersException();
        } catch (MalformedParametersException e) {
            exact = e.getMessage() == null;
        }
        check(exact, "catch bare exact type");
    }

    static void testThrowableMechanics() {
        MalformedParametersException e = new MalformedParametersException("wired");
        RuntimeException root = new RuntimeException("root");
        e.initCause(root);
        check(e.getCause() == root, "initCause wiring");
        check("root".equals(e.getCause().getMessage()), "cause message");
        e.addSuppressed(new MalformedParametersException("sup"));
        check(e.getSuppressed().length == 1
                && e.getSuppressed()[0] instanceof MalformedParametersException,
                "suppressed attachable");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack captured at construction");
        e.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("Cls", "mth", "File.java", 7)});
        check(e.getStackTrace().length == 1
                && e.getStackTrace()[0].getMethodName().equals("mth"),
                "programmatic stack accepted");
    }

    static void testSerialization() throws Exception {
        MalformedParametersException orig = new MalformedParametersException("ser-msg");
        orig.initCause(new IllegalStateException("mps"));
        Object back = serRoundTrip(orig);
        check(back instanceof MalformedParametersException, "round-trip exact type");
        MalformedParametersException res = (MalformedParametersException) back;
        check("ser-msg".equals(res.getMessage()), "round-trip keeps message");
        check("java.lang.reflect.MalformedParametersException: ser-msg".equals(res.toString()),
                "round-trip keeps exact toString");
        check(res.getCause() instanceof IllegalStateException, "round-trip keeps cause type");
        check("mps".equals(res.getCause().getMessage()), "round-trip keeps cause message");
        check(res instanceof java.io.Serializable, "instanceof Serializable");
    }

    static void testContextSanity() throws Exception {
        // the exception documents malformed MethodParameters attributes; a healthy
        // reflective lookup here must not throw it
        Method m = Holder.class.getDeclaredMethod("withParams", int.class, String.class);
        check(m.getParameterCount() == 2, "healthy parameters reachable");
        check(m.getParameters().length == 2, "Parameter array intact");
    }

    static void testPrinting() throws Exception {
        MalformedParametersException e = new MalformedParametersException("print-me");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos, true, "UTF-8"));
        String rendered = bos.toString("UTF-8");
        check(rendered.contains("MalformedParametersException"), "stack names type");
        check(rendered.contains("print-me"), "stack renders message");
    }

    public static void main(String[] args) throws Exception {
        testMalformedParametersException();
        testHierarchyCensus();
        testBareState();
        testUncheckedThrow();
        testThrowableMechanics();
        testSerialization();
        testContextSanity();
        testPrinting();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
