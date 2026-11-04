/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DeepNodeListImpl conformance: class hierarchy and members.
 * @run main DeepNodeListImplTest
 */
public class DeepNodeListImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DeepNodeListImpl", false, DeepNodeListImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.NodeList"), "has interface org.w3c.dom.NodeList");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("item"), "method item");
        check(mn.contains("nextMatchingElementAfter"), "method nextMatchingElementAfter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
