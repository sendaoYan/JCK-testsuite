/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DeferredEntityReferenceImpl conformance: class hierarchy and members.
 * @run main DeferredEntityReferenceImplTest
 */
public class DeferredEntityReferenceImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DeferredEntityReferenceImpl", false, DeferredEntityReferenceImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.EntityReferenceImpl"), "superclass com.sun.org.apache.xerces.internal.dom.EntityReferenceImpl");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.dom.DeferredNode"), "has interface com.sun.org.apache.xerces.internal.dom.DeferredNode");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNodeIndex"), "method getNodeIndex");
        check(mn.contains("synchronizeData"), "method synchronizeData");
        check(mn.contains("synchronizeChildren"), "method synchronizeChildren");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
