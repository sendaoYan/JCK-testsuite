/*
 * @test
 * @summary java.net.URLEncoder conformance: white-box structure + full contract
 *          coverage. space->'+', unreserved pass-through, everything else %XX
 *          uppercase UTF-8 hex, UTF-16 round-trip, surrogate-pair UTF-8
 *          expansion, JDK 10+ Charset overload, the deprecated single-arg
 *          overload, and null/empty-charset NPE/UnsupportedEncodingException.
 * @run main URLEncoderTest
 */

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class URLEncoderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLEncoder");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getMethod("encode", String.class) != null, "encode(String)");
        check(c.getMethod("encode", String.class, String.class) != null, "encode(String,String)");
        check(c.getMethod("encode", String.class, Charset.class) != null, "encode(String,Charset)");
        check(Modifier.isPrivate(c.getDeclaredConstructor().getModifiers()), "private ctor");
    }

    static void testBasic() throws Exception {
        check("a+b%26c".equals(URLEncoder.encode("a b&c", "UTF-8")), "space and ampersand");
        check("%C3%BC".equals(URLEncoder.encode("ü", "UTF-8")), "UTF-8 non-ascii");
        check("%FE%FF%01%00".equals(URLEncoder.encode("Ā", "UTF-16")), "UTF-16 with BOM");
    }

    static void testCharacterClass() throws Exception {
        String in = "Hello +%-_.!~*'()@®āႠ";
        String expected = "Hello+%2B%25-_.%21%7E*%27%28%29%40%C2%AE%C4%81%E1%82%A0";
        check(expected.equals(URLEncoder.encode(in, "UTF-8")), "full character-class encoding");
        check("AZaz09-_.*".equals(URLEncoder.encode("AZaz09-_.*", "UTF-8")), "unreserved passthrough");
    }

    static void testSurrogatePairs() throws Exception {
        String[] inputs = {"𐀀", "𐏿", "􏰀", "􏿿", "􏰀ā", "ā􏰀"};
        String[] expected = {
            "%F0%90%80%80", "%F0%90%8F%BF", "%F4%8F%B0%80", "%F4%8F%BF%BF",
            "%F4%8F%B0%80%C4%81", "%C4%81%F4%8F%B0%80"
        };
        for (int i = 0; i < inputs.length; i++) {
            check(expected[i].equals(URLEncoder.encode(inputs[i], "UTF-8")), "surrogate pair " + i);
        }
    }

    static void testCharsetOverload() throws Exception {
        check("a+b".equals(URLEncoder.encode("a b", StandardCharsets.UTF_8)), "Charset overload space");
        check("%C3%BC".equals(URLEncoder.encode("ü", StandardCharsets.UTF_8)), "Charset overload umlaut");
        checkThrows(NullPointerException.class, () -> URLEncoder.encode("x", (Charset) null), "Charset overload null charset");
    }

    static void testDefaultOverload() throws Exception {
        check("a+b%C3%BC".equals(encodeDeprecated("a bü")), "deprecated single-arg uses UTF-8");
        check(encodeDeprecated("a bü").equals(URLEncoder.encode("a bü", "UTF-8")), "single-arg == UTF-8");
    }

    @SuppressWarnings("deprecation")
    static String encodeDeprecated(String s) {
        return URLEncoder.encode(s);
    }

    static void testValidation() {
        checkThrows(NullPointerException.class, () -> URLEncoder.encode("x", (String) null), "null charset -> NPE");
        checkThrows(UnsupportedEncodingException.class, () -> URLEncoder.encode("x", ""), "empty charset -> UEE");
        checkThrows(NullPointerException.class, () -> URLEncoder.encode((String) null, "UTF-8"), "null string -> NPE");
        checkThrows(NullPointerException.class, () -> encodeDeprecated(null), "deprecated null string -> NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBasic();
        testCharacterClass();
        testSurrogatePairs();
        testCharsetOverload();
        testDefaultOverload();
        testValidation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
