/*
 * @test
 * @summary javax.security.sasl.AuthorizeCallback conformance: white-box structure + black-box behavior.
 * @run main AuthorizeCallbackTest
 */

import java.lang.reflect.*;
import javax.security.sasl.AuthorizeCallback;

public class AuthorizeCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.AuthorizeCallback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(hasInterface(c, "javax.security.auth.callback.Callback"), "implements Callback");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        boolean twoStr = false;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 2 && p[0] == String.class && p[1] == String.class) twoStr = true;
        }
        check(twoStr, "String,String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AuthorizeCallback c = new AuthorizeCallback("authId", "authzId");
        check("authId".equals(c.getAuthenticationID()), "getAuthenticationID");
        check("authzId".equals(c.getAuthorizationID()), "getAuthorizationID");
        check(!c.isAuthorized(), "not authorized initially");
        c.setAuthorized(true);
        c.setAuthorizedID("newId");
        check(c.isAuthorized(), "authorized after set");
        check("newId".equals(c.getAuthorizedID()), "getAuthorizedID");
        c.setAuthorized(false);
        check(!c.isAuthorized(), "authorized toggles off");

        AuthorizeCallback d = new AuthorizeCallback(null, null);
        check(d.getAuthenticationID() == null, "null authenticationID");
        check(d.getAuthorizationID() == null, "null authorizationID");
        d.setAuthorizedID(null);
        check(d.getAuthorizedID() == null, "setAuthorizedID(null)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
