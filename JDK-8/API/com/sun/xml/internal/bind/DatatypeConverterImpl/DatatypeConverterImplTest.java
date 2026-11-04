/*
 * @test
 * @summary com.sun.xml.internal.bind.DatatypeConverterImpl conformance: class hierarchy and members.
 * @run main DatatypeConverterImplTest
 */

public class DatatypeConverterImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.DatatypeConverterImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.DatatypeConverterImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.bind.DatatypeConverterInterface"), "has interface javax.xml.bind.DatatypeConverterInterface");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("_parseBase64Binary"), "has method _parseBase64Binary");
        check(mn.contains("_parseBoolean"), "has method _parseBoolean");
        check(mn.contains("_parseByte"), "has method _parseByte");
        check(mn.contains("_parseDateTime"), "has method _parseDateTime");
        check(mn.contains("_parseDecimal"), "has method _parseDecimal");
        check(mn.contains("_parseDouble"), "has method _parseDouble");
        check(mn.contains("_parseFloat"), "has method _parseFloat");
        check(mn.contains("_parseInt"), "has method _parseInt");
        check(mn.contains("_parseInteger"), "has method _parseInteger");
        check(mn.contains("_parseLong"), "has method _parseLong");
        check(mn.contains("_parseQName"), "has method _parseQName");
        check(mn.contains("_parseShort"), "has method _parseShort");
        check(mn.contains("_printBase64Binary"), "has method _printBase64Binary");
        check(mn.contains("_printBoolean"), "has method _printBoolean");
        check(mn.contains("_printByte"), "has method _printByte");
        check(mn.contains("_printDate"), "has method _printDate");
        check(mn.contains("_printDateTime"), "has method _printDateTime");
        check(mn.contains("_printDecimal"), "has method _printDecimal");
        check(mn.contains("_printDouble"), "has method _printDouble");
        check(mn.contains("_printFloat"), "has method _printFloat");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
