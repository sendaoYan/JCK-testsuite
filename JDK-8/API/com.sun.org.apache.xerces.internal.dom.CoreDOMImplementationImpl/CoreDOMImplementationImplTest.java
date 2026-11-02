/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl conformance: class hierarchy and members.
 * @run main CoreDOMImplementationImplTest
 */
public class CoreDOMImplementationImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl", false, CoreDOMImplementationImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.DOMImplementation"), "has interface org.w3c.dom.DOMImplementation");
        check(hasInterface(c, "org.w3c.dom.ls.DOMImplementationLS"), "has interface org.w3c.dom.ls.DOMImplementationLS");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDOMImplementation"), "method getDOMImplementation");
        check(mn.contains("hasFeature"), "method hasFeature");
        check(mn.contains("createDocumentType"), "method createDocumentType");
        check(mn.contains("checkQName"), "method checkQName");
        check(mn.contains("createDocument"), "method createDocument");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("createLSParser"), "method createLSParser");
        check(mn.contains("createLSSerializer"), "method createLSSerializer");
        check(mn.contains("createLSInput"), "method createLSInput");
        check(mn.contains("getValidator"), "method getValidator");
        check(mn.contains("releaseValidator"), "method releaseValidator");
        check(mn.contains("assignDocumentNumber"), "method assignDocumentNumber");
        check(mn.contains("assignDocTypeNumber"), "method assignDocTypeNumber");
        check(mn.contains("createLSOutput"), "method createLSOutput");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
