/*
 * @test
 * @summary java.security.interfaces.DSAPrivateKey conformance: white-box structure + black-box behavior.
 * @run main DSAPrivateKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class DSAPrivateKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAPrivateKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.interfaces.DSAKey", "java.security.PrivateKey"});
        checkMethods(c, new String[]{"getParams", "getX"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        DSAPrivateKey priv = (DSAPrivateKey) kp.getPrivate();
        check(DSAPrivateKey.serialVersionUID == 7776497482533790279L, "serialVersionUID");
        check(priv instanceof PrivateKey && priv instanceof DSAKey, "supertypes");
        // x in [1, q)
        BigInteger x = priv.getX();
        DSAParams pp = priv.getParams();
        check(x.signum() > 0 && x.compareTo(pp.getQ()) < 0, "x in [1, q)");
        check(priv.getAlgorithm().equals("DSA"), "algorithm");
        check(priv.getFormat().equals("PKCS#8"), "PKCS#8 format");
        check(priv.getEncoded() != null && priv.getEncoded().length > 0, "encoded present");
        // sign with the interface fields, verify against the public half
        Signature sig = Signature.getInstance("SHA256withDSA");
        sig.initSign(priv);
        byte[] msg = "jck-dsa".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(kp.getPublic());
        sig.update(msg);
        check(sig.verify(s), "signature verifies");
        // KeyFactory round trip from x, p, q, g
        KeyFactory kf = KeyFactory.getInstance("DSA");
        DSAPrivateKey back = (DSAPrivateKey) kf.generatePrivate(
            new DSAPrivateKeySpec(x, pp.getP(), pp.getQ(), pp.getG()));
        check(back.getX().equals(x), "round trip x");
        check(back.getParams().getP().equals(pp.getP()), "round trip params");
        // serialization keeps x and params
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(priv); }
        Object copy = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(((DSAPrivateKey) copy).getX().equals(x), "serialized x preserved");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — record impl, lattice, equals semantics ----
    static void testContract() throws Exception {
        // record-style minimal implementation passes the full Key surface through
        DSAPrivateKey rec = new RecKey();
        check(rec.getX().equals(BigInteger.valueOf(7)), "record getX passthrough");
        check("DSA-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("PKCS#8-REC".equals(rec.getFormat()), "record getFormat passthrough");
        check(rec.getEncoded() != null && rec.getEncoded().length == 3, "record getEncoded passthrough");

        // lattice: a private DSA key is a DSAKey, PrivateKey, Key and Serializable
        check(DSAKey.class.isAssignableFrom(DSAPrivateKey.class), "is a DSAKey");
        check(PrivateKey.class.isAssignableFrom(DSAPrivateKey.class), "is a PrivateKey");
        check(Key.class.isAssignableFrom(DSAPrivateKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(DSAPrivateKey.class), "is Serializable");
        check(DSAPrivateKey.class.getInterfaces().length == 2, "exactly two direct superinterfaces");

        // real key: spec and serialization round-trips are fully equal
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        DSAPrivateKey key = (DSAPrivateKey) kp.getPrivate();
        KeyFactory kf = KeyFactory.getInstance("DSA");
        DSAPrivateKeySpec spec = kf.getKeySpec(key, DSAPrivateKeySpec.class);
        DSAPrivateKey back = (DSAPrivateKey) kf.generatePrivate(
                new DSAPrivateKeySpec(spec.getX(), spec.getP(), spec.getQ(), spec.getG()));
        check(back.equals(key), "spec round-trip equals");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.equals(key), "serialization round-trip equals");
        check(key instanceof Serializable, "real key serializable");

        // equals semantics
        check(key.equals(key), "reflexive equals");
        check(!key.equals(kp.getPublic()), "private not equal to public half");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements DSAPrivateKey {
        public BigInteger getX() { return BigInteger.valueOf(7); }
        public DSAParams getParams() {
            return new DSAParams() {
                public BigInteger getP() { return BigInteger.valueOf(23); }
                public BigInteger getQ() { return BigInteger.valueOf(11); }
                public BigInteger getG() { return BigInteger.valueOf(2); }
            };
        }
        public String getAlgorithm() { return "DSA-REC"; }
        public String getFormat() { return "PKCS#8-REC"; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
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
