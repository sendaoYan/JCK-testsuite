/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSGroupDecl conformance: class hierarchy and members.
 * @run main XSGroupDeclTest
 */
public class XSGroupDeclTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSGroupDecl", false, XSGroupDeclTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSModelGroupDefinition"), "has interface com.sun.org.apache.xerces.internal.xs.XSModelGroupDefinition");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getModelGroup"), "method getModelGroup");
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        check(mn.contains("setNamespaceItem"), "method setNamespaceItem");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
