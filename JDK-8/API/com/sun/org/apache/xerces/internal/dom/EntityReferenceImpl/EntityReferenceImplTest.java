/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.EntityReferenceImpl conformance: class hierarchy and members.
 * @run main EntityReferenceImplTest
 */
public class EntityReferenceImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.EntityReferenceImpl", false, EntityReferenceImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.ParentNode"), "superclass com.sun.org.apache.xerces.internal.dom.ParentNode");
        check(hasInterface(c, "org.w3c.dom.EntityReference"), "has interface org.w3c.dom.EntityReference");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getBaseURI"), "method getBaseURI");
        check(mn.contains("setBaseURI"), "method setBaseURI");
        check(mn.contains("getEntityRefValue"), "method getEntityRefValue");
        check(mn.contains("synchronizeChildren"), "method synchronizeChildren");
        check(mn.contains("setReadOnly"), "method setReadOnly");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
