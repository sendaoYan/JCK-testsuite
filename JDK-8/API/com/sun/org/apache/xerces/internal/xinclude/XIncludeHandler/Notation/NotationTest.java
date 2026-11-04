/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler.Notation conformance: class hierarchy and members.
 * @run main NotationTest
 */
public class NotationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler$Notation", false, NotationTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("equals"), "method equals");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("isDuplicate"), "method isDuplicate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
