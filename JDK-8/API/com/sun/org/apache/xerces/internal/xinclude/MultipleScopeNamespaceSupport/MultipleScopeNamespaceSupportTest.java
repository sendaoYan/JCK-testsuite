/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xinclude.MultipleScopeNamespaceSupport conformance: class hierarchy and members.
 * @run main MultipleScopeNamespaceSupportTest
 */
public class MultipleScopeNamespaceSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xinclude.MultipleScopeNamespaceSupport", false, MultipleScopeNamespaceSupportTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.util.NamespaceSupport"), "superclass com.sun.org.apache.xerces.internal.util.NamespaceSupport");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAllPrefixes"), "method getAllPrefixes");
        check(mn.contains("getScopeForContext"), "method getScopeForContext");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("pushScope"), "method pushScope");
        check(mn.contains("popScope"), "method popScope");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
