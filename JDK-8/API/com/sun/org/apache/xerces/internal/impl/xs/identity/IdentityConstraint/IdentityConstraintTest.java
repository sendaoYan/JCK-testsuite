/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint conformance: abstract class hierarchy and members.
 * @run main IdentityConstraintTest
 */
public class IdentityConstraintTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint", false, IdentityConstraintTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSIDCDefinition"), "has interface com.sun.org.apache.xerces.internal.xs.XSIDCDefinition");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getIdentityConstraintName"), "method getIdentityConstraintName");
        check(mn.contains("setSelector"), "method setSelector");
        check(mn.contains("getSelector"), "method getSelector");
        check(mn.contains("addField"), "method addField");
        check(mn.contains("getFieldCount"), "method getFieldCount");
        check(mn.contains("getFieldAt"), "method getFieldAt");
        check(mn.contains("getElementName"), "method getElementName");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("resize"), "method resize");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getCategory"), "method getCategory");
        check(mn.contains("getSelectorStr"), "method getSelectorStr");
        check(mn.contains("getFieldStrs"), "method getFieldStrs");
        check(mn.contains("getRefKey"), "method getRefKey");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        check(mn.contains("addAnnotation"), "method addAnnotation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
