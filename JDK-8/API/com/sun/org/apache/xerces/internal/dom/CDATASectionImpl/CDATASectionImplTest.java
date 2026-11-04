/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.CDATASectionImpl conformance: class hierarchy and members.
 * @run main CDATASectionImplTest
 */
public class CDATASectionImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.CDATASectionImpl", false, CDATASectionImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.TextImpl"), "superclass com.sun.org.apache.xerces.internal.dom.TextImpl");
        check(hasInterface(c, "org.w3c.dom.CDATASection"), "has interface org.w3c.dom.CDATASection");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getNodeName"), "method getNodeName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
