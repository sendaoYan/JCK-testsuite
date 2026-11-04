/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter_sv conformance: class hierarchy and members.
 * @run main XMLMessageFormatter_svTest
 */
public class XMLMessageFormatter_svTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter_sv", false, XMLMessageFormatter_svTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.util.MessageFormatter"), "has interface com.sun.org.apache.xerces.internal.util.MessageFormatter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("formatMessage"), "method formatMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
