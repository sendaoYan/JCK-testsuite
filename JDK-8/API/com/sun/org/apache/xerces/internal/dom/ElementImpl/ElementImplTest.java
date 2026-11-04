/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.ElementImpl conformance: class hierarchy and members.
 * @run main ElementImplTest
 */
public class ElementImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.ElementImpl", false, ElementImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.ParentNode"), "superclass com.sun.org.apache.xerces.internal.dom.ParentNode");
        check(hasInterface(c, "org.w3c.dom.Element"), "has interface org.w3c.dom.Element");
        check(hasInterface(c, "org.w3c.dom.TypeInfo"), "has interface org.w3c.dom.TypeInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("rename"), "method rename");
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getBaseURI"), "method getBaseURI");
        check(mn.contains("setOwnerDocument"), "method setOwnerDocument");
        check(mn.contains("getAttribute"), "method getAttribute");
        check(mn.contains("getAttributeNode"), "method getAttributeNode");
        check(mn.contains("getElementsByTagName"), "method getElementsByTagName");
        check(mn.contains("getTagName"), "method getTagName");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("removeAttribute"), "method removeAttribute");
        check(mn.contains("removeAttributeNode"), "method removeAttributeNode");
        check(mn.contains("setAttribute"), "method setAttribute");
        check(mn.contains("setAttributeNode"), "method setAttributeNode");
        check(mn.contains("getAttributeNS"), "method getAttributeNS");
        check(mn.contains("setAttributeNS"), "method setAttributeNS");
        check(mn.contains("removeAttributeNS"), "method removeAttributeNS");
        check(mn.contains("getAttributeNodeNS"), "method getAttributeNodeNS");
        check(mn.contains("setAttributeNodeNS"), "method setAttributeNodeNS");
        check(mn.contains("setXercesAttributeNode"), "method setXercesAttributeNode");
        check(mn.contains("getXercesAttribute"), "method getXercesAttribute");
        check(mn.contains("hasAttributes"), "method hasAttributes");
        check(mn.contains("hasAttribute"), "method hasAttribute");
        check(mn.contains("hasAttributeNS"), "method hasAttributeNS");
        check(mn.contains("getElementsByTagNameNS"), "method getElementsByTagNameNS");
        check(mn.contains("isEqualNode"), "method isEqualNode");
        check(mn.contains("setIdAttributeNode"), "method setIdAttributeNode");
        check(mn.contains("setIdAttribute"), "method setIdAttribute");
        check(mn.contains("setIdAttributeNS"), "method setIdAttributeNS");
        check(mn.contains("getTypeName"), "method getTypeName");
        check(mn.contains("getTypeNamespace"), "method getTypeNamespace");
        check(mn.contains("isDerivedFrom"), "method isDerivedFrom");
        check(mn.contains("getSchemaTypeInfo"), "method getSchemaTypeInfo");
        check(mn.contains("setReadOnly"), "method setReadOnly");
        check(mn.contains("synchronizeData"), "method synchronizeData");
        check(mn.contains("moveSpecifiedAttributes"), "method moveSpecifiedAttributes");
        check(mn.contains("setupDefaultAttributes"), "method setupDefaultAttributes");
        check(mn.contains("reconcileDefaultAttributes"), "method reconcileDefaultAttributes");
        check(mn.contains("getDefaultAttributes"), "method getDefaultAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
