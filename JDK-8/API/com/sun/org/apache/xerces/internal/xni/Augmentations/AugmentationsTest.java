/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.Augmentations conformance: interface hierarchy and members.
 * @run main AugmentationsTest
 */
public class AugmentationsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.Augmentations", false, AugmentationsTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("putItem"), "method putItem");
        check(mn.contains("getItem"), "method getItem");
        check(mn.contains("removeItem"), "method removeItem");
        check(mn.contains("keys"), "method keys");
        check(mn.contains("removeAllItems"), "method removeAllItems");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
