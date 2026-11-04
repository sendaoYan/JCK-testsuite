/*
 * @test
 * @summary java.security.interfaces.ECPublicKey conformance: white-box structure + black-box behavior.
 * @run main ECPublicKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class ECPublicKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.ECPublicKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.PublicKey", "java.security.interfaces.ECKey"});
        checkMethods(c, new String[]{"getParams", "getW"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kp = kpg.generateKeyPair();
        ECPublicKey pub = (ECPublicKey) kp.getPublic();
        check(ECPublicKey.serialVersionUID == -3314988629879632826L, "serialVersionUID");
        check(pub instanceof PublicKey && pub instanceof ECKey, "supertypes");
        // W is an affine point strictly inside the field
        ECPoint w = pub.getW();
        java.math.BigInteger fieldPrime = ((java.security.spec.ECFieldFp) pub.getParams().getCurve().getField()).getP();
        check(w.getAffineX().signum() >= 0 && w.getAffineX().compareTo(fieldPrime) < 0, "x in [0, p)");
        check(w.getAffineY().signum() >= 0 && w.getAffineY().compareTo(fieldPrime) < 0, "y in [0, p)");
        check(pub.getFormat().equals("X.509"), "X.509 format");
        // KeyFactory round trip from W + params
        KeyFactory kf = KeyFactory.getInstance("EC");
        ECPublicKey back = (ECPublicKey) kf.generatePublic(new ECPublicKeySpec(w, pub.getParams()));
        check(back.getW().getAffineX().equals(w.getAffineX()) && back.getW().getAffineY().equals(w.getAffineY()),
            "round trip W");
        // the factory does not validate curve membership: an arbitrary point is accepted
        ECPublicKey weird = (ECPublicKey) kf.generatePublic(new ECPublicKeySpec(new ECPoint(BigInteger.ONE, BigInteger.ONE), pub.getParams()));
        check(weird != null, "off-curve point accepted by factory");
        // serialization preserves W
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(pub); }
        Object copy = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(((ECPublicKey) copy).getW().getAffineX().equals(w.getAffineX()), "serialized W preserved");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — record impl, lattice, on-curve math, equals ----
    static void testContract() throws Exception {
        // record-style minimal implementation
        ECPublicKey rec = new RecKey();
        check(rec.getW().getAffineX().equals(BigInteger.valueOf(1)), "record getW x passthrough");
        check(rec.getW().getAffineY().equals(BigInteger.valueOf(2)), "record getW y passthrough");
        check("X.509-REC".equals(rec.getFormat()), "record getFormat passthrough");

        // lattice
        check(ECKey.class.isAssignableFrom(ECPublicKey.class), "is an ECKey");
        check(PublicKey.class.isAssignableFrom(ECPublicKey.class), "is a PublicKey");
        check(Key.class.isAssignableFrom(ECPublicKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(ECPublicKey.class), "is Serializable");
        check(ECPublicKey.class.getInterfaces().length == 2, "exactly two direct superinterfaces");

        // real key: W satisfies the curve equation y^2 = x^3 + ax + b (mod p)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kp = kpg.generateKeyPair();
        ECPublicKey key = (ECPublicKey) kp.getPublic();
        ECParameterSpec pp = key.getParams();
        ECPoint w = key.getW();
        BigInteger p = ((ECFieldFp) pp.getCurve().getField()).getP();
        BigInteger lhs = w.getAffineY().pow(2).mod(p);
        BigInteger rhs = w.getAffineX().pow(3)
                .add(pp.getCurve().getA().multiply(w.getAffineX()))
                .add(pp.getCurve().getB()).mod(p);
        check(lhs.equals(rhs), "W satisfies y^2 = x^3 + ax + b (mod p)");

        // factory round-trips fully equal, serialization equal
        KeyFactory kf = KeyFactory.getInstance("EC");
        ECPublicKeySpec spec = kf.getKeySpec(key, ECPublicKeySpec.class);
        ECPublicKey back = (ECPublicKey) kf.generatePublic(new ECPublicKeySpec(spec.getW(), spec.getParams()));
        check(back.equals(key), "ECPublicKeySpec round-trip equals");
        ECPublicKey x509 = (ECPublicKey) kf.generatePublic(new X509EncodedKeySpec(key.getEncoded()));
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

    static class RecKey implements ECPublicKey {
        public ECPoint getW() { return new ECPoint(BigInteger.valueOf(1), BigInteger.valueOf(2)); }
        public ECParameterSpec getParams() {
            try {
                return ((ECKey) KeyPairGenerator.getInstance("EC").generateKeyPair()
                        .getPublic()).getParams();
            } catch (Exception e) { throw new RuntimeException(e); }
        }
        public String getAlgorithm() { return "EC-REC"; }
        public String getFormat() { return "X.509-REC"; }
        public byte[] getEncoded() { return new byte[]{8}; }
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
