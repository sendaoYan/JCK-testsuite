/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.ElementPSVImpl conformance: class hierarchy and members.
 * @run main ElementPSVImplTest
 */
public class ElementPSVImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.ElementPSVImpl", false, ElementPSVImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.ElementPSVI"), "has interface com.sun.org.apache.xerces.internal.xs.ElementPSVI");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getSchemaDefault"), "method getSchemaDefault");
        check(mn.contains("getSchemaNormalizedValue"), "method getSchemaNormalizedValue");
        check(mn.contains("getIsSchemaSpecified"), "method getIsSchemaSpecified");
        check(mn.contains("getValidationAttempted"), "method getValidationAttempted");
        check(mn.contains("getValidity"), "method getValidity");
        check(mn.contains("getErrorCodes"), "method getErrorCodes");
        check(mn.contains("getValidationContext"), "method getValidationContext");
        check(mn.contains("getNil"), "method getNil");
        check(mn.contains("getNotation"), "method getNotation");
        check(mn.contains("getTypeDefinition"), "method getTypeDefinition");
        check(mn.contains("getMemberTypeDefinition"), "method getMemberTypeDefinition");
        check(mn.contains("getElementDeclaration"), "method getElementDeclaration");
        check(mn.contains("getSchemaInformation"), "method getSchemaInformation");
        check(mn.contains("getActualNormalizedValue"), "method getActualNormalizedValue");
        check(mn.contains("getActualNormalizedValueType"), "method getActualNormalizedValueType");
        check(mn.contains("getItemValueTypes"), "method getItemValueTypes");
        check(mn.contains("reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
