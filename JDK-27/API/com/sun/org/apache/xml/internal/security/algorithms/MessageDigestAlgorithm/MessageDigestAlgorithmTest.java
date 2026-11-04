/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.MessageDigestAlgorithm conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.algorithms:+open
 * @run main MessageDigestAlgorithmTest
 */
import java.lang.reflect.*;
import java.util.*;
public class MessageDigestAlgorithmTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.MessageDigestAlgorithm", false, MessageDigestAlgorithmTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.algorithms.MessageDigestAlgorithm"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.algorithms.Algorithm"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("digest"), "method:digest");
        check(methods.contains("getAlgorithm"), "method:getAlgorithm");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getBaseNamespace"), "method:getBaseNamespace");
        check(methods.contains("getDigestInstance"), "method:getDigestInstance");
        check(methods.contains("getDigestLength"), "method:getDigestLength");
        check(methods.contains("getInstance"), "method:getInstance");
        check(methods.contains("getJCEAlgorithmString"), "method:getJCEAlgorithmString");
        check(methods.contains("getJCEProvider"), "method:getJCEProvider");
        check(methods.contains("isEqual"), "method:isEqual");
        check(methods.contains("reset"), "method:reset");
        check(methods.contains("update"), "method:update");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ALGO_ID_DIGEST_NOT_RECOMMENDED_MD5"), "field:ALGO_ID_DIGEST_NOT_RECOMMENDED_MD5");
        check(fields.contains("ALGO_ID_DIGEST_RIPEMD160"), "field:ALGO_ID_DIGEST_RIPEMD160");
        check(fields.contains("ALGO_ID_DIGEST_SHA1"), "field:ALGO_ID_DIGEST_SHA1");
        check(fields.contains("ALGO_ID_DIGEST_SHA224"), "field:ALGO_ID_DIGEST_SHA224");
        check(fields.contains("ALGO_ID_DIGEST_SHA256"), "field:ALGO_ID_DIGEST_SHA256");
        check(fields.contains("ALGO_ID_DIGEST_SHA384"), "field:ALGO_ID_DIGEST_SHA384");
        check(fields.contains("ALGO_ID_DIGEST_SHA3_224"), "field:ALGO_ID_DIGEST_SHA3_224");
        check(fields.contains("ALGO_ID_DIGEST_SHA3_256"), "field:ALGO_ID_DIGEST_SHA3_256");
        check(fields.contains("ALGO_ID_DIGEST_SHA3_384"), "field:ALGO_ID_DIGEST_SHA3_384");
        check(fields.contains("ALGO_ID_DIGEST_SHA3_512"), "field:ALGO_ID_DIGEST_SHA3_512");
        check(fields.contains("ALGO_ID_DIGEST_SHA512"), "field:ALGO_ID_DIGEST_SHA512");
        check(fields.contains("ALGO_ID_DIGEST_WHIRLPOOL"), "field:ALGO_ID_DIGEST_WHIRLPOOL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
