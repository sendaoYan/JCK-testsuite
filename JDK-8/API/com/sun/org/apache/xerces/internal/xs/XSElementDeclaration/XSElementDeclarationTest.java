/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xs.XSElementDeclaration conformance: interface hierarchy and members.
 * @run main XSElementDeclarationTest
 */
public class XSElementDeclarationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xs.XSElementDeclaration", false, XSElementDeclarationTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSTerm"), "has interface com.sun.org.apache.xerces.internal.xs.XSTerm");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getTypeDefinition"), "method getTypeDefinition");
        check(mn.contains("getScope"), "method getScope");
        check(mn.contains("getEnclosingCTDefinition"), "method getEnclosingCTDefinition");
        check(mn.contains("getConstraintType"), "method getConstraintType");
        check(mn.contains("getConstraintValue"), "method getConstraintValue");
        check(mn.contains("getActualVC"), "method getActualVC");
        check(mn.contains("getActualVCType"), "method getActualVCType");
        check(mn.contains("getItemValueTypes"), "method getItemValueTypes");
        check(mn.contains("getNillable"), "method getNillable");
        check(mn.contains("getIdentityConstraints"), "method getIdentityConstraints");
        check(mn.contains("getSubstitutionGroupAffiliation"), "method getSubstitutionGroupAffiliation");
        check(mn.contains("isSubstitutionGroupExclusion"), "method isSubstitutionGroupExclusion");
        check(mn.contains("getSubstitutionGroupExclusions"), "method getSubstitutionGroupExclusions");
        check(mn.contains("isDisallowedSubstitution"), "method isDisallowedSubstitution");
        check(mn.contains("getDisallowedSubstitutions"), "method getDisallowedSubstitutions");
        check(mn.contains("getAbstract"), "method getAbstract");
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
