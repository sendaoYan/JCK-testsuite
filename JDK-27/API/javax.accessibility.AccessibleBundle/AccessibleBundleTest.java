/*
 * @test
 * @summary javax.accessibility.AccessibleBundle conformance: white-box structure + black-box behavior.
 * @run main AccessibleBundleTest
 */

import java.lang.reflect.*;
import java.util.Locale;

public class AccessibleBundleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // Minimal concrete subclass exposing the protected key field and the
    // protected toDisplayString(String, Locale) helper.
    static class TestBundle extends javax.accessibility.AccessibleBundle {
        TestBundle(String k) { this.key = k; }
        String display(String bundle, Locale l) { return toDisplayString(bundle, l); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleBundle");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "toDisplayString"), "declares toDisplayString");
        check(hasMethod(c, "toString"), "declares toString");
    }

    static void testBlackBox() throws Exception {
        TestBundle active = new TestBundle("active");
        check("active".equals(active.toDisplayString(Locale.US)), "known key localized US");
        check("active".equals(active.toDisplayString(Locale.ROOT)), "known key localized ROOT");

        TestBundle unknown = new TestBundle("completelyUnknownKey_zzz_123");
        check("completelyUnknownKey_zzz_123".equals(unknown.toDisplayString(Locale.US)),
                "unknown key falls back to raw key (US)");
        check("completelyUnknownKey_zzz_123".equals(unknown.display("no.such.bundle.Name", Locale.US)),
                "missing bundle falls back to raw key");
        check("active".equals(active.display("com.sun.accessibility.internal.resources.accessibility", Locale.US)),
                "protected toDisplayString(name,locale) works");

        // toString() and toDisplayString() both use the default locale.
        check(active.toString() != null, "toString non-null");
        check(active.toString().equals(active.toDisplayString()), "toString == toDisplayString()");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
