/*
 * @test
 * @summary java.net.URLDecoder conformance: white-box structure + black-box behavior.
 * @run main URLDecoderTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class URLDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLDecoder");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getMethod("decode", String.class) != null, "decode(String)");
        check(c.getMethod("decode", String.class, String.class) != null, "decode(String,String)");
        check(c.getMethod("decode", String.class, java.nio.charset.Charset.class) != null, "decode(String,Charset)");
    }

    static void testBlackBox() throws Exception {
        // '+' decoded to space, '%xx' decoded
        check(URLDecoder.decode("a+b", StandardCharsets.UTF_8).equals("a b"), "plus to space");
        check(URLDecoder.decode("a%2Bb", StandardCharsets.UTF_8).equals("a+b"), "encoded plus");
        check(URLDecoder.decode("a%2Fb", StandardCharsets.UTF_8).equals("a/b"), "encoded slash");
        check(URLDecoder.decode("%C3%BC", StandardCharsets.UTF_8).equals("ü"), "utf-8 decode");
        check(URLDecoder.decode("a%20b", StandardCharsets.UTF_8).equals("a b"), "encoded space");
        // legacy one-arg decode uses platform default; ASCII subset is stable
        check(URLDecoder.decode("a+b").equals("a b"), "one-arg decode ASCII");
        // roundtrip
        String s = "hello world & friends!";
        check(URLDecoder.decode(URLEncoder.encode(s, StandardCharsets.UTF_8), StandardCharsets.UTF_8).equals(s), "roundtrip");
        fails(java.io.UnsupportedEncodingException.class, "bad charset", () -> URLDecoder.decode("a", "no-such-charset"));
        fails(IllegalArgumentException.class, "bad percent", () -> URLDecoder.decode("%zz", StandardCharsets.UTF_8));
        fails(NullPointerException.class, "null string", () -> URLDecoder.decode(null, StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
