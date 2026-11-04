/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.helper.C14nHelper conformance: class hierarchy and members.
 * @run main C14nHelperTest
 */
public class C14nHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.helper.C14nHelper", false, C14nHelperTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("namespaceIsRelative"), "method namespaceIsRelative");
        check(mn.contains("namespaceIsAbsolute"), "method namespaceIsAbsolute");
        check(mn.contains("assertNotRelativeNS"), "method assertNotRelativeNS");
        check(mn.contains("checkTraversability"), "method checkTraversability");
        check(mn.contains("checkForRelativeNamespace"), "method checkForRelativeNamespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
