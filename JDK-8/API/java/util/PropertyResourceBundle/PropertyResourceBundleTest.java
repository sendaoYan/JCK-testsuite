/*
 * @test
 * @summary java.util.PropertyResourceBundle API conformance (Java 8 surface): complete contract
 *          coverage of InputStream/Reader constructors (null NPE), getString/getObject/handleGetObject,
 *          getKeys/containsKey/keySet, MissingResourceException for missing key, unicode escapes,
 *          and getStringArray ClassCastException (values are Strings).
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


    // ===================== 契约深化：转义读取/父链 keySet/IO 异常透传/UTF-8 语义 =====================

    static void testUtf8EncodingContract() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("prb8");
        java.nio.file.Path iso = dir.resolve("iso.properties");
        java.nio.file.Files.write(iso, "k=caf\\u00e9".getBytes("ISO-8859-1"));
        // ctor 会一次性读完 InputStream，读完即关；句柄留着会让 Windows 上后续
        // 删除临时文件报 ERROR_SHARING_VIOLATION。
        PropertyResourceBundle b1;
        try (java.io.FileInputStream in = new java.io.FileInputStream(iso.toFile())) {
            b1 = new PropertyResourceBundle(in);
        }
        check("caf\u00e9".equals(b1.getString("k")),
              "ISO-8859-1 file with unicode escape round-trips");
        java.nio.file.Path raw = dir.resolve("raw.properties");
        java.nio.file.Files.write(raw, "k=caf\u00e9".getBytes("UTF-8"));
        PropertyResourceBundle b2;
        try (java.io.FileInputStream in = new java.io.FileInputStream(raw.toFile())) {
            b2 = new PropertyResourceBundle(in);
        }
        check(!"caf\u00e9".equals(b2.getString("k")),
              "JDK-8 reads raw UTF-8 bytes as ISO-8859-1 (mojibake, escapes required)");
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

    // 清理不是被测契约。Windows 上类加载器/资源缓存可能短暂持有句柄，
    // 删除抛 IOException 时重试几次，仍失败就登记 deleteOnExit。
    static void deletePrbTemp(java.nio.file.Path dir) throws Exception {
        try (java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                     java.nio.file.Files.newDirectoryStream(dir)) {
            for (java.nio.file.Path p : ds) deleteBestEffort(p);
        }
        deleteBestEffort(dir);
    }

    static void deleteBestEffort(java.nio.file.Path p) {
        for (int i = 0; i < 10; i++) {
            try {
                java.nio.file.Files.deleteIfExists(p);
                return;
            } catch (java.io.IOException e) {
                System.gc();
                try { Thread.sleep(50); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        p.toFile().deleteOnExit();
    }

    public static void main(String[] args) throws Exception {
        testPropertyResourceBundle();
        testReaderConstructor();
        testNullConstructor();
        testMissingAndUnicode();
        testUtf8EncodingContract();
        testParentKeySetUnion();
        testReaderConstructorAndIoFailure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
