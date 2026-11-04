/*
 * @test
 * @summary java.security.interfaces.RSAPrivateCrtKey conformance: white-box structure + black-box behavior.
 * @run main RSAPrivateCrtKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class RSAPrivateCrtKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAPrivateCrtKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.interfaces.RSAPrivateKey"});
        checkMethods(c, new String[]{"getCrtCoefficient", "getPrimeExponentP", "getPrimeExponentQ", "getPrimeP", "getPrimeQ", "getPublicExponent"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPrivateCrtKey key = (RSAPrivateCrtKey) kp.getPrivate();
        check(RSAPrivateCrtKey.serialVersionUID == -5682214253527700368L, "serialVersionUID");
        check(key instanceof RSAPrivateKey && key instanceof RSAKey, "supertypes");
        // CRT fields present and mathematically consistent
        BigInteger p = key.getPrimeP(), q = key.getPrimeQ();
        check(p.bitLength() == 1024 && q.bitLength() == 1024, "half-size primes");
        check(p.isProbablePrime(64) && q.isProbablePrime(64), "both primes");
        check(key.getPublicExponent().intValue() == 65537, "standard exponent");
        check(key.getPrimeExponentP().equals(key.getPrivateExponent().mod(p.subtract(BigInteger.ONE))),
            "dP = d mod (p-1)");
        check(key.getPrimeExponentQ().equals(key.getPrivateExponent().mod(q.subtract(BigInteger.ONE))),
            "dQ = d mod (q-1)");
        check(key.getCrtCoefficient().equals(q.modInverse(p)), "qInv = q^-1 mod p");
        check(key.getModulus().equals(p.multiply(q)), "n = p*q");
        check(key.getFormat().equals("PKCS#8"), "PKCS#8 format");
        // a CRT key signs and the public half verifies
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(key);
        byte[] msg = "jck-crt".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(kp.getPublic());
        sig.update(msg);
        check(sig.verify(s), "CRT key signs");
        // KeyFactory round trip from a CRT spec reproduces every field
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPrivateCrtKey back = (RSAPrivateCrtKey) kf.generatePrivate(new RSAPrivateCrtKeySpec(
            key.getModulus(), key.getPublicExponent(), key.getPrivateExponent(),
            p, q, key.getPrimeExponentP(), key.getPrimeExponentQ(), key.getCrtCoefficient()));
        check(back.getPrimeP().equals(p) && back.getPrimeQ().equals(q), "round trip primes");
        check(back.getCrtCoefficient().equals(key.getCrtCoefficient()), "round trip coefficient");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
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
