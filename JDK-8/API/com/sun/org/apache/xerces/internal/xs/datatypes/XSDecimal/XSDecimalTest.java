/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xs.datatypes.XSDecimal conformance: interface hierarchy and members.
 * @run main XSDecimalTest
 */
public class XSDecimalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xs.datatypes.XSDecimal", false, XSDecimalTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBigDecimal"), "method getBigDecimal");
        check(mn.contains("getBigInteger"), "method getBigInteger");
        check(mn.contains("getLong"), "method getLong");
        check(mn.contains("getInt"), "method getInt");
        check(mn.contains("getShort"), "method getShort");
        check(mn.contains("getByte"), "method getByte");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
