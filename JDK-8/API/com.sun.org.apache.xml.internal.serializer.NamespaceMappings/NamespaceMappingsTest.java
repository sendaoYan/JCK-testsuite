/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.NamespaceMappings conformance: class hierarchy and members.
 * @run main NamespaceMappingsTest
 */
public class NamespaceMappingsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.NamespaceMappings", false, NamespaceMappingsTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("lookupNamespace"), "method lookupNamespace");
        check(mn.contains("getMappingFromPrefix"), "method getMappingFromPrefix");
        check(mn.contains("lookupPrefix"), "method lookupPrefix");
        check(mn.contains("getMappingFromURI"), "method getMappingFromURI");
        check(mn.contains("popNamespace"), "method popNamespace");
        check(mn.contains("pushNamespace"), "method pushNamespace");
        check(mn.contains("popNamespaces"), "method popNamespaces");
        check(mn.contains("generateNextPrefix"), "method generateNextPrefix");
        check(mn.contains("clone"), "method clone");
        check(mn.contains("reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
