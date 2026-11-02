/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509Digest conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.keys.content.x509:+open
 * @run main XMLX509DigestTest
 */
import java.lang.reflect.*;
import java.util.*;
public class XMLX509DigestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509Digest", false, XMLX509DigestTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509Digest"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.Signature11ElementProxy"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509DataContent"), "iface:com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509DataContent");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getAlgorithm"), "method:getAlgorithm");
        check(methods.contains("getAlgorithmAttr"), "method:getAlgorithmAttr");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getDigestBytes"), "method:getDigestBytes");
        check(methods.contains("getDigestBytesFromCert"), "method:getDigestBytesFromCert");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
