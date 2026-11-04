/*
 * @test
 * @summary java.security.interfaces.DSAPublicKey conformance: interface hierarchy and key methods,
 *          serialVersionUID 1234526332779022332L, record-style implementation passthrough,
 *          supertype lattice (DSAKey + PublicKey + Key + Serializable), real DSA-1024 public
 *          key contracts: y in (0, p), X.509 format, DSAPublicKeySpec/X509EncodedKeySpec
 *          round-trips equal, serialization round-trip, signature verify closed loop.
 * @run main DSAPublicKeyTest
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
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class DSAPublicKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAPublicKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getY"), "declares getY");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — constant, record impl, real-key closed loops ----
    static void testContract() throws Exception {
        // serialVersionUID is a fixed public contract value
        check(DSAPublicKey.class.getField("serialVersionUID").getLong(null)
                == 1234526332779022332L, "serialVersionUID == 1234526332779022332L");

        // record-style minimal implementation
        DSAPublicKey rec = new RecKey();
        check(rec.getY().equals(BigInteger.valueOf(8)), "record getY passthrough");
        check("DSA-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("X.509-REC".equals(rec.getFormat()), "record getFormat passthrough");
        check(rec.getEncoded() != null && rec.getEncoded().length == 2, "record getEncoded passthrough");

        // supertype lattice
        check(DSAKey.class.isAssignableFrom(DSAPublicKey.class), "is a DSAKey");
        check(PublicKey.class.isAssignableFrom(DSAPublicKey.class), "is a PublicKey");
        check(Key.class.isAssignableFrom(DSAPublicKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(DSAPublicKey.class), "is Serializable");
        check(DSAPublicKey.class.getInterfaces().length == 2, "exactly two direct superinterfaces");

        // real generated key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        DSAPublicKey key = (DSAPublicKey) kp.getPublic();
        check(key instanceof DSAKey && key instanceof PublicKey, "real key supertypes");
        check("DSA".equals(key.getAlgorithm()), "getAlgorithm is DSA");
        check("X.509".equals(key.getFormat()), "getFormat is X.509");
        check(key.getEncoded() != null && key.getEncoded().length > 0, "encoded non-empty");
        DSAParams pp = key.getParams();
        check(pp != null && pp.getP() != null, "params with p present");
        check(key.getY().signum() > 0, "y positive");
        check(key.getY().compareTo(pp.getP()) < 0, "y < p (public key range)");
        // closed math loop: y == g^x mod p with the matching private half
        BigInteger x = ((DSAPrivateKey) kp.getPrivate()).getX();
        check(pp.getG().modPow(x, pp.getP()).equals(key.getY()), "y == g^x mod p");

        // KeyFactory round-trips restore an equal key with the same y
        KeyFactory kf = KeyFactory.getInstance("DSA");
        DSAPublicKeySpec spec = kf.getKeySpec(key, DSAPublicKeySpec.class);
        check(spec.getY().equals(key.getY()), "spec preserves y");
        DSAPublicKey back = (DSAPublicKey) kf.generatePublic(
                new DSAPublicKeySpec(spec.getY(), spec.getP(), spec.getQ(), spec.getG()));
        check(back.equals(key), "DSAPublicKeySpec round-trip equals");
        DSAPublicKey x509 = (DSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(key.getEncoded()));
        check(x509.equals(key), "X509EncodedKeySpec round-trip equals");

        // serialization round-trip
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy instanceof DSAPublicKey, "deserialized still a DSAPublicKey");
        check(copy.equals(key), "serialization round-trip equals");

        // signature closed loop through the interface reference
        Signature sig = Signature.getInstance("SHA256withDSA");
        sig.initSign((DSAPrivateKey) kp.getPrivate());
        byte[] msg = "interfaces-DSA".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(key);
        sig.update(msg);
        check(sig.verify(s), "verify with DSAPublicKey succeeds");
        sig.update("other".getBytes("UTF-8"));
        check(!sig.verify(s), "verify fails for different message");

        // equals semantics
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
}
