/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.models.XSEmptyCM conformance: class hierarchy and members.
 * @run main XSEmptyCMTest
 */
public class XSEmptyCMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.models.XSEmptyCM", false, XSEmptyCMTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator"), "has interface com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startContentModel"), "method startContentModel");
        check(mn.contains("oneTransition"), "method oneTransition");
        check(mn.contains("endContentModel"), "method endContentModel");
        check(mn.contains("checkUniqueParticleAttribution"), "method checkUniqueParticleAttribution");
        check(mn.contains("whatCanGoHere"), "method whatCanGoHere");
        check(mn.contains("checkMinMaxBounds"), "method checkMinMaxBounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
