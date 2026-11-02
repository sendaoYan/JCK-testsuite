/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xs.ShortList conformance: interface hierarchy and members.
 * @run main ShortListTest
 */
public class ShortListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xs.ShortList", false, ShortListTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.List"), "has interface java.util.List");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("contains"), "method contains");
        check(mn.contains("item"), "method item");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
