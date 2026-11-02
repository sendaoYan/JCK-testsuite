/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.validation.ValidationState conformance: class hierarchy and members.
 * @run main ValidationStateTest
 */
public class ValidationStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.validation.ValidationState", false, ValidationStateTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.dv.ValidationContext"), "has interface com.sun.org.apache.xerces.internal.impl.dv.ValidationContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setExtraChecking"), "method setExtraChecking");
        check(mn.contains("setFacetChecking"), "method setFacetChecking");
        check(mn.contains("setNormalizationRequired"), "method setNormalizationRequired");
        check(mn.contains("setUsingNamespaces"), "method setUsingNamespaces");
        check(mn.contains("setEntityState"), "method setEntityState");
        check(mn.contains("setNamespaceSupport"), "method setNamespaceSupport");
        check(mn.contains("setSymbolTable"), "method setSymbolTable");
        check(mn.contains("checkIDRefID"), "method checkIDRefID");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("resetIDTables"), "method resetIDTables");
        check(mn.contains("needExtraChecking"), "method needExtraChecking");
        check(mn.contains("needFacetChecking"), "method needFacetChecking");
        check(mn.contains("needToNormalize"), "method needToNormalize");
        check(mn.contains("useNamespaces"), "method useNamespaces");
        check(mn.contains("isEntityDeclared"), "method isEntityDeclared");
        check(mn.contains("isEntityUnparsed"), "method isEntityUnparsed");
        check(mn.contains("isIdDeclared"), "method isIdDeclared");
        check(mn.contains("addId"), "method addId");
        check(mn.contains("addIdRef"), "method addIdRef");
        check(mn.contains("getSymbol"), "method getSymbol");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("getLocale"), "method getLocale");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
