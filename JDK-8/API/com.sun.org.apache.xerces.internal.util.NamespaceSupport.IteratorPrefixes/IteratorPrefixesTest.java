/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.NamespaceSupport.IteratorPrefixes conformance: class hierarchy and members.
 * @run main IteratorPrefixesTest
 */
public class IteratorPrefixesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.NamespaceSupport$IteratorPrefixes", false, IteratorPrefixesTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "java.util.Iterator"), "has interface java.util.Iterator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("hasNext"), "method hasNext");
        check(mn.contains("next"), "method next");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("remove"), "method remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
