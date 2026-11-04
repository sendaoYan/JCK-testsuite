/*
 * @test
 * @summary java.security.interfaces.RSAPublicKey conformance: white-box structure + black-box behavior.
 * @run main RSAPublicKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class RSAPublicKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAPublicKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.PublicKey", "java.security.interfaces.RSAKey"});
        checkMethods(c, new String[]{"getParams", "getPublicExponent"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPublicKey key = (RSAPublicKey) kp.getPublic();
        check(RSAPublicKey.serialVersionUID == -8727434096241101194L, "serialVersionUID");
        check(key instanceof PublicKey && key instanceof RSAKey, "supertypes");
        check(key.getPublicExponent().intValue() == 65537, "standard exponent");
        check(key.getModulus().bitLength() == 2048, "2048-bit modulus");
        check(key.getFormat().equals("X.509"), "X.509 format");
        // e is odd and greater than 1
        BigInteger e = key.getPublicExponent();
        check(e.signum() == 1 && e.compareTo(BigInteger.TWO) > 0 && e.testBit(0), "e positive, odd and > 2");
        // KeyFactory round trip from (n, e)
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey back = (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(key.getModulus(), e));
        check(back.getPublicExponent().equals(e), "round trip e");
        check(back.getModulus().equals(key.getModulus()), "round trip n");
        // verification through the reconstructed key
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign((RSAPrivateKey) kp.getPrivate());
        byte[] msg = "jck-rpub".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(back);
        sig.update(msg);
        check(sig.verify(s), "round-trip key verifies");
        // serialization preserves e
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(key); }
        Object copy = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(((RSAPublicKey) copy).getPublicExponent().equals(e), "serialized e preserved");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — record impl, lattice, equals semantics ----
    static void testContract() throws Exception {
        // record-style minimal implementation
        RSAPublicKey rec = new RecKey();
        check(rec.getPublicExponent().equals(BigInteger.valueOf(65537)), "record e passthrough");
        check(rec.getModulus().equals(BigInteger.valueOf(3233)), "record modulus passthrough");
        check("X.509-REC".equals(rec.getFormat()), "record getFormat passthrough");
        check(rec.getEncoded() != null && rec.getEncoded().length == 1, "record getEncoded passthrough");

        // lattice
        check(RSAKey.class.isAssignableFrom(RSAPublicKey.class), "is an RSAKey");
        check(PublicKey.class.isAssignableFrom(RSAPublicKey.class), "is a PublicKey");
        check(Key.class.isAssignableFrom(RSAPublicKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(RSAPublicKey.class), "is Serializable");
        check(RSAPublicKey.class.getInterfaces().length == 2, "exactly two direct superinterfaces");

        // real key: exponent and modulus invariants, both factory round-trips equal
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPublicKey key = (RSAPublicKey) kp.getPublic();
        check(key.getPublicExponent().compareTo(key.getModulus()) < 0, "e < n");
        check(key.getPublicExponent().testBit(0), "e odd");
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec spec = kf.getKeySpec(key, RSAPublicKeySpec.class);
        RSAPublicKey back = (RSAPublicKey) kf.generatePublic(
                new RSAPublicKeySpec(spec.getModulus(), spec.getPublicExponent()));
        check(back.equals(key), "RSAPublicKeySpec round-trip equals");
        RSAPublicKey x509 = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(key.getEncoded()));
        check(x509.equals(key), "X509EncodedKeySpec round-trip equals");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.equals(key), "serialization round-trip equals");

        // equals semantics
        check(key.equals(key), "reflexive equals");
        check(!key.equals(kp.getPrivate()), "public not equal to private half");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements RSAPublicKey {
        public BigInteger getPublicExponent() { return BigInteger.valueOf(65537); }
        public BigInteger getModulus() { return BigInteger.valueOf(3233); }
        public String getAlgorithm() { return "RSA-REC"; }
        public String getFormat() { return "X.509-REC"; }
        public byte[] getEncoded() { return new byte[]{5}; }
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
