/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSDeclarationPool conformance: class hierarchy and members.
 * @run main XSDeclarationPoolTest
 */
public class XSDeclarationPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSDeclarationPool", false, XSDeclarationPoolTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setDVFactory"), "method setDVFactory");
        check(mn.contains("getElementDecl"), "method getElementDecl");
        check(mn.contains("getAttributeDecl"), "method getAttributeDecl");
        check(mn.contains("getAttributeUse"), "method getAttributeUse");
        check(mn.contains("getComplexTypeDecl"), "method getComplexTypeDecl");
        check(mn.contains("getSimpleTypeDecl"), "method getSimpleTypeDecl");
        check(mn.contains("getParticleDecl"), "method getParticleDecl");
        check(mn.contains("getModelGroup"), "method getModelGroup");
        check(mn.contains("reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
