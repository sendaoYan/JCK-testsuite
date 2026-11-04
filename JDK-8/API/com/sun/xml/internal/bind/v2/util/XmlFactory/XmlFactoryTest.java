/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.util.XmlFactory conformance: class hierarchy and members.
 * @run main XmlFactoryTest
 */

public class XmlFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.util.XmlFactory", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.util.XmlFactory"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("allowExternalAccess"), "has method allowExternalAccess");
        check(mn.contains("allowExternalDTDAccess"), "has method allowExternalDTDAccess");
        check(mn.contains("createDocumentBuilderFactory"), "has method createDocumentBuilderFactory");
        check(mn.contains("createParserFactory"), "has method createParserFactory");
        check(mn.contains("createSchemaFactory"), "has method createSchemaFactory");
        check(mn.contains("createTransformerFactory"), "has method createTransformerFactory");
        check(mn.contains("createXPathFactory"), "has method createXPathFactory");
        check(mn.contains("isXMLSecurityDisabled"), "has method isXMLSecurityDisabled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
