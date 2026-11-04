/*
 * @test
 * @summary java.net.URLDecoder conformance: full contract coverage. '+' and
 *          '%20' -> space, %XX hex decoding, UTF-16 round-trip, surrogate-pair
 *          round-trip, non-encoded chars left untouched, exact
 *          IllegalArgumentException on malformed/incomplete/negative hex
 *          escapes, and null/empty-charset NPE/UnsupportedEncodingException.
 * @run main URLDecoderTest
 */

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class URLDecoderTest {
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

    static void testBasic() throws Exception {
        check("a b c".equals(URLDecoder.decode("a+b%20c", "UTF-8")), "decode + and %20");
        check("a&b".equals(URLDecoder.decode("a%26b", "UTF-8")), "decode %26");
        check("a+b".equals(URLDecoder.decode("a%2Bb", "UTF-8")), "encoded plus stays plus");
        check("ü".equals(URLDecoder.decode("%C3%BC", "UTF-8")), "decode UTF-8 non-ascii");
    }

    static void testNonEncodedUntouched() throws Exception {
        String[] untouched = {"#", "X@Y", "Hello There", "fds@$"};
        for (String s : untouched) {
            check(s.equals(URLDecoder.decode(s, "UTF-8")), "non-encoded untouched: " + s);
        }
    }

    static void testRoundTrip() throws Exception {
        String[] inputs = {"a b&c", "ü ā", "fds@$", "a+b=c%d", "𐀀𐏿 ā"};
        for (String s : inputs) {
            check(s.equals(URLDecoder.decode(URLEncoder.encode(s, "UTF-8"), "UTF-8")), "UTF-8 round trip: " + s);
        }
        // UTF-16 round trip (the historical '+' bug 4407610).
        String[] utf16 = {"Ā ā", "Ā āĂ", "foo?bar"};
        for (String s : utf16) {
            check(s.equals(URLDecoder.decode(URLEncoder.encode(s, "UTF-16"), "UTF-16")), "UTF-16 round trip: " + s);
        }
    }

    static void testSurrogatePairs() throws Exception {
        String[] pairs = {"𐀀", "𐏿", "􏰀", "􏿿", "􏰀ā", "ā􏰀"};
        for (String s : pairs) {
            check(s.equals(URLDecoder.decode(URLEncoder.encode(s, "UTF-8"), "UTF-8")), "surrogate round trip: " + s);
        }
    }

    static void testValidation() {
        String[] bad = {"%", "%A", "Hello%", "%xy", "%az", "%ab%q", "%-1"};
        for (String s : bad) {
            checkThrows(IllegalArgumentException.class, () -> URLDecoder.decode(s, "UTF-8"), "malformed escape: " + s);
        }
        checkThrows(NullPointerException.class, () -> URLDecoder.decode("x", null), "null charset -> NPE");
        checkThrows(UnsupportedEncodingException.class, () -> URLDecoder.decode("x", ""), "empty charset -> UEE");
        checkThrows(NullPointerException.class, () -> URLDecoder.decode((String) null, "UTF-8"), "null string -> NPE");
        checkThrows(NullPointerException.class, () -> decodeDeprecated(null), "deprecated null string -> NPE");
    }

    @SuppressWarnings("deprecation")
    static String decodeDeprecated(String s) throws Exception {
        return URLDecoder.decode(s);
    }

    static void testDeprecatedDefault() throws Exception {
        check("a b".equals(decodeDeprecated("a+b")), "deprecated single-arg decode");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testNonEncodedUntouched();
        testRoundTrip();
        testSurrogatePairs();
        testValidation();
        testDeprecatedDefault();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
