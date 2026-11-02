/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.XMLDeclarationParser conformance: class hierarchy and members.
 * @run main XMLDeclarationParserTest
 */

public class XMLDeclarationParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.XMLDeclarationParser", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.XMLDeclarationParser"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getEncoding"), "has method getEncoding");
        check(mn.contains("getXmlDeclaration"), "has method getXmlDeclaration");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("parseEncoding"), "has method parseEncoding");
        check(mn.contains("writeTo"), "has method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
