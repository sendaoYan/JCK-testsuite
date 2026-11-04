/*
 * @test
 * @summary javax.security.auth.callback.TextInputCallback conformance: white-box structure + black-box behavior.
 * @run main TextInputCallbackTest
 */

import java.lang.reflect.*;

public class TextInputCallbackTest {
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
        Class<?> c = Class.forName("javax.security.auth.callback.TextInputCallback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(hasInterface(c, "javax.security.auth.callback.Callback"), "implements Callback");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        String[] ms = {"getPrompt", "getDefaultText", "setText", "getText"};
        for (String n : ms) check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Prompt-only constructor.
        javax.security.auth.callback.TextInputCallback c = new javax.security.auth.callback.TextInputCallback("prompt");
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(c.getDefaultText() == null, "default text null");
        check(c.getText() == null, "input text null initially");

        // Default-text constructor.
        javax.security.auth.callback.TextInputCallback c2 = new javax.security.auth.callback.TextInputCallback("p", "d");
        check("d".equals(c2.getDefaultText()), "default text ctor");
        check(c2.getText() == null, "input text null with default ctor");

        // setText/getText round-trip.
        c.setText("value");
        check("value".equals(c.getText()), "setText/getText");
        c.setText(null);
        check(c.getText() == null, "setText(null)");

        // Constructor validation -> IllegalArgumentException.
        for (String p : new String[]{null, ""}) {
            boolean t = false;
            try { new javax.security.auth.callback.TextInputCallback(p); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for prompt=" + (p == null ? "null" : "empty"));
        }
        for (String d : new String[]{null, ""}) {
            boolean t = false;
            try { new javax.security.auth.callback.TextInputCallback("p", d); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for defaultText=" + (d == null ? "null" : "empty"));
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
