/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.ObjectPool conformance: class hierarchy and members.
 * @run main ObjectPoolTest
 */
public class ObjectPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.ObjectPool", false, ObjectPoolTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "java.io.Serializable"), "has interface java.io.Serializable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getInstanceIfFree"), "method getInstanceIfFree");
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("freeInstance"), "method freeInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
