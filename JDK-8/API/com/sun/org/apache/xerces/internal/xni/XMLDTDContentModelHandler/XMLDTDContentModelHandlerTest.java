/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler conformance: interface hierarchy and members.
 * @run main XMLDTDContentModelHandlerTest
 */
public class XMLDTDContentModelHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler", false, XMLDTDContentModelHandlerTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startContentModel"), "method startContentModel");
        check(mn.contains("any"), "method any");
        check(mn.contains("empty"), "method empty");
        check(mn.contains("startGroup"), "method startGroup");
        check(mn.contains("pcdata"), "method pcdata");
        check(mn.contains("element"), "method element");
        check(mn.contains("separator"), "method separator");
        check(mn.contains("occurrence"), "method occurrence");
        check(mn.contains("endGroup"), "method endGroup");
        check(mn.contains("endContentModel"), "method endContentModel");
        check(mn.contains("setDTDContentModelSource"), "method setDTDContentModelSource");
        check(mn.contains("getDTDContentModelSource"), "method getDTDContentModelSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
