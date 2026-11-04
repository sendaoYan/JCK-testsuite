/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.ChildNode conformance: abstract class hierarchy and members.
 * @run main ChildNodeTest
 */
public class ChildNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.ChildNode", false, ChildNodeTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.NodeImpl"), "superclass com.sun.org.apache.xerces.internal.dom.NodeImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getParentNode"), "method getParentNode");
        check(mn.contains("parentNode"), "method parentNode");
        check(mn.contains("getNextSibling"), "method getNextSibling");
        check(mn.contains("getPreviousSibling"), "method getPreviousSibling");
        check(mn.contains("previousSibling"), "method previousSibling");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
