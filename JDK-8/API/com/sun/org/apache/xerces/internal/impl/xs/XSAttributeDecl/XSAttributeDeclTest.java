/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSAttributeDecl conformance: class hierarchy and members.
 * @run main XSAttributeDeclTest
 */
public class XSAttributeDeclTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSAttributeDecl", false, XSAttributeDeclTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration"), "has interface com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setValues"), "method setValues");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getTypeDefinition"), "method getTypeDefinition");
        check(mn.contains("getScope"), "method getScope");
        check(mn.contains("getEnclosingCTDefinition"), "method getEnclosingCTDefinition");
        check(mn.contains("getConstraintType"), "method getConstraintType");
        check(mn.contains("getConstraintValue"), "method getConstraintValue");
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("getValInfo"), "method getValInfo");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        check(mn.contains("setNamespaceItem"), "method setNamespaceItem");
        check(mn.contains("getActualVC"), "method getActualVC");
        check(mn.contains("getActualVCType"), "method getActualVCType");
        check(mn.contains("getItemValueTypes"), "method getItemValueTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
