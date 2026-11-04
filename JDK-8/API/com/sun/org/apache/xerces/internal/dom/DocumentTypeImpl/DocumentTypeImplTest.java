/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DocumentTypeImpl conformance: class hierarchy and members.
 * @run main DocumentTypeImplTest
 */
public class DocumentTypeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DocumentTypeImpl", false, DocumentTypeImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.ParentNode"), "superclass com.sun.org.apache.xerces.internal.dom.ParentNode");
        check(hasInterface(c, "org.w3c.dom.DocumentType"), "has interface org.w3c.dom.DocumentType");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("getSystemId"), "method getSystemId");
        check(mn.contains("setInternalSubset"), "method setInternalSubset");
        check(mn.contains("getInternalSubset"), "method getInternalSubset");
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getTextContent"), "method getTextContent");
        check(mn.contains("setTextContent"), "method setTextContent");
        check(mn.contains("isEqualNode"), "method isEqualNode");
        check(mn.contains("setOwnerDocument"), "method setOwnerDocument");
        check(mn.contains("getNodeNumber"), "method getNodeNumber");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getEntities"), "method getEntities");
        check(mn.contains("getNotations"), "method getNotations");
        check(mn.contains("setReadOnly"), "method setReadOnly");
        check(mn.contains("getElements"), "method getElements");
        check(mn.contains("setUserData"), "method setUserData");
        check(mn.contains("getUserData"), "method getUserData");
        check(mn.contains("getUserDataRecord"), "method getUserDataRecord");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
