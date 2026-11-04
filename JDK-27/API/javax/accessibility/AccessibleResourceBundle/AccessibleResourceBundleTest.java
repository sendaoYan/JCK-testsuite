/*
 * @test
 * @summary javax.accessibility.AccessibleResourceBundle conformance: white-box structure + black-box behavior.
 * @run main AccessibleResourceBundleTest
 */

import java.lang.reflect.*;
import java.util.Enumeration;

public class AccessibleResourceBundleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleResourceBundle");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.util.ListResourceBundle.class, "extends ListResourceBundle");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
        check(hasMethod(c, "getContents"), "declares getContents");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleResourceBundle b = new javax.accessibility.AccessibleResourceBundle();
        Object[][] contents = b.getContents();
        check(contents != null, "getContents non-null");
        check(contents.length > 0, "getContents non-empty");

        // Every row is a two-element key/value pair.
        for (Object[] row : contents) {
            check(row != null && row.length == 2, "content row is [key,value]");
        }

        // getString (inherited from ResourceBundle) resolves known keys.
        check("active".equals(b.getString("active")), "getString active");
        check("push button".equals(b.getString("pushbutton")), "getString pushbutton");
        check("label for".equals(b.getString("labelFor")), "getString labelFor");

        // handleGetObject returns the localized value.
        check("active".equals(b.handleGetObject("active")), "handleGetObject active");

        // Unknown keys throw MissingResourceException (getString).
        boolean threw = false;
        try { b.getString("noSuchKey_zzz"); } catch (java.util.MissingResourceException e) { threw = true; }
        check(threw, "unknown key throws MissingResourceException");

        // getKeys yields all keys.
        Enumeration<String> keys = b.getKeys();
        int n = 0;
        while (keys.hasMoreElements()) { keys.nextElement(); n++; }
        check(n == contents.length, "getKeys count == contents count");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
