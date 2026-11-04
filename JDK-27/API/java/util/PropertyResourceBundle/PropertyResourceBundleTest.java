/*
 * @test
 * @summary java.util.PropertyResourceBundle conformance: white-box structure plus black-box
 *          behavior of loading from a properties stream/reader, getString/getObject/containsKey,
 *          unicode escapes, keySet, and missing-key exceptions.
 * @run main PropertyResourceBundleTest
 */

import java.util.Locale;
import java.util.ResourceBundle;
import java.net.URL;
import java.net.URLClassLoader;
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


    // ===================== 契约深化：转义读取/父链 keySet/IO 异常透传/UTF-8 语义 =====================

    static void testUtf8EncodingContract() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("prb27");
        java.nio.file.Path iso = dir.resolve("iso.properties");
        java.nio.file.Files.write(iso, "k=caf\\u00e9".getBytes("ISO-8859-1"));
        PropertyResourceBundle b1 = new PropertyResourceBundle(
              new java.io.FileInputStream(iso.toFile()));
        check("caf\u00e9".equals(b1.getString("k")),
              "ISO-8859-1 file with unicode escape round-trips");
        java.nio.file.Path raw = dir.resolve("raw.properties");
        java.nio.file.Files.write(raw, "k=caf\u00e9".getBytes("UTF-8"));
        PropertyResourceBundle b2 = new PropertyResourceBundle(
              new java.io.FileInputStream(raw.toFile()));
        check("caf\u00e9".equals(b2.getString("k")),
              "JDK-9+ PropertyResourceBundle(InputStream) decodes UTF-8 directly");
        deletePrbTemp(dir);
    }

    static void testParentKeySetUnion() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("prbp");
        java.nio.file.Files.write(dir.resolve("chain.properties"),
              "pk=pv\nboth=parent".getBytes("ISO-8859-1"));
        java.nio.file.Files.write(dir.resolve("chain_fr.properties"),
              "ck=cv\nboth=child".getBytes("ISO-8859-1"));
        URLClassLoader cl = new URLClassLoader(new URL[]{ dir.toUri().toURL() });
        ResourceBundle child = ResourceBundle.getBundle("chain", new Locale("fr"), cl);
        check("cv".equals(child.getString("ck")), "child (fr) own key");
        check("child".equals(child.getString("both")), "child shadows parent value");
        check("pv".equals(child.getString("pk")), "parent key resolved through chain");
        check(child.keySet().containsAll(java.util.Arrays.asList("ck", "pk", "both")),
              "PropertyResourceBundle keySet includes parent keys");
        check(child.getClass().getName().endsWith("PropertyResourceBundle")
              || child instanceof PropertyResourceBundle,
              "properties-format bundle loaded from classpath");
        cl.close();
        deletePrbTemp(dir);
    }

    static void testReaderConstructorAndIoFailure() throws Exception {
        PropertyResourceBundle ok = new PropertyResourceBundle(new java.io.StringReader("a=1\nb=2"));
        check("1".equals(ok.getString("a")) && "2".equals(ok.getString("b")),
              "Reader ctor parses all entries");
        java.io.Reader bad = new java.io.Reader() {
            public int read(char[] cbuf, int off, int len) throws java.io.IOException {
                throw new java.io.IOException("boom");
            }
            public void close() { }
        };
        try {
            new PropertyResourceBundle(bad);
            check(false, "IOException from Reader propagates");
        } catch (java.io.IOException ioe) {
            check("boom".equals(ioe.getMessage()), "IOException from Reader propagates");
        }
    }

    static void deletePrbTemp(java.nio.file.Path dir) throws Exception {
        java.nio.file.DirectoryStream<java.nio.file.Path> ds = java.nio.file.Files.newDirectoryStream(dir);
        for (java.nio.file.Path p : ds) java.nio.file.Files.deleteIfExists(p);
        ds.close();
        java.nio.file.Files.deleteIfExists(dir);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testUtf8EncodingContract();
        testParentKeySetUnion();
        testReaderConstructorAndIoFailure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
