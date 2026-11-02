/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.ElementImpl conformance: class hierarchy and members.
 * @run main ElementImplTest
 */
public class ElementImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.ElementImpl", false, ElementImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement"), "superclass com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getOwnerDocument"), "method getOwnerDocument");
        check(mn.contains("getParentNode"), "method getParentNode");
        check(mn.contains("hasChildNodes"), "method hasChildNodes");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        check(mn.contains("getLastChild"), "method getLastChild");
        check(mn.contains("getPreviousSibling"), "method getPreviousSibling");
        check(mn.contains("getNextSibling"), "method getNextSibling");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("hasAttributes"), "method hasAttributes");
        check(mn.contains("getTagName"), "method getTagName");
        check(mn.contains("getAttribute"), "method getAttribute");
        check(mn.contains("getAttributeNode"), "method getAttributeNode");
        check(mn.contains("getAttributeNS"), "method getAttributeNS");
        check(mn.contains("getAttributeNodeNS"), "method getAttributeNodeNS");
        check(mn.contains("hasAttribute"), "method hasAttribute");
        check(mn.contains("hasAttributeNS"), "method hasAttributeNS");
        check(mn.contains("setAttribute"), "method setAttribute");
        check(mn.contains("getLineNumber"), "method getLineNumber");
        check(mn.contains("getColumnNumber"), "method getColumnNumber");
        check(mn.contains("getCharacterOffset"), "method getCharacterOffset");
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getSyntheticAnnotation"), "method getSyntheticAnnotation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
