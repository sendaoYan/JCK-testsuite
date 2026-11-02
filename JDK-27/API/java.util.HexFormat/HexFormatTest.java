/*
 * @test
 * @summary java.util.HexFormat conformance: of/formatHex/parseHex/fromHexDigit.
 * @run main HexFormatTest
 */
import java.util.Arrays;
import java.util.HexFormat;

public class HexFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        HexFormat hf = HexFormat.of();
        check("01020aff".equals(hf.formatHex(new byte[]{1, 2, 10, (byte) 255})), "formatHex");
        check(Arrays.equals(hf.parseHex("0102ff"), new byte[]{1, 2, (byte) 255}), "parseHex");
        check(HexFormat.fromHexDigit('A') == 10, "fromHexDigit");
        check(HexFormat.isHexDigit('F'), "isHexDigit");
        check(HexFormat.fromHexDigits("0a") == 10, "fromHexDigits");
        check(HexFormat.fromHexDigitsToLong("0a") == 10, "fromHexDigitsToLong");
        check("ff".equals(hf.toHexDigits((byte) 255)), "toHexDigits byte");
        check("000000ff".equals(hf.toHexDigits(255)), "toHexDigits int");
        check("AB".equals(hf.withUpperCase().formatHex(new byte[]{(byte) 0xab})), "withUpperCase");
        check(!hf.isUpperCase(), "isUpperCase false");
        check("[01]:[02]".equals(HexFormat.ofDelimiter(":").withPrefix("[").withSuffix("]").formatHex(new byte[]{1, 2})), "delimiter/prefix/suffix");
        check(":".equals(HexFormat.ofDelimiter(":").delimiter()), "delimiter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
