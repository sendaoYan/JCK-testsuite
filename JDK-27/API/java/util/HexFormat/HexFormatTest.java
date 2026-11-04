/*
 * @test
 * @summary java.util.HexFormat conformance: white-box structure + black-box behavior.
 * @run main HexFormatTest
 */

import java.util.HexFormat;

public class HexFormatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.HexFormat");
        check(c.isInterface() == false && java.lang.reflect.Modifier.isFinal(c.getModifiers()),
                "final class");
        c.getDeclaredMethod("of");
        c.getDeclaredMethod("ofDelimiter", String.class);
        c.getDeclaredMethod("formatHex", byte[].class);
        c.getDeclaredMethod("parseHex", CharSequence.class);
    }

    static void testBlackBox() {
        HexFormat h = HexFormat.of();
        byte[] bytes = {0, 1, 10, (byte) 0xff};
        check("00010aff".equals(h.formatHex(bytes)), "formatHex lowercase");
        check("00010AFF".equals(h.withUpperCase().formatHex(bytes)), "withUpperCase");
        check("00:01:0a:ff".equals(h.withDelimiter(":").formatHex(bytes)), "withDelimiter");
        check("#00#01#0a#ff".equals(h.withPrefix("#").formatHex(bytes)), "withPrefix");
        check("00;01;0a;ff;".equals(h.withSuffix(";").formatHex(bytes)), "withSuffix");
        check(h.delimiter().equals("") && h.prefix().equals("") && h.suffix().equals(""),
                "default delimiter/prefix/suffix empty");
        check(!h.isUpperCase(), "default lowercase");

        check(java.util.Arrays.equals(h.parseHex("00010aff"), bytes), "parseHex round-trip");
        expectThrows(() -> h.parseHex("abc"), IllegalArgumentException.class, "odd length");
        expectThrows(() -> h.parseHex("zz"), NumberFormatException.class, "non-hex char");

        check(HexFormat.fromHexDigit('a') == 10, "fromHexDigit");
        expectThrows(() -> HexFormat.fromHexDigit('z'), NumberFormatException.class,
                "fromHexDigit invalid");
        check(HexFormat.isHexDigit('f') && !HexFormat.isHexDigit('g'), "isHexDigit");
        check(HexFormat.fromHexDigits("ff") == 255, "fromHexDigits int");
        check(HexFormat.fromHexDigitsToLong("ffffffffffffffff") == -1L, "fromHexDigitsToLong");
        check(h.toHexDigits(255).length() == 8 && h.toHexDigits(255).equals("000000ff"), "toHexDigits(int)");
        check(h.toHexDigits((byte) 0xff).equals("ff"), "toHexDigits(byte)");
        check(h.toHighHexDigit(0xAB) == 'a' && h.toLowHexDigit(0xAB) == 'b', "toHigh/toLowHexDigit");
        check("00:01".equals(HexFormat.ofDelimiter(":").formatHex(new byte[] {0, 1})),
                "ofDelimiter factory");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
