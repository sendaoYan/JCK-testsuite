/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.staxex.NamespaceContextEx conformance: interface hierarchy and members.
 * @run main NamespaceContextExTest
 */

public class NamespaceContextExTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.staxex.NamespaceContextEx", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.staxex.NamespaceContextEx"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("java.lang.Iterable"), "has interface java.lang.Iterable");
        check(ifs.contains("javax.xml.namespace.NamespaceContext"), "has interface javax.xml.namespace.NamespaceContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("iterator"), "has method iterator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
