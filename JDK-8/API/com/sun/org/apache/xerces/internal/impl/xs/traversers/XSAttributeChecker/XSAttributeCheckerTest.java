/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.traversers.XSAttributeChecker conformance: class hierarchy and members.
 * @run main XSAttributeCheckerTest
 */
public class XSAttributeCheckerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.traversers.XSAttributeChecker", false, XSAttributeCheckerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("reset"), "method reset");
        check(mn.contains("checkAttributes"), "method checkAttributes");
        check(mn.contains("reportSchemaFatalError"), "method reportSchemaFatalError");
        check(mn.contains("reportSchemaError"), "method reportSchemaError");
        check(mn.contains("checkNonSchemaAttributes"), "method checkNonSchemaAttributes");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("getAvailableArray"), "method getAvailableArray");
        check(mn.contains("returnAttrArray"), "method returnAttrArray");
        check(mn.contains("resolveNamespace"), "method resolveNamespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
