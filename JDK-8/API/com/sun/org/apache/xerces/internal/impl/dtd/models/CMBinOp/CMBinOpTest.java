/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dtd.models.CMBinOp conformance: class hierarchy and members.
 * @run main CMBinOpTest
 */
public class CMBinOpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dtd.models.CMBinOp", false, CMBinOpTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode"), "superclass com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLeft"), "method getLeft");
        check(mn.contains("getRight"), "method getRight");
        check(mn.contains("isNullable"), "method isNullable");
        check(mn.contains("calcFirstPos"), "method calcFirstPos");
        check(mn.contains("calcLastPos"), "method calcLastPos");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
