/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.DefaultErrorHandler conformance: class hierarchy and members.
 * @run main DefaultErrorHandlerTest
 */
public class DefaultErrorHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.DefaultErrorHandler", false, DefaultErrorHandlerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.xml.sax.ErrorHandler"), "has interface org.xml.sax.ErrorHandler");
        check(hasInterface(c, "javax.xml.transform.ErrorListener"), "has interface javax.xml.transform.ErrorListener");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("warning"), "method warning");
        check(mn.contains("error"), "method error");
        check(mn.contains("fatalError"), "method fatalError");
        check(mn.contains("ensureLocationSet"), "method ensureLocationSet");
        check(mn.contains("printLocation"), "method printLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
