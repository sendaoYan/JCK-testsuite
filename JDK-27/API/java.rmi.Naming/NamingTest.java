/*
 * @test
 * @summary java.rmi.Naming conformance: white-box structure + black-box behavior (no live registry).
 * @run main NamingTest
 */

import java.rmi.Naming;
import java.rmi.Remote;

public class NamingTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.Naming");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "final class");
        check(c.getDeclaredConstructors().length == 1, "single ctor");
        check(java.lang.reflect.Modifier.isPrivate(c.getDeclaredConstructors()[0].getModifiers()), "private ctor");
        java.util.TreeSet<String> m = new java.util.TreeSet<>();
        for (java.lang.reflect.Method mm : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isStatic(mm.getModifiers()) && java.lang.reflect.Modifier.isPublic(mm.getModifiers())) m.add(mm.getName());
        check(m.equals(java.util.Set.of("bind", "list", "lookup", "rebind", "unbind")), "static methods");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try {
            Naming.lookup("rmi:foo");
            check(false, "opaque URL must throw");
        } catch (java.net.MalformedURLException expected) {
            check(true, "lookup opaque URL -> MalformedURLException");
        }

        try {
            Naming.lookup("http://localhost/x");
            check(false, "bad scheme must throw");
        } catch (java.net.MalformedURLException expected) {
            check(true, "lookup bad scheme -> MalformedURLException");
        }

        Remote r = Naming.lookup("//localhost:0/");
        check(r != null, "empty-name lookup returns registry stub without connecting");

        try {
            Naming.bind("//localhost:0/x", null);
            check(false, "bind null must throw");
        } catch (NullPointerException expected) {
            check(true, "bind(null) -> NullPointerException (no connect)");
        }

        try {
            Naming.rebind("//localhost:0/x", null);
            check(false, "rebind null must throw");
        } catch (NullPointerException expected) {
            check(true, "rebind(null) -> NullPointerException (no connect)");
        }

        try {
            Naming.list("rmi:foo");
            check(false, "list opaque URL must throw");
        } catch (java.net.MalformedURLException expected) {
            check(true, "list opaque URL -> MalformedURLException");
        }

        try {
            Naming.unbind("rmi:foo");
            check(false, "unbind opaque URL must throw");
        } catch (java.net.MalformedURLException expected) {
            check(true, "unbind opaque URL -> MalformedURLException");
        }

        boolean threw = false;
        try {
            Remote r2 = Naming.lookup("//localhost:0/x");
            check(r2 == null, "lookup on closed port unexpectedly returned");
        } catch (java.rmi.ConnectException e) {
            threw = true;
        } catch (java.rmi.NotBoundException e) {
            threw = true;
        } catch (java.rmi.RemoteException e) {
            threw = true;
        }
        check(threw, "lookup on closed port threw RemoteException/NotBoundException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
