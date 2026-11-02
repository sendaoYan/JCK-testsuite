/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.AttrNSImpl conformance: class hierarchy and members.
 * @run main AttrNSImplTest
 */
public class AttrNSImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.AttrNSImpl", false, AttrNSImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.AttrImpl"), "superclass com.sun.org.apache.xerces.internal.dom.AttrImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("rename"), "method rename");
        check(mn.contains("setValues"), "method setValues");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("setPrefix"), "method setPrefix");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getTypeName"), "method getTypeName");
        check(mn.contains("isDerivedFrom"), "method isDerivedFrom");
        check(mn.contains("getTypeNamespace"), "method getTypeNamespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
