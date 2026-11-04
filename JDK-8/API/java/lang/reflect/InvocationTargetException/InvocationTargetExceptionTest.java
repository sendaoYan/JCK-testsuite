/*
 * @test
 * @summary java.lang.reflect.InvocationTargetException conformance: cause/target/message.
 *          Deepened batch: constructor census ((Throwable)/(Throwable,String) public plus
 *          protected no-arg via subclass), null target semantics (getCause/getTargetException
 *          null and initCause refused with "Can't overwrite cause"), ReflectiveOperationException
 *          hierarchy, real Method.invoke wrapping for runtime and checked causes with identity,
 *          suppressed exceptions and printStackTrace rendering, Serializable contract with a
 *          full serialization round-trip (message, cause type/message, target==cause link).
 * @run main InvocationTargetExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvocationTargetExceptionTest {
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

    /** Same as checkThrows but additionally asserts the message contains a fragment. */
    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
                               String fragment, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                String m = t.getMessage();
                check(m != null && m.contains(fragment),
                        msg + ": message should contain \"" + fragment + "\" but was " + m);
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static Object serRoundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }

    static class Boom {
        void runtimeBoom() {
            throw new ArithmeticException("/boom-marker");
        }

        void checkedBoom() throws IOException {
            throw new IOException("io-boom");
        }
    }

    /** Exercises the protected no-arg constructor (target set later is impossible). */
    static class Sub extends InvocationTargetException {
        Sub() {
            super();
        }
    }

    static void testInvocationTargetException() {
        InvocationTargetException e =
                new InvocationTargetException(new RuntimeException("cause"));
        check(e.getCause() instanceof RuntimeException, "getCause");
        check(e.getTargetException() == e.getCause(), "getTargetException == cause");
        InvocationTargetException e2 =
                new InvocationTargetException(new RuntimeException("cause"), "msg");
        check("msg".equals(e2.getMessage()), "message constructor");
        check(e2.getCause() instanceof RuntimeException, "cause constructor");
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testHierarchy() {
        check(InvocationTargetException.class.getSuperclass()
                == ReflectiveOperationException.class, "direct superclass");
        check(ReflectiveOperationException.class.isAssignableFrom(InvocationTargetException.class),
                "extends ReflectiveOperationException");
        check(Exception.class.isAssignableFrom(InvocationTargetException.class),
                "extends Exception");
        check(java.io.Serializable.class.isAssignableFrom(InvocationTargetException.class),
                "Throwable makes it Serializable");
        check(InvocationTargetException.class.getConstructors().length == 2,
                "two public constructors: (Throwable) and (Throwable,String)");
    }

    static void testNullTarget() {
        InvocationTargetException n = new InvocationTargetException(null);
        check(n.getCause() == null, "null target: getCause null");
        check(n.getTargetException() == null, "null target: getTargetException null");
        check(n.getMessage() == null, "null target: no message");
        checkThrowsMsg(IllegalStateException.class,
                () -> n.initCause(new RuntimeException("late")),
                "Can't overwrite cause", "initCause refused after null target");
    }

    static void testProtectedNoArg() {
        Sub sub = new Sub();
        check(sub instanceof InvocationTargetException, "subclass usable");
        check(sub.getCause() == null && sub.getTargetException() == null,
                "protected no-arg leaves target unset");
        check(sub.getMessage() == null, "protected no-arg leaves message unset");
        checkThrowsMsg(IllegalStateException.class,
                () -> sub.initCause(new RuntimeException("late")),
                "Can't overwrite cause", "no-arg subclass cannot set cause later");
    }

    static void testRealInvokeWrap() throws Exception {
        Method runtimeBoom = Boom.class.getDeclaredMethod("runtimeBoom");
        try {
            runtimeBoom.invoke(new Boom());
            throw new AssertionError("expected InvocationTargetException");
        } catch (InvocationTargetException e) {
            passed++;
            check(e.getCause() instanceof ArithmeticException, "runtime cause exact type");
            check("/boom-marker".equals(e.getCause().getMessage()), "runtime cause message");
            check(e.getTargetException() == e.getCause(), "target == cause identity");
        }
        Method checkedBoom = Boom.class.getDeclaredMethod("checkedBoom");
        try {
            checkedBoom.invoke(new Boom());
            throw new AssertionError("expected InvocationTargetException");
        } catch (InvocationTargetException e) {
            passed++;
            check(e.getCause() instanceof IOException, "checked cause exact type");
            check("io-boom".equals(e.getCause().getMessage()), "checked cause message");
            check(e.getTargetException() == e.getCause(), "checked target == cause identity");
        }
    }

    static void testConstructorState() {
        RuntimeException root = new RuntimeException("root");
        InvocationTargetException withMsg = new InvocationTargetException(root, "wrapper-msg");
        check("wrapper-msg".equals(withMsg.getMessage()), "message kept");
        check(withMsg.getCause() == root, "cause kept by reference");
        check(withMsg.getTargetException() == root, "target kept by reference");
        InvocationTargetException bare = new InvocationTargetException(root);
        check(bare.getMessage() == null, "no message when not given");
        check(bare.toString().contains("InvocationTargetException"), "toString names the type");
        check(withMsg.toString().contains("wrapper-msg"), "toString carries the message");
        // preset cause cannot be re-initialized
        checkThrowsMsg(IllegalStateException.class,
                () -> withMsg.initCause(new RuntimeException("other")),
                "Can't overwrite cause", "re-init refused");
        // an Error can ride as the target just as well as an exception
        OutOfMemoryError oom = new OutOfMemoryError("err-target");
        InvocationTargetException errWrap = new InvocationTargetException(oom, "e");
        check(errWrap.getCause() == oom, "error target kept");
        check(errWrap.getTargetException() == oom, "error target link");
    }

    static void testSuppressedAndPrinting() throws Exception {
        RuntimeException root = new RuntimeException("root");
        InvocationTargetException printed = new InvocationTargetException(root, "print-me");
        printed.addSuppressed(new IllegalArgumentException("sup"));
        check(printed.getSuppressed().length == 1, "suppressed carried");
        check(printed.getSuppressed()[0] instanceof IllegalArgumentException,
                "suppressed exact type");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos, true, "UTF-8");
        printed.printStackTrace(ps);
        String rendered = bos.toString("UTF-8");
        check(rendered.contains("InvocationTargetException"), "stack names wrapper");
        check(rendered.contains("root"), "stack renders cause");
        check(rendered.contains("sup"), "stack renders suppressed");
    }

    static void testSerialization() throws Exception {
        RuntimeException root = new RuntimeException("ser-root");
        InvocationTargetException orig = new InvocationTargetException(root, "ser-msg");
        orig.addSuppressed(new IllegalStateException("sup-ser"));
        Object back = serRoundTrip(orig);
        check(back instanceof InvocationTargetException, "round-trip keeps exact type");
        InvocationTargetException res = (InvocationTargetException) back;
        check("ser-msg".equals(res.getMessage()), "round-trip keeps message");
        check(res.getCause() instanceof RuntimeException, "round-trip keeps cause type");
        check("ser-root".equals(res.getCause().getMessage()), "round-trip keeps cause message");
        check(res.getTargetException() == res.getCause(), "round-trip keeps target==cause link");
        check(res.getSuppressed().length == 1, "round-trip keeps suppressed");
        check(res.getCause() != root, "cause is a copy, not the same reference");
        check(res instanceof java.io.Serializable, "instanceof Serializable");
    }

    static void testCheckedHolderSemantics() {
        // holding a checked exception inside needs no throws declaration on the holder
        InvocationTargetException holder = new InvocationTargetException(
                new IOException("held"), "wrapped");
        check(holder.getCause() instanceof IOException, "checked cause held without declares");
        String msg = holder.toString();
        check(msg.startsWith("java.lang.reflect.InvocationTargetException"),
                "toString starts with FQCN");
    }

    public static void main(String[] args) throws Exception {
        testInvocationTargetException();
        testHierarchy();
        testNullTarget();
        testProtectedNoArg();
        testRealInvokeWrap();
        testConstructorState();
        testSuppressedAndPrinting();
        testSerialization();
        testCheckedHolderSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
