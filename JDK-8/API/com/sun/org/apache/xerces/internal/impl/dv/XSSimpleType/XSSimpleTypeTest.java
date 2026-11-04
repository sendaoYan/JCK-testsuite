/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType conformance: interface hierarchy and members.
 * @run main XSSimpleTypeTest
 */
public class XSSimpleTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType", false, XSSimpleTypeTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition"), "has interface com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getPrimitiveKind"), "method getPrimitiveKind");
        check(mn.contains("validate"), "method validate");
        check(mn.contains("applyFacets"), "method applyFacets");
        check(mn.contains("isEqual"), "method isEqual");
        check(mn.contains("isIDType"), "method isIDType");
        check(mn.contains("getWhitespace"), "method getWhitespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
