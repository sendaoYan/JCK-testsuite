/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.NamespaceContext conformance: interface hierarchy and members.
 * @run main NamespaceContextTest
 */
public class NamespaceContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.NamespaceContext", false, NamespaceContextTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("pushContext"), "method pushContext");
        check(mn.contains("popContext"), "method popContext");
        check(mn.contains("declarePrefix"), "method declarePrefix");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getDeclaredPrefixCount"), "method getDeclaredPrefixCount");
        check(mn.contains("getDeclaredPrefixAt"), "method getDeclaredPrefixAt");
        check(mn.contains("getAllPrefixes"), "method getAllPrefixes");
        check(mn.contains("reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
