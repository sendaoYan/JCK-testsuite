/*
 * @test
 * @summary javax.security.auth.callback.NameCallback conformance: white-box structure + black-box behavior.
 * @run main NameCallbackTest
 */

import java.lang.reflect.*;

public class NameCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.callback.NameCallback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(hasInterface(c, "javax.security.auth.callback.Callback"), "implements Callback");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        String[] ms = {"getPrompt", "getDefaultName", "setName", "getName"};
        for (String n : ms) check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Prompt-only constructor: no default name, no input name.
        javax.security.auth.callback.NameCallback c = new javax.security.auth.callback.NameCallback("prompt");
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(c.getDefaultName() == null, "default name null");
        check(c.getName() == null, "input name null initially");

        // Default-name constructor.
        javax.security.auth.callback.NameCallback c2 = new javax.security.auth.callback.NameCallback("p", "bob");
        check("bob".equals(c2.getDefaultName()), "default name ctor");
        check(c2.getName() == null, "input name null with default ctor");

        // setName/getName round-trip.
        c.setName("alice");
        check("alice".equals(c.getName()), "setName/getName");
        c.setName(null);
        check(c.getName() == null, "setName(null)");

        // Constructor validation -> IllegalArgumentException.
        for (String p : new String[]{null, ""}) {
            boolean t = false;
            try { new javax.security.auth.callback.NameCallback(p); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for prompt=" + (p == null ? "null" : "empty"));
        }
        // defaultName null / empty
        for (String d : new String[]{null, ""}) {
            boolean t = false;
            try { new javax.security.auth.callback.NameCallback("p", d); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for defaultName=" + (d == null ? "null" : "empty"));
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
