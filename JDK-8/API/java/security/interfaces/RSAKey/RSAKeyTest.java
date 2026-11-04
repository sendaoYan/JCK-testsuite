/*
 * @test
 * @summary java.security.interfaces.RSAKey conformance: interface hierarchy and key methods,
 *          default method getParams() (returns null for implementations that do not
 *          override it, both on record-style impls and on real SunRsaSign keys),
 *          record-style getModulus passthrough, supertype lattice (no superinterfaces,
 *          not a Key, not Serializable), real RSA-2048 modulus size, no serialVersionUID.
 * @run main RSAKeyTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;

public class RSAKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getModulus"), "declares getModulus");
        check(ms.contains("getParams"), "declares getParams");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — default method, record impl, real-key closed loops ----
    static void testContract() throws Exception {
        // default getParams() returns null for implementations that do not override it
        RSAKey rec = new RecRSAKey();
        check(rec.getModulus().equals(BigInteger.valueOf(3233)), "record getModulus passthrough");
        check(rec.getParams() == null, "default getParams returns null when not overridden");
        check(new NullModulusKey().getParams() == null, "default getParams null independent of modulus");

        // supertype lattice
        check(RSAKey.class.getInterfaces().length == 0, "no superinterfaces");
        check(!Key.class.isAssignableFrom(RSAKey.class), "not a Key");
        check(!Serializable.class.isAssignableFrom(RSAKey.class), "not Serializable");
        check(RSAKey.class.isAssignableFrom(RSAPublicKey.class), "RSAPublicKey is an RSAKey");
        check(RSAKey.class.isAssignableFrom(RSAPrivateKey.class), "RSAPrivateKey is an RSAKey");
        check(RSAKey.class.isAssignableFrom(RSAPrivateCrtKey.class), "RSAPrivateCrtKey is an RSAKey");
        try {
            RSAKey.class.getDeclaredField("serialVersionUID");
            check(false, "no serialVersionUID declared");
        } catch (NoSuchFieldException e) {
            check(true, "no serialVersionUID declared");
        }

        // real RSA-2048 pair: both halves are RSAKeys with a 2048-bit modulus
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAKey pub = (RSAKey) kp.getPublic();
        RSAKey priv = (RSAKey) kp.getPrivate();
        check(pub instanceof RSAPublicKey, "public half is an RSAPublicKey");
        check(priv instanceof RSAPrivateKey, "private half is an RSAPrivateKey");
        check(pub.getModulus().bitLength() == 2048, "public modulus is 2048 bits");
        check(priv.getModulus().equals(pub.getModulus()), "both halves share the modulus");
        check(pub.getParams() == null, "real RSA public key params are null");
        check(priv.getParams() == null, "real RSA private key params are null");
        check(pub.getModulus().signum() > 0, "modulus positive");

        // modulus size can be steered through the generator spec
        KeyPairGenerator k1024 = KeyPairGenerator.getInstance("RSA");
        k1024.initialize(new RSAKeyGenParameterSpec(1024, BigInteger.valueOf(3)));
        RSAKey small = (RSAKey) k1024.generateKeyPair().getPublic();
        check(small.getModulus().bitLength() == 1024, "1024-bit modulus via RSAKeyGenParameterSpec");
        check(((RSAPublicKey) small).getPublicExponent().intValue() == 3,
                "public exponent 3 via RSAKeyGenParameterSpec");

        // cross-family discrimination: a DSA key is not an RSAKey
        java.security.KeyPairGenerator dsa = java.security.KeyPairGenerator.getInstance("DSA");
        dsa.initialize(1024);
        check(!(dsa.generateKeyPair().getPrivate() instanceof RSAKey),
                "DSA private key is not an RSAKey");
        check(!RSAKey.class.isAssignableFrom(DSAPrivateKey.class),
                "DSAPrivateKey not assignable to RSAKey");

        // equals semantics
        check(pub.equals(pub), "reflexive equals");
        check(!pub.equals(priv), "public half not equal to private half");
    }

    static class RecRSAKey implements RSAKey {
        public BigInteger getModulus() { return BigInteger.valueOf(3233); }
    }

    static class NullModulusKey implements RSAKey {
        public BigInteger getModulus() { return null; }
    }
}
