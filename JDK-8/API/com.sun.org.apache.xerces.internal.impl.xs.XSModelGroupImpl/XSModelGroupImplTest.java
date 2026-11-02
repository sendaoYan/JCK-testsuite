/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSModelGroupImpl conformance: class hierarchy and members.
 * @run main XSModelGroupImplTest
 */
public class XSModelGroupImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSModelGroupImpl", false, XSModelGroupImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSModelGroup"), "has interface com.sun.org.apache.xerces.internal.xs.XSModelGroup");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isEmpty"), "method isEmpty");
        check(mn.contains("minEffectiveTotalRange"), "method minEffectiveTotalRange");
        check(mn.contains("maxEffectiveTotalRange"), "method maxEffectiveTotalRange");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getCompositor"), "method getCompositor");
        check(mn.contains("getParticles"), "method getParticles");
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
