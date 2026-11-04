/*
 * @test
 * @summary java.net.SocketPermission conformance: white-box structure + black-box behavior.
 * @run main SocketPermissionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class SocketPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(java.security.Permission.class.isAssignableFrom(c), "extends Permission");
        check(c.getConstructor(String.class, String.class) != null, "ctor(String,String)");
        check(c.getMethod("getActions") != null, "getActions");
        check(c.getMethod("implies", java.security.Permission.class) != null, "implies");
        check(c.getMethod("newPermissionCollection") != null, "newPermissionCollection");
    }

    static void testBlackBox() throws Exception {
        SocketPermission sp = new SocketPermission("127.0.0.1:80", "connect");
        String actions = sp.getActions();
        check(actions.contains("connect"), "actions has connect");
        check(actions.contains("resolve"), "connect implies resolve");
        check(sp.getName().equals("127.0.0.1:80"), "name");

        check(sp.implies(new SocketPermission("127.0.0.1:80", "connect")), "implies same");
        check(sp.implies(new SocketPermission("127.0.0.1:80", "resolve")), "implies subset");
        check(!sp.implies(new SocketPermission("127.0.0.1:81", "connect")), "not implies different port");
        check(!sp.implies(new SocketPermission("127.0.0.1:80", "accept")), "not implies accept");

        // listen/accept actions
        SocketPermission listen = new SocketPermission("127.0.0.1:80", "listen");
        check(listen.getActions().contains("listen"), "listen action");

        check(sp.equals(new SocketPermission("127.0.0.1:80", "connect")), "equals");
        check(sp.hashCode() == new SocketPermission("127.0.0.1:80", "connect").hashCode(), "hashCode");

        java.security.PermissionCollection pc = sp.newPermissionCollection();
        check(pc != null, "permission collection non-null");
        pc.add(sp);
        check(pc.implies(new SocketPermission("127.0.0.1:80", "resolve")), "collection implies");

        fails(IllegalArgumentException.class, "bad action", () -> new SocketPermission("127.0.0.1:80", "bogus"));
        fails(NullPointerException.class, "null action", () -> new SocketPermission("127.0.0.1:80", null));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
