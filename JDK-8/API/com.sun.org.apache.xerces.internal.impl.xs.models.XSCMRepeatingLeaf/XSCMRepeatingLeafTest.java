/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.models.XSCMRepeatingLeaf conformance: class hierarchy and members.
 * @run main XSCMRepeatingLeafTest
 */
public class XSCMRepeatingLeafTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.models.XSCMRepeatingLeaf", false, XSCMRepeatingLeafTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.models.XSCMLeaf"), "superclass com.sun.org.apache.xerces.internal.impl.xs.models.XSCMLeaf");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getMinOccurs"), "method getMinOccurs");
        check(mn.contains("getMaxOccurs"), "method getMaxOccurs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
