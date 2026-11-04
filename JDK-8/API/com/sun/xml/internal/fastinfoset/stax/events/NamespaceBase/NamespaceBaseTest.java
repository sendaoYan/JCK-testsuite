/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.events.NamespaceBase conformance: class hierarchy and members.
 * @run main NamespaceBaseTest
 */

public class NamespaceBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.events.NamespaceBase", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.events.NamespaceBase"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.stax.events.AttributeBase".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.stax.events.AttributeBase");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.events.Namespace"), "has interface javax.xml.stream.events.Namespace");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNamespaceURI"), "has method getNamespaceURI");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("isDefaultNamespaceDeclaration"), "has method isDefaultNamespaceDeclaration");
        check(mn.contains("isNamespace"), "has method isNamespace");
        check(mn.contains("setNamespaceURI"), "has method setNamespaceURI");
        check(mn.contains("setPrefix"), "has method setPrefix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
