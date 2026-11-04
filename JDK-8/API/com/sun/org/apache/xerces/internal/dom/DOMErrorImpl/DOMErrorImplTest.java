/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DOMErrorImpl conformance: class hierarchy and members.
 * @run main DOMErrorImplTest
 */
public class DOMErrorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DOMErrorImpl", false, DOMErrorImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.DOMError"), "has interface org.w3c.dom.DOMError");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getSeverity"), "method getSeverity");
        check(mn.contains("getMessage"), "method getMessage");
        check(mn.contains("getLocation"), "method getLocation");
        check(mn.contains("getRelatedException"), "method getRelatedException");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getRelatedData"), "method getRelatedData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
