/*
 * @test
 * @summary java.security.interfaces.RSAKey conformance: white-box structure + black-box behavior.
 * @run main RSAKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class RSAKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getModulus", "getParams"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAKey pub = (RSAKey) kp.getPublic();
        RSAKey priv = (RSAKey) kp.getPrivate();
        check(pub != null && priv != null, "both halves implement RSAKey");
        // shared modulus, exact size
        check(pub.getModulus().bitLength() == 2048, "2048-bit modulus");
        check(pub.getModulus().equals(priv.getModulus()), "shared modulus");
        // default getParams(): null unless the key carries explicit parameters
        check(pub.getParams() == null, "public params null");
        check(priv.getParams() == null, "private params null");
        // sizes are selectable and reported through the interface
        KeyPairGenerator k3072 = KeyPairGenerator.getInstance("RSA");
        k3072.initialize(3072);
        check(((RSAKey) k3072.generateKeyPair().getPublic()).getModulus().bitLength() == 3072, "3072-bit modulus");
        // modulus is odd (product of two odd primes)
        check(pub.getModulus().testBit(0), "modulus odd");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — default method, record impl, lattice, steering ----
    static void testContract() throws Exception {
        // default getParams() returns null for implementations that do not override it
        RSAKey rec = new RecRSAKey();
        check(rec.getModulus().equals(BigInteger.valueOf(3233)), "record getModulus passthrough");
        check(rec.getParams() == null, "default getParams returns null when not overridden");
        check(new NullModulusKey().getParams() == null,
                "default getParams null independent of modulus");

        // lattice: bare interface, not a Key, not Serializable, no constant
        check(RSAKey.class.getInterfaces().length == 0, "no superinterfaces");
        check(!Key.class.isAssignableFrom(RSAKey.class), "not a Key");
        check(!Serializable.class.isAssignableFrom(RSAKey.class), "not Serializable");
        check(RSAKey.class.isAssignableFrom(RSAPublicKey.class), "RSAPublicKey is an RSAKey");
        check(RSAKey.class.isAssignableFrom(RSAPrivateKey.class), "RSAPrivateKey is an RSAKey");
        try {
            RSAKey.class.getDeclaredField("serialVersionUID");
            check(false, "no serialVersionUID declared");
        } catch (NoSuchFieldException e) {
            check(true, "no serialVersionUID declared");
        }

        // modulus size and exponent steered through RSAKeyGenParameterSpec
        KeyPairGenerator k1024 = KeyPairGenerator.getInstance("RSA");
        k1024.initialize(new RSAKeyGenParameterSpec(1024, BigInteger.valueOf(3)));
        KeyPair small = k1024.generateKeyPair();
        check(((RSAKey) small.getPublic()).getModulus().bitLength() == 1024,
                "1024-bit modulus via RSAKeyGenParameterSpec");
        check(((RSAPublicKey) small.getPublic()).getPublicExponent().intValue() == 3,
                "public exponent 3 via RSAKeyGenParameterSpec");
        check(((RSAKey) small.getPrivate()).getModulus().equals(
                ((RSAKey) small.getPublic()).getModulus()), "shared modulus after steering");

        // cross-family discrimination: a DSA key is not an RSAKey
        KeyPairGenerator dsa = KeyPairGenerator.getInstance("DSA");
        dsa.initialize(1024);
        check(!(dsa.generateKeyPair().getPrivate() instanceof RSAKey),
                "DSA private key is not an RSAKey");
        check(!RSAKey.class.isAssignableFrom(DSAPrivateKey.class),
                "DSAPrivateKey not assignable to RSAKey");

        // equals semantics
        RSAKey pub = (RSAKey) KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();
        check(pub.equals(pub), "reflexive equals");
        check(!pub.equals(rec), "real key not equal to record impl");
    }

    static class RecRSAKey implements RSAKey {
        public BigInteger getModulus() { return BigInteger.valueOf(3233); }
    }

    static class NullModulusKey implements RSAKey {
        public BigInteger getModulus() { return null; }
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
