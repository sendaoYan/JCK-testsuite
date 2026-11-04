/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.NotationImpl conformance: class hierarchy and members.
 * @run main NotationImplTest
 */
public class NotationImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.NotationImpl", false, NotationImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.NodeImpl"), "superclass com.sun.org.apache.xerces.internal.dom.NodeImpl");
        check(hasInterface(c, "org.w3c.dom.Notation"), "has interface org.w3c.dom.Notation");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("getSystemId"), "method getSystemId");
        check(mn.contains("setPublicId"), "method setPublicId");
        check(mn.contains("setSystemId"), "method setSystemId");
        check(mn.contains("getBaseURI"), "method getBaseURI");
        check(mn.contains("setBaseURI"), "method setBaseURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
