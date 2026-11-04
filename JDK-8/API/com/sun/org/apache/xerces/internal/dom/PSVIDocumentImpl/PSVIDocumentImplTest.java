/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.PSVIDocumentImpl conformance: class hierarchy and members.
 * @run main PSVIDocumentImplTest
 */
public class PSVIDocumentImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.PSVIDocumentImpl", false, PSVIDocumentImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.DocumentImpl"), "superclass com.sun.org.apache.xerces.internal.dom.DocumentImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getImplementation"), "method getImplementation");
        check(mn.contains("createElementNS"), "method createElementNS");
        check(mn.contains("createAttributeNS"), "method createAttributeNS");
        check(mn.contains("getDomConfig"), "method getDomConfig");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
