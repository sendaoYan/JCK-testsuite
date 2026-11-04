/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.utils.SecuritySupport conformance: class hierarchy and members.
 * @run main SecuritySupportTest
 */
public class SecuritySupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.utils.SecuritySupport", false, SecuritySupportTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("getContextClassLoader"), "method getContextClassLoader");
        check(mn.contains("getSystemClassLoader"), "method getSystemClassLoader");
        check(mn.contains("getParentClassLoader"), "method getParentClassLoader");
        check(mn.contains("getSystemProperty"), "method getSystemProperty");
        check(mn.contains("getFileInputStream"), "method getFileInputStream");
        check(mn.contains("getResourceBundle"), "method getResourceBundle");
        check(mn.contains("getFileExists"), "method getFileExists");
        check(mn.contains("getLastModified"), "method getLastModified");
        check(mn.contains("sanitizePath"), "method sanitizePath");
        check(mn.contains("checkAccess"), "method checkAccess");
        check(mn.contains("getJAXPSystemProperty"), "method getJAXPSystemProperty");
        check(mn.contains("readJAXPProperty"), "method readJAXPProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
