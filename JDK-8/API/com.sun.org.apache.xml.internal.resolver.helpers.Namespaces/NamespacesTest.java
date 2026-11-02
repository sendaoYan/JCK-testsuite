/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.helpers.Namespaces conformance: class hierarchy and members.
 * @run main NamespacesTest
 */
public class NamespacesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.helpers.Namespaces", false, NamespacesTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
