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


    // ===================== 契约深化：round-trip 闭环/区间校验/大写流/toHexDigits 定位参数 =====================
    static void testHexRoundTripContracts() {
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < 3; i++) {
            byte[] data = new byte[32];
            rnd.nextBytes(data);
            String lower = HexFormat.of().formatHex(data);
            String upper = HexFormat.of().withUpperCase().formatHex(data);
            check(lower.equals(upper.toLowerCase()), "lower/upper agree modulo case");
            check(java.util.Arrays.equals(HexFormat.of().parseHex(lower), data),
                  "parseHex(formatHex) round-trip sample " + i);
            check(java.util.Arrays.equals(HexFormat.of().parseHex(upper), data),
                  "parseHex accepts uppercase digits");
        }
        byte[] few = { 0x12, 0x34, 0x56 };
        check("123456".equals(HexFormat.of().formatHex(few, 0, 3)), "formatHex full range");
        check("3456".equals(HexFormat.of().formatHex(few, 1, 3)), "formatHex sub-range");
        check(java.util.Arrays.equals(HexFormat.of().parseHex("123456", 2, 6), new byte[] { 0x34, 0x56 }),
              "parseHex(CharSequence, from, to) sub-range");
        try {
            HexFormat.of().formatHex(few, 2, 1);
            check(false, "formatHex fromIndex>toIndex IndexOutOfBounds");
        } catch (IndexOutOfBoundsException ioobe) { check(true, "formatHex fromIndex>toIndex IndexOutOfBounds"); }
        try {
            HexFormat.of().formatHex(few, 0, 99);
            check(false, "formatHex out-of-bounds toIndex");
        } catch (IndexOutOfBoundsException ioobe) {
            check(true, "formatHex out-of-bounds toIndex");
        }
        check("000000ff".equals(HexFormat.of().toHexDigits(255)), "toHexDigits(int) 8 digits");
        check("00000000000000ff".equals(HexFormat.of().toHexDigits(255L)), "toHexDigits(long) 16 digits");
        check("ff".equals(HexFormat.of().toHexDigits(0xffL, 2)),
              "toHexDigits(long,fromIndex) returns last fromIndex digits");
        check("0000ff".equals(HexFormat.of().toHexDigits(0xffL, 6)),
              "toHexDigits digit range keeps leading zeros in range");
        try {
            HexFormat.of().toHexDigits(255L, 17);
            check(false, "toHexDigits fromIndex>16 IAE");
        } catch (IllegalArgumentException iae) {
            check(true, "toHexDigits fromIndex>16 IAE");
        }
        check(HexFormat.of().withUpperCase().toHexDigits(255).equals("000000FF"),
              "toHexDigits honors uppercase");
        try {
            HexFormat.of().toHexDigits(255L, -1);
            check(false, "toHexDigits negative fromIndex IAE");
        } catch (IllegalArgumentException iae) { check(true, "toHexDigits negative fromIndex IAE"); }
        check(HexFormat.fromHexDigits("0123456789abcdef", 12, 16) == 0xcdef,
              "fromHexDigits(CharSequence,from,to) sub-range");
        check(HexFormat.fromHexDigits("ffffffff") == -1,
              "fromHexDigits 8 digits is unsigned (ffffffff -> -1)");
        check(HexFormat.fromHexDigits("1234567") == 0x1234567,
              "fromHexDigits accepts up to 8 digits");
        try {
            HexFormat.fromHexDigits("123456789");
            check(false, "fromHexDigits 9 digits IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            check(true, "fromHexDigits 9 digits IllegalArgumentException");
        }
        check(HexFormat.fromHexDigitsToLong("ffffffffffffffff") == -1L,
              "fromHexDigitsToLong all-ones is -1");
        try {
            HexFormat.fromHexDigitsToLong("12345678901234567");
            check(false, "fromHexDigitsToLong 17 digits IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            check(true, "fromHexDigitsToLong 17 digits IllegalArgumentException");
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder back = HexFormat.of().formatHex(sb, few);
        check("123456".equals(sb.toString()) && back == sb, "formatHex(Appendable) returns same builder");
        HexFormat custom = HexFormat.ofDelimiter("##").withPrefix("0x").withSuffix(";");
        check("0x12;##0x34;##0x56;".equals(custom.formatHex(few)),
              "prefix+delimiter+suffix composition order");
        check(custom.delimiter().equals("##") && custom.prefix().equals("0x")
              && custom.suffix().equals(";") && !custom.isUpperCase(),
              "parameter getters after composition");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHexRoundTripContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
