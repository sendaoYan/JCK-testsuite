/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl conformance: class hierarchy and members.
 * @run main XSAnnotationImplTest
 */
public class XSAnnotationImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl", false, XSAnnotationImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSAnnotation"), "has interface com.sun.org.apache.xerces.internal.xs.XSAnnotation");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("writeAnnotation"), "method writeAnnotation");
        check(mn.contains("getAnnotationString"), "method getAnnotationString");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
