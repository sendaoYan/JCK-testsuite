/*
 * @test
 * @summary javax.xml.bind.annotation.adapters.HexBinaryAdapter conformance: hexBinary parse/print contract.
 * @run main HexBinaryAdapterTest
 */

import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class HexBinaryAdapterTest {
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
        javax.xml.bind.annotation.adapters.HexBinaryAdapter a = new javax.xml.bind.annotation.adapters.HexBinaryAdapter();
        byte[] bts = a.unmarshal("CAFE");
        check(bts.length == 2 && (bts[0] & 0xff) == 0xca && (bts[1] & 0xff) == 0xfe, "unmarshal");
        check("CAFE".equals(a.marshal(new byte[]{(byte) 0xca, (byte) 0xfe})), "marshal");
        testClassContract();
        testUnmarshal();
        testMarshal();
        testNullAndEmpty();
        testIllegalInput();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassContract() {
        Class<HexBinaryAdapter> c = HexBinaryAdapter.class;
        check(Modifier.isFinal(c.getModifiers()), "class is final");
        check(c.getSuperclass() == XmlAdapter.class, "extends XmlAdapter<String, byte[]>");
        check(c.getConstructors().length == 1, "single public no-arg constructor");
        // usable through the generic supertype (bridge methods)
        XmlAdapter<String, byte[]> gen = new HexBinaryAdapter();
        check(gen != null, "assignable to XmlAdapter<String, byte[]> reference");
        try {
            check(gen.unmarshal("00ff").length == 2, "generic-typed unmarshal bridge works");
            check("414243".equals(gen.marshal(new byte[]{65, 66, 67})), "generic-typed marshal bridge works");
        } catch (Exception e) {
            check(false, "bridge invocation failed: " + e);
        }
    }

    static void testUnmarshal() {
        HexBinaryAdapter a = new HexBinaryAdapter();
        byte[] z = a.unmarshal("00ff");
        check(z.length == 2 && (z[0] & 0xff) == 0x00 && (z[1] & 0xff) == 0xff, "unmarshal(\"00ff\") == {0x00, 0xff}");
        byte[] lo = a.unmarshal("0f");
        check(lo.length == 1 && (lo[0] & 0xff) == 15, "lowercase hex digit accepted");
        byte[] mixed = a.unmarshal("AbCd");
        check(mixed.length == 2 && (mixed[0] & 0xff) == 0xab && (mixed[1] & 0xff) == 0xcd, "mixed-case hex accepted");
        byte[] empty = a.unmarshal("");
        check(empty != null && empty.length == 0, "unmarshal(\"\") == empty array");
        byte[] ff = a.unmarshal("FF");
        check(ff.length == 1 && (ff[0] & 0xff) == 255, "uppercase FF");
        byte[] wide = a.unmarshal("000102030405060708090a0B0c0D0e0F");
        check(wide.length == 16 && (wide[0] & 0xff) == 0 && (wide[15] & 0xff) == 0x0f, "16-byte vector");
        byte[] alphabet = a.unmarshal("0123456789ABCDEF");
        check(alphabet.length == 8, "16 hex digits decode to 8 bytes");
        check((alphabet[0] & 0xff) == 0x01, "full hex alphabet: 01");
        check((alphabet[5] & 0xff) == 0xab, "full hex alphabet: AB");
        check((alphabet[7] & 0xff) == 0xef, "full hex alphabet: EF");
    }

    static void testMarshal() {
        HexBinaryAdapter a = new HexBinaryAdapter();
        check("".equals(a.marshal(new byte[0])), "marshal(empty) == \"\"");
        check("01".equals(a.marshal(new byte[]{1})), "single byte zero-padded");
        check("0F".equals(a.marshal(new byte[]{0x0f})), "output is uppercase");
        check("FFFE".equals(a.marshal(new byte[]{(byte) -1, (byte) -2})), "negative bytes marshal as unsigned");
        check("414243".equals(a.marshal("ABC".getBytes(java.nio.charset.StandardCharsets.US_ASCII))), "ASCII bytes");
        check(a.marshal(new byte[]{(byte) 0x80}).equals("80"), "high-bit byte");
        // round-trips
        check("DEADBEEF".equals(a.marshal(a.unmarshal("DEADBEEF"))), "round-trip DEADBEEF");
        byte[] rt = a.unmarshal(a.marshal(new byte[]{0, 1, (byte) 0xfe, (byte) 0xff}));
        check(rt.length == 4 && rt[0] == 0 && rt[1] == 1 && (rt[2] & 0xff) == 0xfe && (rt[3] & 0xff) == 0xff,
                "round-trip byte vector");
    }

    static void testNullAndEmpty() {
        HexBinaryAdapter a = new HexBinaryAdapter();
        check(a.unmarshal(null) == null, "unmarshal(null) == null");
        check(a.marshal(null) == null, "marshal(null) == null");
    }

    static void testIllegalInput() {
        HexBinaryAdapter a = new HexBinaryAdapter();
        try {
            a.unmarshal("zz");
            check(false, "illegal hex characters must throw");
        } catch (IllegalArgumentException e) {
            check(true, "non-hex input -> IllegalArgumentException");
            check(e.getMessage() != null && e.getMessage().contains("contains illegal character for hexBinary: zz"),
                    "error message exact: contains illegal character for hexBinary: zz");
        }
        try {
            a.unmarshal("abc");
            check(false, "odd-length hex must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("hexBinary needs to be even-length: abc"),
                    "error message exact: hexBinary needs to be even-length: abc");
        }
        try {
            a.unmarshal("0");
            check(false, "single hex digit must throw");
        } catch (IllegalArgumentException e) {
            check(true, "1-char input -> IllegalArgumentException");
        }
    }
}
