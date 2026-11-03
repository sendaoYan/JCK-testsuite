/*
 * @test
 * @summary java.lang.reflect.UndeclaredThrowableException conformance: white-box structure + black-box behavior.
 * @run main UndeclaredThrowableExceptionTest
 */

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
