/*
 * @test
 * @summary java.security.interfaces.RSAPrivateKey conformance: interface hierarchy and key methods,
 *          serialVersionUID 5187144804936595022L, record-style implementation passthrough
 *          (getPrivateExponent/getModulus/getAlgorithm/getFormat/getEncoded), supertype
 *          lattice (RSAKey + PrivateKey + Key + Serializable), real RSA-2048 private key
 *          contracts: d*e == 1 (mod lcm(p-1,q-1)), (n,d) KeyFactory round-trip yields a
 *          non-CRT key with matching values, serialization round-trip equal.
 * @run main RSAPrivateKeyTest
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
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;

public class RSAPrivateKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAPrivateKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getPrivateExponent"), "declares getPrivateExponent");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — constant, record impl, real-key closed loops ----
    static void testContract() throws Exception {
        // serialVersionUID is a fixed public contract value
        check(RSAPrivateKey.class.getField("serialVersionUID").getLong(null)
                == 5187144804936595022L, "serialVersionUID == 5187144804936595022L");

        // record-style minimal implementation passes the full Key surface through
        RSAPrivateKey rec = new RecKey();
        check(rec.getPrivateExponent().equals(BigInteger.valueOf(2753)), "record d passthrough");
        check(rec.getModulus().equals(BigInteger.valueOf(3233)), "record modulus passthrough");
        check("RSA-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("PKCS#8-REC".equals(rec.getFormat()), "record getFormat passthrough");
        check(rec.getEncoded() != null && rec.getEncoded().length == 2, "record getEncoded passthrough");

        // supertype lattice
        check(RSAKey.class.isAssignableFrom(RSAPrivateKey.class), "is an RSAKey");
        check(PrivateKey.class.isAssignableFrom(RSAPrivateKey.class), "is a PrivateKey");
        check(Key.class.isAssignableFrom(RSAPrivateKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(RSAPrivateKey.class), "is Serializable");
        check(RSAPrivateKey.class.getInterfaces().length == 2, "exactly two direct superinterfaces");

        // real generated key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPrivateKey key = (RSAPrivateKey) kp.getPrivate();
        check(key instanceof RSAKey && key instanceof PrivateKey && key instanceof Serializable,
                "real key supertypes");
        check("RSA".equals(key.getAlgorithm()), "getAlgorithm is RSA");
        check("PKCS#8".equals(key.getFormat()), "getFormat is PKCS#8");
        check(key.getEncoded() != null && key.getEncoded().length > 0, "encoded non-empty");
        check(key.getModulus().bitLength() == 2048, "modulus is 2048 bits");
        check(key.getPrivateExponent().signum() > 0, "private exponent positive");
        check(key.getPrivateExponent().compareTo(key.getModulus()) < 0, "d < n");

        // RSA math closed loop: d*e == 1 (mod lcm(p-1, q-1)) using the CRT primes
        RSAPrivateCrtKey crt = (RSAPrivateCrtKey) key;
        BigInteger p1 = crt.getPrimeP().subtract(BigInteger.ONE);
        BigInteger q1 = crt.getPrimeQ().subtract(BigInteger.ONE);
        BigInteger lcm = p1.multiply(q1).divide(p1.gcd(q1));
        BigInteger e = crt.getPublicExponent();
        check(key.getPrivateExponent().multiply(e).mod(lcm).equals(BigInteger.ONE),
                "d*e == 1 (mod lcm(p-1, q-1))");

        // (n, d) KeyFactory round-trip: values match, CRT information is dropped
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPrivateKeySpec spec = kf.getKeySpec(key, RSAPrivateKeySpec.class);
        check(spec.getModulus().equals(key.getModulus()), "spec preserves modulus");
        check(spec.getPrivateExponent().equals(key.getPrivateExponent()), "spec preserves d");
        RSAPrivateKey back = (RSAPrivateKey) kf.generatePrivate(
                new RSAPrivateKeySpec(spec.getModulus(), spec.getPrivateExponent()));
        check(back.getModulus().equals(key.getModulus()), "round-trip modulus identical");
        check(back.getPrivateExponent().equals(key.getPrivateExponent()), "round-trip d identical");
        check(!(back instanceof RSAPrivateCrtKey), "(n,d) round-trip is not a CRT key");
        check(!back.equals(key), "(n,d) round-trip differs from the CRT original encoding");

        // serialization round-trip via the KeyRep machinery
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy instanceof RSAPrivateKey, "deserialized still an RSAPrivateKey");
        check(copy.equals(key), "serialization round-trip equals");

        // equals semantics
        check(key.equals(key), "reflexive equals");
        check(!key.equals(kp.getPublic()), "private not equal to public half");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements RSAPrivateKey {
        public BigInteger getPrivateExponent() { return BigInteger.valueOf(2753); }
        public BigInteger getModulus() { return BigInteger.valueOf(3233); }
        public String getAlgorithm() { return "RSA-REC"; }
        public String getFormat() { return "PKCS#8-REC"; }
        public byte[] getEncoded() { return new byte[]{4, 4}; }
    }
}
