/*
 * @test
 * @summary java.security.KeyFactory conformance: white-box structure + black-box behavior.
 * @run main KeyFactoryTest
 */

import java.lang.reflect.Modifier;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class KeyFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkMethods(c, new String[]{"getInstance", "getProvider", "getAlgorithm",
            "generatePublic", "generatePrivate", "getKeySpec", "translateKey"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        byte[] pubEncoded = kp.getPublic().getEncoded();
        byte[] privEncoded = kp.getPrivate().getEncoded();

        KeyFactory kf = KeyFactory.getInstance("RSA");
        check("RSA".equals(kf.getAlgorithm()), "getAlgorithm RSA");
        check(kf.getProvider() != null, "getProvider non-null");

        // X509/PKCS8 encoded roundtrip through generatePublic/generatePrivate
        PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(pubEncoded));
        PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(privEncoded));
        check("RSA".equals(pub.getAlgorithm()), "generated public algorithm");
        check("RSA".equals(priv.getAlgorithm()), "generated private algorithm");
        check(Arrays.equals(pubEncoded, pub.getEncoded()), "public encoded roundtrip");
        check(Arrays.equals(privEncoded, priv.getEncoded()), "private encoded roundtrip");

        // getKeySpec back to an algorithm-specific spec
        RSAPublicKeySpec pubSpec = kf.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
        check(pubSpec.getModulus() != null, "RSAPublicKeySpec modulus non-null");
        check(pubSpec.getPublicExponent() != null, "RSAPublicKeySpec exponent non-null");

        // translateKey returns a key with the same encoding
        Key translated = kf.translateKey(kp.getPublic());
        check(translated != null, "translateKey non-null");
        check(Arrays.equals(kp.getPublic().getEncoded(), translated.getEncoded()),
            "translateKey preserves encoding");

        // EC key factory
        KeyPairGenerator ecg = KeyPairGenerator.getInstance("EC");
        ecg.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));
        KeyPair ecp = ecg.generateKeyPair();
        KeyFactory ecf = KeyFactory.getInstance("EC");
        PublicKey ecPub = ecf.generatePublic(new X509EncodedKeySpec(ecp.getPublic().getEncoded()));
        check("EC".equals(ecPub.getAlgorithm()), "EC public algorithm");
        check(Arrays.equals(ecp.getPublic().getEncoded(), ecPub.getEncoded()), "EC public encoded roundtrip");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
