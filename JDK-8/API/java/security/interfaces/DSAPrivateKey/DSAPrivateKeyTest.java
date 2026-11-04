/*
 * @test
 * @summary java.security.interfaces.DSAPrivateKey conformance: interface hierarchy and key methods,
 *          serialVersionUID 7776497482533790279L, record-style implementation passthrough
 *          (getX/getParams/getAlgorithm/getFormat/getEncoded), supertype lattice
 *          (DSAKey + PrivateKey + Key + Serializable), real DSA-1024 private key contracts:
 *          x in (0, q), PKCS#8 format, DSAPrivateKeySpec and serialization round-trips equal.
 * @run main DSAPrivateKeyTest
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
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.spec.DSAPrivateKeySpec;

public class DSAPrivateKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAPrivateKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getX"), "declares getX");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — constant, record impl, real-key closed loops ----
    static void testContract() throws Exception {
        // serialVersionUID is a fixed public contract value
        check(DSAPrivateKey.class.getField("serialVersionUID").getLong(null)
                == 7776497482533790279L, "serialVersionUID == 7776497482533790279L");

        // record-style minimal implementation passes the full Key surface through
        DSAPrivateKey rec = new RecKey();
        check(rec.getX().equals(BigInteger.valueOf(7)), "record getX passthrough");
        check(rec.getParams() != null && rec.getParams().getP() != null,
                "record getParams passthrough");
        check("DSA-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("PKCS#8-REC".equals(rec.getFormat()), "record getFormat passthrough");
        check(rec.getEncoded() != null && rec.getEncoded().length == 3, "record getEncoded passthrough");

        // supertype lattice: a private DSA key is a DSAKey, PrivateKey, Key and Serializable
        check(DSAKey.class.isAssignableFrom(DSAPrivateKey.class), "is a DSAKey");
        check(PrivateKey.class.isAssignableFrom(DSAPrivateKey.class), "is a PrivateKey");
        check(Key.class.isAssignableFrom(DSAPrivateKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(DSAPrivateKey.class), "is Serializable");
        check(DSAPrivateKey.class.getInterfaces().length == 2, "exactly two direct superinterfaces");

        // real generated key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        DSAPrivateKey key = (DSAPrivateKey) kp.getPrivate();
        check(key instanceof DSAKey && key instanceof PrivateKey && key instanceof Serializable,
                "real key supertypes");
        check("DSA".equals(key.getAlgorithm()), "getAlgorithm is DSA");
        check("PKCS#8".equals(key.getFormat()), "getFormat is PKCS#8");
        check(key.getEncoded() != null && key.getEncoded().length > 0, "encoded non-empty");
        DSAParams pp = key.getParams();
        check(pp != null && pp.getQ() != null, "params with q present");
        check(key.getX().signum() > 0, "x positive");
        check(key.getX().compareTo(pp.getQ()) < 0, "x < q (private key range)");

        // KeyFactory spec round-trip restores an equal key with the same x
        KeyFactory kf = KeyFactory.getInstance("DSA");
        DSAPrivateKeySpec spec = kf.getKeySpec(key, DSAPrivateKeySpec.class);
        check(spec.getX().equals(key.getX()), "spec preserves x");
        DSAPrivateKey back = (DSAPrivateKey) kf.generatePrivate(
                new DSAPrivateKeySpec(spec.getX(), spec.getP(), spec.getQ(), spec.getG()));
        check(back.equals(key), "spec round-trip equals");
        check(back.getX().equals(key.getX()), "round-trip x identical");

        // serialization round-trip via the KeyRep machinery
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy instanceof DSAPrivateKey, "deserialized still a DSAPrivateKey");
        check(copy.equals(key), "serialization round-trip equals");

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
}
