/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.NodeImpl conformance: abstract class hierarchy and members.
 * @run main NodeImplTest
 */
public class NodeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.NodeImpl", false, NodeImplTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.Node"), "has interface org.w3c.dom.Node");
        check(hasInterface(c, "org.w3c.dom.NodeList"), "has interface org.w3c.dom.NodeList");
        check(hasInterface(c, "org.w3c.dom.events.EventTarget"), "has interface org.w3c.dom.events.EventTarget");
        check(hasInterface(c, "java.lang.Cloneable"), "has interface java.lang.Cloneable");
        check(hasInterface(c, "java.io.Serializable"), "has interface java.io.Serializable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("setNodeValue"), "method setNodeValue");
        check(mn.contains("appendChild"), "method appendChild");
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getOwnerDocument"), "method getOwnerDocument");
        check(mn.contains("ownerDocument"), "method ownerDocument");
        check(mn.contains("setOwnerDocument"), "method setOwnerDocument");
        check(mn.contains("getNodeNumber"), "method getNodeNumber");
        check(mn.contains("getParentNode"), "method getParentNode");
        check(mn.contains("parentNode"), "method parentNode");
        check(mn.contains("getNextSibling"), "method getNextSibling");
        check(mn.contains("getPreviousSibling"), "method getPreviousSibling");
        check(mn.contains("previousSibling"), "method previousSibling");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("hasAttributes"), "method hasAttributes");
        check(mn.contains("hasChildNodes"), "method hasChildNodes");
        check(mn.contains("getChildNodes"), "method getChildNodes");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        check(mn.contains("getLastChild"), "method getLastChild");
        check(mn.contains("insertBefore"), "method insertBefore");
        check(mn.contains("removeChild"), "method removeChild");
        check(mn.contains("replaceChild"), "method replaceChild");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("item"), "method item");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("isSupported"), "method isSupported");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("setPrefix"), "method setPrefix");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("addEventListener"), "method addEventListener");
        check(mn.contains("removeEventListener"), "method removeEventListener");
        check(mn.contains("dispatchEvent"), "method dispatchEvent");
        check(mn.contains("getBaseURI"), "method getBaseURI");
        check(mn.contains("compareTreePosition"), "method compareTreePosition");
        check(mn.contains("compareDocumentPosition"), "method compareDocumentPosition");
        check(mn.contains("getTextContent"), "method getTextContent");
        check(mn.contains("setTextContent"), "method setTextContent");
        check(mn.contains("isSameNode"), "method isSameNode");
        check(mn.contains("isDefaultNamespace"), "method isDefaultNamespace");
        check(mn.contains("lookupPrefix"), "method lookupPrefix");
        check(mn.contains("lookupNamespaceURI"), "method lookupNamespaceURI");
        check(mn.contains("getElementAncestor"), "method getElementAncestor");
        check(mn.contains("lookupNamespacePrefix"), "method lookupNamespacePrefix");
        check(mn.contains("isEqualNode"), "method isEqualNode");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("setUserData"), "method setUserData");
        check(mn.contains("getUserData"), "method getUserData");
        check(mn.contains("getUserDataRecord"), "method getUserDataRecord");
        check(mn.contains("setReadOnly"), "method setReadOnly");
        check(mn.contains("getReadOnly"), "method getReadOnly");
        check(mn.contains("changed"), "method changed");
        check(mn.contains("changes"), "method changes");
        check(mn.contains("synchronizeData"), "method synchronizeData");
        check(mn.contains("getContainer"), "method getContainer");
        check(mn.contains("isReadOnly"), "method isReadOnly");
        check(mn.contains("needsSyncData"), "method needsSyncData");
        check(mn.contains("needsSyncChildren"), "method needsSyncChildren");
        check(mn.contains("isOwned"), "method isOwned");
        check(mn.contains("isFirstChild"), "method isFirstChild");
        check(mn.contains("isSpecified"), "method isSpecified");
        check(mn.contains("internalIsIgnorableWhitespace"), "method internalIsIgnorableWhitespace");
        check(mn.contains("isIgnorableWhitespace"), "method isIgnorableWhitespace");
        check(mn.contains("hasStringValue"), "method hasStringValue");
        check(mn.contains("isNormalized"), "method isNormalized");
        check(mn.contains("isIdAttribute"), "method isIdAttribute");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
