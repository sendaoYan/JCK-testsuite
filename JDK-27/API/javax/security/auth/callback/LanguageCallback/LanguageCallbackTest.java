/*
 * @test
 * @summary javax.security.auth.callback.LanguageCallback conformance: white-box structure + black-box behavior.
 * @run main LanguageCallbackTest
 */

import java.lang.reflect.*;
import java.util.Locale;

public class LanguageCallbackTest {
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
        Class<?> c = Class.forName("javax.security.auth.callback.LanguageCallback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(hasInterface(c, "javax.security.auth.callback.Callback"), "implements Callback");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "setLocale"), "method setLocale");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.security.auth.callback.LanguageCallback c =
            new javax.security.auth.callback.LanguageCallback();

        // Default locale is null.
        check(c.getLocale() == null, "initial locale null");

        // Set/get round-trip with fixed locales (no system default dependence).
        c.setLocale(Locale.ROOT);
        check(Locale.ROOT.equals(c.getLocale()), "getLocale ROOT");
        c.setLocale(Locale.US);
        check(Locale.US.equals(c.getLocale()), "getLocale US");
        c.setLocale(Locale.forLanguageTag("zh-CN"));
        check("zh-CN".equals(c.getLocale().toLanguageTag()), "getLocale zh-CN");

        // null locale is accepted and returned.
        c.setLocale(null);
        check(c.getLocale() == null, "setLocale(null)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
