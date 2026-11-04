/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement conformance: class hierarchy and members.
 * @run main DefaultElementTest
 */
public class DefaultElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement", false, DefaultElementTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.opti.NodeImpl"), "superclass com.sun.org.apache.xerces.internal.impl.xs.opti.NodeImpl");
        check(hasInterface(c, "org.w3c.dom.Element"), "has interface org.w3c.dom.Element");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getTagName"), "method getTagName");
        check(mn.contains("getAttribute"), "method getAttribute");
        check(mn.contains("getAttributeNode"), "method getAttributeNode");
        check(mn.contains("getElementsByTagName"), "method getElementsByTagName");
        check(mn.contains("getAttributeNS"), "method getAttributeNS");
        check(mn.contains("getAttributeNodeNS"), "method getAttributeNodeNS");
        check(mn.contains("getElementsByTagNameNS"), "method getElementsByTagNameNS");
        check(mn.contains("hasAttribute"), "method hasAttribute");
        check(mn.contains("hasAttributeNS"), "method hasAttributeNS");
        check(mn.contains("getSchemaTypeInfo"), "method getSchemaTypeInfo");
        check(mn.contains("setAttribute"), "method setAttribute");
        check(mn.contains("removeAttribute"), "method removeAttribute");
        check(mn.contains("removeAttributeNode"), "method removeAttributeNode");
        check(mn.contains("setAttributeNode"), "method setAttributeNode");
        check(mn.contains("setAttributeNS"), "method setAttributeNS");
        check(mn.contains("removeAttributeNS"), "method removeAttributeNS");
        check(mn.contains("setAttributeNodeNS"), "method setAttributeNodeNS");
        check(mn.contains("setIdAttributeNode"), "method setIdAttributeNode");
        check(mn.contains("setIdAttribute"), "method setIdAttribute");
        check(mn.contains("setIdAttributeNS"), "method setIdAttributeNS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
