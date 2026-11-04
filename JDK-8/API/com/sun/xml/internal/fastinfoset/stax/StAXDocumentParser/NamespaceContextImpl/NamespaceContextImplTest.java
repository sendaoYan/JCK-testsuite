/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.StAXDocumentParser.NamespaceContextImpl conformance: class hierarchy and members.
 * @run main NamespaceContextImplTest
 */

public class NamespaceContextImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.StAXDocumentParser$NamespaceContextImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.StAXDocumentParser$NamespaceContextImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is not public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.namespace.NamespaceContext"), "has interface javax.xml.namespace.NamespaceContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNamespaceURI"), "has method getNamespaceURI");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("getPrefixes"), "has method getPrefixes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
