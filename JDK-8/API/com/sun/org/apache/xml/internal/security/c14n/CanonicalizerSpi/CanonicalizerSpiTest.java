/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.CanonicalizerSpi conformance: abstract class hierarchy and members.
 * @run main CanonicalizerSpiTest
 */
public class CanonicalizerSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.CanonicalizerSpi", false, CanonicalizerSpiTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("engineCanonicalize"), "method engineCanonicalize");
        check(mn.contains("engineCanonicalizeXPathNodeSet"), "method engineCanonicalizeXPathNodeSet");
        check(mn.contains("engineGetURI"), "method engineGetURI");
        check(mn.contains("engineGetIncludeComments"), "method engineGetIncludeComments");
        check(mn.contains("engineCanonicalizeSubTree"), "method engineCanonicalizeSubTree");
        check(mn.contains("setWriter"), "method setWriter");
        check(mn.contains("isSecureValidation"), "method isSecureValidation");
        check(mn.contains("setSecureValidation"), "method setSecureValidation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
