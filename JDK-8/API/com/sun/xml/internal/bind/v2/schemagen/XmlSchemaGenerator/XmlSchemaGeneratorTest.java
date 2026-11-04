/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.schemagen.XmlSchemaGenerator conformance: class hierarchy and members.
 * @run main XmlSchemaGeneratorTest
 */

public class XmlSchemaGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.schemagen.XmlSchemaGenerator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.schemagen.XmlSchemaGenerator"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "has method add");
        check(mn.contains("calculateRelativePath"), "has method calculateRelativePath");
        check(mn.contains("fixNull"), "has method fixNull");
        check(mn.contains("generateSwaRefAdapter"), "has method generateSwaRefAdapter");
        check(mn.contains("getNamespace"), "has method getNamespace");
        check(mn.contains("getProcessContentsModeName"), "has method getProcessContentsModeName");
        check(mn.contains("relativize"), "has method relativize");
        check(mn.contains("startsWithIgnoreCase"), "has method startsWithIgnoreCase");
        check(mn.contains("toString"), "has method toString");
        check(mn.contains("write"), "has method write");
        check(mn.contains("writeEpisodeFile"), "has method writeEpisodeFile");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
