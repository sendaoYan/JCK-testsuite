/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDelegate conformance: class hierarchy and members.
 * @run main XSSimpleTypeDelegateTest
 */
public class XSSimpleTypeDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDelegate", false, XSSimpleTypeDelegateTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType"), "has interface com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getWrappedXSSimpleType"), "method getWrappedXSSimpleType");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("getBounded"), "method getBounded");
        check(mn.contains("getBuiltInKind"), "method getBuiltInKind");
        check(mn.contains("getDefinedFacets"), "method getDefinedFacets");
        check(mn.contains("getFacets"), "method getFacets");
        check(mn.contains("getFinite"), "method getFinite");
        check(mn.contains("getFixedFacets"), "method getFixedFacets");
        check(mn.contains("getItemType"), "method getItemType");
        check(mn.contains("getLexicalEnumeration"), "method getLexicalEnumeration");
        check(mn.contains("getLexicalFacetValue"), "method getLexicalFacetValue");
        check(mn.contains("getLexicalPattern"), "method getLexicalPattern");
        check(mn.contains("getMemberTypes"), "method getMemberTypes");
        check(mn.contains("getMultiValueFacets"), "method getMultiValueFacets");
        check(mn.contains("getNumeric"), "method getNumeric");
        check(mn.contains("getOrdered"), "method getOrdered");
        check(mn.contains("getPrimitiveType"), "method getPrimitiveType");
        check(mn.contains("getVariety"), "method getVariety");
        check(mn.contains("isDefinedFacet"), "method isDefinedFacet");
        check(mn.contains("isFixedFacet"), "method isFixedFacet");
        check(mn.contains("derivedFrom"), "method derivedFrom");
        check(mn.contains("derivedFromType"), "method derivedFromType");
        check(mn.contains("getAnonymous"), "method getAnonymous");
        check(mn.contains("getBaseType"), "method getBaseType");
        check(mn.contains("getFinal"), "method getFinal");
        check(mn.contains("getTypeCategory"), "method getTypeCategory");
        check(mn.contains("isFinal"), "method isFinal");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("applyFacets"), "method applyFacets");
        check(mn.contains("getPrimitiveKind"), "method getPrimitiveKind");
        check(mn.contains("getWhitespace"), "method getWhitespace");
        check(mn.contains("isEqual"), "method isEqual");
        check(mn.contains("isIDType"), "method isIDType");
        check(mn.contains("validate"), "method validate");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
