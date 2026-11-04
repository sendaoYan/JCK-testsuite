/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSAttributeUseImpl conformance: class hierarchy and members.
 * @run main XSAttributeUseImplTest
 */
public class XSAttributeUseImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSAttributeUseImpl", false, XSAttributeUseImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSAttributeUse"), "has interface com.sun.org.apache.xerces.internal.xs.XSAttributeUse");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getRequired"), "method getRequired");
        check(mn.contains("getAttrDeclaration"), "method getAttrDeclaration");
        check(mn.contains("getConstraintType"), "method getConstraintType");
        check(mn.contains("getConstraintValue"), "method getConstraintValue");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        check(mn.contains("getActualVC"), "method getActualVC");
        check(mn.contains("getActualVCType"), "method getActualVCType");
        check(mn.contains("getItemValueTypes"), "method getItemValueTypes");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
