/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.ListingErrorHandler conformance: class hierarchy and members.
 * @run main ListingErrorHandlerTest
 */
public class ListingErrorHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.ListingErrorHandler", false, ListingErrorHandlerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.xml.sax.ErrorHandler"), "has interface org.xml.sax.ErrorHandler");
        check(hasInterface(c, "javax.xml.transform.ErrorListener"), "has interface javax.xml.transform.ErrorListener");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("warning"), "method warning");
        check(mn.contains("error"), "method error");
        check(mn.contains("fatalError"), "method fatalError");
        check(mn.contains("logExceptionLocation"), "method logExceptionLocation");
        check(mn.contains("logSourceLine"), "method logSourceLine");
        check(mn.contains("getSourceLine"), "method getSourceLine");
        check(mn.contains("setThrowOnWarning"), "method setThrowOnWarning");
        check(mn.contains("getThrowOnWarning"), "method getThrowOnWarning");
        check(mn.contains("setThrowOnError"), "method setThrowOnError");
        check(mn.contains("getThrowOnError"), "method getThrowOnError");
        check(mn.contains("setThrowOnFatalError"), "method setThrowOnFatalError");
        check(mn.contains("getThrowOnFatalError"), "method getThrowOnFatalError");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
