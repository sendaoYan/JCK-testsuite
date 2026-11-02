/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DeferredNode conformance: interface hierarchy and members.
 * @run main DeferredNodeTest
 */
public class DeferredNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DeferredNode", false, DeferredNodeTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "org.w3c.dom.Node"), "has interface org.w3c.dom.Node");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNodeIndex"), "method getNodeIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
