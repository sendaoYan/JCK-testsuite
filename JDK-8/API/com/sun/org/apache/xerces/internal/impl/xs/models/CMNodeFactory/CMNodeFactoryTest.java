/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.models.CMNodeFactory conformance: class hierarchy and members.
 * @run main CMNodeFactoryTest
 */
public class CMNodeFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.models.CMNodeFactory", false, CMNodeFactoryTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getCMLeafNode"), "method getCMLeafNode");
        check(mn.contains("getCMRepeatingLeafNode"), "method getCMRepeatingLeafNode");
        check(mn.contains("getCMUniOpNode"), "method getCMUniOpNode");
        check(mn.contains("getCMBinOpNode"), "method getCMBinOpNode");
        check(mn.contains("nodeCountCheck"), "method nodeCountCheck");
        check(mn.contains("resetNodeCount"), "method resetNodeCount");
        check(mn.contains("setProperty"), "method setProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
