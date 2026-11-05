/*
 * @test
 * @summary javax.xml.bind.DatatypeConverterInterface conformance: 38-method surface, custom implementation and DatatypeConverter registration channel.
 * @run main DatatypeConverterInterfaceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DatatypeConverterInterfaceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // full marker implementation; every method returns a fixed distinguishable value
    static class Marker implements javax.xml.bind.DatatypeConverterInterface {
        java.util.List<String> log = new java.util.ArrayList<String>();
        public String parseString(String s) { log.add("parseString"); return "M-s"; }
        public java.math.BigInteger parseInteger(String s) { log.add("parseInteger"); return java.math.BigInteger.TEN; }
        public int parseInt(String s) { log.add("parseInt"); return 111; }
        public long parseLong(String s) { log.add("parseLong"); return 5L; }
        public short parseShort(String s) { log.add("parseShort"); return (short) 6; }
        public java.math.BigDecimal parseDecimal(String s) { log.add("parseDecimal"); return java.math.BigDecimal.ONE; }
        public float parseFloat(String s) { log.add("parseFloat"); return 7f; }
        public double parseDouble(String s) { log.add("parseDouble"); return 8d; }
        public boolean parseBoolean(String s) { log.add("parseBoolean"); return false; }
        public byte parseByte(String s) { log.add("parseByte"); return (byte) 9; }
        public javax.xml.namespace.QName parseQName(String s, javax.xml.namespace.NamespaceContext n) { log.add("parseQName"); return new javax.xml.namespace.QName("mu", "ml"); }
        public java.util.Calendar parseDateTime(String s) { log.add("parseDateTime"); return makeCal(); }
        public byte[] parseBase64Binary(String s) { log.add("parseBase64Binary"); return new byte[]{5}; }
        public byte[] parseHexBinary(String s) { log.add("parseHexBinary"); return new byte[]{6}; }
        public long parseUnsignedInt(String s) { log.add("parseUnsignedInt"); return 10L; }
        public int parseUnsignedShort(String s) { log.add("parseUnsignedShort"); return 11; }
        public java.util.Calendar parseTime(String s) { log.add("parseTime"); return makeCal(); }
        public java.util.Calendar parseDate(String s) { log.add("parseDate"); return makeCal(); }
        public String parseAnySimpleType(String s) { log.add("parseAnySimpleType"); return "M-asa"; }
        public String printString(String s) { log.add("printString"); return "P-s"; }
        public String printInteger(java.math.BigInteger v) { log.add("printInteger"); return "P-i"; }
        public String printInt(int v) { log.add("printInt"); return "P-in"; }
        public String printLong(long v) { log.add("printLong"); return "P-l"; }
        public String printShort(short v) { log.add("printShort"); return "P-sh"; }
        public String printDecimal(java.math.BigDecimal v) { log.add("printDecimal"); return "P-d"; }
        public String printFloat(float v) { log.add("printFloat"); return "P-f"; }
        public String printDouble(double v) { log.add("printDouble"); return "P-db"; }
        public String printBoolean(boolean v) { log.add("printBoolean"); return "P-b"; }
        public String printByte(byte v) { log.add("printByte"); return "P-by"; }
        public String printQName(javax.xml.namespace.QName v, javax.xml.namespace.NamespaceContext n) { log.add("printQName"); return "P-q"; }
        public String printDateTime(java.util.Calendar v) { log.add("printDateTime"); return "P-dt"; }
        public String printBase64Binary(byte[] v) { log.add("printBase64Binary"); return "P-b64"; }
        public String printHexBinary(byte[] v) { log.add("printHexBinary"); return "P-hex"; }
        public String printUnsignedInt(long v) { log.add("printUnsignedInt"); return "P-ui"; }
        public String printUnsignedShort(int v) { log.add("printUnsignedShort"); return "P-us"; }
        public String printTime(java.util.Calendar v) { log.add("printTime"); return "P-t"; }
        public String printDate(java.util.Calendar v) { log.add("printDate"); return "P-date"; }
        public String printAnySimpleType(String v) { log.add("printAnySimpleType"); return "P-asa"; }
        static java.util.Calendar makeCal() {
            java.util.Calendar c = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT);
            c.clear();
            c.set(2024, java.util.Calendar.JANUARY, 15);
            return c;
        }
        int parseLog() { return log.size(); }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.DatatypeConverterInterface");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("parseString"), "has method parseString");
        check(names.contains("parseInteger"), "has method parseInteger");
        check(names.contains("parseInt"), "has method parseInt");
        check(names.contains("parseLong"), "has method parseLong");
        check(names.contains("parseShort"), "has method parseShort");
        check(names.contains("parseDecimal"), "has method parseDecimal");
        check(names.contains("parseFloat"), "has method parseFloat");
        check(names.contains("parseDouble"), "has method parseDouble");
        check(names.contains("parseBoolean"), "has method parseBoolean");
        check(names.contains("parseByte"), "has method parseByte");
        check(names.contains("parseQName"), "has method parseQName");
        check(names.contains("parseDateTime"), "has method parseDateTime");
        check(names.contains("parseBase64Binary"), "has method parseBase64Binary");
        check(names.contains("parseHexBinary"), "has method parseHexBinary");
        check(names.contains("parseUnsignedInt"), "has method parseUnsignedInt");
        check(names.contains("parseUnsignedShort"), "has method parseUnsignedShort");
        check(names.contains("parseTime"), "has method parseTime");
        check(names.contains("parseDate"), "has method parseDate");
        check(names.contains("parseAnySimpleType"), "has method parseAnySimpleType");
        check(names.contains("printString"), "has method printString");
        check(names.contains("printInteger"), "has method printInteger");
        check(names.contains("printInt"), "has method printInt");
        check(names.contains("printLong"), "has method printLong");
        check(names.contains("printShort"), "has method printShort");
        check(names.contains("printDecimal"), "has method printDecimal");
        check(names.contains("printFloat"), "has method printFloat");
        check(names.contains("printDouble"), "has method printDouble");
        check(names.contains("printBoolean"), "has method printBoolean");
        check(names.contains("printByte"), "has method printByte");
        check(names.contains("printQName"), "has method printQName");
        check(names.contains("printDateTime"), "has method printDateTime");
        check(names.contains("printBase64Binary"), "has method printBase64Binary");
        check(names.contains("printHexBinary"), "has method printHexBinary");
        check(names.contains("printUnsignedInt"), "has method printUnsignedInt");
        check(names.contains("printUnsignedShort"), "has method printUnsignedShort");
        check(names.contains("printTime"), "has method printTime");
        check(names.contains("printDate"), "has method printDate");
        check(names.contains("printAnySimpleType"), "has method printAnySimpleType");
        testSurface(c);
        testMarkerImpl();
        testRegistrationChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // exact surface: 38 abstract methods, no constants, no inheritance
    static void testSurface(Class<?> c) throws Exception {
        check(c.getDeclaredMethods().length == 38, "exactly 38 declared methods (19 parse + 19 print)");
        check(c.getDeclaredFields().length == 0, "declares no constants");
        check(c.getInterfaces().length == 0, "extends no other interface");
        int parse = 0, print = 0;
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    m.getName() + " is public abstract");
            if (m.getName().startsWith("parse")) parse++;
            if (m.getName().startsWith("print")) print++;
        }
        check(parse == 19, "19 parse* methods");
        check(print == 19, "19 print* methods");
        check(c.getMethod("parseInt", String.class).getReturnType() == int.class, "parseInt returns int");
        check(c.getMethod("parseBoolean", String.class).getReturnType() == boolean.class, "parseBoolean returns boolean");
        check(c.getMethod("parseByte", String.class).getReturnType() == byte.class, "parseByte returns byte");
        check(c.getMethod("parseShort", String.class).getReturnType() == short.class, "parseShort returns short");
        check(c.getMethod("parseFloat", String.class).getReturnType() == float.class, "parseFloat returns float");
        check(c.getMethod("parseLong", String.class).getReturnType() == long.class, "parseLong returns long");
        check(c.getMethod("parseDouble", String.class).getReturnType() == double.class, "parseDouble returns double");
        check(c.getMethod("parseString", String.class).getReturnType() == String.class, "parseString returns String");
        check(c.getMethod("parseDateTime", String.class).getReturnType() == java.util.Calendar.class, "parseDateTime returns Calendar");
        check(c.getMethod("parseQName", String.class, javax.xml.namespace.NamespaceContext.class).getReturnType() == javax.xml.namespace.QName.class,
                "parseQName(String,NamespaceContext) returns QName");
        check(c.getMethod("printInt", int.class).getReturnType() == String.class, "printInt(int) returns String");
        check(c.getMethod("printBoolean", boolean.class).getReturnType() == String.class, "printBoolean(boolean) returns String");
        check(c.getMethod("printDateTime", java.util.Calendar.class).getReturnType() == String.class, "printDateTime(Calendar) returns String");
        check(c.getMethod("printBase64Binary", byte[].class).getReturnType() == String.class, "printBase64Binary(byte[]) returns String");
    }

    // a hand-written implementation satisfies the whole interface; representative calls for each family
    static void testMarkerImpl() {
        Marker m = new Marker();
        check(m instanceof javax.xml.bind.DatatypeConverterInterface, "custom impl passes instanceof");
        check("M-s".equals(m.parseString("x")), "parseString returns marker value");
        check(java.math.BigInteger.TEN.equals(m.parseInteger("1")), "parseInteger returns marker value");
        check(m.parseInt("1") == 111, "parseInt returns marker value");
        check(m.parseLong("1") == 5L, "parseLong returns marker value");
        check(m.parseShort("1") == (short) 6, "parseShort returns marker value");
        check(java.math.BigDecimal.ONE.equals(m.parseDecimal("1")), "parseDecimal returns marker value");
        check(m.parseFloat("1") == 7f, "parseFloat returns marker value");
        check(m.parseDouble("1") == 8d, "parseDouble returns marker value");
        check(m.parseBoolean("true") == false, "parseBoolean returns marker value");
        check(m.parseByte("1") == (byte) 9, "parseByte returns marker value");
        check(new javax.xml.namespace.QName("mu", "ml").equals(m.parseQName("q", null)), "parseQName returns marker value");
        check(m.parseDateTime("2024-01-15T00:00:00Z").get(java.util.Calendar.YEAR) == 2024, "parseDateTime returns marker calendar");
        byte[] b64 = m.parseBase64Binary("QUJD");
        check(b64.length == 1 && b64[0] == 5, "parseBase64Binary returns marker bytes");
        byte[] hex = m.parseHexBinary("00ff");
        check(hex.length == 1 && hex[0] == 6, "parseHexBinary returns marker bytes");
        check(m.parseUnsignedInt("1") == 10L, "parseUnsignedInt returns marker value");
        check(m.parseUnsignedShort("1") == 11, "parseUnsignedShort returns marker value");
        check(m.parseTime("10:30:00").get(java.util.Calendar.YEAR) == 2024, "parseTime returns marker calendar");
        check(m.parseDate("2024-01-15").get(java.util.Calendar.DAY_OF_MONTH) == 15, "parseDate returns marker calendar");
        check("M-asa".equals(m.parseAnySimpleType("x")), "parseAnySimpleType returns marker value");
        check("P-s".equals(m.printString("x")), "printString returns marker value");
        check("P-i".equals(m.printInteger(java.math.BigInteger.ONE)), "printInteger returns marker value");
        check("P-in".equals(m.printInt(1)), "printInt returns marker value");
        check("P-l".equals(m.printLong(1L)), "printLong returns marker value");
        check("P-sh".equals(m.printShort((short) 1)), "printShort returns marker value");
        check("P-d".equals(m.printDecimal(java.math.BigDecimal.ONE)), "printDecimal returns marker value");
        check("P-f".equals(m.printFloat(1f)), "printFloat returns marker value");
        check("P-db".equals(m.printDouble(1d)), "printDouble returns marker value");
        check("P-b".equals(m.printBoolean(true)), "printBoolean returns marker value");
        check("P-by".equals(m.printByte((byte) 1)), "printByte returns marker value");
        check("P-q".equals(m.printQName(new javax.xml.namespace.QName("u", "l"), null)), "printQName returns marker value");
        check("P-dt".equals(m.printDateTime(java.util.Calendar.getInstance())), "printDateTime returns marker value");
        check("P-b64".equals(m.printBase64Binary(new byte[]{1})), "printBase64Binary returns marker value");
        check("P-hex".equals(m.printHexBinary(new byte[]{1})), "printHexBinary returns marker value");
        check("P-ui".equals(m.printUnsignedInt(1L)), "printUnsignedInt returns marker value");
        check("P-us".equals(m.printUnsignedShort(1)), "printUnsignedShort returns marker value");
        check("P-t".equals(m.printTime(java.util.Calendar.getInstance())), "printTime returns marker value");
        check("P-date".equals(m.printDate(java.util.Calendar.getInstance())), "printDate returns marker value");
        check("P-asa".equals(m.printAnySimpleType("x")), "printAnySimpleType returns marker value");
        check(m.log.size() == 38, "all 38 methods invoked");
    }

    // registration channel: must happen BEFORE first static use; later registrations are ignored (locked)
    static void testRegistrationChannel() {
        Marker m = new Marker();
        javax.xml.bind.DatatypeConverter.setDatatypeConverter(m); // first registration in this JVM
        check(true, "setDatatypeConverter(impl) accepted");
        check(javax.xml.bind.DatatypeConverter.parseInt("42") == 111, "static parseInt delegates to registered converter (locked)");
        check("P-in".equals(javax.xml.bind.DatatypeConverter.printInt(42)), "static printInt delegates to registered converter");
        check("M-s".equals(javax.xml.bind.DatatypeConverter.parseString("42")), "static parseString delegates");
        check(m.parseLog() == 3, "three delegation calls recorded");

        Marker m2 = new Marker();
        javax.xml.bind.DatatypeConverter.setDatatypeConverter(m2); // second registration: silently ignored
        check(true, "second setDatatypeConverter call does not throw (locked)");
        check(javax.xml.bind.DatatypeConverter.parseInt("8") == 111, "first converter still in effect after re-registration (locked)");
        check(m2.parseLog() == 0, "second converter never used (locked)");

        boolean iae = false;
        try {
            javax.xml.bind.DatatypeConverter.setDatatypeConverter(null);
        } catch (IllegalArgumentException x) {
            iae = true;
        }
        check(iae, "setDatatypeConverter(null) throws IllegalArgumentException (locked)");
        check(javax.xml.bind.DatatypeConverter.parseInt("1") == 111, "converter unchanged after null attempt");
    }
}
