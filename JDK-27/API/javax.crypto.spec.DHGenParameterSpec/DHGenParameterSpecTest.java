/*
 * @test
 * @summary javax.crypto.spec.DHGenParameterSpec conformance: white-box structure + black-box behavior.
 * @run main DHGenParameterSpecTest
 */

import java.lang.reflect.*;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHGenParameterSpec;
import javax.crypto.spec.DHParameterSpec;

public class DHGenParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = DHGenParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()), "plain class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getPrimeSize", "getExponentSize"});
        // single public constructor (int, int)
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 2 && k.getParameterTypes()[0] == int.class
                    && k.getParameterTypes()[1] == int.class) ctors++;
        }
        check(ctors == 1 && c.getConstructors().length == 1, "single public ctor (int,int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DHGenParameterSpec s = new DHGenParameterSpec(2048, 256);
        check(s.getPrimeSize() == 2048, "getPrimeSize");
        check(s.getExponentSize() == 256, "getExponentSize");
        check(s instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");

        // values are stored verbatim, no validation in constructor
        DHGenParameterSpec s0 = new DHGenParameterSpec(0, 0);
        check(s0.getPrimeSize() == 0 && s0.getExponentSize() == 0, "zero values stored verbatim");
        DHGenParameterSpec sMin = new DHGenParameterSpec(Integer.MIN_VALUE, Integer.MAX_VALUE);
        check(sMin.getPrimeSize() == Integer.MIN_VALUE && sMin.getExponentSize() == Integer.MAX_VALUE,
                "extreme int values stored verbatim");

        // independent instances with equal inputs expose equal getters (value semantics)
        DHGenParameterSpec s2 = new DHGenParameterSpec(2048, 256);
        check(s.getPrimeSize() == s2.getPrimeSize() && s.getExponentSize() == s2.getExponentSize(),
                "two instances equal getters");
        check(new DHGenParameterSpec(1024, 128).getExponentSize() == 128, "1024/128 exponent");

        // real chain: DHGenParameterSpec drives AlgorithmParameterGenerator("DH"),
        // whose output feeds KeyPairGenerator + KeyAgreement
        AlgorithmParameterGenerator apg = AlgorithmParameterGenerator.getInstance("DH");
        apg.init(new DHGenParameterSpec(512, 128));
        AlgorithmParameters ap = apg.generateParameters();
        DHParameterSpec dps = ap.getParameterSpec(DHParameterSpec.class);
        check(dps instanceof AlgorithmParameterSpec, "generated DHParameterSpec type");
        check(dps.getP().bitLength() == 512, "generated prime is 512 bits (primeSize honored)");
        check(dps.getL() == 128, "generated private exponent length 128 (exponentSize honored)");
        check(dps.getG().signum() > 0, "generator positive");

        KeyPairGenerator kpgA = KeyPairGenerator.getInstance("DiffieHellman");
        kpgA.initialize(dps);
        KeyPair kpA = kpgA.generateKeyPair();
        KeyPairGenerator kpgB = KeyPairGenerator.getInstance("DiffieHellman");
        kpgB.initialize(dps);
        KeyPair kpB = kpgB.generateKeyPair();
        javax.crypto.interfaces.DHPublicKey pubA = (javax.crypto.interfaces.DHPublicKey) kpA.getPublic();
        check(pubA.getParams().getP().equals(dps.getP()), "key pair uses generated p");
        KeyAgreement kaA = KeyAgreement.getInstance("DiffieHellman");
        kaA.init(kpA.getPrivate());
        kaA.doPhase(kpB.getPublic(), true);
        byte[] secretA = kaA.generateSecret();
        KeyAgreement kaB = KeyAgreement.getInstance("DiffieHellman");
        kaB.init(kpB.getPrivate());
        kaB.doPhase(kpA.getPublic(), true);
        check(java.util.Arrays.equals(secretA, kaB.generateSecret()), "both sides derive identical secret");
        check(secretA.length == 64, "512-bit shared secret length 64");
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
