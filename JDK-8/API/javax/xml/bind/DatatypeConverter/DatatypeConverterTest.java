/*
 * @test
 * @summary javax.xml.bind.DatatypeConverter conformance: datatype converter round trips.
 * @run main DatatypeConverterTest
 */


public class DatatypeConverterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        check(javax.xml.bind.DatatypeConverter.parseInt("42") == 42, "parseInt");
        check("42".equals(javax.xml.bind.DatatypeConverter.printInt(42)), "printInt");
        check(javax.xml.bind.DatatypeConverter.parseBoolean("true"), "parseBoolean");
        check("true".equals(javax.xml.bind.DatatypeConverter.printBoolean(true)), "printBoolean");
        check(javax.xml.bind.DatatypeConverter.parseLong("123") == 123L, "parseLong");
        check(javax.xml.bind.DatatypeConverter.parseInteger("123").equals(new java.math.BigInteger("123")), "parseInteger");
        check(javax.xml.bind.DatatypeConverter.parseShort("7") == (short) 7, "parseShort");
        check(javax.xml.bind.DatatypeConverter.parseFloat("1.5") == 1.5f, "parseFloat");
        check(javax.xml.bind.DatatypeConverter.parseDouble("1.5") == 1.5, "parseDouble");
        check(javax.xml.bind.DatatypeConverter.parseDecimal("1.5").equals(new java.math.BigDecimal("1.5")), "parseDecimal");
        byte[] hex = javax.xml.bind.DatatypeConverter.parseHexBinary("CAFE");
        check(hex.length == 2 && (hex[0] & 0xff) == 0xca && (hex[1] & 0xff) == 0xfe, "parseHexBinary");
        check("CAFE".equals(javax.xml.bind.DatatypeConverter.printHexBinary(hex)), "printHexBinary");
        byte[] b64 = javax.xml.bind.DatatypeConverter.parseBase64Binary("TWFu");
        check("TWFu".equals(javax.xml.bind.DatatypeConverter.printBase64Binary(b64)), "printBase64Binary");
        check(javax.xml.bind.DatatypeConverter.parseUnsignedInt("4294967295") == 4294967295L, "parseUnsignedInt");
        check(javax.xml.bind.DatatypeConverter.parseUnsignedShort("65535") == 65535, "parseUnsignedShort");
        check("abc".equals(javax.xml.bind.DatatypeConverter.parseString("abc")), "parseString");
        check("x".equals(javax.xml.bind.DatatypeConverter.parseAnySimpleType("x")), "parseAnySimpleType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
