/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.util.HexBin conformance: class hierarchy and members.
 * @run main HexBinTest
 */
public class HexBinTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.HexBin", false, HexBinTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("encode"), "method encode");
        check(mn.contains("decode"), "method decode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
