/*
 * @test
 * @summary java.lang.invoke.WrongMethodTypeException conformance: white-box + black-box behavior.
 *          Deepened: serialization round-trip preserving the message, initCause
 *          rules ("Can't overwrite cause" ISE), exact toString form, and the
 *          JDK-27 wording of trigger messages ("handle's method type X but
 *          found Y" for invokeExact).
 * @run main WrongMethodTypeExceptionTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

public class WrongMethodTypeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.WrongMethodTypeException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 2, "two public constructors");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        check(new WrongMethodTypeException() instanceof RuntimeException, "RuntimeException");
        check(new WrongMethodTypeException() instanceof RuntimeException,
              "direct RuntimeException subclass");
        WrongMethodTypeException e0 = new WrongMethodTypeException();
        check(e0.getMessage() == null, "no-arg message null");
        WrongMethodTypeException e1 = new WrongMethodTypeException("type mismatch");
        check("type mismatch".equals(e1.getMessage()), "String ctor message");
        check(e1.toString().startsWith("java.lang.invoke.WrongMethodTypeException: "),
              "toString format");

        // real trigger 1: invokeExact with statically wrong parameter types
        MethodHandle id = MethodHandles.identity(int.class);
        try {
            int r = (int) id.invokeExact("not an int");
            check(r == 0, "unreachable");
            check(false, "invokeExact wrong arg must throw");
        } catch (WrongMethodTypeException e) {
            check(true, "invokeExact wrong static type -> WrongMethodTypeException");
        }

        // real trigger 2: invokeWithArguments with wrong arity
        try {
            id.invokeWithArguments(1, 2, 3);
            check(false, "arity mismatch must throw");
        } catch (WrongMethodTypeException e) {
            check(true, "arity -> WrongMethodTypeException");
        }

        // real trigger 3: asType requiring an impossible conversion
        MethodHandle len = MethodHandles.lookup().findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        try {
            len.asType(MethodType.methodType(int.class, int.class));
            check(false, "impossible conversion must throw");
        } catch (WrongMethodTypeException e) {
            check(true, "asType -> WrongMethodTypeException");
        }

        // real trigger 4: dynamic invoker of a CallSite called at the wrong type
        java.lang.invoke.MutableCallSite mcs =
                new java.lang.invoke.MutableCallSite(MethodType.methodType(int.class));
        MethodHandle inv = mcs.dynamicInvoker();
        try {
            inv.invokeExact("wrong");
            check(false, "dynamic invoker wrong type must throw");
        } catch (WrongMethodTypeException e) {
            check(true, "dynamic invoker -> WrongMethodTypeException");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testToStringAndCauseRules();
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

    static void testToStringAndCauseRules() {
        WrongMethodTypeException e = new WrongMethodTypeException("bad type");
        check(e.toString().equals("java.lang.invoke.WrongMethodTypeException: bad type"),
              "toString full form");
        check(e.getCause() == null, "fresh exception has no cause");
        RuntimeException first = new RuntimeException("c1");
        check(e.initCause(first) == e, "initCause returns this");
        check(e.getCause() == first, "cause installed");
        checkThrowsMsg(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")),
                "Can't overwrite cause", "second initCause ISE message");
    }

    // Serialization round-trip preserves the exact message.
    static void testSerializationRoundTrip() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new WrongMethodTypeException("kept-msg"));
        oos.close();
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof WrongMethodTypeException, "round-trip same class");
        check("kept-msg".equals(((WrongMethodTypeException) back).getMessage()),
              "message preserved by serialization");
        check(java.io.Serializable.class.isAssignableFrom(back.getClass()),
              "round-trip object is Serializable");
    }

    // Exact JDK-27 trigger messages.
    static void testTriggerMessages() throws Throwable {
        MethodHandle id = MethodHandles.identity(int.class);
        checkThrowsMsg(WrongMethodTypeException.class,
                () -> { int r = (int) id.invokeExact("not an int"); },
                "handle's method type (int)int but found (String)int",
                "invokeExact static mismatch WMTE message");
        checkThrowsMsg(WrongMethodTypeException.class,
                () -> id.invokeWithArguments(1, 2, 3),
                "cannot convert MethodHandle(int)int to (Object,Object,Object)Object",
                "arity mismatch WMTE message");
        MethodHandle len = MethodHandles.lookup().findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        checkThrowsMsg(WrongMethodTypeException.class,
                () -> len.asType(MethodType.methodType(int.class, int.class)),
                "cannot convert MethodHandle(String)int to (int)int",
                "impossible asType WMTE message");
    }
}
