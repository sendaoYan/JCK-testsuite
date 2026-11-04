/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.I18n conformance: class hierarchy and members.
 * @run main I18nTest
 */
public class I18nTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.I18n", false, I18nTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("translate"), "method translate");
        check(mn.contains("getExceptionMessage"), "method getExceptionMessage");
        check(mn.contains("init"), "method init");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
