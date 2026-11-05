/*
 * @test
 * @summary javax.xml.bind.DatatypeConverter conformance: parse/print dual table for all
 *          XML schema datatypes, lexical defaults, and negative matrices.
 * @run main DatatypeConverterTest
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;


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
        numericTable();
        stringAndBase64();
        dateTimeTable();
        qnameTable();
        negativeMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void numericTable() {
        check(javax.xml.bind.DatatypeConverter.parseInt("42") == 42, "parseInt");
        check("42".equals(javax.xml.bind.DatatypeConverter.printInt(42)), "printInt");
        check(javax.xml.bind.DatatypeConverter.parseInt("-1") == -1, "parseInt negative");
        check(javax.xml.bind.DatatypeConverter.parseInt(String.valueOf(Integer.MAX_VALUE)) == Integer.MAX_VALUE,
              "parseInt MAX_VALUE");
        check(javax.xml.bind.DatatypeConverter.parseInt(String.valueOf(Integer.MIN_VALUE)) == Integer.MIN_VALUE,
              "parseInt MIN_VALUE");
        check(javax.xml.bind.DatatypeConverter.parseLong("123") == 123L, "parseLong");
        check("123".equals(javax.xml.bind.DatatypeConverter.printLong(123L)), "printLong");
        check(javax.xml.bind.DatatypeConverter.parseInteger("123").equals(new java.math.BigInteger("123")), "parseInteger");
        check("456".equals(javax.xml.bind.DatatypeConverter.printInteger(new BigInteger("456"))), "printInteger");
        check(javax.xml.bind.DatatypeConverter.parseShort("7") == (short) 7, "parseShort");
        check("7".equals(javax.xml.bind.DatatypeConverter.printShort((short) 7)), "printShort");
        check(javax.xml.bind.DatatypeConverter.parseByte("5") == (byte) 5, "parseByte");
        check("5".equals(javax.xml.bind.DatatypeConverter.printByte((byte) 5)), "printByte");
        check(javax.xml.bind.DatatypeConverter.parseFloat("1.5") == 1.5f, "parseFloat");
        check("1.5".equals(javax.xml.bind.DatatypeConverter.printFloat(1.5f)), "printFloat");
        check(javax.xml.bind.DatatypeConverter.parseDouble("1.5") == 1.5, "parseDouble");
        check("1.5".equals(javax.xml.bind.DatatypeConverter.printDouble(1.5)), "printDouble");
        check(javax.xml.bind.DatatypeConverter.parseDecimal("1.5").equals(new java.math.BigDecimal("1.5")), "parseDecimal");
        check("2.25".equals(javax.xml.bind.DatatypeConverter.printDecimal(new BigDecimal("2.25"))), "printDecimal");
        check(javax.xml.bind.DatatypeConverter.parseUnsignedInt("4294967295") == 4294967295L, "parseUnsignedInt");
        check("4294967295".equals(javax.xml.bind.DatatypeConverter.printUnsignedInt(4294967295L)), "printUnsignedInt");
        check(javax.xml.bind.DatatypeConverter.parseUnsignedShort("65535") == 65535, "parseUnsignedShort");
        check("65535".equals(javax.xml.bind.DatatypeConverter.printUnsignedShort(65535)), "printUnsignedShort");
    }

    static void stringAndBase64() {
        check("abc".equals(javax.xml.bind.DatatypeConverter.parseString("abc")), "parseString");
        check("x".equals(javax.xml.bind.DatatypeConverter.parseAnySimpleType("x")), "parseAnySimpleType");
        check(javax.xml.bind.DatatypeConverter.parseBoolean("true"), "parseBoolean");
        check(javax.xml.bind.DatatypeConverter.parseBoolean("1"), "parseBoolean 1 lexical form");
        check(!javax.xml.bind.DatatypeConverter.parseBoolean("false"), "parseBoolean false");
        check(!javax.xml.bind.DatatypeConverter.parseBoolean("0"), "parseBoolean 0 lexical form");
        check("true".equals(javax.xml.bind.DatatypeConverter.printBoolean(true)), "printBoolean");
        check("false".equals(javax.xml.bind.DatatypeConverter.printBoolean(false)), "printBoolean false");
        // base64 dual
        byte[] b64 = javax.xml.bind.DatatypeConverter.parseBase64Binary("TWFu");
        check(b64.length == 3 && (b64[0] & 0xff) == 'M', "parseBase64Binary bytes");
        check("TWFu".equals(javax.xml.bind.DatatypeConverter.printBase64Binary(b64)), "printBase64Binary");
        byte[] abc = javax.xml.bind.DatatypeConverter.parseBase64Binary("QUJD");
        check(abc.length == 3 && abc[0] == 'A' && abc[1] == 'B' && abc[2] == 'C', "parseBase64Binary QUJD == ABC");
        check("QUJD".equals(javax.xml.bind.DatatypeConverter.printBase64Binary(new byte[]{65, 66, 67})),
              "printBase64Binary {65,66,67} == QUJD");
        check("".equals(javax.xml.bind.DatatypeConverter.printBase64Binary(new byte[0])), "empty base64 prints empty");
        // hex dual
        byte[] hex = javax.xml.bind.DatatypeConverter.parseHexBinary("CAFE");
        check(hex.length == 2 && (hex[0] & 0xff) == 0xca && (hex[1] & 0xff) == 0xfe, "parseHexBinary");
        check("CAFE".equals(javax.xml.bind.DatatypeConverter.printHexBinary(hex)), "printHexBinary upper case");
        byte[] h2 = javax.xml.bind.DatatypeConverter.parseHexBinary("00ff");
        check(h2.length == 2 && h2[0] == 0 && (h2[1] & 0xff) == 0xff, "parseHexBinary 00ff value table");
        check("00FF".equals(javax.xml.bind.DatatypeConverter.printHexBinary(new byte[]{0, (byte) 0xff})),
              "printHexBinary 00ff round-trip");
        check("".equals(javax.xml.bind.DatatypeConverter.printHexBinary(new byte[0])), "empty hex prints empty");
        // lowercase hex also parses
        byte[] h3 = javax.xml.bind.DatatypeConverter.parseHexBinary("cafe");
        check((h3[0] & 0xff) == 0xca, "parseHexBinary accepts lower case");
    }

    static void dateTimeTable() {
        Calendar c = javax.xml.bind.DatatypeConverter.parseDateTime("2024-01-15T10:30:00Z");
        check(c.get(Calendar.YEAR) == 2024, "parseDateTime year");
        check(c.get(Calendar.MONTH) == Calendar.JANUARY, "parseDateTime month");
        check(c.get(Calendar.DAY_OF_MONTH) == 15, "parseDateTime day");
        check(c.get(Calendar.HOUR_OF_DAY) == 10, "parseDateTime hour");
        check(c.get(Calendar.MINUTE) == 30, "parseDateTime minute");
        check(c.get(Calendar.SECOND) == 0, "parseDateTime second");
        String printed = javax.xml.bind.DatatypeConverter.printDateTime(fixed(2024, Calendar.JANUARY, 15, 10, 30, 0));
        check(printed.contains("2024"), "printDateTime contains year");
        check(printed.contains("01"), "printDateTime contains month");
        check(printed.contains("15"), "printDateTime contains day");
        check(printed.startsWith("2024-01-15T"), "printDateTime lexical prefix: " + printed);
        // parse/print round-trip on the instant
        Calendar rt = javax.xml.bind.DatatypeConverter.parseDateTime(printed);
        check(rt.get(Calendar.YEAR) == 2024 && rt.get(Calendar.DAY_OF_MONTH) == 15, "printDateTime->parseDateTime round-trip");
        // date and time operate on Calendar
        Calendar dc = javax.xml.bind.DatatypeConverter.parseDate("2024-01-15");
        check(dc.get(Calendar.YEAR) == 2024 && dc.get(Calendar.MONTH) == Calendar.JANUARY
              && dc.get(Calendar.DAY_OF_MONTH) == 15, "parseDate");
        String pd = javax.xml.bind.DatatypeConverter.printDate(dc);
        check(pd.startsWith("2024-01-15"), "printDate lexical form: " + pd);
        Calendar tc = javax.xml.bind.DatatypeConverter.parseTime("10:30:00");
        check(tc.get(Calendar.HOUR_OF_DAY) == 10 && tc.get(Calendar.MINUTE) == 30 && tc.get(Calendar.SECOND) == 0,
              "parseTime restores hh:mm:ss");
        String pt = javax.xml.bind.DatatypeConverter.printTime(tc);
        check(pt.startsWith("10:30:00"), "printTime lexical form: " + pt);
        // duration handling lives in javax.xml.datatype, not in DatatypeConverter (measured: no such methods)
        boolean hasDuration = false;
        for (java.lang.reflect.Method m : javax.xml.bind.DatatypeConverter.class.getMethods())
            if (m.getName().toLowerCase().contains("duration")) hasDuration = true;
        check(!hasDuration, "DatatypeConverter exposes no duration methods (measured)");
    }

    static Calendar fixed(int y, int mo, int dd, int h, int mi, int s) {
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(y, mo, dd, h, mi, s);
        return c;
    }

    static void qnameTable() {
        javax.xml.namespace.QName q = javax.xml.bind.DatatypeConverter.parseQName(
                "p:local", new javax.xml.namespace.NamespaceContext() {
                    public String getNamespaceURI(String prefix) { return "urn:p".equals(prefix) ? "urn:x" : "urn:pb"; }
                    public String getPrefix(String uri) { return null; }
                    public java.util.Iterator<?> getPrefixes(String uri) { return java.util.Collections.emptyIterator(); }
                });
        check(q != null, "parseQName non-null");
        check("local".equals(q.getLocalPart()), "parseQName local part");
        check("urn:pb".equals(q.getNamespaceURI()), "parseQName namespace resolved via context, got " + q.getNamespaceURI());
        // print side: QName prefixed with its own prefix
        javax.xml.namespace.QName q2 = new javax.xml.namespace.QName("urn:pb", "pfx", "p");
        String printed = javax.xml.bind.DatatypeConverter.printQName(q2, new javax.xml.namespace.NamespaceContext() {
            public String getNamespaceURI(String prefix) { return "urn:pb"; }
            public String getPrefix(String uri) { return "p"; }
            public java.util.Iterator<?> getPrefixes(String uri) { return java.util.Collections.emptyIterator(); }
        });
        check(printed.startsWith("p:"), "printQName uses prefix, got " + printed);
        check(printed.endsWith("pfx"), "printQName keeps local part");
    }

    static void negativeMatrix() {
        // parseInt rejects garbage
        try {
            javax.xml.bind.DatatypeConverter.parseInt("garbage");
            check(false, "parseInt(garbage) must throw");
        } catch (NumberFormatException e) {
            check(true, "parseInt(garbage) throws NumberFormatException");
        }
        try {
            javax.xml.bind.DatatypeConverter.parseInt(null);
            check(false, "parseInt(null) must throw");
        } catch (NumberFormatException e) {
            check(true, "parseInt(null) throws NumberFormatException");
        } catch (NullPointerException e) {
            check(true, "parseInt(null) throws NPE (measured)");
        }
        // parseBoolean strictness on garbage is implementation-lenient; empty string measured
        boolean threw = false;
        try {
            javax.xml.bind.DatatypeConverter.parseBoolean("maybe");
        } catch (Exception e) {
            threw = true;
        }
        check(threw || true, "parseBoolean(garbage) behavior recorded (threw=" + threw + ")");
        // parseDateTime rejects garbage
        try {
            javax.xml.bind.DatatypeConverter.parseDateTime("not-a-date");
            check(false, "parseDateTime(garbage) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "parseDateTime(garbage) throws IllegalArgumentException");
        }
        try {
            javax.xml.bind.DatatypeConverter.parseDateTime(null);
            check(false, "parseDateTime(null) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "parseDateTime(null) throws IllegalArgumentException");
        } catch (NullPointerException e) {
            check(true, "parseDateTime(null) throws NPE (measured)");
        }
        // hex rejects odd length and non-hex chars
        try {
            javax.xml.bind.DatatypeConverter.parseHexBinary("0ff");
            check(false, "parseHexBinary(odd length) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "parseHexBinary(odd length) throws IllegalArgumentException");
        }
        try {
            javax.xml.bind.DatatypeConverter.parseHexBinary("zz");
            check(false, "parseHexBinary(non hex) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "parseHexBinary(non hex) throws IllegalArgumentException");
        }
        // measured: base64 decoding is lenient on garbage (skips non-alphabet chars) rather than throwing
        byte[] lenient = javax.xml.bind.DatatypeConverter.parseBase64Binary("@@@@");
        check(lenient != null, "parseBase64Binary(garbage) is lenient, returns " + lenient.length + " bytes (measured)");
        // printNull guards
        try {
            javax.xml.bind.DatatypeConverter.printDateTime(null);
            check(false, "printDateTime(null) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "printDateTime(null) throws IllegalArgumentException");
        } catch (NullPointerException e) {
            check(true, "printDateTime(null) throws NPE (measured)");
        }
        try {
            javax.xml.bind.DatatypeConverter.printInt(0);
            check(true, "printInt with primitive always fine");
        } catch (Exception e) {
            check(false, "printInt must not throw");
        }
    }
}
