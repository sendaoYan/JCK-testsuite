/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.DOMUtil conformance: class hierarchy and members.
 * @run main DOMUtilTest
 */
public class DOMUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.DOMUtil", false, DOMUtilTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("copyInto"), "method copyInto");
        check(mn.contains("getFirstChildElement"), "method getFirstChildElement");
        check(mn.contains("getFirstVisibleChildElement"), "method getFirstVisibleChildElement");
        check(mn.contains("getLastChildElement"), "method getLastChildElement");
        check(mn.contains("getLastVisibleChildElement"), "method getLastVisibleChildElement");
        check(mn.contains("getNextSiblingElement"), "method getNextSiblingElement");
        check(mn.contains("getNextVisibleSiblingElement"), "method getNextVisibleSiblingElement");
        check(mn.contains("setHidden"), "method setHidden");
        check(mn.contains("setVisible"), "method setVisible");
        check(mn.contains("isHidden"), "method isHidden");
        check(mn.contains("getFirstChildElementNS"), "method getFirstChildElementNS");
        check(mn.contains("getLastChildElementNS"), "method getLastChildElementNS");
        check(mn.contains("getNextSiblingElementNS"), "method getNextSiblingElementNS");
        check(mn.contains("getChildText"), "method getChildText");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getParent"), "method getParent");
        check(mn.contains("getDocument"), "method getDocument");
        check(mn.contains("getRoot"), "method getRoot");
        check(mn.contains("getAttr"), "method getAttr");
        check(mn.contains("getAttrNS"), "method getAttrNS");
        check(mn.contains("getAttrs"), "method getAttrs");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("getAttrValue"), "method getAttrValue");
        check(mn.contains("getAttrValueNS"), "method getAttrValueNS");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getSyntheticAnnotation"), "method getSyntheticAnnotation");
        check(mn.contains("createDOMException"), "method createDOMException");
        check(mn.contains("createLSException"), "method createLSException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
