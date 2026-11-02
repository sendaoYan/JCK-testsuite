/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.SignedInfo conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.signature:+open
 * @run main SignedInfoTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SignedInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.SignedInfo", false, SignedInfoTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.signature.SignedInfo"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.signature.Manifest"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("createSecretKey"), "method:createSecretKey");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getCanonicalizationMethodURI"), "method:getCanonicalizationMethodURI");
        check(methods.contains("getCanonicalizedOctetStream"), "method:getCanonicalizedOctetStream");
        check(methods.contains("getInclusiveNamespaces"), "method:getInclusiveNamespaces");
        check(methods.contains("getSignatureAlgorithm"), "method:getSignatureAlgorithm");
        check(methods.contains("getSignatureMethodElement"), "method:getSignatureMethodElement");
        check(methods.contains("getSignatureMethodURI"), "method:getSignatureMethodURI");
        check(methods.contains("signInOctetStream"), "method:signInOctetStream");
        check(methods.contains("verify"), "method:verify");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
