/*
 * @test
 * @summary java.security.interfaces.ECPrivateKey conformance: interface hierarchy and key methods,
 *          serialVersionUID -7896394956925609184L, record-style implementation passthrough
 *          (getS/getParams/getAlgorithm/getFormat/getEncoded), supertype lattice
 *          (ECKey + PrivateKey + Key + Serializable), real EC secp256r1 private key contracts:
 *          s in range, PKCS#8 format, ECPrivateKeySpec and serialization round-trips equal.
 * @run main ECPrivateKeyTest
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
import java.security.PrivateKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPrivateKeySpec;

public class ECPrivateKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.ECPrivateKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getS"), "declares getS");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — constant, record impl, real-key closed loops ----
    static void testContract() throws Exception {
        // serialVersionUID is a fixed public contract value
        check(ECPrivateKey.class.getField("serialVersionUID").getLong(null)
                == -7896394956925609184L, "serialVersionUID == -7896394956925609184L");

        // record-style minimal implementation passes the full Key surface through
        ECPrivateKey rec = new RecKey();
        check(rec.getS().equals(BigInteger.valueOf(5)), "record getS passthrough");
        check(rec.getParams() != null, "record getParams passthrough");
        check("EC-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("PKCS#8-REC".equals(rec.getFormat()), "record getFormat passthrough");
        check(rec.getEncoded() != null && rec.getEncoded().length == 1, "record getEncoded passthrough");

        // supertype lattice
        check(ECKey.class.isAssignableFrom(ECPrivateKey.class), "is an ECKey");
        check(PrivateKey.class.isAssignableFrom(ECPrivateKey.class), "is a PrivateKey");
        check(Key.class.isAssignableFrom(ECPrivateKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(ECPrivateKey.class), "is Serializable");
        check(ECPrivateKey.class.getInterfaces().length == 2, "exactly two direct superinterfaces");

        // real generated key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kp = kpg.generateKeyPair();
        ECPrivateKey key = (ECPrivateKey) kp.getPrivate();
        check(key instanceof ECKey && key instanceof PrivateKey && key instanceof Serializable,
                "real key supertypes");
        check("EC".equals(key.getAlgorithm()), "getAlgorithm is EC");
        check("PKCS#8".equals(key.getFormat()), "getFormat is PKCS#8");
        check(key.getEncoded() != null && key.getEncoded().length > 0, "encoded non-empty");
        check(key.getS().signum() > 0, "s positive");
        check(key.getS().bitLength() <= key.getParams().getCurve().getField().getFieldSize(),
                "s fits in the field");
        check(key.getParams() != null && key.getParams().getOrder() != null, "params with order present");

        // KeyFactory spec round-trip restores an equal key with the same s
        KeyFactory kf = KeyFactory.getInstance("EC");
        ECPrivateKeySpec spec = kf.getKeySpec(key, ECPrivateKeySpec.class);
        check(spec.getS().equals(key.getS()), "spec preserves s");
        ECPrivateKey back = (ECPrivateKey) kf.generatePrivate(
                new ECPrivateKeySpec(spec.getS(), spec.getParams()));
        check(back.equals(key), "spec round-trip equals");
        check(back.getS().equals(key.getS()), "round-trip s identical");

        // serialization round-trip via the KeyRep machinery
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy instanceof ECPrivateKey, "deserialized still an ECPrivateKey");
        check(copy.equals(key), "serialization round-trip equals");

        // equals semantics
        check(key.equals(key), "reflexive equals");
        check(!key.equals(kp.getPublic()), "private not equal to public half");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements ECPrivateKey {
        public BigInteger getS() { return BigInteger.valueOf(5); }
        public java.security.spec.ECParameterSpec getParams() {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
                kpg.initialize(new ECGenParameterSpec("secp256r1"));
                return ((ECKey) kpg.generateKeyPair().getPublic()).getParams();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        public String getAlgorithm() { return "EC-REC"; }
        public String getFormat() { return "PKCS#8-REC"; }
        public byte[] getEncoded() { return new byte[]{7}; }
    }
}
