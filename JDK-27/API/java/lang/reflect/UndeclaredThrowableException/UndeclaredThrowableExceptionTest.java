/*
 * @test
 * @summary java.lang.reflect.UndeclaredThrowableException conformance: white-box structure + black-box behavior.
 *          Deepened batch: constructor census + always-preset cause (initCause refused),
 *          unchecked semantics without declaring, serialization round-trip keeping the
 *          undeclared==cause link, bare toString exact form, suppressed printing.
 * @run main UndeclaredThrowableExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        UndeclaredThrowableException e =
                new UndeclaredThrowableException(new RuntimeException("cause"));
        check(e.getCause() instanceof RuntimeException, "getCause");
        check(e.getUndeclaredThrowable() == e.getCause(), "getUndeclaredThrowable == cause");
        UndeclaredThrowableException e2 =
                new UndeclaredThrowableException(new RuntimeException("cause"), "msg");
        check("msg".equals(e2.getMessage()), "message constructor");
        check(e2.getCause() instanceof RuntimeException, "cause constructor");
        check(java.lang.RuntimeException.class
                .isAssignableFrom(UndeclaredThrowableException.class), "extends RuntimeException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. real trigger: proxy handler throws undeclared checked exception
        java.io.IOException checked = new java.io.IOException("io-root");
        InvocationHandler h = (p, m, a) -> { throw checked; };
        Safe proxy = (Safe) Proxy.newProxyInstance(
                UndeclaredThrowableExceptionTest.class.getClassLoader(),
                new Class<?>[]{Safe.class}, h);
        try {
            proxy.run();
            check(false, "undeclared checked must wrap");
        } catch (UndeclaredThrowableException e) {
            check(e.getUndeclaredThrowable() == checked, "undeclared kept by reference");
            check(e.getCause() == checked, "cause is the undeclared throwable");
        }

        // 2. declared checked exceptions propagate raw (no wrap)
        InvocationHandler dh = (p, m, a) -> { throw new java.io.IOException("declared"); };
        Declared dp = (Declared) Proxy.newProxyInstance(
                UndeclaredThrowableExceptionTest.class.getClassLoader(),
                new Class<?>[]{Declared.class}, dh);
        try {
            dp.run();
            check(false, "declared must propagate raw");
        } catch (java.io.IOException e) {
            check("declared".equals(e.getMessage()), "declared raw IOException");
        } catch (UndeclaredThrowableException e) {
            check(false, "declared must not wrap");
        }

        // 3. runtime exceptions propagate raw through proxies
        InvocationHandler rh = (p, m, a) -> { throw new IllegalStateException("rt"); };
        Safe rp = (Safe) Proxy.newProxyInstance(
                UndeclaredThrowableExceptionTest.class.getClassLoader(),
                new Class<?>[]{Safe.class}, rh);
        try {
            rp.run();
            check(false, "runtime must propagate raw");
        } catch (IllegalStateException e) {
            check("rt".equals(e.getMessage()), "runtime raw");
        } catch (UndeclaredThrowableException e) {
            check(false, "runtime must not wrap");
        }

        // 4. constructor surface
        RuntimeException root = new RuntimeException("r");
        UndeclaredThrowableException withMsg = new UndeclaredThrowableException(root, "m");
        check("m".equals(withMsg.getMessage()), "message kept");
        check(withMsg.getUndeclaredThrowable() == root, "undeclared by reference");
        check(withMsg.getCause() == root, "cause by reference");
        UndeclaredThrowableException bare = new UndeclaredThrowableException(root);
        check(bare.getMessage() == null, "no message");
        check(bare.toString().contains("UndeclaredThrowableException"), "toString names type");

        // 5. wrapping preserves the original on repeated access
        try {
            proxy.fetch("k");
            check(false, "second dispatch must wrap again");
        } catch (UndeclaredThrowableException e) {
            check(e.getUndeclaredThrowable() == checked, "second wrap same reference");
        }

        // 6. error-type throwables also propagate raw from handlers
        InvocationHandler eh = (p, m, a) -> { throw new UnknownError("err"); };
        Safe ep = (Safe) Proxy.newProxyInstance(
                UndeclaredThrowableExceptionTest.class.getClassLoader(),
                new Class<?>[]{Safe.class}, eh);
        try {
            ep.run();
            check(false, "error must propagate raw");
        } catch (UnknownError e) {
            check(true, "error raw");
        } catch (UndeclaredThrowableException e) {
            check(false, "error must not wrap");
        }
    }

    interface Declared {
        void run() throws java.io.IOException;
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testCensusAndUnchecked() {
        check(UndeclaredThrowableException.class.getSuperclass() == RuntimeException.class,
              "direct RuntimeException subclass");
        check(java.io.Serializable.class.isAssignableFrom(UndeclaredThrowableException.class),
              "Serializable via Throwable");
        // javap census: only (Throwable) and (Throwable,String); no no-arg constructor,
        // the cause can never be left uninitialized by construction
        check(UndeclaredThrowableException.class.getConstructors().length == 2,
              "exactly two public constructors");
        UndeclaredThrowableException thrown = noDeclares();
        check(thrown.getCause() instanceof java.io.IOException,
              "checked cause held without any throws clause");
    }

    static UndeclaredThrowableException noDeclares() {
        return new UndeclaredThrowableException(new java.io.IOException("u"), "why");
    }

    static void testCauseAlwaysPreset() {
        RuntimeException root = new RuntimeException("r2");
        UndeclaredThrowableException e = new UndeclaredThrowableException(root);
        try {
            e.initCause(new RuntimeException("other"));
            check(false, "initCause must be refused");
        } catch (IllegalStateException ex) {
            check(ex.getMessage() != null && ex.getMessage().contains("Can't overwrite cause"),
                  "initCause refused with canonical message");
        }
        check("java.lang.reflect.UndeclaredThrowableException".equals(e.toString()),
              "bare toString exact (message null, cause only in stack traces)");
    }

    static void testSerializationRoundTrip() throws Exception {
        UndeclaredThrowableException orig =
                new UndeclaredThrowableException(new java.io.IOException("ser-io"), "ser-msg");
        Object back = serRoundTrip(orig);
        check(back instanceof UndeclaredThrowableException, "round-trip keeps exact type");
        UndeclaredThrowableException res = (UndeclaredThrowableException) back;
        check("ser-msg".equals(res.getMessage()), "round-trip keeps message");
        check(res.getCause() instanceof java.io.IOException, "round-trip keeps cause type");
        check("ser-io".equals(res.getCause().getMessage()), "round-trip keeps cause message");
        check(res.getUndeclaredThrowable() == res.getCause(),
              "round-trip keeps undeclared==cause link");
        check(res.getCause() != orig.getCause(), "cause is a copy");
    }

    static void testSuppressedPrinting() throws Exception {
        UndeclaredThrowableException e =
                new UndeclaredThrowableException(new java.io.IOException("io2"), "p2");
        e.addSuppressed(new IllegalArgumentException("sup2"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos, true, "UTF-8"));
        String rendered = bos.toString("UTF-8");
        check(rendered.contains("UndeclaredThrowableException"), "stack names wrapper");
        check(rendered.contains("io2"), "stack renders cause");
        check(rendered.contains("sup2"), "stack renders suppressed");
    }

    static void testProxyWrapShape() throws Exception {
        // the proxy machinery wraps each offending dispatch in a fresh UTE with null message
        final java.io.IOException checked = new java.io.IOException("shape-io");
        InvocationHandler h = (p, m, a) -> { throw checked; };
        Safe proxy = (Safe) Proxy.newProxyInstance(
                UndeclaredThrowableExceptionTest.class.getClassLoader(),
                new Class<?>[]{Safe.class}, h);
        UndeclaredThrowableException first;
        try {
            proxy.run();
            throw new AssertionError("must wrap");
        } catch (UndeclaredThrowableException e) {
            first = e;
        }
        check(first.getMessage() == null, "proxy-generated UTE message null (probed 8/27)");
        check(first instanceof RuntimeException, "UTE is unchecked");
        UndeclaredThrowableException second;
        try {
            proxy.fetch("k");
            throw new AssertionError("must wrap again");
        } catch (UndeclaredThrowableException e) {
            second = e;
        }
        check(second != first, "each dispatch wraps in a fresh UTE instance");
        check(second.getUndeclaredThrowable() == checked, "fresh wrap same cause reference");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCensusAndUnchecked();
        testCauseAlwaysPreset();
        testSerializationRoundTrip();
        testSuppressedPrinting();
        testProxyWrapShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
