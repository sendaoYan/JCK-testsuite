/*
 * @test
 * @summary java.util.PropertyResourceBundle conformance: white-box structure plus black-box
 *          behavior of loading from a properties stream/reader, getString/getObject/containsKey,
 *          unicode escapes, keySet, and missing-key exceptions.
 * @run main PropertyResourceBundleTest
 */

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Set;

public class PropertyResourceBundleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.PropertyResourceBundle");
        check(!c.isInterface(), "PropertyResourceBundle is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "PropertyResourceBundle is concrete");
        check(java.util.ResourceBundle.class.isAssignableFrom(c), "PropertyResourceBundle extends ResourceBundle");
        c.getMethod("handleGetObject", String.class);
        c.getMethod("getKeys");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testFromStream();
        testFromReader();
        testUnicodeEscape();
        testNullConstructor();
        testMissingAndCasts();
    }

    static void testFromStream() throws Exception {
        PropertyResourceBundle b = new PropertyResourceBundle(
            new ByteArrayInputStream("k=v\nn=3\n".getBytes("US-ASCII")));
        check("v".equals(b.getString("k")), "getString");
        check("3".equals(b.getString("n")), "getString for int-valued property");
        check(b.containsKey("k"), "containsKey");
        check(!b.containsKey("missing"), "!containsKey missing");
    }

    static void testFromReader() throws Exception {
        PropertyResourceBundle b = new PropertyResourceBundle(
            new StringReader("greeting=hello\ncount=7\n"));
        check("hello".equals(b.getString("greeting")), "getString from reader");
        check("7".equals(b.getString("count")), "count value");
        Set<String> keys = b.keySet();
        check(keys.contains("greeting") && keys.contains("count"), "keySet contains keys");
        assertThrows(MissingResourceException.class,
                () -> b.getString("missing"), "getString missing throws MissingResourceException");
    }

    static void testUnicodeEscape() throws Exception {
        // " " in the properties text is parsed to a space by the properties loader.
        PropertyResourceBundle b = new PropertyResourceBundle(
            new StringReader("msg=hi\\u0020there"));
        check("hi there".equals(b.getString("msg")), "unicode escape parsed");
    }

    static void testNullConstructor() {
        assertThrows(NullPointerException.class, () -> {
            try { new PropertyResourceBundle((java.io.InputStream) null); }
            catch (java.io.IOException e) { throw new AssertionError("unexpected IOException", e); }
        }, "PropertyResourceBundle((InputStream)null) NPE");
        assertThrows(NullPointerException.class, () -> {
            try { new PropertyResourceBundle((java.io.Reader) null); }
            catch (java.io.IOException e) { throw new AssertionError("unexpected IOException", e); }
        }, "PropertyResourceBundle((Reader)null) NPE");
    }

    static void testMissingAndCasts() throws Exception {
        PropertyResourceBundle b = new PropertyResourceBundle(
            new StringReader("greeting=hello\n"));
        assertThrows(MissingResourceException.class,
                () -> b.getObject("missing"), "getObject missing throws MissingResourceException");
        // property values are Strings, so getStringArray ClassCastException.
        assertThrows(ClassCastException.class,
                () -> b.getStringArray("greeting"),
                "getStringArray on String value throws ClassCastException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
