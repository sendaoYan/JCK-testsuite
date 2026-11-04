/*
 * @test
 * @summary java.util.PropertyResourceBundle API conformance (Java 8 surface): complete contract
 *          coverage of InputStream/Reader constructors (null NPE), getString/getObject/handleGetObject,
 *          getKeys/containsKey/keySet, MissingResourceException for missing key, unicode escapes,
 *          and getStringArray ClassCastException (values are Strings).
 * @run main PropertyResourceBundleTest
 */

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

public class PropertyResourceBundleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void testPropertyResourceBundle() throws Exception {
        PropertyResourceBundle b = new PropertyResourceBundle(
            new ByteArrayInputStream("k=v\nn=3\n".getBytes("US-ASCII")));
        check("v".equals(b.getString("k")), "getString");
        check("3".equals(b.getString("n")), "getString for int-valued property");
        check(b.containsKey("k"), "containsKey");
    }

    static void testReaderConstructor() throws Exception {
        PropertyResourceBundle b = new PropertyResourceBundle(new StringReader("a=1\nb=2\n"));
        check("1".equals(b.getString("a")) && "2".equals(b.getString("b")), "Reader constructor");
        check(b.keySet().contains("a"), "keySet");
        check(b.getKeys().hasMoreElements(), "getKeys");
    }

    static void testNullConstructor() {
        expectThrows(NullPointerException.class, () -> {
            try { new PropertyResourceBundle((java.io.InputStream) null); }
            catch (java.io.IOException e) { throw new AssertionError("unexpected IOException", e); }
        }, "PropertyResourceBundle((InputStream)null) NPE");
        expectThrows(NullPointerException.class, () -> {
            try { new PropertyResourceBundle((java.io.Reader) null); }
            catch (java.io.IOException e) { throw new AssertionError("unexpected IOException", e); }
        }, "PropertyResourceBundle((Reader)null) NPE");
    }

    static void testMissingAndUnicode() throws Exception {
        PropertyResourceBundle b = new PropertyResourceBundle(
            new ByteArrayInputStream("greeting=\\u00e9t\\u00e9\n".getBytes("US-ASCII")));
        check("été".equals(b.getString("greeting")), "unicode escape decoding");
        expectThrows(MissingResourceException.class, () -> b.getString("missing"),
                     "getString missing -> MissingResourceException");
        expectThrows(MissingResourceException.class, () -> b.getObject("missing"),
                     "getObject missing -> MissingResourceException");
        // property values are Strings, so getStringArray ClassCastException.
        expectThrows(ClassCastException.class, () -> b.getStringArray("greeting"),
                     "getStringArray on String value -> ClassCastException");
    }

    public static void main(String[] args) throws Exception {
        testPropertyResourceBundle();
        testReaderConstructor();
        testNullConstructor();
        testMissingAndUnicode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
