/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator.XPathMatcherStack conformance: class hierarchy and members.
 * @run main XPathMatcherStackTest
 */
public class XPathMatcherStackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator$XPathMatcherStack", false, XPathMatcherStackTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clear"), "method clear");
        check(mn.contains("size"), "method size");
        check(mn.contains("getMatcherCount"), "method getMatcherCount");
        check(mn.contains("addMatcher"), "method addMatcher");
        check(mn.contains("getMatcherAt"), "method getMatcherAt");
        check(mn.contains("pushContext"), "method pushContext");
        check(mn.contains("popContext"), "method popContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
