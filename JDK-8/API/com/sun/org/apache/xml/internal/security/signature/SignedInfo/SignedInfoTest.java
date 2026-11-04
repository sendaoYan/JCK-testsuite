/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.SignedInfo conformance: class hierarchy and members.
 * @run main SignedInfoTest
 */
public class SignedInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.SignedInfo", false, SignedInfoTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.signature.Manifest"), "superclass com.sun.org.apache.xml.internal.security.signature.Manifest");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("verify"), "method verify");
        check(mn.contains("getCanonicalizedOctetStream"), "method getCanonicalizedOctetStream");
        check(mn.contains("signInOctetStream"), "method signInOctetStream");
        check(mn.contains("getCanonicalizationMethodURI"), "method getCanonicalizationMethodURI");
        check(mn.contains("getSignatureMethodURI"), "method getSignatureMethodURI");
        check(mn.contains("getSignatureMethodElement"), "method getSignatureMethodElement");
        check(mn.contains("createSecretKey"), "method createSecretKey");
        check(mn.contains("getSignatureAlgorithm"), "method getSignatureAlgorithm");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        check(mn.contains("getInclusiveNamespaces"), "method getInclusiveNamespaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
