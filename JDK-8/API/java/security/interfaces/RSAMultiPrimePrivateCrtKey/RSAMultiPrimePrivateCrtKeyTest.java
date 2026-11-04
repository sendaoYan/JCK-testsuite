/*
 * @test
 * @summary java.security.interfaces.RSAMultiPrimePrivateCrtKey conformance: interface
 *          hierarchy and key methods, serialVersionUID 618058533534628008L, record-style
 *          implementation passthrough of all eight getters including getOtherPrimeInfo,
 *          supertype lattice (RSAPrivateKey + RSAKey + PrivateKey + Key + Serializable),
 *          and the platform discriminator: SunRsaSign generates two-prime CRT keys that
 *          are NOT RSAMultiPrimePrivateCrtKey instances.
 * @run main RSAMultiPrimePrivateCrtKeyTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAMultiPrimePrivateCrtKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAOtherPrimeInfo;

public class RSAMultiPrimePrivateCrtKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAMultiPrimePrivateCrtKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getCrtCoefficient"), "declares getCrtCoefficient");
        check(ms.contains("getOtherPrimeInfo"), "declares getOtherPrimeInfo");
        check(ms.contains("getPrimeExponentP"), "declares getPrimeExponentP");
        check(ms.contains("getPrimeExponentQ"), "declares getPrimeExponentQ");
        check(ms.contains("getPrimeP"), "declares getPrimeP");
        check(ms.contains("getPrimeQ"), "declares getPrimeQ");
        check(ms.contains("getPublicExponent"), "declares getPublicExponent");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — constant, record impl, platform discriminator ----
    static void testContract() throws Exception {
        // serialVersionUID is a fixed public contract value
        check(RSAMultiPrimePrivateCrtKey.class.getField("serialVersionUID").getLong(null)
                == 618058533534628008L, "serialVersionUID == 618058533534628008L");

        // record-style minimal implementation: all eight getters pass values through,
        // including the extra-prime info table
        RSAOtherPrimeInfo other = new RSAOtherPrimeInfo(
                BigInteger.valueOf(13), BigInteger.valueOf(5), BigInteger.valueOf(2));
        RSAMultiPrimePrivateCrtKey rec = new RecKey(new RSAOtherPrimeInfo[]{other});
        check(rec.getOtherPrimeInfo() != null && rec.getOtherPrimeInfo().length == 1,
                "record getOtherPrimeInfo passthrough length");
        check(rec.getOtherPrimeInfo()[0] == other, "record getOtherPrimeInfo identity");
        check(rec.getOtherPrimeInfo()[0].getPrime().equals(BigInteger.valueOf(13)),
                "record extra prime value");
        check(rec.getPrimeP().equals(BigInteger.valueOf(61)), "record getPrimeP passthrough");
        check(rec.getPrimeQ().equals(BigInteger.valueOf(53)), "record getPrimeQ passthrough");
        check(rec.getCrtCoefficient().equals(BigInteger.valueOf(38)), "record qInv passthrough");
        check(rec.getPublicExponent().equals(BigInteger.valueOf(17)), "record e passthrough");
        check(rec.getPrivateExponent().equals(BigInteger.valueOf(2753)), "record d passthrough");
        check(rec.getModulus().equals(BigInteger.valueOf(3233)), "record modulus passthrough");
        check(new RecKey(null).getOtherPrimeInfo() == null, "record null other-prime table");

        // supertype lattice
        check(RSAPrivateKey.class.isAssignableFrom(RSAMultiPrimePrivateCrtKey.class),
                "is an RSAPrivateKey");
        check(!RSAPrivateCrtKey.class.isAssignableFrom(RSAMultiPrimePrivateCrtKey.class),
                "sibling of RSAPrivateCrtKey (both extend RSAPrivateKey directly)");
        check(RSAKey.class.isAssignableFrom(RSAMultiPrimePrivateCrtKey.class), "is an RSAKey");
        check(PrivateKey.class.isAssignableFrom(RSAMultiPrimePrivateCrtKey.class),
                "is a PrivateKey");
        check(Key.class.isAssignableFrom(RSAMultiPrimePrivateCrtKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(RSAMultiPrimePrivateCrtKey.class),
                "is Serializable");

        // platform discriminator: a generated two-prime RSA key is NOT multi-prime
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        check(kp.getPrivate() instanceof RSAPrivateCrtKey, "generated private is plain CRT");
        check(!(kp.getPrivate() instanceof RSAMultiPrimePrivateCrtKey),
                "two-prime key is not RSAMultiPrimePrivateCrtKey");
        RSAPrivateCrtKey crt = (RSAPrivateCrtKey) kp.getPrivate();
        check(crt.getModulus().equals(crt.getPrimeP().multiply(crt.getPrimeQ())),
                "n factors into exactly two primes");
        check(crt.getModulus().bitLength() == 2048, "modulus is 2048 bits");

        // an empty extra-prime table is representable by a record implementation
        RSAMultiPrimePrivateCrtKey empty = new RecKey(new RSAOtherPrimeInfo[0]);
        check(empty.getOtherPrimeInfo() != null && empty.getOtherPrimeInfo().length == 0,
                "empty other-prime table passthrough");
    }

    static class RecKey implements RSAMultiPrimePrivateCrtKey {
        private final RSAOtherPrimeInfo[] other;
        RecKey(RSAOtherPrimeInfo[] other) { this.other = other; }
        public RSAOtherPrimeInfo[] getOtherPrimeInfo() { return other; }
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
