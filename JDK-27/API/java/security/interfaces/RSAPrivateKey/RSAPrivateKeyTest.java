/*
 * @test
 * @summary java.security.interfaces.RSAPrivateKey conformance: white-box structure + black-box behavior.
 * @run main RSAPrivateKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class RSAPrivateKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAPrivateKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.PrivateKey", "java.security.interfaces.RSAKey"});
        checkMethods(c, new String[]{"getParams", "getPrivateExponent"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPrivateKey key = (RSAPrivateKey) kp.getPrivate();
        check(RSAPrivateKey.serialVersionUID == 5187144804936595022L, "serialVersionUID");
        check(key instanceof PrivateKey && key instanceof RSAKey, "supertypes");
        // d in [1, n), shares the modulus with the public half
        BigInteger d = key.getPrivateExponent();
        check(d.signum() > 0 && d.compareTo(key.getModulus()) < 0, "d in [1, n)");
        check(key.getModulus().equals(((RSAKey) kp.getPublic()).getModulus()), "shared modulus");
        check(key.getFormat().equals("PKCS#8"), "PKCS#8 format");
        // e*d = 1 mod lambda(n) is implied by a successful sign/verify
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(key);
        byte[] msg = "jck-priv".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(kp.getPublic());
        sig.update(msg);
        check(sig.verify(s), "signature verifies");
        // KeyFactory from the minimal (n, d) spec yields a plain RSAPrivateKey
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPrivateKey plain = (RSAPrivateKey) kf.generatePrivate(new RSAPrivateKeySpec(key.getModulus(), d));
        check(plain.getPrivateExponent().equals(d), "minimal spec round trip");
        check(!(plain instanceof RSAPrivateCrtKey), "minimal spec is not CRT");
        // serialization preserves d
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(key); }
        Object copy = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(((RSAPrivateKey) copy).getPrivateExponent().equals(d), "serialized d preserved");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
