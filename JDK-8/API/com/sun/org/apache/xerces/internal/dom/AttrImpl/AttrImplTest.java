/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.AttrImpl conformance: class hierarchy and members.
 * @run main AttrImplTest
 */
public class AttrImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.AttrImpl", false, AttrImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.NodeImpl"), "superclass com.sun.org.apache.xerces.internal.dom.NodeImpl");
        check(hasInterface(c, "org.w3c.dom.Attr"), "has interface org.w3c.dom.Attr");
        check(hasInterface(c, "org.w3c.dom.TypeInfo"), "has interface org.w3c.dom.TypeInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("rename"), "method rename");
        check(mn.contains("makeChildNode"), "method makeChildNode");
        check(mn.contains("setOwnerDocument"), "method setOwnerDocument");
        check(mn.contains("setIdAttribute"), "method setIdAttribute");
        check(mn.contains("isId"), "method isId");
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("setNodeValue"), "method setNodeValue");
        check(mn.contains("getTypeName"), "method getTypeName");
        check(mn.contains("getTypeNamespace"), "method getTypeNamespace");
        check(mn.contains("getSchemaTypeInfo"), "method getSchemaTypeInfo");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("setValue"), "method setValue");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("getSpecified"), "method getSpecified");
        check(mn.contains("getElement"), "method getElement");
        check(mn.contains("getOwnerElement"), "method getOwnerElement");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("setSpecified"), "method setSpecified");
        check(mn.contains("setType"), "method setType");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("hasChildNodes"), "method hasChildNodes");
        check(mn.contains("getChildNodes"), "method getChildNodes");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        check(mn.contains("getLastChild"), "method getLastChild");
        check(mn.contains("lastChild"), "method lastChild");
        check(mn.contains("insertBefore"), "method insertBefore");
        check(mn.contains("internalInsertBefore"), "method internalInsertBefore");
        check(mn.contains("removeChild"), "method removeChild");
        check(mn.contains("internalRemoveChild"), "method internalRemoveChild");
        check(mn.contains("replaceChild"), "method replaceChild");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("item"), "method item");
        check(mn.contains("isEqualNode"), "method isEqualNode");
        check(mn.contains("isDerivedFrom"), "method isDerivedFrom");
        check(mn.contains("setReadOnly"), "method setReadOnly");
        check(mn.contains("synchronizeChildren"), "method synchronizeChildren");
        check(mn.contains("checkNormalizationAfterInsert"), "method checkNormalizationAfterInsert");
        check(mn.contains("checkNormalizationAfterRemove"), "method checkNormalizationAfterRemove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
