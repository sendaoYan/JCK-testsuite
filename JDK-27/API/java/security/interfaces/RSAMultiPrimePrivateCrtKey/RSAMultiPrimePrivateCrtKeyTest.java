/*
 * @test
 * @summary java.security.interfaces.RSAMultiPrimePrivateCrtKey conformance: white-box structure + black-box behavior.
 * @run main RSAMultiPrimePrivateCrtKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class RSAMultiPrimePrivateCrtKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAMultiPrimePrivateCrtKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.interfaces.RSAPrivateKey"});
        checkMethods(c, new String[]{"getCrtCoefficient", "getOtherPrimeInfo", "getPrimeExponentP", "getPrimeExponentQ", "getPrimeP", "getPrimeQ", "getPublicExponent"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // No JDK provider produces multi-prime keys: the interface is exercised by an implementation
        RSAOtherPrimeInfo other = new RSAOtherPrimeInfo(
            new BigInteger("c1a7b3d5", 16), new BigInteger("8f2a91", 16), new BigInteger("5d1c", 16));
        class Impl implements RSAMultiPrimePrivateCrtKey {
            final RSAOtherPrimeInfo[] others;
            Impl(RSAOtherPrimeInfo[] o) { others = o; }
            public BigInteger getModulus() { return new BigInteger("a538c", 16); }
            public java.security.spec.AlgorithmParameterSpec getParams() { return null; }
            public BigInteger getPrivateExponent() { return new BigInteger("11", 16); }
            public BigInteger getPublicExponent() { return BigInteger.valueOf(65537); }
            public BigInteger getPrimeP() { return new BigInteger("1f", 16); }
            public BigInteger getPrimeQ() { return new BigInteger("2b", 16); }
            public BigInteger getPrimeExponentP() { return new BigInteger("3", 16); }
            public BigInteger getPrimeExponentQ() { return new BigInteger("5", 16); }
            public BigInteger getCrtCoefficient() { return new BigInteger("7", 16); }
            public RSAOtherPrimeInfo[] getOtherPrimeInfo() { return others.clone(); }
            public String getAlgorithm() { return "RSA"; }
            public String getFormat() { return "PKCS#8"; }
            public byte[] getEncoded() { return new byte[0]; }
        }
        Impl impl = new Impl(new RSAOtherPrimeInfo[]{other});
        check(RSAMultiPrimePrivateCrtKey.serialVersionUID == 618058533534628008L, "serialVersionUID");
        // JDK-27 hierarchy: RSAMultiPrimePrivateCrtKey extends RSAPrivateKey (sibling of RSAPrivateCrtKey)
        check(impl instanceof RSAPrivateKey, "extends RSAPrivateKey");
        check(impl.getOtherPrimeInfo().length == 1, "one extra prime");
        check(impl.getOtherPrimeInfo()[0].getPrime().equals(new BigInteger("c1a7b3d5", 16)), "prime value");
        check(impl.getOtherPrimeInfo()[0].getExponent().equals(new BigInteger("8f2a91", 16)), "exponent value");
        check(impl.getOtherPrimeInfo()[0].getCrtCoefficient().equals(new BigInteger("5d1c", 16)), "crt value");
        check(impl.getOtherPrimeInfo() != impl.getOtherPrimeInfo(), "defensive copy each call");
        // empty extra-prime array is legal
        Impl empty = new Impl(new RSAOtherPrimeInfo[0]);
        check(empty.getOtherPrimeInfo().length == 0, "no extra primes");
        // JDK reality: generated keys are two-prime CRT keys, and the RSA KeyFactory
        // collapses multi-prime specs into a plain RSAPrivateKey
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        check(!(kp.getPrivate() instanceof RSAMultiPrimePrivateCrtKey), "generated keys are two-prime");
        RSAPrivateCrtKey crt = (RSAPrivateCrtKey) kp.getPrivate();
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey translated = kf.generatePrivate(new RSAMultiPrimePrivateCrtKeySpec(
            crt.getModulus(), crt.getPublicExponent(), crt.getPrivateExponent(),
            crt.getPrimeP(), crt.getPrimeQ(), crt.getPrimeExponentP(), crt.getPrimeExponentQ(),
            crt.getCrtCoefficient(), new RSAOtherPrimeInfo[]{other}));
        check(!(translated instanceof RSAMultiPrimePrivateCrtKey), "factory drops multi-prime info");
        check(translated instanceof RSAPrivateKey, "still an RSAPrivateKey");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — lattice and RSAOtherPrimeInfo construction ----
    static void testContract() throws Exception {
        // lattice: extends RSAPrivateKey directly, sibling of RSAPrivateCrtKey
        check(RSAPrivateKey.class.isAssignableFrom(RSAMultiPrimePrivateCrtKey.class),
                "is an RSAPrivateKey");
        check(!RSAPrivateCrtKey.class.isAssignableFrom(RSAMultiPrimePrivateCrtKey.class),
                "sibling of RSAPrivateCrtKey (both extend RSAPrivateKey directly)");
        check(RSAPrivateCrtKey.class.getSuperclass() == null
                && RSAPrivateCrtKey.class.getInterfaces().length == 1,
                "RSAPrivateCrtKey has a single direct superinterface");
        check(Key.class.isAssignableFrom(RSAMultiPrimePrivateCrtKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(RSAMultiPrimePrivateCrtKey.class),
                "is Serializable");

        // RSAOtherPrimeInfo rejects null components
        BigInteger v = BigInteger.valueOf(7);
        try {
            new RSAOtherPrimeInfo(null, v, v);
            check(false, "RSAOtherPrimeInfo(null prime) throws NPE");
        } catch (NullPointerException e) {
            check(true, "RSAOtherPrimeInfo(null prime) throws NPE");
        }
        try {
            new RSAOtherPrimeInfo(v, null, v);
            check(false, "RSAOtherPrimeInfo(null exponent) throws NPE");
        } catch (NullPointerException e) {
            check(true, "RSAOtherPrimeInfo(null exponent) throws NPE");
        }
        try {
            new RSAOtherPrimeInfo(v, v, null);
            check(false, "RSAOtherPrimeInfo(null coefficient) throws NPE");
        } catch (NullPointerException e) {
            check(true, "RSAOtherPrimeInfo(null coefficient) throws NPE");
        }
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
