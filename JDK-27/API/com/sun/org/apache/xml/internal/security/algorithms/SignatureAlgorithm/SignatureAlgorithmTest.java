/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithm conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.algorithms:+open
 * @run main SignatureAlgorithmTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SignatureAlgorithmTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithm", false, SignatureAlgorithmTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithm"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.algorithms.Algorithm"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getBaseNamespace"), "method:getBaseNamespace");
        check(methods.contains("getJCEAlgorithmString"), "method:getJCEAlgorithmString");
        check(methods.contains("getJCEProviderName"), "method:getJCEProviderName");
        check(methods.contains("getURI"), "method:getURI");
        check(methods.contains("initSign"), "method:initSign");
        check(methods.contains("initVerify"), "method:initVerify");
        check(methods.contains("register"), "method:register");
        check(methods.contains("registerDefaultAlgorithms"), "method:registerDefaultAlgorithms");
        check(methods.contains("setParameter"), "method:setParameter");
        check(methods.contains("sign"), "method:sign");
        check(methods.contains("update"), "method:update");
        check(methods.contains("verify"), "method:verify");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
