/*
 * @test
 * @summary java.net.NetPermission conformance: white-box structure + black-box behavior.
 * @run main NetPermissionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class NetPermissionTest {
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
        Class<?> c = Class.forName("java.net.NetPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(java.security.BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, String.class) != null, "ctor(String,String)");
    }

    static void testBlackBox() throws Exception {
        NetPermission p = new NetPermission("setDefaultAuthenticator");
        check(p.getName().equals("setDefaultAuthenticator"), "name");
        check(p.getActions().equals(""), "empty actions default");

        NetPermission p2 = new NetPermission("allowHttpTrace", "perm");
        check(p2.getName().equals("allowHttpTrace"), "name with actions");
        check(p2.getActions().equals(""), "actions ignored (BasicPermission)");

        check(p.implies(new NetPermission("setDefaultAuthenticator")), "implies same name");
        check(!p.implies(new NetPermission("allowHttpTrace")), "not implies different name");

        check(p.equals(new NetPermission("setDefaultAuthenticator")), "equals");
        check(p.hashCode() == new NetPermission("setDefaultAuthenticator").hashCode(), "hashCode");

        fails(IllegalArgumentException.class, "empty name", () -> new NetPermission(""));
        fails(NullPointerException.class, "null name", () -> new NetPermission(null));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
