/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator conformance: abstract class hierarchy and members.
 * @run main TypeValidatorTest
 */
public class TypeValidatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator", false, TypeValidatorTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAllowedFacets"), "method getAllowedFacets");
        check(mn.contains("getActualValue"), "method getActualValue");
        check(mn.contains("checkExtraRules"), "method checkExtraRules");
        check(mn.contains("isIdentical"), "method isIdentical");
        check(mn.contains("compare"), "method compare");
        check(mn.contains("getDataLength"), "method getDataLength");
        check(mn.contains("getTotalDigits"), "method getTotalDigits");
        check(mn.contains("getFractionDigits"), "method getFractionDigits");
        check(mn.contains("isDigit"), "method isDigit");
        check(mn.contains("getDigit"), "method getDigit");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
