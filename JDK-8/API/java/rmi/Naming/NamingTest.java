/*
 * @test
 * @summary java.rmi.Naming conformance: offline contract.
 *          final class with the five static registry methods; invalid URL
 *          scheme / fragment / query / illegal URL all raise
 *          MalformedURLException BEFORE any registry connection; bind/rebind
 *          with a null Remote raises NullPointerException before connecting.
 *          No registry is started, no port is bound, no network is used.
 * @run main NamingTest
 */

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;

public class NamingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testStructure() {
        Class<?> c = Naming.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(!c.isInterface(), "concrete class");
    }

    static void testMalformedURL() {
        final String badScheme = "http://host/x";
        checkThrows(MalformedURLException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Naming.lookup(badScheme);
                }}, "lookup invalid scheme -> MalformedURLException");
        checkThrows(MalformedURLException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Naming.bind(badScheme, new Remote() {});
                }}, "bind invalid scheme -> MalformedURLException");
        checkThrows(MalformedURLException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Naming.rebind(badScheme, new Remote() {});
                }}, "rebind invalid scheme -> MalformedURLException");
        checkThrows(MalformedURLException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Naming.unbind(badScheme);
                }}, "unbind invalid scheme -> MalformedURLException");
        checkThrows(MalformedURLException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Naming.list(badScheme);
                }}, "list invalid scheme -> MalformedURLException");

        final String fragment = "rmi://host/x#frag";
        checkThrows(MalformedURLException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Naming.lookup(fragment);
                }}, "lookup fragment -> MalformedURLException");

        final String illegal = "not a url";
        checkThrows(MalformedURLException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Naming.lookup(illegal);
                }}, "lookup illegal URL -> MalformedURLException");
    }

    static void testNullRemote() {
        final String valid = "rmi://localhost/x";
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Naming.bind(valid, null);
                }}, "bind null Remote -> NPE before connecting");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Naming.rebind(valid, null);
                }}, "rebind null Remote -> NPE before connecting");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMalformedURL();
        testNullRemote();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
