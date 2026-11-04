/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl conformance: class hierarchy and members.
 * @run main DOMImplementationSourceImplTest
 */
public class DOMImplementationSourceImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl", false, DOMImplementationSourceImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.DOMImplementationSource"), "has interface org.w3c.dom.DOMImplementationSource");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDOMImplementation"), "method getDOMImplementation");
        check(mn.contains("getDOMImplementationList"), "method getDOMImplementationList");
        check(mn.contains("testImpl"), "method testImpl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
