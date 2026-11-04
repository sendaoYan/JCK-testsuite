/*
 * @test
 * @summary javax.crypto.spec.DHParameterSpec conformance: white-box structure + black-box behavior.
 * @run main DHParameterSpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;

public class DHParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** RFC 2409 group 1: the well-known 768-bit MODP prime, generator 2. */
    static final BigInteger P768 = new BigInteger(
        "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
        "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
        "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
        "E485B576625E7EC6F44C42E9A63A3620FFFFFFFFFFFFFFFF", 16);
    static final BigInteger G2 = BigInteger.valueOf(2);

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = DHParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()), "plain class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getP", "getG", "getL"});
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 2 && k.getParameterTypes()[0] == BigInteger.class
                    && k.getParameterTypes()[1] == BigInteger.class) ctors++;
            if (k.getParameterCount() == 3 && k.getParameterTypes()[0] == BigInteger.class
                    && k.getParameterTypes()[1] == BigInteger.class
                    && k.getParameterTypes()[2] == int.class) ctors++;
        }
        check(ctors == 2 && c.getConstructors().length == 2, "two public ctors (p,g)/(p,g,l)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (preserved) small-value accessors
        BigInteger p = BigInteger.valueOf(23);
        BigInteger g = BigInteger.valueOf(5);
        DHParameterSpec s = new DHParameterSpec(p, g, 8);
        check(s.getP().equals(p) && s.getG().equals(g), "getP/getG");
        check(s.getL() == 8, "getL");
        check(new DHParameterSpec(p, g).getL() == 0, "default L 0");
        check(s instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");

        // getters return the exact same value objects (BigInteger immutable passthrough)
        DHParameterSpec big = new DHParameterSpec(P768, G2, 96);
        check(big.getP() == P768 || big.getP().equals(P768), "768-bit p exact");
        check(big.getG().equals(G2) && big.getG().intValue() == 2, "g == 2");
        check(big.getL() == 96, "explicit l");

        // p from hex string round trip: toString(16) parses back to the same value
        check(new BigInteger(big.getP().toString(16), 16).equals(P768), "p hex roundtrip");

        // AlgorithmParameters("DH") roundtrip preserves p, g and l
        AlgorithmParameters ap = AlgorithmParameters.getInstance("DH");
        ap.init(big);
        DHParameterSpec back = ap.getParameterSpec(DHParameterSpec.class);
        check(back.getP().equals(P768), "AlgorithmParameters p roundtrip");
        check(back.getG().equals(G2), "AlgorithmParameters g roundtrip");
        check(back.getL() == 96, "AlgorithmParameters l roundtrip");

        // real chain: fixed 768-bit group -> two key pairs -> identical shared secrets
        DHParameterSpec group = new DHParameterSpec(P768, G2);
        KeyPairGenerator kpgA = KeyPairGenerator.getInstance("DiffieHellman");
        kpgA.initialize(group);
        KeyPair kpA = kpgA.generateKeyPair();
        KeyPairGenerator kpgB = KeyPairGenerator.getInstance("DiffieHellman");
        kpgB.initialize(group);
        KeyPair kpB = kpgB.generateKeyPair();
        javax.crypto.interfaces.DHPublicKey pubA = (javax.crypto.interfaces.DHPublicKey) kpA.getPublic();
        javax.crypto.interfaces.DHPublicKey pubB = (javax.crypto.interfaces.DHPublicKey) kpB.getPublic();
        check(pubA.getParams().getP().equals(P768), "key params p equals fixed p");
        check(pubA.getParams().getG().equals(G2) && pubB.getParams().getG().equals(G2),
                "key params g equals fixed g");
        check(pubA.getY().mod(P768).signum() >= 0 && pubA.getY().bitLength() <= 768, "y in range");
        KeyAgreement kaA = KeyAgreement.getInstance("DiffieHellman");
        kaA.init(kpA.getPrivate());
        kaA.doPhase(kpB.getPublic(), true);
        byte[] secretA = kaA.generateSecret();
        KeyAgreement kaB = KeyAgreement.getInstance("DiffieHellman");
        kaB.init(kpB.getPrivate());
        kaB.doPhase(kpA.getPublic(), true);
        check(java.util.Arrays.equals(secretA, kaB.generateSecret()), "shared secret identical");
        check(secretA.length == 96, "768-bit secret length 96");
        // repeated agreement is deterministic for the same key pair
        KeyAgreement kaA2 = KeyAgreement.getInstance("DiffieHellman");
        kaA2.init(kpA.getPrivate());
        kaA2.doPhase(kpB.getPublic(), true);
        check(java.util.Arrays.equals(secretA, kaA2.generateSecret()), "agreement deterministic");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
}
