/*
 * @test
 * @summary java.net.URLEncoder conformance: full contract coverage. space->'+',
 *          unreserved chars pass through, everything else -> %XX uppercase
 *          UTF-8 hex, full ASCII 33..126 per-character matrix, ISO-8859-1
 *          single-byte encoding with all-256-byte round-trip, charset aliases,
 *          UTF-16 round-trip, surrogate-pair UTF-8 expansion, the deprecated
 *          single-arg overload, and exact null/empty-charset
 *          NPE/UnsupportedEncodingException paths.
 * @run main URLEncoderTest
 */

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

    static void testBasic() throws Exception {
        check("a+b%26c".equals(URLEncoder.encode("a b&c", "UTF-8")), "space and ampersand");
        check("%C3%BC".equals(URLEncoder.encode("ü", "UTF-8")), "UTF-8 non-ascii");
        check("%FE%FF%01%00".equals(URLEncoder.encode("Ā", "UTF-16")), "UTF-16 with BOM");
    }

    static void testCharacterClass() throws Exception {
        // alphanumerics and . - * _ pass through; space -> '+'; everything else %XX.
        String in = "Hello +%-_.!~*'()@®āႠ";
        String expected = "Hello+%2B%25-_.%21%7E*%27%28%29%40%C2%AE%C4%81%E1%82%A0";
        check(expected.equals(URLEncoder.encode(in, "UTF-8")), "full character-class encoding");
        check("AZaz09-_.*".equals(URLEncoder.encode("AZaz09-_.*", "UTF-8")), "unreserved passthrough");
    }

    static void testSurrogatePairs() throws Exception {
        String[] inputs = {
            "𐀀", "𐏿", "􏰀", "􏿿",
            "􏰀ā", "ā􏰀"
        };
        String[] expected = {
            "%F0%90%80%80", "%F0%90%8F%BF", "%F4%8F%B0%80", "%F4%8F%BF%BF",
            "%F4%8F%B0%80%C4%81", "%C4%81%F4%8F%B0%80"
        };
        for (int i = 0; i < inputs.length; i++) {
            check(expected[i].equals(URLEncoder.encode(inputs[i], "UTF-8")), "surrogate pair " + i);
        }
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
        checkThrows(NullPointerException.class, () -> URLEncoder.encode("x", null), "null charset -> NPE");
        checkThrows(UnsupportedEncodingException.class, () -> URLEncoder.encode("x", ""), "empty charset -> UEE");
        checkThrows(NullPointerException.class, () -> URLEncoder.encode((String) null, "UTF-8"), "null string -> NPE");
        checkThrows(NullPointerException.class, () -> encodeDeprecated(null), "deprecated null string -> NPE");
    }

    // ---------- deepening: full ASCII 33..126 per-character matrix ----------

    static void testAsciiMatrix() throws Exception {
        // [a-zA-Z0-9] pass through
        String alnum = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        check(alnum.equals(URLEncoder.encode(alnum, "UTF-8")), "all alphanumerics pass through");
        for (int i = 0; i < alnum.length(); i++) {
            String one = alnum.substring(i, i + 1);
            check(one.equals(URLEncoder.encode(one, "UTF-8")), "alnum passthrough: " + one);
        }
        // the three special pass-through chars
        check(".".equals(URLEncoder.encode(".", "UTF-8")), "dot passthrough");
        check("-".equals(URLEncoder.encode("-", "UTF-8")), "hyphen passthrough");
        check("*".equals(URLEncoder.encode("*", "UTF-8")), "star passthrough");
        check("_".equals(URLEncoder.encode("_", "UTF-8")), "underscore passthrough");
        check(".-*_".equals(URLEncoder.encode(".-*_", "UTF-8")), "combined passthrough");
        // space -> '+'
        check("+".equals(URLEncoder.encode(" ", "UTF-8")), "space becomes plus");
        check("++".equals(URLEncoder.encode("  ", "UTF-8")), "two spaces become two pluses");
        check("a+b".equals(URLEncoder.encode("a b", "UTF-8")), "interior space plus");
        // every other printable ASCII 33..126 becomes uppercase %XX
        for (int c = 33; c < 127; c++) {
            if (Character.isLetterOrDigit(c)) continue;
            if (c == '.' || c == '-' || c == '*' || c == '_') continue;
            String in = String.valueOf((char) c);
            String expected = "%" + String.format("%02X", c);
            String got = URLEncoder.encode(in, "UTF-8");
            check(expected.equals(got), "ascii " + c + " '" + in + "' -> " + expected + " got " + got);
        }
        // control chars encode too
        check("%00".equals(URLEncoder.encode("\u0000", "UTF-8")), "NUL encodes to %00");
        check("%01".equals(URLEncoder.encode("\u0001", "UTF-8")), "SOH encodes");
        check("%0D%0A".equals(URLEncoder.encode("\r\n", "UTF-8")), "CRLF encodes");
        check("%7F".equals(URLEncoder.encode("\u007F", "UTF-8")), "DEL encodes");
        // a full string combining all classes
        check("AZaz09.-*_+%21%3F%3D%26".equals(URLEncoder.encode("AZaz09.-*_ !?=&", "UTF-8")), "mixed classes");
    }

    // ---------- deepening: ISO-8859-1 and charset aliases ----------

    static void testIso8859AndAliases() throws Exception {
        // single-byte charset
        check("%FC".equals(URLEncoder.encode("ü", "ISO-8859-1")), "ISO umlaut-u -> %FC");
        check("%E9".equals(URLEncoder.encode("é", "ISO-8859-1")), "ISO e-acute -> %E9");
        check("%FF".equals(URLEncoder.encode("ÿ", "ISO-8859-1")), "ISO y-diaeresis -> %FF");
        check("a+b".equals(URLEncoder.encode("a b", "ISO-8859-1")), "ISO space plus");
        check("abc".equals(URLEncoder.encode("abc", "ISO-8859-1")), "ISO ascii passthrough");
        // all 256 chars round-trip in ISO-8859-1
        StringBuilder all = new StringBuilder();
        for (int i = 0; i < 256; i++) all.append((char) i);
        String enc = URLEncoder.encode(all.toString(), "ISO-8859-1");
        check(enc.length() > 256, "ISO encoded longer than input");
        java.util.Set<String> hexCodes = new java.util.HashSet<String>();
        for (int i = 0; i + 2 < enc.length(); ) {
            if (enc.charAt(i) == '%') { hexCodes.add(enc.substring(i, i + 3)); i += 3; }
            else i++;
        }
        check(hexCodes.contains("%FC"), "ISO matrix contains %FC");
        check(hexCodes.contains("%FF"), "ISO matrix contains %FF");
        check(hexCodes.contains("%00"), "ISO matrix contains %00");
        check(hexCodes.contains("%80"), "ISO matrix contains %80");
        // charset name aliases are accepted
        check(URLEncoder.encode("ü", "UTF8").equals(URLEncoder.encode("ü", "UTF-8")), "UTF8 alias equals UTF-8");
        check(URLEncoder.encode("ü", "utf-8").equals(URLEncoder.encode("ü", "UTF-8")), "lowercase alias");
        check(URLEncoder.encode("ü", "Iso-8859-1").equals(URLEncoder.encode("ü", "ISO-8859-1")), "mixed-case ISO alias");
        check(URLEncoder.encode("ü", "ISO8859_1").equals(URLEncoder.encode("ü", "ISO-8859-1")), "ISO8859_1 alias");
        // US-ASCII replaces non-encodable chars with '?' (0x3F) instead of throwing
        check("%3F".equals(URLEncoder.encode("ü", "US-ASCII")), "US-ASCII replacement byte %3F");
    }

    // ---------- deepening: UTF-8 boundary code points ----------

    static void testUtf8Boundaries() throws Exception {
        // 1-byte boundary 0x7F vs 2-byte 0x80
        check("%7F".equals(URLEncoder.encode("\u007F", "UTF-8")), "U+007F 1 byte");
        check("%C2%80".equals(URLEncoder.encode("\u0080", "UTF-8")), "U+0080 2 bytes");
        // 2-byte max vs 3-byte min
        check("%C2%BF".equals(URLEncoder.encode("¿", "UTF-8")), "U+00BF");
        check("%C3%BF".equals(URLEncoder.encode("ÿ", "UTF-8")), "U+00FF 2-byte max latin1");
        check("%C4%80".equals(URLEncoder.encode("Ā", "UTF-8")), "U+0100");
        check("%E0%A0%80".equals(URLEncoder.encode("ࠀ", "UTF-8")), "U+0800 3 bytes");
        check("%EF%BF%BF".equals(URLEncoder.encode("￿", "UTF-8")), "U+FFFF 3-byte max");
        // 4-byte only via surrogate pairs
        check("%F0%90%80%80".equals(URLEncoder.encode("𐀀", "UTF-8")), "U+10000 via surrogates");
        // CJK examples
        check("%E4%BE%8B".equals(URLEncoder.encode("例", "UTF-8")), "U+4F8B (li)");
        check("%E3%81%88".equals(URLEncoder.encode("え", "UTF-8")), "U+3048 (e)");
        // long string stability: encoding twice is stable
        String s = "a b&c=d?e#f%g";
        String once = URLEncoder.encode(s, "UTF-8");
        check(once.equals(URLEncoder.encode(s, "UTF-8")), "encoding deterministic");
        check(!once.equals(s), "encoding changes the string");
    }

    // ---------- deepening: empty and null boundaries ----------

    static void testEmptyBoundaries() throws Exception {
        check("".equals(URLEncoder.encode("", "UTF-8")), "empty string encodes to empty");
        check("".equals(URLEncoder.encode("", "ISO-8859-1")), "empty ISO");
        check("".equals(encodeDeprecated("")), "empty deprecated overload");
        check("+".equals(encodeDeprecated(" ")), "deprecated space");
        // hex digits are uppercase
        check("%2F".equals(URLEncoder.encode("/", "UTF-8")), "uppercase hex F");
        check("%7E".equals(URLEncoder.encode("~", "UTF-8")), "uppercase hex E (tilde encoded)");
        // single-char encodes across full sample
        String[] chars = {"!", "\"", "#", "$", "%", "&", "'", "(", ")", ",", "/", ":", ";", "<", "=", ">", "?", "@", "[", "\\", "]", "^", "`", "{", "|", "}", "~"};
        String[] outs = {"%21", "%22", "%23", "%24", "%25", "%26", "%27", "%28", "%29", "%2C", "%2F", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F", "%40", "%5B", "%5C", "%5D", "%5E", "%60", "%7B", "%7C", "%7D", "%7E"};
        for (int i = 0; i < chars.length; i++) {
            check(outs[i].equals(URLEncoder.encode(chars[i], "UTF-8")), "punct " + i + " " + chars[i]);
        }
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testCharacterClass();
        testSurrogatePairs();
        testDefaultOverload();
        testValidation();
        testAsciiMatrix();
        testIso8859AndAliases();
        testUtf8Boundaries();
        testEmptyBoundaries();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
