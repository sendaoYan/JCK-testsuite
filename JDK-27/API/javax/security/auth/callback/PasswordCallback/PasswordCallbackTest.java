/*
 * @test
 * @summary javax.security.auth.callback.PasswordCallback conformance: white-box structure + black-box behavior.
 * @run main PasswordCallbackTest
 */

import java.lang.reflect.*;
import java.util.Arrays;

public class PasswordCallbackTest {
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
        Class<?> c = Class.forName("javax.security.auth.callback.PasswordCallback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(hasInterface(c, "javax.security.auth.callback.Callback"), "implements Callback");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        String[] ms = {"getPrompt", "isEchoOn", "setPassword", "getPassword", "clearPassword"};
        for (String n : ms) check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Construction and echo flag.
        javax.security.auth.callback.PasswordCallback c = new javax.security.auth.callback.PasswordCallback("prompt", false);
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(!c.isEchoOn(), "isEchoOn false");
        check(new javax.security.auth.callback.PasswordCallback("p", true).isEchoOn(), "isEchoOn true");

        // No password set -> null.
        check(c.getPassword() == null, "getPassword null initially");

        // setPassword stores a copy; getPassword returns a copy.
        char[] pw = "secret".toCharArray();
        c.setPassword(pw);
        pw[0] = 'X'; // mutating the argument must not affect stored password
        char[] got = c.getPassword();
        check(Arrays.equals("secret".toCharArray(), got), "setPassword stores copy");
        got[0] = 'Y'; // mutating the returned copy must not affect stored password
        check(Arrays.equals("secret".toCharArray(), c.getPassword()), "getPassword returns copy");

        // setPassword(null) -> null.
        c.setPassword(null);
        check(c.getPassword() == null, "setPassword(null)");

        // clearPassword scrubs the internal buffer; getPassword no longer returns the secret.
        char[] secret = "hunter2".toCharArray();
        c.setPassword(secret);
        c.clearPassword();
        char[] cleared = c.getPassword();
        check(cleared != null, "cleared password non-null buffer");
        check(cleared.length == secret.length, "cleared password keeps length");
        check(!Arrays.equals(secret, cleared), "clearPassword scrubs secret");
        // A second clearPassword on an already-cleared callback is harmless.
        c.clearPassword();
        check(!Arrays.equals(secret, c.getPassword()), "double clearPassword harmless");

        // clearPassword with no password set is a no-op (still null).
        javax.security.auth.callback.PasswordCallback empty = new javax.security.auth.callback.PasswordCallback("p", false);
        empty.clearPassword();
        check(empty.getPassword() == null, "clearPassword without password stays null");

        // Constructor validation -> IllegalArgumentException.
        for (String p : new String[]{null, ""}) {
            boolean t = false;
            try { new javax.security.auth.callback.PasswordCallback(p, false); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for prompt=" + (p == null ? "null" : "empty"));
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
