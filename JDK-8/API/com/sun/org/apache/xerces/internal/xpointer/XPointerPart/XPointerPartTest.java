/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xpointer.XPointerPart conformance: interface hierarchy and members.
 * @run main XPointerPartTest
 */
public class XPointerPartTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xpointer.XPointerPart", false, XPointerPartTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("parseXPointer"), "method parseXPointer");
        check(mn.contains("resolveXPointer"), "method resolveXPointer");
        check(mn.contains("isFragmentResolved"), "method isFragmentResolved");
        check(mn.contains("isChildFragmentResolved"), "method isChildFragmentResolved");
        check(mn.contains("getSchemeName"), "method getSchemeName");
        check(mn.contains("getSchemeData"), "method getSchemeData");
        check(mn.contains("setSchemeName"), "method setSchemeName");
        check(mn.contains("setSchemeData"), "method setSchemeData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
