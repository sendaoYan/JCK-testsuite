/*
 * @test
 * @summary java.lang.invoke.WrongMethodTypeException conformance: hierarchy and ctors.
 *          Deepened: exact public constructor set (only () and (String)),
 *          toString format, Serializable contract with serialization
 *          round-trip preserving the message, initCause rules (once then
 *          IllegalStateException "Can't overwrite cause"), and three real
 *          triggers with exact message fragments (invokeExact static type
 *          mismatch, invokeWithArguments arity, asType impossible conversion).
 * @run main WrongMethodTypeExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

    static void testWrongMethodTypeException() {
        check(RuntimeException.class.isAssignableFrom(WrongMethodTypeException.class),
              "extends RuntimeException");
        check(new WrongMethodTypeException().getMessage() == null, "()");
        check("wrong".equals(new WrongMethodTypeException("wrong").getMessage()), "(String)");
    }

    // Exactly two public constructors (no cause-taking overloads exist).
    static void testConstructorChain() throws Exception {
        Class<?> c = WrongMethodTypeException.class;
        check(c.getSuperclass() == RuntimeException.class, "direct RuntimeException subclass");
        int publicCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) publicCtors++;
        }
        check(publicCtors == 2, "exactly two public constructors");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        check(Serializable.class.isAssignableFrom(c), "Serializable via Throwable");
        WrongMethodTypeException e = new WrongMethodTypeException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getCause() == null, "no-arg cause null");
        check(e.toString().startsWith("java.lang.invoke.WrongMethodTypeException"),
              "toString starts with class name");
        WrongMethodTypeException m = new WrongMethodTypeException("bad type");
        check("bad type".equals(m.getMessage()), "(String) message");
        check(m.toString().equals("java.lang.invoke.WrongMethodTypeException: bad type"),
              "toString full form");
    }

    // Serialization round-trip preserves the exact message.
    static void testSerializationRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new WrongMethodTypeException("kept-msg"));
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        check(back instanceof WrongMethodTypeException, "round-trip same class");
        check("kept-msg".equals(((WrongMethodTypeException) back).getMessage()),
              "message preserved by serialization");
        check(back instanceof Serializable, "round-trip object is Serializable");
    }

    // initCause may be applied once (there is no cause constructor); a second
    // call is the standard IllegalStateException.
    static void testInitCauseRules() {
        WrongMethodTypeException e = new WrongMethodTypeException("m");
        RuntimeException first = new RuntimeException("c1");
        RuntimeException second = new RuntimeException("c2");
        check(e.initCause(first) == e, "initCause returns this");
        check(e.getCause() == first, "cause installed");
        checkThrowsMsg(IllegalStateException.class, () -> e.initCause(second),
              "Can't overwrite cause", "second initCause ISE message");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
    }

    // Real triggers with the exact messages the JDK produces.
    static void testRealTriggers() throws Throwable {
        MethodHandle id = MethodHandles.identity(int.class);
        checkThrowsMsg(WrongMethodTypeException.class,
              () -> { int r = (int) id.invokeExact("not an int"); },
              "but found (String)int", "invokeExact wrong static arg WMTE message");
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
        // every trigger still yields a RuntimeException
        try {
            int r = (int) id.invokeExact("x");
            check(r == 0, "unreachable");
            check(false, "must throw");
        } catch (WrongMethodTypeException e) {
            check(e instanceof RuntimeException, "WMTE is a RuntimeException");
            check(e.getMessage() != null, "trigger carries a message");
        }
    }

    // Suppression/stack-trace defaults and the message-less toString form.
    static void testThrowableDefaults() {
        WrongMethodTypeException e = new WrongMethodTypeException();
        check(e.toString().equals("java.lang.invoke.WrongMethodTypeException"),
              "no-message toString has no colon");
        check(e.getStackTrace() != null, "stack trace array non-null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        RuntimeException sup = new RuntimeException("sup");
        e.addSuppressed(sup);
        check(e.getSuppressed().length == 1, "suppression enabled by default");
        check(e.getSuppressed()[0] == sup, "suppressed exception identity");
        check(new WrongMethodTypeException("x").equals(new WrongMethodTypeException("x")) == false,
              "exceptions never compare equal");
        try {
            WrongMethodTypeException.class.getDeclaredField("serialVersionUID");
            check(true, "serialVersionUID declared");
        } catch (NoSuchFieldException nsf) {
            check(false, "serialVersionUID should be declared: " + nsf);
        }
    }

    public static void main(String[] args) throws Throwable {
        testWrongMethodTypeException();
        testConstructorChain();
        testSerializationRoundTrip();
        testInitCauseRules();
        testRealTriggers();
        testThrowableDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
