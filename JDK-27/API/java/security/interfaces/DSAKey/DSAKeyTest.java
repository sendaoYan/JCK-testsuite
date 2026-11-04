/*
 * @test
 * @summary java.security.interfaces.DSAKey conformance: white-box structure + black-box behavior.
 * @run main DSAKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class DSAKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getParams"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Both halves of a real generated DSA pair implement DSAKey
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        DSAKey pub = (DSAKey) kp.getPublic();
        DSAKey priv = (DSAKey) kp.getPrivate();
        check(pub != null && priv != null, "both halves implement DSAKey");
        DSAParams pp = pub.getParams();
        DSAParams sp = priv.getParams();
        check(pp != null && sp != null, "params present");
        // parameter set: 2048-bit p, 224-bit q, generator in range
        check(pp.getP().bitLength() == 2048, "p is 2048 bits");
        check(pp.getQ().bitLength() == 224, "q is 224 bits");
        check(pp.getG().signum() > 0 && pp.getG().compareTo(pp.getP()) < 0, "g in [1, p)");
        // public and private halves carry the same domain parameters
        check(pp.getP().equals(sp.getP()) && pp.getQ().equals(sp.getQ()) && pp.getG().equals(sp.getG()),
            "shared domain parameters");
        // params are themselves exposed for reuse in key generation
        KeyPairGenerator reuse = KeyPairGenerator.getInstance("DSA");
        reuse.initialize(new DSAParameterSpec(pp.getP(), pp.getQ(), pp.getG()));
        KeyPair kp2 = reuse.generateKeyPair();
        DSAParams again = ((DSAKey) kp2.getPublic()).getParams();
        check(again.getP().equals(pp.getP()) && again.getQ().equals(pp.getQ()) && again.getG().equals(pp.getG()),
            "params reusable for generation");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — record impl, lattice, equals semantics ----
    static void testContract() throws Exception {
        // record-style minimal implementation passes the holder value through, null included
        DSAParams recParams = new RecParams();
        DSAKey rec = new RecDSAKey(recParams);
        check(rec.getParams() == recParams, "record getParams identity passthrough");
        check(new RecDSAKey(null).getParams() == null, "record null params passthrough");

        // lattice: DSAKey is a bare interface, not a Key, not Serializable, no constant
        check(!Key.class.isAssignableFrom(DSAKey.class), "DSAKey is not a Key");
        check(!Serializable.class.isAssignableFrom(DSAKey.class), "DSAKey is not Serializable");
        check(DSAKey.class.getInterfaces().length == 0, "DSAKey has no superinterfaces");
        check(DSAKey.class.isAssignableFrom(DSAPublicKey.class), "DSAPublicKey is a DSAKey");
        check(DSAKey.class.isAssignableFrom(DSAPrivateKey.class), "DSAPrivateKey is a DSAKey");
        try {
            DSAKey.class.getDeclaredField("serialVersionUID");
            check(false, "DSAKey declares no serialVersionUID");
        } catch (NoSuchFieldException e) {
            check(true, "DSAKey declares no serialVersionUID");
        }

        // domain parameter math: order of g divides q, and y == g^x mod p ties the halves
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        DSAKey pub = (DSAKey) kp.getPublic();
        DSAKey priv = (DSAKey) kp.getPrivate();
        DSAParams pp = pub.getParams();
        check(pp.getG().modPow(pp.getQ(), pp.getP()).equals(BigInteger.ONE), "g^q mod p == 1");
        check(pp.getG().modPow(((DSAPrivateKey) priv).getX(), pp.getP())
                .equals(((DSAPublicKey) pub).getY()), "y == g^x mod p");
        check(pp.getQ().isProbablePrime(64), "q is prime");

        // equals semantics
        check(pub.equals(pub), "reflexive equals");
        check(!pub.equals(priv), "public half not equal to private half");
        check(!pub.equals(rec), "real key not equal to record impl");
        check(pub.hashCode() == pub.hashCode(), "hashCode stable");
    }

    static class RecParams implements DSAParams {
        public BigInteger getP() { return BigInteger.valueOf(23); }
        public BigInteger getQ() { return BigInteger.valueOf(11); }
        public BigInteger getG() { return BigInteger.valueOf(2); }
    }

    static class RecDSAKey implements DSAKey {
        private final DSAParams params;
        RecDSAKey(DSAParams params) { this.params = params; }
        public DSAParams getParams() { return params; }
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
