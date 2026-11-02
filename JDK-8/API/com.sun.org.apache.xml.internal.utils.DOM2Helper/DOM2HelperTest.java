/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.DOM2Helper conformance: class hierarchy and members.
 * @run main DOM2HelperTest
 */
public class DOM2HelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.DOM2Helper", false, DOM2HelperTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLocalNameOfNode"), "method getLocalNameOfNode");
        check(mn.contains("getNamespaceOfNode"), "method getNamespaceOfNode");
        check(mn.contains("isNodeAfter"), "method isNodeAfter");
        check(mn.contains("isNodeTheSame"), "method isNodeTheSame");
        check(mn.contains("getParentOfNode"), "method getParentOfNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
