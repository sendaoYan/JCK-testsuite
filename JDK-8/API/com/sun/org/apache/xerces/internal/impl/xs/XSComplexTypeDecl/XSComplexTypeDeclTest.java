/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl conformance: class hierarchy and members.
 * @run main XSComplexTypeDeclTest
 */
public class XSComplexTypeDeclTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl", false, XSComplexTypeDeclTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition"), "has interface com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition");
        check(hasInterface(c, "org.w3c.dom.TypeInfo"), "has interface org.w3c.dom.TypeInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setValues"), "method setValues");
        check(mn.contains("setName"), "method setName");
        check(mn.contains("getTypeCategory"), "method getTypeCategory");
        check(mn.contains("getTypeName"), "method getTypeName");
        check(mn.contains("getFinalSet"), "method getFinalSet");
        check(mn.contains("getTargetNamespace"), "method getTargetNamespace");
        check(mn.contains("containsTypeID"), "method containsTypeID");
        check(mn.contains("setIsAbstractType"), "method setIsAbstractType");
        check(mn.contains("setContainsTypeID"), "method setContainsTypeID");
        check(mn.contains("setIsAnonymous"), "method setIsAnonymous");
        check(mn.contains("getContentModel"), "method getContentModel");
        check(mn.contains("getAttrGrp"), "method getAttrGrp");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("appendTypeInfo"), "method appendTypeInfo");
        check(mn.contains("derivedFromType"), "method derivedFromType");
        check(mn.contains("derivedFrom"), "method derivedFrom");
        check(mn.contains("isDOMDerivedFrom"), "method isDOMDerivedFrom");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getAnonymous"), "method getAnonymous");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getBaseType"), "method getBaseType");
        check(mn.contains("getDerivationMethod"), "method getDerivationMethod");
        check(mn.contains("isFinal"), "method isFinal");
        check(mn.contains("getFinal"), "method getFinal");
        check(mn.contains("getAbstract"), "method getAbstract");
        check(mn.contains("getAttributeUses"), "method getAttributeUses");
        check(mn.contains("getAttributeWildcard"), "method getAttributeWildcard");
        check(mn.contains("getContentType"), "method getContentType");
        check(mn.contains("getSimpleType"), "method getSimpleType");
        check(mn.contains("getParticle"), "method getParticle");
        check(mn.contains("isProhibitedSubstitution"), "method isProhibitedSubstitution");
        check(mn.contains("getProhibitedSubstitutions"), "method getProhibitedSubstitutions");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        check(mn.contains("setNamespaceItem"), "method setNamespaceItem");
        check(mn.contains("getAttributeUse"), "method getAttributeUse");
        check(mn.contains("getTypeNamespace"), "method getTypeNamespace");
        check(mn.contains("isDerivedFrom"), "method isDerivedFrom");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
