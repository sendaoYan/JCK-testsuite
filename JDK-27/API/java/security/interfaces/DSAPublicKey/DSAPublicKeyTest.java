/*
 * @test
 * @summary java.security.interfaces.DSAPublicKey conformance: white-box structure + black-box behavior.
 * @run main DSAPublicKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class DSAPublicKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAPublicKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.interfaces.DSAKey", "java.security.PublicKey"});
        checkMethods(c, new String[]{"getParams", "getY"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        DSAPublicKey pub = (DSAPublicKey) kp.getPublic();
        check(DSAPublicKey.serialVersionUID == 1234526332779022332L, "serialVersionUID");
        check(pub instanceof PublicKey && pub instanceof DSAKey, "supertypes");
        // y in [1, p)
        BigInteger y = pub.getY();
        DSAParams pp = pub.getParams();
        check(y.signum() > 0 && y.compareTo(pp.getP()) < 0, "y in [1, p)");
        check(pub.getAlgorithm().equals("DSA"), "algorithm");
        check(pub.getFormat().equals("X.509"), "X.509 format");
        // g^x mod p == y links the two halves
        DSAPrivateKey priv = (DSAPrivateKey) kp.getPrivate();
        check(pp.getG().modPow(priv.getX(), pp.getP()).equals(y), "y == g^x mod p");
        // KeyFactory round trip from y, p, q, g
        KeyFactory kf = KeyFactory.getInstance("DSA");
        DSAPublicKey back = (DSAPublicKey) kf.generatePublic(
            new DSAPublicKeySpec(y, pp.getP(), pp.getQ(), pp.getG()));
        check(back.getY().equals(y), "round trip y");
        // verify path through the interface fields only
        Signature sig = Signature.getInstance("SHA256withDSA");
        sig.initSign(priv);
        sig.update("jck".getBytes("UTF-8"));
        byte[] s = sig.sign();
        sig.initVerify(back);
        sig.update("jck".getBytes("UTF-8"));
        check(sig.verify(s), "round-trip key verifies signature");
        // serialization preserves y
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(pub); }
        Object copy = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(((DSAPublicKey) copy).getY().equals(y), "serialized y preserved");

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
        DSAPublicKey rec = new RecKey();
        check(rec.getY().equals(BigInteger.valueOf(8)), "record getY passthrough");
        check("DSA-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("X.509-REC".equals(rec.getFormat()), "record getFormat passthrough");

        // lattice
        check(DSAKey.class.isAssignableFrom(DSAPublicKey.class), "is a DSAKey");
        check(PublicKey.class.isAssignableFrom(DSAPublicKey.class), "is a PublicKey");
        check(Key.class.isAssignableFrom(DSAPublicKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(DSAPublicKey.class), "is Serializable");
        check(DSAPublicKey.class.getInterfaces().length == 2, "exactly two direct superinterfaces");

        // real key: both factory round-trips fully equal, serialization equal
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        DSAPublicKey key = (DSAPublicKey) kp.getPublic();
        KeyFactory kf = KeyFactory.getInstance("DSA");
        DSAPublicKeySpec spec = kf.getKeySpec(key, DSAPublicKeySpec.class);
        check(spec.getP().equals(key.getParams().getP()), "spec preserves p");
        DSAPublicKey back = (DSAPublicKey) kf.generatePublic(
                new DSAPublicKeySpec(spec.getY(), spec.getP(), spec.getQ(), spec.getG()));
        check(back.equals(key), "DSAPublicKeySpec round-trip equals");
        DSAPublicKey x509 = (DSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(key.getEncoded()));
        check(x509.equals(key), "X509EncodedKeySpec round-trip equals");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.equals(key), "serialization round-trip equals");

        // domain math and equals semantics
        check(key.getParams().getG().modPow(key.getParams().getQ(), key.getParams().getP())
                .equals(BigInteger.ONE), "g^q mod p == 1");
        check(key.equals(key), "reflexive equals");
        check(!key.equals(kp.getPrivate()), "public not equal to private half");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements DSAPublicKey {
        public BigInteger getY() { return BigInteger.valueOf(8); }
        public DSAParams getParams() {
            return new DSAParams() {
                public BigInteger getP() { return BigInteger.valueOf(23); }
                public BigInteger getQ() { return BigInteger.valueOf(11); }
                public BigInteger getG() { return BigInteger.valueOf(2); }
            };
        }
        public String getAlgorithm() { return "DSA-REC"; }
        public String getFormat() { return "X.509-REC"; }
        public byte[] getEncoded() { return new byte[]{9, 9}; }
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
