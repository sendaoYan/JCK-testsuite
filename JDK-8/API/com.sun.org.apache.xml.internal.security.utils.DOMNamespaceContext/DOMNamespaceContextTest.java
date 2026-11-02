/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.DOMNamespaceContext conformance: class hierarchy and members.
 * @run main DOMNamespaceContextTest
 */
public class DOMNamespaceContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.DOMNamespaceContext", false, DOMNamespaceContextTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "javax.xml.namespace.NamespaceContext"), "has interface javax.xml.namespace.NamespaceContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getPrefixes"), "method getPrefixes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
