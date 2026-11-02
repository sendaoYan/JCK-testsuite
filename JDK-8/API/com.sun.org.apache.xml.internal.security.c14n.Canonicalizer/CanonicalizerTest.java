/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.Canonicalizer conformance: class hierarchy and members.
 * @run main CanonicalizerTest
 */
public class CanonicalizerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.Canonicalizer", false, CanonicalizerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("register"), "method register");
        check(mn.contains("registerDefaultAlgorithms"), "method registerDefaultAlgorithms");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("getIncludeComments"), "method getIncludeComments");
        check(mn.contains("canonicalize"), "method canonicalize");
        check(mn.contains("canonicalizeSubtree"), "method canonicalizeSubtree");
        check(mn.contains("canonicalizeXPathNodeSet"), "method canonicalizeXPathNodeSet");
        check(mn.contains("setWriter"), "method setWriter");
        check(mn.contains("getImplementingCanonicalizerClass"), "method getImplementingCanonicalizerClass");
        check(mn.contains("notReset"), "method notReset");
        check(mn.contains("isSecureValidation"), "method isSecureValidation");
        check(mn.contains("setSecureValidation"), "method setSecureValidation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
