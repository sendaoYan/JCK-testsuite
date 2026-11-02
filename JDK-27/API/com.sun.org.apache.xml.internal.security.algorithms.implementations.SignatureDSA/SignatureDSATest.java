/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureDSA conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.algorithms.implementations:+open
 * @run main SignatureDSATest
 */
import java.lang.reflect.*;
import java.util.*;
public class SignatureDSATest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureDSA", false, SignatureDSATest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureDSA"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithmSpi"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("engineGetJCEAlgorithmString"), "method:engineGetJCEAlgorithmString");
        check(methods.contains("engineGetJCEProviderName"), "method:engineGetJCEProviderName");
        check(methods.contains("engineGetURI"), "method:engineGetURI");
        check(methods.contains("engineInitSign"), "method:engineInitSign");
        check(methods.contains("engineInitVerify"), "method:engineInitVerify");
        check(methods.contains("engineSetHMACOutputLength"), "method:engineSetHMACOutputLength");
        check(methods.contains("engineSetParameter"), "method:engineSetParameter");
        check(methods.contains("engineSign"), "method:engineSign");
        check(methods.contains("engineUpdate"), "method:engineUpdate");
        check(methods.contains("engineVerify"), "method:engineVerify");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("URI"), "field:URI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
