/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.utils.ConfigurationError conformance: class hierarchy and members.
 * @run main ConfigurationErrorTest
 */
public class ConfigurationErrorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.utils.ConfigurationError", false, ConfigurationErrorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Error"), "superclass java.lang.Error");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getException"), "method getException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
