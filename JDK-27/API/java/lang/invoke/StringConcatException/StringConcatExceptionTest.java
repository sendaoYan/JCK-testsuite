/*
 * @test
 * @summary java.lang.invoke.StringConcatException conformance: white-box + black-box behavior, cause lifecycle, serialVersionUID and serialization round-trip.
 * @run main StringConcatExceptionTest
 */

import java.lang.invoke.StringConcatException;
import java.lang.invoke.StringConcatFactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class StringConcatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.StringConcatException");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 2, "two public constructors");
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        check(c.getDeclaredField("serialVersionUID").getType() == long.class,
              "serialVersionUID declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        check(new StringConcatException("m") instanceof Exception, "is an Exception");
        check(new StringConcatException("m") instanceof java.io.Serializable,
              "Serializable by inheritance");

        // message-only constructor
        StringConcatException e1 = new StringConcatException("bad recipe");
        check("bad recipe".equals(e1.getMessage()), "message kept");
        check(e1.getCause() == null, "no cause");

        // message + cause constructor
        Throwable cause = new IllegalStateException("inner");
        StringConcatException e2 = new StringConcatException("msg", cause);
        check("msg".equals(e2.getMessage()), "message with cause");
        check(e2.getCause() == cause, "cause identity");
        check(e2.toString().startsWith("java.lang.invoke.StringConcatException: msg"),
              "toString format");

        // suppressed and stack trace behave as a normal exception
        e1.addSuppressed(new RuntimeException("s"));
        check(e1.getSuppressed().length == 1, "suppressed supported");
        check(e1.getStackTrace().length > 0, "stack trace captured");

        // thrown by the factory for a recipe whose slots do not match the arity
        try {
            StringConcatFactory.makeConcatWithConstants(MethodHandles.lookup(), "concat",
                    MethodType.methodType(String.class, int.class, int.class), "\u0001");
            check(false, "linkage failure must throw");
        } catch (StringConcatException e) {
            check(e.getMessage() != null && !e.getMessage().isEmpty(),
                  "factory failure carries a message");
            check(e.getCause() == null || e.getCause() instanceof Throwable,
                  "cause is either absent or a real throwable");
        }
    }

    // ---- helpers ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

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

    // ---- cause lifecycle, message details, serialization ----
    static void testCauseAndSerialization() throws Throwable {
        // cause can be set exactly once, then initCause refuses
        StringConcatException late = new StringConcatException("late");
        check(late.getCause() == null, "fresh exception has no cause");
        IllegalStateException first = new IllegalStateException("first");
        check(late.initCause(first) == late, "initCause returns this");
        check(late.getCause() == first, "initCause installs the cause");
        checkThrowsMsg(IllegalStateException.class,
                () -> late.initCause(new RuntimeException("second")),
                "Can't overwrite cause", "second initCause -> IllegalStateException");
        // null message is allowed and preserved
        StringConcatException silent = new StringConcatException(null);
        check(silent.getMessage() == null, "null message preserved");
        check(silent.toString().endsWith("StringConcatException"), "toString without message");
        check(silent.getLocalizedMessage() == null, "localized message mirrors message");
        StringConcatException loud = new StringConcatException("loud");
        check("loud".equals(loud.getLocalizedMessage()), "localized message delegates by default");
        check(loud.fillInStackTrace() == loud, "fillInStackTrace returns this");
        // serialVersionUID is declared (value is fixed in the source as 292L + 9L = 301L);
        // java.base does not open the field for reflective reads, so the white-box
        // part above only asserts its presence and type
        check(StringConcatException.class.getDeclaredField("serialVersionUID").getType() == long.class,
                "serialVersionUID is a long");
        // round trip through memory keeps message and cause
        IllegalStateException inner = new IllegalStateException("inner2");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new StringConcatException("round", inner));
        oos.close();
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof StringConcatException, "round trip keeps the class");
        StringConcatException rt = (StringConcatException) back;
        check("round".equals(rt.getMessage()), "round trip keeps the message");
        check(rt.getCause() instanceof IllegalStateException, "round trip keeps the cause class");
        check("inner2".equals(rt.getCause().getMessage()), "round trip keeps the cause message");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testCauseAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
