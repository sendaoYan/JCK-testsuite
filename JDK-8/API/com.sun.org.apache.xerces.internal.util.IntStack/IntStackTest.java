/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.IntStack conformance: class hierarchy and members.
 * @run main IntStackTest
 */
public class IntStackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.IntStack", false, IntStackTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("size"), "method size");
        check(mn.contains("push"), "method push");
        check(mn.contains("peek"), "method peek");
        check(mn.contains("elementAt"), "method elementAt");
        check(mn.contains("pop"), "method pop");
        check(mn.contains("clear"), "method clear");
        check(mn.contains("print"), "method print");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
