/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.ValidationContext conformance: interface hierarchy and members.
 * @run main ValidationContextTest
 */
public class ValidationContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.ValidationContext", false, ValidationContextTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("needFacetChecking"), "method needFacetChecking");
        check(mn.contains("needExtraChecking"), "method needExtraChecking");
        check(mn.contains("needToNormalize"), "method needToNormalize");
        check(mn.contains("useNamespaces"), "method useNamespaces");
        check(mn.contains("isEntityDeclared"), "method isEntityDeclared");
        check(mn.contains("isEntityUnparsed"), "method isEntityUnparsed");
        check(mn.contains("isIdDeclared"), "method isIdDeclared");
        check(mn.contains("addId"), "method addId");
        check(mn.contains("addIdRef"), "method addIdRef");
        check(mn.contains("getSymbol"), "method getSymbol");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("getLocale"), "method getLocale");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
