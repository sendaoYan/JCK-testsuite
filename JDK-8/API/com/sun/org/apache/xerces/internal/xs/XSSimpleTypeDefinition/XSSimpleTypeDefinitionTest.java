/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition conformance: interface hierarchy and members.
 * @run main XSSimpleTypeDefinitionTest
 */
public class XSSimpleTypeDefinitionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition", false, XSSimpleTypeDefinitionTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSTypeDefinition"), "has interface com.sun.org.apache.xerces.internal.xs.XSTypeDefinition");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getVariety"), "method getVariety");
        check(mn.contains("getPrimitiveType"), "method getPrimitiveType");
        check(mn.contains("getBuiltInKind"), "method getBuiltInKind");
        check(mn.contains("getItemType"), "method getItemType");
        check(mn.contains("getMemberTypes"), "method getMemberTypes");
        check(mn.contains("getDefinedFacets"), "method getDefinedFacets");
        check(mn.contains("isDefinedFacet"), "method isDefinedFacet");
        check(mn.contains("getFixedFacets"), "method getFixedFacets");
        check(mn.contains("isFixedFacet"), "method isFixedFacet");
        check(mn.contains("getLexicalFacetValue"), "method getLexicalFacetValue");
        check(mn.contains("getLexicalEnumeration"), "method getLexicalEnumeration");
        check(mn.contains("getLexicalPattern"), "method getLexicalPattern");
        check(mn.contains("getOrdered"), "method getOrdered");
        check(mn.contains("getFinite"), "method getFinite");
        check(mn.contains("getBounded"), "method getBounded");
        check(mn.contains("getNumeric"), "method getNumeric");
        check(mn.contains("getFacets"), "method getFacets");
        check(mn.contains("getMultiValueFacets"), "method getMultiValueFacets");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
