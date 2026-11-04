/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode conformance: class hierarchy and members.
 * @run main DefaultNodeTest
 */
public class DefaultNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode", false, DefaultNodeTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.Node"), "has interface org.w3c.dom.Node");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getParentNode"), "method getParentNode");
        check(mn.contains("getChildNodes"), "method getChildNodes");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        check(mn.contains("getLastChild"), "method getLastChild");
        check(mn.contains("getPreviousSibling"), "method getPreviousSibling");
        check(mn.contains("getNextSibling"), "method getNextSibling");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("getOwnerDocument"), "method getOwnerDocument");
        check(mn.contains("hasChildNodes"), "method hasChildNodes");
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("isSupported"), "method isSupported");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getBaseURI"), "method getBaseURI");
        check(mn.contains("hasAttributes"), "method hasAttributes");
        check(mn.contains("setNodeValue"), "method setNodeValue");
        check(mn.contains("insertBefore"), "method insertBefore");
        check(mn.contains("replaceChild"), "method replaceChild");
        check(mn.contains("removeChild"), "method removeChild");
        check(mn.contains("appendChild"), "method appendChild");
        check(mn.contains("setPrefix"), "method setPrefix");
        check(mn.contains("compareDocumentPosition"), "method compareDocumentPosition");
        check(mn.contains("getTextContent"), "method getTextContent");
        check(mn.contains("setTextContent"), "method setTextContent");
        check(mn.contains("isSameNode"), "method isSameNode");
        check(mn.contains("lookupPrefix"), "method lookupPrefix");
        check(mn.contains("isDefaultNamespace"), "method isDefaultNamespace");
        check(mn.contains("lookupNamespaceURI"), "method lookupNamespaceURI");
        check(mn.contains("isEqualNode"), "method isEqualNode");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("setUserData"), "method setUserData");
        check(mn.contains("getUserData"), "method getUserData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
