/*
 * @test
 * @summary javax.security.auth.login.CredentialExpiredException conformance: white-box structure + black-box behavior.
 * @run main CredentialExpiredExceptionTest
 */

import javax.security.auth.login.CredentialExpiredException;

public class CredentialExpiredExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            check(type.isInstance(t), msg + " (got " + t.getClass().getName() + ")");
            return;
        }
        check(false, msg + " (no exception)");
    }

    static boolean hasCtor(Class<?> c, Class<?>... p) {
        try { c.getDeclaredConstructor(p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.login.CredentialExpiredException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("javax.security.auth.login.CredentialException"), "extends javax.security.auth.login.CredentialException");
        check(hasCtor(c) && hasCtor(c, String.class), "no-arg + String constructors present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CredentialExpiredException e = new CredentialExpiredException("hello");
        check("hello".equals(e.getMessage()), "message ctor getMessage");
        check(new CredentialExpiredException().getMessage() == null, "no-arg message null");
        check(e instanceof javax.security.auth.login.CredentialExpiredException, "e instanceof javax.security.auth.login.CredentialExpiredException");
        check(e instanceof javax.security.auth.login.CredentialException, "e instanceof javax.security.auth.login.CredentialException");
        check(e instanceof javax.security.auth.login.LoginException, "e instanceof javax.security.auth.login.LoginException");
        check(e instanceof java.security.GeneralSecurityException, "e instanceof java.security.GeneralSecurityException");

        // cause semantics inherited from Throwable
        CredentialExpiredException c = new CredentialExpiredException();
        IllegalStateException cause = new IllegalStateException("cause");
        check(c.initCause(cause) == c, "initCause returns this");
        check(c.getCause() == cause, "getCause returns cause");
        expect(IllegalStateException.class, () -> c.initCause(new RuntimeException()), "second initCause throws IllegalStateException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
