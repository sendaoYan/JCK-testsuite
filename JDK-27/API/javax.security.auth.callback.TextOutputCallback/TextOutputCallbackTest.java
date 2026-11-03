/*
 * @test
 * @summary javax.security.auth.callback.TextOutputCallback conformance: white-box structure + black-box behavior.
 * @run main TextOutputCallbackTest
 */

import java.lang.reflect.*;

public class TextOutputCallbackTest {
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
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.callback.TextOutputCallback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(hasInterface(c, "javax.security.auth.callback.Callback"), "implements Callback");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getMessageType"), "method getMessageType");
        check(hasMethod(c, "getMessage"), "method getMessage");
        check(hasField(c, "INFORMATION"), "constant INFORMATION");
        check(hasField(c, "WARNING"), "constant WARNING");
        check(hasField(c, "ERROR"), "constant ERROR");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Message-type constants.
        check(javax.security.auth.callback.TextOutputCallback.INFORMATION == 0, "INFORMATION==0");
        check(javax.security.auth.callback.TextOutputCallback.WARNING == 1, "WARNING==1");
        check(javax.security.auth.callback.TextOutputCallback.ERROR == 2, "ERROR==2");

        // Each valid message type round-trips through getters.
        javax.security.auth.callback.TextOutputCallback info =
            new javax.security.auth.callback.TextOutputCallback(
                javax.security.auth.callback.TextOutputCallback.INFORMATION, "msg");
        check(info.getMessageType() == javax.security.auth.callback.TextOutputCallback.INFORMATION, "getMessageType info");
        check("msg".equals(info.getMessage()), "getMessage info");

        javax.security.auth.callback.TextOutputCallback warn =
            new javax.security.auth.callback.TextOutputCallback(
                javax.security.auth.callback.TextOutputCallback.WARNING, "careful");
        check(warn.getMessageType() == javax.security.auth.callback.TextOutputCallback.WARNING, "getMessageType warning");
        check("careful".equals(warn.getMessage()), "getMessage warning");

        javax.security.auth.callback.TextOutputCallback err =
            new javax.security.auth.callback.TextOutputCallback(
                javax.security.auth.callback.TextOutputCallback.ERROR, "boom");
        check(err.getMessageType() == javax.security.auth.callback.TextOutputCallback.ERROR, "getMessageType error");
        check("boom".equals(err.getMessage()), "getMessage error");

        // Constructor validation -> IllegalArgumentException.
        for (int mt : new int[]{-1, 3, 99}) {
            boolean t = false;
            try { new javax.security.auth.callback.TextOutputCallback(mt, "m"); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for messageType=" + mt);
        }
        for (String m : new String[]{null, ""}) {
            boolean t = false;
            try { new javax.security.auth.callback.TextOutputCallback(javax.security.auth.callback.TextOutputCallback.INFORMATION, m); }
            catch (IllegalArgumentException e) { t = true; }
            check(t, "IAE for message=" + (m == null ? "null" : "empty"));
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
