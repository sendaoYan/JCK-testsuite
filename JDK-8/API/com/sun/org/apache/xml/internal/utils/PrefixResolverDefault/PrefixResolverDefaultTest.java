/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.PrefixResolverDefault conformance: class hierarchy and members.
 * @run main PrefixResolverDefaultTest
 */
public class PrefixResolverDefaultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.PrefixResolverDefault", false, PrefixResolverDefaultTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.utils.PrefixResolver"), "has interface com.sun.org.apache.xml.internal.utils.PrefixResolver");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNamespaceForPrefix"), "method getNamespaceForPrefix");
        check(mn.contains("getBaseIdentifier"), "method getBaseIdentifier");
        check(mn.contains("handlesNullPrefixes"), "method handlesNullPrefixes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
