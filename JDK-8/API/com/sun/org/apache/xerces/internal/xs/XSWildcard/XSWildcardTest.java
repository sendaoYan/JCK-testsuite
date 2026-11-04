/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xs.XSWildcard conformance: interface hierarchy and members.
 * @run main XSWildcardTest
 */
public class XSWildcardTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xs.XSWildcard", false, XSWildcardTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSTerm"), "has interface com.sun.org.apache.xerces.internal.xs.XSTerm");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getConstraintType"), "method getConstraintType");
        check(mn.contains("getNsConstraintList"), "method getNsConstraintList");
        check(mn.contains("getProcessContents"), "method getProcessContents");
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
