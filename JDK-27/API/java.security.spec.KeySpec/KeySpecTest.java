/*
 * @test
 * @summary java.security.spec.KeySpec conformance: white-box structure + black-box behavior.
 * @run main KeySpecTest
 */

import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.RSAPrivateKeySpec;

public class KeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = KeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getDeclaredMethods().length == 0, "no methods");
    }

    static void testBlackBox() {
        check(new RSAPublicKeySpec(new BigInteger("33"), new BigInteger("3")) instanceof KeySpec, "RSAPublicKeySpec instanceof KeySpec");
        check(new RSAPrivateKeySpec(new BigInteger("33"), new BigInteger("7")) instanceof KeySpec, "RSAPrivateKeySpec instanceof KeySpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
