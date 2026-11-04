/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.AttrImpl conformance: class hierarchy and members.
 * @run main AttrImplTest
 */
public class AttrImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.AttrImpl", false, AttrImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.opti.NodeImpl"), "superclass com.sun.org.apache.xerces.internal.impl.xs.opti.NodeImpl");
        check(hasInterface(c, "org.w3c.dom.Attr"), "has interface org.w3c.dom.Attr");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getSpecified"), "method getSpecified");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("getOwnerElement"), "method getOwnerElement");
        check(mn.contains("getOwnerDocument"), "method getOwnerDocument");
        check(mn.contains("setValue"), "method setValue");
        check(mn.contains("isId"), "method isId");
        check(mn.contains("getSchemaTypeInfo"), "method getSchemaTypeInfo");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
