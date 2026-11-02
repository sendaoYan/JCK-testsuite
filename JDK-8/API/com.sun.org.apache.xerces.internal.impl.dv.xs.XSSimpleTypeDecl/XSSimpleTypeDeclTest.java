/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl conformance: class hierarchy and members.
 * @run main XSSimpleTypeDeclTest
 */
public class XSSimpleTypeDeclTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl", false, XSSimpleTypeDeclTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType"), "has interface com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType");
        check(hasInterface(c, "org.w3c.dom.TypeInfo"), "has interface org.w3c.dom.TypeInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getGDVs"), "method getGDVs");
        check(mn.contains("setDVs"), "method setDVs");
        check(mn.contains("setRestrictionValues"), "method setRestrictionValues");
        check(mn.contains("setListValues"), "method setListValues");
        check(mn.contains("setUnionValues"), "method setUnionValues");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getTypeCategory"), "method getTypeCategory");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getTypeName"), "method getTypeName");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getFinal"), "method getFinal");
        check(mn.contains("isFinal"), "method isFinal");
        check(mn.contains("getBaseType"), "method getBaseType");
        check(mn.contains("getAnonymous"), "method getAnonymous");
        check(mn.contains("getVariety"), "method getVariety");
        check(mn.contains("isIDType"), "method isIDType");
        check(mn.contains("getWhitespace"), "method getWhitespace");
        check(mn.contains("getPrimitiveKind"), "method getPrimitiveKind");
        check(mn.contains("getBuiltInKind"), "method getBuiltInKind");
        check(mn.contains("getPrimitiveType"), "method getPrimitiveType");
        check(mn.contains("getItemType"), "method getItemType");
        check(mn.contains("getMemberTypes"), "method getMemberTypes");
        check(mn.contains("applyFacets"), "method applyFacets");
        check(mn.contains("applyFacets1"), "method applyFacets1");
        check(mn.contains("validate"), "method validate");
        check(mn.contains("getActualEnumValue"), "method getActualEnumValue");
        check(mn.contains("validateWithInfo"), "method validateWithInfo");
        check(mn.contains("isEqual"), "method isEqual");
        check(mn.contains("isIdentical"), "method isIdentical");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("reportError"), "method reportError");
        check(mn.contains("getOrdered"), "method getOrdered");
        check(mn.contains("getBounded"), "method getBounded");
        check(mn.contains("getFinite"), "method getFinite");
        check(mn.contains("getNumeric"), "method getNumeric");
        check(mn.contains("isDefinedFacet"), "method isDefinedFacet");
        check(mn.contains("getDefinedFacets"), "method getDefinedFacets");
        check(mn.contains("isFixedFacet"), "method isFixedFacet");
        check(mn.contains("getFixedFacets"), "method getFixedFacets");
        check(mn.contains("getLexicalFacetValue"), "method getLexicalFacetValue");
        check(mn.contains("getLexicalEnumeration"), "method getLexicalEnumeration");
        check(mn.contains("getActualEnumeration"), "method getActualEnumeration");
        check(mn.contains("getEnumerationItemTypeList"), "method getEnumerationItemTypeList");
        check(mn.contains("getEnumerationTypeList"), "method getEnumerationTypeList");
        check(mn.contains("getLexicalPattern"), "method getLexicalPattern");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("derivedFromType"), "method derivedFromType");
        check(mn.contains("derivedFrom"), "method derivedFrom");
        check(mn.contains("isDOMDerivedFrom"), "method isDOMDerivedFrom");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        check(mn.contains("setNamespaceItem"), "method setNamespaceItem");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("getFacets"), "method getFacets");
        check(mn.contains("getMultiValueFacets"), "method getMultiValueFacets");
        check(mn.contains("getMinInclusiveValue"), "method getMinInclusiveValue");
        check(mn.contains("getMinExclusiveValue"), "method getMinExclusiveValue");
        check(mn.contains("getMaxInclusiveValue"), "method getMaxInclusiveValue");
        check(mn.contains("getMaxExclusiveValue"), "method getMaxExclusiveValue");
        check(mn.contains("setAnonymous"), "method setAnonymous");
        check(mn.contains("getTypeNamespace"), "method getTypeNamespace");
        check(mn.contains("isDerivedFrom"), "method isDerivedFrom");
        check(mn.contains("access$100"), "method access$100");
        check(mn.contains("access$200"), "method access$200");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
