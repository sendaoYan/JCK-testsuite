/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl conformance: class hierarchy and members.
 * @run main XSParticleDeclTest
 */
public class XSParticleDeclTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl", false, XSParticleDeclTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSParticle"), "has interface com.sun.org.apache.xerces.internal.xs.XSParticle");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("makeClone"), "method makeClone");
        check(mn.contains("emptiable"), "method emptiable");
        check(mn.contains("isEmpty"), "method isEmpty");
        check(mn.contains("minEffectiveTotalRange"), "method minEffectiveTotalRange");
        check(mn.contains("maxEffectiveTotalRange"), "method maxEffectiveTotalRange");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("appendParticle"), "method appendParticle");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getMinOccurs"), "method getMinOccurs");
        check(mn.contains("getMaxOccursUnbounded"), "method getMaxOccursUnbounded");
        check(mn.contains("getMaxOccurs"), "method getMaxOccurs");
        check(mn.contains("getTerm"), "method getTerm");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
