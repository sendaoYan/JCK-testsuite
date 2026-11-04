/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509Digest conformance: class hierarchy and members.
 * @run main XMLX509DigestTest
 */
public class XMLX509DigestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509Digest", false, XMLX509DigestTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.Signature11ElementProxy"), "superclass com.sun.org.apache.xml.internal.security.utils.Signature11ElementProxy");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509DataContent"), "has interface com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509DataContent");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAlgorithmAttr"), "method getAlgorithmAttr");
        check(mn.contains("getAlgorithm"), "method getAlgorithm");
        check(mn.contains("getDigestBytes"), "method getDigestBytes");
        check(mn.contains("getDigestBytesFromCert"), "method getDigestBytesFromCert");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
