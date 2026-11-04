/*
 * @test
 * @summary java.net.URLDecoder conformance: white-box structure + full contract
 *          coverage. '+' and '%20' -> space, %XX hex decoding, UTF-16 round-trip,
 *          surrogate-pair round-trip, non-encoded chars left untouched, exact
 *          IllegalArgumentException on malformed/incomplete/negative hex escapes,
 *          JDK 10+ Charset overload, and null/empty-charset NPE/UEE paths.
 * @run main URLDecoderTest
 */

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLDecoder");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getMethod("decode", String.class) != null, "decode(String)");
        check(c.getMethod("decode", String.class, String.class) != null, "decode(String,String)");
        check(c.getMethod("decode", String.class, Charset.class) != null, "decode(String,Charset)");
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

    static void testCharsetOverload() throws Exception {
        check("a b".equals(URLDecoder.decode("a+b", StandardCharsets.UTF_8)), "Charset overload plus");
        check("ü".equals(URLDecoder.decode("%C3%BC", StandardCharsets.UTF_8)), "Charset overload umlaut");
        checkThrows(NullPointerException.class, () -> URLDecoder.decode("x", (Charset) null), "Charset overload null charset");
    }

    static void testValidation() {
        String[] bad = {"%", "%A", "Hello%", "%xy", "%az", "%ab%q", "%-1"};
        for (String s : bad) {
            checkThrows(IllegalArgumentException.class, () -> URLDecoder.decode(s, "UTF-8"), "malformed escape: " + s);
        }
        checkThrows(NullPointerException.class, () -> URLDecoder.decode("x", (String) null), "null charset -> NPE");
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
        check("a b".equals(decodeDeprecated("a%20b")), "deprecated single-arg percent");
        check("a b".equals(URLDecoder.decode("a+b", "UTF-8")), "deprecated equals UTF-8 overload");
    }

    // ---------- deepening: full %XX matrix over ISO-8859-1 ----------

    static void testHexMatrix256() throws Exception {
        // every byte value 0x00..0xFF decodes exactly in ISO-8859-1
        for (int b = 0; b < 256; b++) {
            String in = "%" + String.format("%02X", b);
            String out = URLDecoder.decode(in, "ISO-8859-1");
            check(out.length() == 1 && out.charAt(0) == (char) b, "hex " + in + " -> char " + b);
        }
        // lowercase hex digits are accepted
        check("!".equals(URLDecoder.decode("%21", "UTF-8")), "lowercase hex a");
        check("!".equals(URLDecoder.decode("%21", "UTF-8")), "lowercase hex b");
        check("~".equals(URLDecoder.decode("%7e", "UTF-8")), "lowercase %7e");
        check("~".equals(URLDecoder.decode("%7E", "UTF-8")), "uppercase %7E");
        check("*".equals(URLDecoder.decode("%2a", "UTF-8")), "lowercase %2a");
        // mixed-case hex per escape
        check("+".equals(URLDecoder.decode("%2B", "UTF-8")), "plus from %2B");
        check("+".equals(URLDecoder.decode("%2b", "UTF-8")), "plus from %2b");
        // UTF-8 multi-byte decode
        check("ü".equals(URLDecoder.decode("%C3%BC", "UTF-8")), "UTF-8 2-byte");
        check("例え".equals(URLDecoder.decode("%E4%BE%8B%E3%81%88", "UTF-8")), "UTF-8 CJK");
        check("𐀀".equals(URLDecoder.decode("%F0%90%80%80", "UTF-8")), "UTF-8 4-byte supplementary");
        // UTF-16 BOM form decodes back
        check("Ā".equals(URLDecoder.decode("%FE%FF%01%00", "UTF-16")), "UTF-16 decode");
        // Charset-object overload agrees on a sample
        check("ü".equals(URLDecoder.decode("%C3%BC", StandardCharsets.UTF_8)), "Charset overload UTF-8");
        check("ü".equals(URLDecoder.decode("%FC", StandardCharsets.ISO_8859_1)), "Charset overload ISO");
    }

    // ---------- deepening: IAE message classification ----------

    static void testIAEMessages() throws Exception {
        // incomplete trailing escapes: '%' with fewer than 2 following chars
        String[] incomplete = {"%", "%A", "%4", "abc%", "a%", "a%2", "% ", "%1"};
        for (String s : incomplete) {
            try {
                URLDecoder.decode(s, "UTF-8");
                check(false, "incomplete escape must throw: [" + s + "]");
            } catch (IllegalArgumentException e) {
                check(e.getMessage() != null
                        && e.getMessage().contains("URLDecoder: Incomplete trailing escape (%) pattern"),
                        "incomplete message: [" + s + "] -> " + e.getMessage());
            }
        }
        // illegal hex digits: '%' followed by non-hex chars
        String[] illegal = {"%zz", "%1z", "%z1", "%G1", "%1G", "%%%"};
        for (String s : illegal) {
            try {
                URLDecoder.decode(s, "UTF-8");
                check(false, "illegal hex must throw: [" + s + "]");
            } catch (IllegalArgumentException e) {
                check(e.getMessage() != null
                        && e.getMessage().contains("URLDecoder: Illegal hex characters in escape (%) pattern"),
                        "illegal-hex message: [" + s + "] -> " + e.getMessage());
            }
        }
        // the exception is precisely IllegalArgumentException (not a subclass)
        try {
            URLDecoder.decode("%zz", "UTF-8");
        } catch (IllegalArgumentException e) {
            check(IllegalArgumentException.class == e.getClass(), "IAE exact class");
        }
    }

    // ---------- deepening: plus/space and mixed decoding ----------

    static void testPlusMixed() throws Exception {
        check(" ".equals(URLDecoder.decode("+", "UTF-8")), "lone plus -> space");
        check("  ".equals(URLDecoder.decode("++", "UTF-8")), "double plus -> two spaces");
        check("a b".equals(URLDecoder.decode("a+b", "UTF-8")), "interior plus");
        check("a+b".equals(URLDecoder.decode("a%2Bb", "UTF-8")), "encoded plus literal");
        check("a b".equals(URLDecoder.decode("a%20b", "UTF-8")), "encoded space");
        check("a  b".equals(URLDecoder.decode("a+%20b", "UTF-8")), "mixed plus and %20");
        check("A B".equals(URLDecoder.decode("%41+%42", "UTF-8")), "two escapes and plus");
        check("a+b c".equals(URLDecoder.decode("a%2Bb+c", "UTF-8")), "plus literal and space");
        check("".equals(URLDecoder.decode("", "UTF-8")), "empty decodes empty");
        check("plain".equals(URLDecoder.decode("plain", "UTF-8")), "no-op plain");
        check("a=b&c=d".equals(URLDecoder.decode("a%3Db%26c%3Dd", "UTF-8")), "form-style");
        check("= ".equals(URLDecoder.decode("%3D+", "UTF-8")), "leading escape trailing plus");
        // decode does not re-encode plain specials
        check("#".equals(URLDecoder.decode("#", "UTF-8")), "raw # untouched");
        check("@".equals(URLDecoder.decode("@", "UTF-8")), "raw @ untouched");
        check("$".equals(URLDecoder.decode("$", "UTF-8")), "raw $ untouched");
        // NUL decodes
        check("\u0000".equals(URLDecoder.decode("%00", "UTF-8")), "NUL from %00");
    }

    // ---------- deepening: charset handling ----------

    static void testCharsetHandling() throws Exception {
        // malformed UTF-8 bytes decode to the replacement character, not an error
        String r = URLDecoder.decode("%FF", "UTF-8");
        check(r.length() == 1, "lone %FF decodes to single char");
        check(r.charAt(0) == '�', "lone %FF -> U+FFFD replacement");
        String r2 = URLDecoder.decode("%C3", "UTF-8");
        check(r2.charAt(0) == '�', "truncated %C3 -> U+FFFD replacement");
        // unknown charset is rejected eagerly on JDK 10+ (unlike JDK 8's lazy check)
        try {
            URLDecoder.decode("a%41", "no-such-charset");
            check(false, "unknown charset with escape must throw UEE");
        } catch (UnsupportedEncodingException e) {
            check("no-such-charset".equals(e.getMessage()), "UEE message is the charset name");
        }
        try {
            URLDecoder.decode("plain", "no-such-charset");
            check(false, "JDK 10+ eager charset validation");
        } catch (UnsupportedEncodingException e) {
            passed++;
        }
        try {
            URLDecoder.decode("+", "no-such-charset");
            check(false, "JDK 10+ eager charset validation for plus");
        } catch (UnsupportedEncodingException e) {
            passed++;
        }
        // empty charset is always rejected
        try {
            URLDecoder.decode("x", "");
            check(false, "empty charset must throw");
        } catch (UnsupportedEncodingException e) {
            check("URLDecoder: empty string enc parameter".equals(e.getMessage()), "empty enc message");
        }
        try {
            URLDecoder.decode("a%41", "");
            check(false, "empty charset with escape must throw");
        } catch (UnsupportedEncodingException e) {
            passed++;
        }
        // charset aliases
        check("ü".equals(URLDecoder.decode("%C3%BC", "UTF8")), "UTF8 alias decode");
        check("ü".equals(URLDecoder.decode("%C3%BC", "utf-8")), "lowercase alias decode");
        check("ü".equals(URLDecoder.decode("%FC", "ISO8859_1")), "ISO8859_1 alias decode");
        check("ü".equals(URLDecoder.decode("%FC", "ISO-8859-1")), "ISO-8859-1 decode");
        // round-trip with ISO-8859-1 across all bytes
        StringBuilder all = new StringBuilder();
        for (int i = 0; i < 256; i++) all.append((char) i);
        check(URLDecoder.decode(URLEncoder.encode(all.toString(), "ISO-8859-1"), "ISO-8859-1").equals(all.toString()),
                "ISO all-256 round-trip");
        // round-trip with UTF-8 across all BMP samples
        String[] samples = {"", "a", " ", "  ", "hello world", "100%", "a+b", "~", "*", ".", "-", "_",
                "例え ü", "ÿÿ", " x", "x ", "a%b+c d"};
        for (String s : samples) {
            check(s.equals(URLDecoder.decode(URLEncoder.encode(s, "UTF-8"), "UTF-8")), "utf8 rt [" + s + "]");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBasic();
        testNonEncodedUntouched();
        testRoundTrip();
        testSurrogatePairs();
        testCharsetOverload();
        testValidation();
        testDeprecatedDefault();
        testHexMatrix256();
        testIAEMessages();
        testPlusMixed();
        testCharsetHandling();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
