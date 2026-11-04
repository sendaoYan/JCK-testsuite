/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureBaseRSA.SignatureRSASSAPSS.DigestAlgorithm conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.algorithms.implementations:+open
 * @run main DigestAlgorithmTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DigestAlgorithmTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureBaseRSA$SignatureRSASSAPSS$DigestAlgorithm", false, DigestAlgorithmTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureBaseRSA$SignatureRSASSAPSS$DigestAlgorithm"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("fromDigestAlgorithm"), "method:fromDigestAlgorithm");
        check(methods.contains("fromXmlDigestAlgorithm"), "method:fromXmlDigestAlgorithm");
        check(methods.contains("getDigestAlgorithm"), "method:getDigestAlgorithm");
        check(methods.contains("getSaltLength"), "method:getSaltLength");
        check(methods.contains("getXmlDigestAlgorithm"), "method:getXmlDigestAlgorithm");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("SHA224"), "field:SHA224");
        check(fields.contains("SHA256"), "field:SHA256");
        check(fields.contains("SHA384"), "field:SHA384");
        check(fields.contains("SHA3_224"), "field:SHA3_224");
        check(fields.contains("SHA3_256"), "field:SHA3_256");
        check(fields.contains("SHA3_384"), "field:SHA3_384");
        check(fields.contains("SHA3_512"), "field:SHA3_512");
        check(fields.contains("SHA512"), "field:SHA512");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("SHA224"), "enum-const:SHA224");
        check(enumConsts.contains("SHA256"), "enum-const:SHA256");
        check(enumConsts.contains("SHA384"), "enum-const:SHA384");
        check(enumConsts.contains("SHA3_224"), "enum-const:SHA3_224");
        check(enumConsts.contains("SHA3_256"), "enum-const:SHA3_256");
        check(enumConsts.contains("SHA3_384"), "enum-const:SHA3_384");
        check(enumConsts.contains("SHA3_512"), "enum-const:SHA3_512");
        check(enumConsts.contains("SHA512"), "enum-const:SHA512");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
