/*
 * @test
 * @summary java.net.URLEncoder conformance: white-box structure + black-box behavior.
 * @run main URLEncoderTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class URLEncoderTest {
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
        Class<?> c = Class.forName("java.net.URLEncoder");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getMethod("encode", String.class) != null, "encode(String)");
        check(c.getMethod("encode", String.class, String.class) != null, "encode(String,String)");
        check(c.getMethod("encode", String.class, java.nio.charset.Charset.class) != null, "encode(String,Charset)");
        check(Modifier.isPrivate(c.getDeclaredConstructor().getModifiers()), "private ctor");
    }

    static void testBlackBox() throws Exception {
        // application/x-www-form-urlencoded rules: space -> '+', '*' preserved, '~' preserved
        check(URLEncoder.encode("a b", StandardCharsets.UTF_8).equals("a+b"), "space to plus");
        check(URLEncoder.encode("a*b~c", StandardCharsets.UTF_8).equals("a*b%7Ec"), "star kept, tilde encoded");
        check(URLEncoder.encode("a+b", StandardCharsets.UTF_8).equals("a%2Bb"), "plus encoded");
        check(URLEncoder.encode("a/b?c=d&e", StandardCharsets.UTF_8).equals("a%2Fb%3Fc%3Dd%26e"), "reserved encoded");
        // UTF-8 multi-byte
        check(URLEncoder.encode("ü", StandardCharsets.UTF_8).equals("%C3%BC"), "utf-8 umlaut");
        check(URLEncoder.encode("中文", StandardCharsets.UTF_8).equals("%E4%B8%AD%E6%96%87"), "utf-8 cjk");
        // default charset overload matches UTF-8 for ASCII input
        check(URLEncoder.encode("a b").equals("a+b"), "default charset ASCII");
        // roundtrip with URLDecoder
        String s = "hello world & friends!";
        check(URLDecoder.decode(URLEncoder.encode(s, StandardCharsets.UTF_8), StandardCharsets.UTF_8).equals(s), "roundtrip");
        // unknown charset name
        fails(java.io.UnsupportedEncodingException.class, "bad charset", () -> URLEncoder.encode("a", "no-such-charset"));
        fails(NullPointerException.class, "null string", () -> URLEncoder.encode(null, StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
