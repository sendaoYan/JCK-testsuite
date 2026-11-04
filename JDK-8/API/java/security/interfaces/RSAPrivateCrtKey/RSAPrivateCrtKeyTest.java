/*
 * @test
 * @summary java.security.interfaces.RSAPrivateCrtKey conformance: interface hierarchy and key methods,
 *          serialVersionUID -5682214253527700368L, record-style implementation passthrough of
 *          all seven CRT getters, supertype lattice (RSAPrivateKey + RSAKey + PrivateKey +
 *          Key + Serializable), real SunRsaSign CRT key math contracts: dP = d mod (p-1),
 *          dQ = d mod (q-1), qInv = q^-1 mod p, n = p*q, RSAPrivateCrtKeySpec round-trip.
 * @run main RSAPrivateCrtKeyTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateCrtKeySpec;

public class RSAPrivateCrtKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAPrivateCrtKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getCrtCoefficient"), "declares getCrtCoefficient");
        check(ms.contains("getPrimeExponentP"), "declares getPrimeExponentP");
        check(ms.contains("getPrimeExponentQ"), "declares getPrimeExponentQ");
        check(ms.contains("getPrimeP"), "declares getPrimeP");
        check(ms.contains("getPrimeQ"), "declares getPrimeQ");
        check(ms.contains("getPublicExponent"), "declares getPublicExponent");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — constant, record impl, real CRT math ----
    static void testContract() throws Exception {
        // serialVersionUID is a fixed public contract value
        check(RSAPrivateCrtKey.class.getField("serialVersionUID").getLong(null)
                == -5682214253527700368L, "serialVersionUID == -5682214253527700368L");

        // record-style minimal implementation: all seven CRT getters pass values through
        RSAPrivateCrtKey rec = new RecKey();
        check(rec.getPrimeP().equals(BigInteger.valueOf(61)), "record getPrimeP passthrough");
        check(rec.getPrimeQ().equals(BigInteger.valueOf(53)), "record getPrimeQ passthrough");
        check(rec.getPrimeExponentP().equals(BigInteger.valueOf(53)), "record dP passthrough");
        check(rec.getPrimeExponentQ().equals(BigInteger.valueOf(49)), "record dQ passthrough");
        check(rec.getCrtCoefficient().equals(BigInteger.valueOf(38)), "record qInv passthrough");
        check(rec.getPublicExponent().equals(BigInteger.valueOf(17)), "record e passthrough");
        check(rec.getPrivateExponent().equals(BigInteger.valueOf(2753)), "record d passthrough");

        // supertype lattice
        check(RSAPrivateKey.class.isAssignableFrom(RSAPrivateCrtKey.class), "is an RSAPrivateKey");
        check(RSAKey.class.isAssignableFrom(RSAPrivateCrtKey.class), "is an RSAKey");
        check(PrivateKey.class.isAssignableFrom(RSAPrivateCrtKey.class), "is a PrivateKey");
        check(Key.class.isAssignableFrom(RSAPrivateCrtKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(RSAPrivateCrtKey.class), "is Serializable");

        // real SunRsaSign private key is a CRT key whose fields satisfy the CRT equations
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        check(kp.getPrivate() instanceof RSAPrivateCrtKey, "generated private key is a CRT key");
        RSAPrivateCrtKey key = (RSAPrivateCrtKey) kp.getPrivate();
        BigInteger p = key.getPrimeP(), q = key.getPrimeQ();
        check(p.bitLength() == 1024 && q.bitLength() == 1024, "half-size primes");
        check(p.isProbablePrime(64) && q.isProbablePrime(64), "both factors prime");
        check(!p.equals(q), "distinct primes");
        check(key.getModulus().equals(p.multiply(q)), "n = p*q");
        check(key.getPublicExponent().intValue() == 65537, "standard exponent 65537");
        check(key.getPrimeExponentP().equals(
                key.getPrivateExponent().mod(p.subtract(BigInteger.ONE))), "dP = d mod (p-1)");
        check(key.getPrimeExponentQ().equals(
                key.getPrivateExponent().mod(q.subtract(BigInteger.ONE))), "dQ = d mod (q-1)");
        check(key.getCrtCoefficient().equals(q.modInverse(p)), "qInv = q^-1 mod p");
        check("PKCS#8".equals(key.getFormat()), "PKCS#8 format");
        check(key.getEncoded() != null && key.getEncoded().length > 0, "encoded non-empty");

        // KeyFactory CRT-spec round-trip reproduces every field
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPrivateCrtKey back = (RSAPrivateCrtKey) kf.generatePrivate(new RSAPrivateCrtKeySpec(
                key.getModulus(), key.getPublicExponent(), key.getPrivateExponent(),
                p, q, key.getPrimeExponentP(), key.getPrimeExponentQ(), key.getCrtCoefficient()));
        check(back.getPrimeP().equals(p) && back.getPrimeQ().equals(q), "round-trip primes");
        check(back.getCrtCoefficient().equals(key.getCrtCoefficient()), "round-trip coefficient");
        check(back.equals(key), "round-trip equals");

        // the CRT key signs and the public half verifies
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(key);
        byte[] msg = "crt-loop".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(kp.getPublic());
        sig.update(msg);
        check(sig.verify(s), "CRT key signs verifiably");

        // equals semantics
        check(key.equals(key), "reflexive equals");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements RSAPrivateCrtKey {
        public BigInteger getPrimeP() { return BigInteger.valueOf(61); }
        public BigInteger getPrimeQ() { return BigInteger.valueOf(53); }
        public BigInteger getPrimeExponentP() { return BigInteger.valueOf(53); }
        public BigInteger getPrimeExponentQ() { return BigInteger.valueOf(49); }
        public BigInteger getCrtCoefficient() { return BigInteger.valueOf(38); }
        public BigInteger getPublicExponent() { return BigInteger.valueOf(17); }
        public BigInteger getPrivateExponent() { return BigInteger.valueOf(2753); }
        public BigInteger getModulus() { return BigInteger.valueOf(3233); }
        public String getAlgorithm() { return "RSA-REC"; }
        public String getFormat() { return "PKCS#8-REC"; }
        public byte[] getEncoded() { return new byte[]{6}; }
    }
}
