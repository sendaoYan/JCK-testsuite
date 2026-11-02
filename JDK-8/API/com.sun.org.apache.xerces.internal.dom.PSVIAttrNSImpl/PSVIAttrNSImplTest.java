/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.PSVIAttrNSImpl conformance: class hierarchy and members.
 * @run main PSVIAttrNSImplTest
 */
public class PSVIAttrNSImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.PSVIAttrNSImpl", false, PSVIAttrNSImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.AttrNSImpl"), "superclass com.sun.org.apache.xerces.internal.dom.AttrNSImpl");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.AttributePSVI"), "has interface com.sun.org.apache.xerces.internal.xs.AttributePSVI");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getSchemaDefault"), "method getSchemaDefault");
        check(mn.contains("getSchemaNormalizedValue"), "method getSchemaNormalizedValue");
        check(mn.contains("getIsSchemaSpecified"), "method getIsSchemaSpecified");
        check(mn.contains("getValidationAttempted"), "method getValidationAttempted");
        check(mn.contains("getValidity"), "method getValidity");
        check(mn.contains("getErrorCodes"), "method getErrorCodes");
        check(mn.contains("getValidationContext"), "method getValidationContext");
        check(mn.contains("getTypeDefinition"), "method getTypeDefinition");
        check(mn.contains("getMemberTypeDefinition"), "method getMemberTypeDefinition");
        check(mn.contains("getAttributeDeclaration"), "method getAttributeDeclaration");
        check(mn.contains("setPSVI"), "method setPSVI");
        check(mn.contains("getActualNormalizedValue"), "method getActualNormalizedValue");
        check(mn.contains("getActualNormalizedValueType"), "method getActualNormalizedValueType");
        check(mn.contains("getItemValueTypes"), "method getItemValueTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
