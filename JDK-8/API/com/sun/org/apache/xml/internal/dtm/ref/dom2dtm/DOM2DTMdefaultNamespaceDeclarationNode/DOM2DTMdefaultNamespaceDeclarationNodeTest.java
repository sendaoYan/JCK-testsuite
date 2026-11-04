/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.dom2dtm.DOM2DTMdefaultNamespaceDeclarationNode conformance: class hierarchy and members.
 * @run main DOM2DTMdefaultNamespaceDeclarationNodeTest
 */
public class DOM2DTMdefaultNamespaceDeclarationNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.dom2dtm.DOM2DTMdefaultNamespaceDeclarationNode", false, DOM2DTMdefaultNamespaceDeclarationNodeTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.Attr"), "has interface org.w3c.dom.Attr");
        check(hasInterface(c, "org.w3c.dom.TypeInfo"), "has interface org.w3c.dom.TypeInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("getOwnerElement"), "method getOwnerElement");
        check(mn.contains("isSupported"), "method isSupported");
        check(mn.contains("hasChildNodes"), "method hasChildNodes");
        check(mn.contains("hasAttributes"), "method hasAttributes");
        check(mn.contains("getParentNode"), "method getParentNode");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        check(mn.contains("getLastChild"), "method getLastChild");
        check(mn.contains("getPreviousSibling"), "method getPreviousSibling");
        check(mn.contains("getNextSibling"), "method getNextSibling");
        check(mn.contains("getSpecified"), "method getSpecified");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("getChildNodes"), "method getChildNodes");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("setNodeValue"), "method setNodeValue");
        check(mn.contains("setValue"), "method setValue");
        check(mn.contains("setPrefix"), "method setPrefix");
        check(mn.contains("insertBefore"), "method insertBefore");
        check(mn.contains("replaceChild"), "method replaceChild");
        check(mn.contains("appendChild"), "method appendChild");
        check(mn.contains("removeChild"), "method removeChild");
        check(mn.contains("getOwnerDocument"), "method getOwnerDocument");
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getHandleOfNode"), "method getHandleOfNode");
        check(mn.contains("getTypeName"), "method getTypeName");
        check(mn.contains("getTypeNamespace"), "method getTypeNamespace");
        check(mn.contains("isDerivedFrom"), "method isDerivedFrom");
        check(mn.contains("getSchemaTypeInfo"), "method getSchemaTypeInfo");
        check(mn.contains("isId"), "method isId");
        check(mn.contains("setUserData"), "method setUserData");
        check(mn.contains("getUserData"), "method getUserData");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("isEqualNode"), "method isEqualNode");
        check(mn.contains("lookupNamespaceURI"), "method lookupNamespaceURI");
        check(mn.contains("isDefaultNamespace"), "method isDefaultNamespace");
        check(mn.contains("lookupPrefix"), "method lookupPrefix");
        check(mn.contains("isSameNode"), "method isSameNode");
        check(mn.contains("setTextContent"), "method setTextContent");
        check(mn.contains("getTextContent"), "method getTextContent");
        check(mn.contains("compareDocumentPosition"), "method compareDocumentPosition");
        check(mn.contains("getBaseURI"), "method getBaseURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
