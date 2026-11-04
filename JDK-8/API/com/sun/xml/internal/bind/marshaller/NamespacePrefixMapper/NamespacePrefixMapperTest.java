/*
 * @test
 * @summary com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper conformance: abstract class hierarchy and members.
 * @run main NamespacePrefixMapperTest
 */

public class NamespacePrefixMapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getContextualNamespaceDecls"), "has method getContextualNamespaceDecls");
        check(mn.contains("getPreDeclaredNamespaceUris"), "has method getPreDeclaredNamespaceUris");
        check(mn.contains("getPreDeclaredNamespaceUris2"), "has method getPreDeclaredNamespaceUris2");
        check(mn.contains("getPreferredPrefix"), "has method getPreferredPrefix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
