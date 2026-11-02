/*
 * @test
 * @summary sun.security.rsa.RSASignature.SHA512_224withRSA conformance: type shape via reflection (headless-safe).
 * @run main SHA512_224withRSATest
 */

import java.lang.reflect.Modifier;

public class SHA512_224withRSATest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.rsa.RSASignature$SHA512_224withRSA", false, SHA512_224withRSATest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.rsa.RSASignature"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
