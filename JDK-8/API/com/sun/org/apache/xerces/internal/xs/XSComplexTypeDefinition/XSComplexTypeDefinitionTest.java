/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition conformance: interface hierarchy and members.
 * @run main XSComplexTypeDefinitionTest
 */
public class XSComplexTypeDefinitionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition", false, XSComplexTypeDefinitionTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSTypeDefinition"), "has interface com.sun.org.apache.xerces.internal.xs.XSTypeDefinition");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDerivationMethod"), "method getDerivationMethod");
        check(mn.contains("getAbstract"), "method getAbstract");
        check(mn.contains("getAttributeUses"), "method getAttributeUses");
        check(mn.contains("getAttributeWildcard"), "method getAttributeWildcard");
        check(mn.contains("getContentType"), "method getContentType");
        check(mn.contains("getSimpleType"), "method getSimpleType");
        check(mn.contains("getParticle"), "method getParticle");
        check(mn.contains("isProhibitedSubstitution"), "method isProhibitedSubstitution");
        check(mn.contains("getProhibitedSubstitutions"), "method getProhibitedSubstitutions");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
