/*
 * @test
 * @summary java.lang.reflect.UndeclaredThrowableException conformance: cause/undeclared/message.
 *          Deepened batch: constructor census (both constructors take a Throwable, so the
 *          cause is always preset and getUndeclaredThrowable mirrors getCause), unchecked
 *          semantics, real proxy triggers (undeclared checked wraps with cause identity,
 *          declared checked / runtime / error relay raw), initCause refusal, suppressed
 *          exceptions, printStackTrace, and a full serialization round-trip.
 * @run main UndeclaredThrowableExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

public class UndeclaredThrowableExceptionTest {
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

    interface Safe {
        void run();

        String fetch(String key);
    }

    interface Declared {
        void run() throws IOException;
    }

    static void testUndeclaredThrowableException() {
        UndeclaredThrowableException e =
                new UndeclaredThrowableException(new RuntimeException("cause"));
        check(e.getCause() instanceof RuntimeException, "getCause");
        check(e.getUndeclaredThrowable() == e.getCause(), "getUndeclaredThrowable == cause");
        UndeclaredThrowableException e2 =
                new UndeclaredThrowableException(new RuntimeException("cause"), "msg");
        check("msg".equals(e2.getMessage()), "message constructor");
        check(e2.getCause() instanceof RuntimeException, "cause constructor");
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testHierarchyAndCensus() {
        check(RuntimeException.class.isAssignableFrom(UndeclaredThrowableException.class),
                "extends RuntimeException (unchecked)");
        check(UndeclaredThrowableException.class.getSuperclass() == RuntimeException.class,
                "direct RuntimeException subclass");
        check(java.io.Serializable.class.isAssignableFrom(UndeclaredThrowableException.class),
                "Serializable via Throwable");
        // javap census: only (Throwable) and (Throwable,String); no no-arg constructor,
        // so the cause can never be left uninitialized by construction
        check(UndeclaredThrowableException.class.getConstructors().length == 2,
                "exactly two public constructors");
    }

    static void testUncheckedSemantics() {
        // no throws clause needed anywhere
        UndeclaredThrowableException thrown = throwIt();
        check(thrown != null, "throwable without declaring");
        check(thrown.getCause() instanceof IOException, "checked cause held unchecked");
    }

    static UndeclaredThrowableException throwIt() {
        return new UndeclaredThrowableException(new IOException("u"), "why");
    }

    static void testConstructorState() {
        RuntimeException root = new RuntimeException("r");
        UndeclaredThrowableException withMsg = new UndeclaredThrowableException(root, "m");
        check("m".equals(withMsg.getMessage()), "message kept");
        check(withMsg.getUndeclaredThrowable() == root, "undeclared by reference");
        check(withMsg.getCause() == root, "cause by reference");
        UndeclaredThrowableException bare = new UndeclaredThrowableException(root);
        check(bare.getMessage() == null, "no message without one");
        check("java.lang.reflect.UndeclaredThrowableException".equals(bare.toString()),
                "bare toString exact (cause only appears in stack traces)");
        // cause is preset by both constructors: re-initialization refused
        checkThrowsMsg(IllegalStateException.class,
                () -> withMsg.initCause(new RuntimeException("other")),
                "Can't overwrite cause", "initCause refused");
    }

    static void testRealProxyTrigger() throws Exception {
        ClassLoader l = UndeclaredThrowableExceptionTest.class.getClassLoader();

        // undeclared checked exception thrown by a handler is wrapped, identity kept
        final IOException checked = new IOException("undeclared-io");
        InvocationHandler uh = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) throws Throwable {
                throw checked;
            }
        };
        Safe p = (Safe) Proxy.newProxyInstance(l, new Class<?>[]{Safe.class}, uh);
        try {
            p.run();
            throw new AssertionError("expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException e) {
            passed++;
            check(e.getCause() == checked, "wrapped cause identity");
            check(e.getUndeclaredThrowable() == checked, "undeclared identity");
            check(e.getMessage() == null, "proxy-generated message null (probed 8/27)");
        }
        // every dispatch wraps a fresh UTE around the same cause
        try {
            p.fetch("k");
            throw new AssertionError("expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException e) {
            passed++;
            check(e.getUndeclaredThrowable() == checked, "second wrap same cause reference");
        }

        // declared checked exception relays raw
        InvocationHandler dh = new InvocationHandler() {
            public Object invoke(Object pr, Method m, Object[] a) throws Throwable {
                throw new IOException("declared");
            }
        };
        Declared d = (Declared) Proxy.newProxyInstance(l, new Class<?>[]{Declared.class}, dh);
        checkThrows(IOException.class, () -> d.run(), "declared checked relays raw");

        // runtime exception relays raw
        InvocationHandler rh = new InvocationHandler() {
            public Object invoke(Object pr, Method m, Object[] a) {
                throw new IllegalStateException("rt");
            }
        };
        Safe rp = (Safe) Proxy.newProxyInstance(l, new Class<?>[]{Safe.class}, rh);
        checkThrows(IllegalStateException.class, () -> rp.run(), "runtime relays raw");

        // errors relay raw too
        InvocationHandler eh = new InvocationHandler() {
            public Object invoke(Object pr, Method m, Object[] a) {
                throw new UnknownError("err");
            }
        };
        Safe ep = (Safe) Proxy.newProxyInstance(l, new Class<?>[]{Safe.class}, eh);
        checkThrows(UnknownError.class, () -> ep.run(), "error relays raw");
    }

    static void testSuppressedAndPrinting() throws Exception {
        UndeclaredThrowableException e =
                new UndeclaredThrowableException(new IOException("io"), "p");
        e.addSuppressed(new IllegalArgumentException("sup"));
        check(e.getSuppressed().length == 1, "suppressed carried");
        check(e.getSuppressed()[0] instanceof IllegalArgumentException, "suppressed exact type");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos, true, "UTF-8");
        e.printStackTrace(ps);
        String rendered = bos.toString("UTF-8");
        check(rendered.contains("UndeclaredThrowableException"), "stack names wrapper");
        check(rendered.contains("io"), "stack renders cause");
        check(rendered.contains("sup"), "stack renders suppressed");
    }

    static void testSerialization() throws Exception {
        UndeclaredThrowableException orig =
                new UndeclaredThrowableException(new IOException("ser-io"), "ser-msg");
        Object back = serRoundTrip(orig);
        check(back instanceof UndeclaredThrowableException, "round-trip keeps exact type");
        UndeclaredThrowableException res = (UndeclaredThrowableException) back;
        check("ser-msg".equals(res.getMessage()), "round-trip keeps message");
        check(res.getCause() instanceof IOException, "round-trip keeps cause type");
        check("ser-io".equals(res.getCause().getMessage()), "round-trip keeps cause message");
        check(res.getUndeclaredThrowable() == res.getCause(),
                "round-trip keeps undeclared==cause link");
        check(res.getCause() != orig.getCause(), "cause is a copy");
        check(res instanceof java.io.Serializable, "instanceof Serializable");
    }

    public static void main(String[] args) throws Exception {
        testUndeclaredThrowableException();
        testHierarchyAndCensus();
        testUncheckedSemantics();
        testConstructorState();
        testRealProxyTrigger();
        testSuppressedAndPrinting();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
