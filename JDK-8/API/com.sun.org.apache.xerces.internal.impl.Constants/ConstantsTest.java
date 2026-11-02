/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.Constants conformance: class hierarchy and members.
 * @run main ConstantsTest
 */
public class ConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.Constants", false, ConstantsTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getSAXFeatures"), "method getSAXFeatures");
        check(mn.contains("getSAXProperties"), "method getSAXProperties");
        check(mn.contains("getXercesFeatures"), "method getXercesFeatures");
        check(mn.contains("getXercesProperties"), "method getXercesProperties");
        check(mn.contains("isJavaVersionAtLeast"), "method isJavaVersionAtLeast");
        check(mn.contains("main"), "method main");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
