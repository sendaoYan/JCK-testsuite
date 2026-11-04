/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.TextImpl conformance: class hierarchy and members.
 * @run main TextImplTest
 */
public class TextImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.TextImpl", false, TextImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultText"), "superclass com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultText");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getParentNode"), "method getParentNode");
        check(mn.contains("getPreviousSibling"), "method getPreviousSibling");
        check(mn.contains("getNextSibling"), "method getNextSibling");
        check(mn.contains("getData"), "method getData");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("substringData"), "method substringData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
