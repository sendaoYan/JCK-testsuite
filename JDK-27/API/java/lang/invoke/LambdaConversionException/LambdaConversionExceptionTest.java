/*
 * @test
 * @summary java.lang.invoke.LambdaConversionException conformance: white-box + black-box behavior.
 *          Deepened: serialization round-trip preserving the message, initCause
 *          rules ("Can't overwrite cause" ISE incl. ctor-provided cause), the
 *          metafactory trigger message on JDK-27 ("... is not an interface"),
 *          and a well-formed metafactory control link.
 * @run main LambdaConversionExceptionTest
 */

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class LambdaConversionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public interface F {
        int len(String s);
    }

    static int body(String s) { return s.length(); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.LambdaConversionException");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 5, "five public constructors");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        c.getDeclaredConstructor(Throwable.class);
        c.getDeclaredConstructor(String.class, Throwable.class, boolean.class, boolean.class);
        check(c.getDeclaredField("serialVersionUID").getType() == long.class,
              "serialVersionUID declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        check(new LambdaConversionException() instanceof Exception, "is an Exception");

        // message-only
        LambdaConversionException e1 = new LambdaConversionException("unsupported");
        check("unsupported".equals(e1.getMessage()), "message ctor");
        check(e1.getCause() == null, "no cause");
        check(e1.getStackTrace().length > 0, "stack trace captured by default");
        e1.addSuppressed(new RuntimeException("s"));
        check(e1.getSuppressed().length == 1, "suppression enabled by default");

        // cause-only
        Throwable cause = new IllegalArgumentException("inner");
        LambdaConversionException e2 = new LambdaConversionException(cause);
        check(e2.getCause() == cause, "cause identity");
        check(e2.getMessage().equals(cause.toString()), "message = cause.toString()");

        // message + cause
        LambdaConversionException e3 = new LambdaConversionException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "msg+cause ctor");

        // full control ctor: no suppression, no stack trace
        LambdaConversionException e4 = new LambdaConversionException("m", cause, false, false);
        check(e4.getStackTrace().length == 0, "stack trace disabled: not captured");
        e4.addSuppressed(new RuntimeException("s"));
        check(e4.getSuppressed().length == 0, "suppression disabled: suppressed dropped");
        e4.addSuppressed(new RuntimeException());
        check(e4.getSuppressed().length == 0, "suppressed dropped when disabled");

        // real trigger: LambdaMetafactory rejects a non-matching SAM name
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        java.lang.invoke.MethodHandle impl = lookup.findStatic(
                LambdaConversionExceptionTest.class, "body",
                MethodType.methodType(int.class, String.class));
        try {
            LambdaMetafactory.metafactory(lookup, "len",
                    MethodType.methodType(String.class),
                    MethodType.methodType(int.class, String.class),
                    impl, MethodType.methodType(int.class, String.class));
            check(false, "conversion failure must throw");
        } catch (LambdaConversionException e) {
            check(e.getMessage() != null, "factory failure has a message");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testInitCauseRules();
        testSerializationRoundTrip();
        testTriggerMessages();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepened contract (2026-11-05) ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String frag, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (t.getMessage() != null && t.getMessage().contains(frag)) {
                    passed++; return;
                }
                throw new AssertionError(msg + ": expected message fragment \"" + frag
                        + "\" but got \"" + t.getMessage() + "\"");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // initCause may be applied once; a constructor-provided cause is final.
    static void testInitCauseRules() {
        LambdaConversionException e = new LambdaConversionException("m");
        RuntimeException first = new RuntimeException("c1");
        check(e.initCause(first) == e, "initCause returns this");
        check(e.getCause() == first, "cause installed");
        checkThrowsMsg(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")),
                "Can't overwrite cause", "second initCause ISE message");
        LambdaConversionException withCause =
                new LambdaConversionException("m", new RuntimeException("c"));
        checkThrowsMsg(IllegalStateException.class,
                () -> withCause.initCause(new RuntimeException("c2")),
                "Can't overwrite cause", "ctor-provided cause is final");
    }

    // Serialization round-trip preserves the exact message.
    static void testSerializationRoundTrip() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new LambdaConversionException("lce-msg"));
        oos.close();
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof LambdaConversionException, "round-trip same class");
        check("lce-msg".equals(((LambdaConversionException) back).getMessage()),
                "message preserved by serialization");
    }

    // Exact JDK-27 trigger message plus a well-formed control link.
    static void testTriggerMessages() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        java.lang.invoke.MethodHandle impl = lookup.findStatic(
                LambdaConversionExceptionTest.class, "body",
                MethodType.methodType(int.class, String.class));
        checkThrowsMsg(LambdaConversionException.class,
                () -> LambdaMetafactory.metafactory(lookup, "len",
                        MethodType.methodType(String.class),
                        MethodType.methodType(int.class, String.class),
                        impl, MethodType.methodType(int.class, String.class)),
                "is not an interface", "metafactory non-interface functional type LCE");
        java.lang.invoke.CallSite cs = LambdaMetafactory.metafactory(lookup, "len",
                MethodType.methodType(F.class),
                MethodType.methodType(int.class, String.class),
                impl, MethodType.methodType(int.class, String.class));
        F f = (F) cs.dynamicInvoker().invokeExact();
        check(f.len("abcd") == 4, "well-formed metafactory link works");
    }
}
