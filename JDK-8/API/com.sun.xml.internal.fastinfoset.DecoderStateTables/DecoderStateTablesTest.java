/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.DecoderStateTables conformance: class hierarchy and members.
 * @run main DecoderStateTablesTest
 */

public class DecoderStateTablesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.DecoderStateTables", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.DecoderStateTables"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("AII"), "has method AII");
        check(mn.contains("DII"), "has method DII");
        check(mn.contains("EII"), "has method EII");
        check(mn.contains("ISTRING"), "has method ISTRING");
        check(mn.contains("ISTRING_PREFIX_NAMESPACE"), "has method ISTRING_PREFIX_NAMESPACE");
        check(mn.contains("NISTRING"), "has method NISTRING");
        check(mn.contains("UTF8"), "has method UTF8");
        check(mn.contains("UTF8_NCNAME"), "has method UTF8_NCNAME");
        check(mn.contains("constructTable"), "has method constructTable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
