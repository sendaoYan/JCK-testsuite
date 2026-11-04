/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.utils.XMLSecurityManager conformance: class hierarchy and members.
 * @run main XMLSecurityManagerTest
 */
public class XMLSecurityManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.utils.XMLSecurityManager", false, XMLSecurityManagerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setSecureProcessing"), "method setSecureProcessing");
        check(mn.contains("isSecureProcessing"), "method isSecureProcessing");
        check(mn.contains("setLimit"), "method setLimit");
        check(mn.contains("getLimitAsString"), "method getLimitAsString");
        check(mn.contains("getLimit"), "method getLimit");
        check(mn.contains("getLimitValueAsString"), "method getLimitValueAsString");
        check(mn.contains("getLimitValueByIndex"), "method getLimitValueByIndex");
        check(mn.contains("getState"), "method getState");
        check(mn.contains("getStateLiteral"), "method getStateLiteral");
        check(mn.contains("getIndex"), "method getIndex");
        check(mn.contains("isNoLimit"), "method isNoLimit");
        check(mn.contains("isOverLimit"), "method isOverLimit");
        check(mn.contains("debugPrint"), "method debugPrint");
        check(mn.contains("isSet"), "method isSet");
        check(mn.contains("printEntityCountInfo"), "method printEntityCountInfo");
        check(mn.contains("printWarning"), "method printWarning");
        check(mn.contains("convert"), "method convert");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
