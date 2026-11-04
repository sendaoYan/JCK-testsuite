/*
 * @test
 * @summary java.net.FileNameMap conformance: interface shape, custom
 *          implementations, URLConnection.getFileNameMap/setFileNameMap
 *          round-trip, and the JDK-provided content-type table via
 *          guessContentTypeFromName (.txt/.html/.gif/.jpg/.png/... plus
 *          case-insensitivity, unknown/no-extension -> null) and
 *          guessContentTypeFromStream (html/xml/gif magic, junk -> null,
 *          NPE on null). JDK-8 note: some newer extensions (.css/.js/.json
 *          etc.) map to null on this JDK but have types on JDK 9+; only the
 *          JDK-8-stable entries are asserted here.
 * @run main FileNameMapTest
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.FileNameMap;
import java.net.URLConnection;

public class FileNameMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---------- interface shape ----------

    static void testInterfaceShape() throws Exception {
        Class<?> c = Class.forName("java.net.FileNameMap");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        Method m = c.getMethod("getContentTypeFor", String.class);
        check(m != null, "has method getContentTypeFor");
        check(m.getReturnType() == String.class, "getContentTypeFor returns String");
        check(Modifier.isPublic(m.getModifiers()), "method public");
        check(Modifier.isAbstract(m.getModifiers()), "method abstract");
        check(c.getMethods().length == 1, "exactly one interface method: " + c.getMethods().length);
    }

    // ---------- custom implementations ----------

    static void testCustomImplementations() {
        FileNameMap map = new FileNameMap() {
            public String getContentTypeFor(String fileName) {
                if (fileName == null) return null;
                if (fileName.endsWith(".txt")) return "text/plain";
                if (fileName.endsWith(".html")) return "text/html";
                if (fileName.endsWith(".bin")) return "application/octet-stream";
                return null;
            }
        };
        check("text/plain".equals(map.getContentTypeFor("readme.txt")), "custom txt");
        check("text/html".equals(map.getContentTypeFor("index.html")), "custom html");
        check("application/octet-stream".equals(map.getContentTypeFor("data.bin")), "custom bin");
        check(null == map.getContentTypeFor("unknown.ext"), "custom unknown null");
        check(null == map.getContentTypeFor("noext"), "custom noext null");
        check(null == map.getContentTypeFor(""), "custom empty null");
        check(null == map.getContentTypeFor(null), "custom null null");
        // second independent implementation: exact-match table
        FileNameMap exact = new FileNameMap() {
            public String getContentTypeFor(String fileName) {
                return "x".equals(fileName) ? "application/x-x" : "zz";
            }
        };
        check("application/x-x".equals(exact.getContentTypeFor("x")), "exact match hit");
        check("zz".equals(exact.getContentTypeFor("anything")), "exact match default");
        check(!"zz".equals(map.getContentTypeFor("anything")), "implementations independent");
        // lambda implements the interface
        FileNameMap lambda = n -> "L";
        check("L".equals(lambda.getContentTypeFor("n")), "lambda implementation");
        // two lambdas are distinct objects with equal behavior
        FileNameMap lambda2 = n -> "L";
        check(lambda != lambda2, "distinct lambda instances");
        check(lambda.getContentTypeFor("q").equals(lambda2.getContentTypeFor("q")), "same behavior");
    }

    // ---------- URLConnection.getFileNameMap / setFileNameMap ----------

    static void testGetSetFileNameMap() {
        FileNameMap original = URLConnection.getFileNameMap();
        check(original != null, "getFileNameMap non-null");
        // JDK-provided table resolves common extensions (stable across JDK 8/27)
        check("text/plain".equals(original.getContentTypeFor("x.txt")), "jdk .txt");
        check("text/html".equals(original.getContentTypeFor("x.html")), "jdk .html");
        check("text/html".equals(original.getContentTypeFor("x.htm")), "jdk .htm");
        check("image/gif".equals(original.getContentTypeFor("x.gif")), "jdk .gif");
        check("image/jpeg".equals(original.getContentTypeFor("x.jpg")), "jdk .jpg");
        check("image/jpeg".equals(original.getContentTypeFor("x.jpeg")), "jdk .jpeg");
        check("image/png".equals(original.getContentTypeFor("x.png")), "jdk .png");
        check("application/pdf".equals(original.getContentTypeFor("x.pdf")), "jdk .pdf");
        check("application/xml".equals(original.getContentTypeFor("x.xml")), "jdk .xml");
        check("application/zip".equals(original.getContentTypeFor("x.zip")), "jdk .zip");
        check("application/x-tar".equals(original.getContentTypeFor("x.tar")), "jdk .tar");
        check("audio/x-wav".equals(original.getContentTypeFor("x.wav")), "jdk .wav");
        // case-insensitive extension matching
        check("text/plain".equals(original.getContentTypeFor("x.TXT")), "jdk .TXT case-insensitive");
        check("text/html".equals(original.getContentTypeFor("x.HTML")), "jdk .HTML case-insensitive");
        check("image/gif".equals(original.getContentTypeFor("x.GIF")), "jdk .GIF case-insensitive");
        check("image/png".equals(original.getContentTypeFor("x.PNG")), "jdk .PNG case-insensitive");
        check("image/jpeg".equals(original.getContentTypeFor("x.Jpg")), "jdk .Jpg case-insensitive");
        // unknown extension and no extension resolve to null
        check(null == original.getContentTypeFor("x.unknownext"), "unknown ext null");
        check(null == original.getContentTypeFor("noext"), "no ext null");
        // bare extension (hidden file) still matches
        check("text/plain".equals(original.getContentTypeFor(".txt")), "bare .txt matches");
        // path with directories matches on the final extension
        check("text/plain".equals(original.getContentTypeFor("a/b/c.txt")), "path .txt matches");
        check("text/html".equals(original.getContentTypeFor("/deep/dir/pic.HTML")), "path .HTML matches");
        // setFileNameMap installs a custom map globally (restored in finally).
        // The getter may hand back a wrapper that delegates, so assert behavior not identity.
        try {
            FileNameMap custom = n -> "custom/type";
            URLConnection.setFileNameMap(custom);
            check("custom/type".equals(URLConnection.getFileNameMap().getContentTypeFor("any")), "custom map consulted");
            check("custom/type".equals(URLConnection.getFileNameMap().getContentTypeFor("x.txt")), "custom overrides txt");
        } finally {
            URLConnection.setFileNameMap(original);
        }
        check("text/plain".equals(URLConnection.getFileNameMap().getContentTypeFor("x.txt")), "original map restored");
        check("image/gif".equals(URLConnection.getFileNameMap().getContentTypeFor("x.gif")), "restored map gif");
        // setFileNameMap accepts null without throwing
        try {
            URLConnection.setFileNameMap(null);
            passed++;
            URLConnection.setFileNameMap(original);
        } catch (NullPointerException e) {
            check(false, "setFileNameMap(null) must not throw NPE");
        }
    }

    // ---------- guessContentTypeFromName ----------

    static void testGuessContentTypeFromName() {
        // stable entries (identical on JDK 8 and 27, probe-verified)
        check("text/plain".equals(URLConnection.guessContentTypeFromName("f.txt")), "guess .txt");
        check("text/html".equals(URLConnection.guessContentTypeFromName("f.html")), "guess .html");
        check("text/html".equals(URLConnection.guessContentTypeFromName("f.htm")), "guess .htm");
        check("image/gif".equals(URLConnection.guessContentTypeFromName("f.gif")), "guess .gif");
        check("image/jpeg".equals(URLConnection.guessContentTypeFromName("f.jpg")), "guess .jpg");
        check("image/jpeg".equals(URLConnection.guessContentTypeFromName("f.jpeg")), "guess .jpeg");
        check("image/png".equals(URLConnection.guessContentTypeFromName("f.png")), "guess .png");
        check("application/pdf".equals(URLConnection.guessContentTypeFromName("f.pdf")), "guess .pdf");
        check("application/xml".equals(URLConnection.guessContentTypeFromName("f.xml")), "guess .xml");
        check("application/zip".equals(URLConnection.guessContentTypeFromName("f.zip")), "guess .zip");
        check("application/x-tar".equals(URLConnection.guessContentTypeFromName("f.tar")), "guess .tar");
        check("audio/x-wav".equals(URLConnection.guessContentTypeFromName("f.wav")), "guess .wav");
        // unknown / missing extensions
        check(null == URLConnection.guessContentTypeFromName("f.unknownext"), "guess unknown null");
        check(null == URLConnection.guessContentTypeFromName("noext"), "guess noext null");
        check(null == URLConnection.guessContentTypeFromName(""), "guess empty null");
        // case-insensitive
        check("text/plain".equals(URLConnection.guessContentTypeFromName("F.TXT")), "guess .TXT");
        check("image/jpeg".equals(URLConnection.guessContentTypeFromName("F.JpEg")), "guess .JpEg");
        // paths and URLs both accepted
        check("text/plain".equals(URLConnection.guessContentTypeFromName("/x/y/z.txt")), "guess path");
        check("image/gif".equals(URLConnection.guessContentTypeFromName("http://h/d/f.gif")), "guess url");
        // JDK-8: these newer web extensions resolve to null (they gained types in later JDKs)
        check(null == URLConnection.guessContentTypeFromName("f.css"), "JDK8 .css null");
        check(null == URLConnection.guessContentTypeFromName("f.js"), "JDK8 .js null");
        check(null == URLConnection.guessContentTypeFromName("f.json"), "JDK8 .json null");
        check(null == URLConnection.guessContentTypeFromName("f.svg"), "JDK8 .svg null");
        // null name -> NPE
        try {
            URLConnection.guessContentTypeFromName(null);
            check(false, "guess null must throw NPE");
        } catch (NullPointerException e) {
            passed++;
        }
    }

    // ---------- guessContentTypeFromStream ----------

    static InputStream ascii(String s) {
        byte[] b = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) b[i] = (byte) s.charAt(i);
        return new ByteArrayInputStream(b);
    }

    static void testGuessContentTypeFromStream() throws Exception {
        check("text/html".equals(URLConnection.guessContentTypeFromStream(ascii("<HTML><HEAD>"))), "stream html");
        check("text/html".equals(URLConnection.guessContentTypeFromStream(ascii("<html><body"))), "stream html lower");
        check("text/html".equals(URLConnection.guessContentTypeFromStream(ascii("<!doctype html>"))), "stream doctype");
        check("text/html".equals(URLConnection.guessContentTypeFromStream(ascii("<head><title>x"))), "stream head");
        check("text/html".equals(URLConnection.guessContentTypeFromStream(ascii("<body>x"))), "stream body");
        check("application/xml".equals(URLConnection.guessContentTypeFromStream(ascii("<?xml version=\"1.0\"?>"))), "stream xml");
        check("image/gif".equals(URLConnection.guessContentTypeFromStream(
                new ByteArrayInputStream(new byte[]{'G', 'I', 'F', '8', '7', 'a', 1, 0, 1, 0}))), "stream gif87");
        check("image/gif".equals(URLConnection.guessContentTypeFromStream(
                new ByteArrayInputStream(new byte[]{'G', 'I', 'F', '8', '9', 'a', 1, 0, 1, 0}))), "stream gif89");
        // non-matching content -> null
        check(null == URLConnection.guessContentTypeFromStream(ascii("just plain text here")), "stream text null");
        check(null == URLConnection.guessContentTypeFromStream(
                new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10})), "stream binary null");
        // empty and too-short streams do not throw
        check(null == URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(new byte[0])), "stream empty null");
        check(null == URLConnection.guessContentTypeFromStream(ascii("<")), "stream one-char null");
        check(null == URLConnection.guessContentTypeFromStream(ascii("<H")), "stream two-char null");
        // the stream is fully rewound after sniffing (mark/reset): nothing consumed
        InputStream probe = ascii("<HTML>rest-of-document");
        String type = URLConnection.guessContentTypeFromStream(probe);
        check("text/html".equals(type), "stream html with body");
        int remaining = probe.available();
        check(remaining == "<HTML>rest-of-document".length(), "stream fully rewound after sniff: " + remaining);
        // null stream -> NPE
        try {
            URLConnection.guessContentTypeFromStream(null);
            check(false, "stream null must throw NPE");
        } catch (NullPointerException e) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testInterfaceShape();
        testCustomImplementations();
        testGetSetFileNameMap();
        testGuessContentTypeFromName();
        testGuessContentTypeFromStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
