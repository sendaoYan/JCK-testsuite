/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.io.UCSReader conformance: class hierarchy and members.
 * @run main UCSReaderTest
 */
public class UCSReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.io.UCSReader", false, UCSReaderTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.io.Reader"), "superclass java.io.Reader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("read"), "method read");
        check(mn.contains("skip"), "method skip");
        check(mn.contains("ready"), "method ready");
        check(mn.contains("markSupported"), "method markSupported");
        check(mn.contains("mark"), "method mark");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("close"), "method close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
