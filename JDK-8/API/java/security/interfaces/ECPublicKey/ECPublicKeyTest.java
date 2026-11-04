/*
 * @test
 * @summary java.security.interfaces.ECPublicKey conformance: interface hierarchy and key methods,
 *          serialVersionUID -3314988629879632826L, record-style implementation passthrough,
 *          supertype lattice (ECKey + PublicKey + Key + Serializable), real EC secp256r1
 *          public key contracts: affine W in [0, p) and on the curve y^2 = x^3 + ax + b (mod p),
 *          X.509 format, ECPublicKeySpec/X509EncodedKeySpec round-trips equal, serialization
 *          round-trip, signature verify closed loop.
 * @run main ECPublicKeyTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class ECPublicKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.ECPublicKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getW"), "declares getW");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — constant, record impl, real-key closed loops ----
    static void testContract() throws Exception {
        // serialVersionUID is a fixed public contract value
        check(ECPublicKey.class.getField("serialVersionUID").getLong(null)
                == -3314988629879632826L, "serialVersionUID == -3314988629879632826L");

        // record-style minimal implementation
        ECPublicKey rec = new RecKey();
        check(rec.getW().getAffineX().equals(BigInteger.valueOf(1)), "record getW x passthrough");
        check(rec.getW().getAffineY().equals(BigInteger.valueOf(2)), "record getW y passthrough");
        check("EC-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("X.509-REC".equals(rec.getFormat()), "record getFormat passthrough");

        // supertype lattice
        check(ECKey.class.isAssignableFrom(ECPublicKey.class), "is an ECKey");
        check(PublicKey.class.isAssignableFrom(ECPublicKey.class), "is a PublicKey");
        check(Key.class.isAssignableFrom(ECPublicKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(ECPublicKey.class), "is Serializable");
        check(ECPublicKey.class.getInterfaces().length == 2, "exactly two direct superinterfaces");

        // real generated key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kp = kpg.generateKeyPair();
        ECPublicKey key = (ECPublicKey) kp.getPublic();
        check(key instanceof ECKey && key instanceof PublicKey, "real key supertypes");
        check("EC".equals(key.getAlgorithm()), "getAlgorithm is EC");
        check("X.509".equals(key.getFormat()), "getFormat is X.509");
        check(key.getEncoded() != null && key.getEncoded().length > 0, "encoded non-empty");

        // affine point W lies in the field and on the curve
        ECParameterSpec pp = key.getParams();
        ECPoint w = key.getW();
        BigInteger p = ((ECFieldFp) pp.getCurve().getField()).getP();
        check(w.getAffineX().signum() >= 0 && w.getAffineX().compareTo(p) < 0, "affine x in [0, p)");
        check(w.getAffineY().signum() >= 0 && w.getAffineY().compareTo(p) < 0, "affine y in [0, p)");
        BigInteger lhs = w.getAffineY().pow(2).mod(p);
        BigInteger rhs = w.getAffineX().pow(3)
                .add(pp.getCurve().getA().multiply(w.getAffineX()))
                .add(pp.getCurve().getB()).mod(p);
        check(lhs.equals(rhs), "W satisfies y^2 = x^3 + ax + b (mod p)");
        check(pp.getCurve().getField().getFieldSize() == 256, "secp256r1 field size is 256");

        // KeyFactory round-trips restore an equal key with the same point
        KeyFactory kf = KeyFactory.getInstance("EC");
        ECPublicKeySpec spec = kf.getKeySpec(key, ECPublicKeySpec.class);
        check(spec.getW().equals(w), "spec preserves W");
        ECPublicKey back = (ECPublicKey) kf.generatePublic(new ECPublicKeySpec(spec.getW(), spec.getParams()));
        check(back.equals(key), "ECPublicKeySpec round-trip equals");
        ECPublicKey x509 = (ECPublicKey) kf.generatePublic(new X509EncodedKeySpec(key.getEncoded()));
        check(x509.equals(key), "X509EncodedKeySpec round-trip equals");

        // serialization round-trip
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy instanceof ECPublicKey, "deserialized still an ECPublicKey");
        check(copy.equals(key), "serialization round-trip equals");

        // signature closed loop through the interface reference
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initSign((ECPrivateKey) kp.getPrivate());
        byte[] msg = "interfaces-EC".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(key);
        sig.update(msg);
        check(sig.verify(s), "verify with ECPublicKey succeeds");
        sig.update("other".getBytes("UTF-8"));
        check(!sig.verify(s), "verify fails for different message");

        // equals semantics
        check(key.equals(key), "reflexive equals");
        check(!key.equals(kp.getPrivate()), "public not equal to private half");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements ECPublicKey {
        public ECPoint getW() { return new ECPoint(BigInteger.valueOf(1), BigInteger.valueOf(2)); }
        public ECParameterSpec getParams() {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
                kpg.initialize(new ECGenParameterSpec("secp256r1"));
                return ((ECKey) kpg.generateKeyPair().getPublic()).getParams();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        public String getAlgorithm() { return "EC-REC"; }
        public String getFormat() { return "X.509-REC"; }
        public byte[] getEncoded() { return new byte[]{8}; }
    }
}
