/*
 * @test
 * @summary javax.xml.bind.DatatypeConverterInterface conformance: interface hierarchy and methods.
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
