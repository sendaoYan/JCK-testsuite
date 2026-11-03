/*
 * @test
 * @summary javax.crypto.spec.DHPrivateKeySpec conformance: white-box structure + black-box behavior.
 * @run main DHPrivateKeySpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.KeySpec;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPrivateKeySpec;

public class DHPrivateKeySpecTest {
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
        Class<?> c = DHPrivateKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()), "plain class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getX", "getP", "getG"});
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 3 && k.getParameterTypes()[0] == BigInteger.class
                    && k.getParameterTypes()[1] == BigInteger.class
                    && k.getParameterTypes()[2] == BigInteger.class) ctors++;
        }
        check(ctors == 1 && c.getConstructors().length == 1, "single public ctor (x,p,g)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (preserved) plain accessor behavior
        BigInteger x = BigInteger.valueOf(4);
        BigInteger p = BigInteger.valueOf(23);
        BigInteger g = BigInteger.valueOf(5);
        DHPrivateKeySpec s = new DHPrivateKeySpec(x, p, g);
        check(s.getX().equals(x) && s.getP().equals(p) && s.getG().equals(g), "accessors");
        check(s instanceof KeySpec, "instanceof KeySpec");
        check(new DHPrivateKeySpec(x, p, g).getX().equals(x), "second instance same x");
        check(new DHPrivateKeySpec(x, p, g).getG().equals(g), "second instance same g");

        // large fixed values
        BigInteger xBig = new BigInteger(
            "0B392459CD5A0CE1388AA26C0B6A1751313B79B4E2E0AFE1", 16);
        DHPrivateKeySpec big = new DHPrivateKeySpec(xBig, P768, G2);
        check(big.getX().equals(xBig), "big x exact");
        check(big.getP().equals(P768), "big p exact");
        check(big.getG().equals(G2), "big g exact");

        // real chain (reverse direction): runtime DH key -> getKeySpec(DHPrivateKeySpec.class)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
        kpg.initialize(new DHParameterSpec(P768, G2));
        KeyPair kp = kpg.generateKeyPair();
        DHPrivateKey priv = (DHPrivateKey) kp.getPrivate();
        KeyFactory kf = KeyFactory.getInstance("DH");
        DHPrivateKeySpec spec = kf.getKeySpec(priv, DHPrivateKeySpec.class);
        check(spec instanceof KeySpec, "factory-produced spec instanceof KeySpec");
        check(spec.getX().equals(priv.getX()), "spec x equals key x");
        check(spec.getP().equals(priv.getParams().getP()), "spec p equals key p");
        check(spec.getG().equals(priv.getParams().getG()), "spec g equals key g");

        // forward direction: spec -> generatePrivate -> usable private key
        PrivateKey back = kf.generatePrivate(spec);
        check(back instanceof DHPrivateKey, "generatePrivate returns DHPrivateKey");
        check(((DHPrivateKey) back).getX().equals(spec.getX()), "reconstructed x equals spec x");
        check(((DHPrivateKey) back).getParams().getP().equals(P768), "reconstructed params p");

        // the reconstructed private key drives a KeyAgreement to the same shared secret
        KeyPairGenerator kpg2 = KeyPairGenerator.getInstance("DiffieHellman");
        kpg2.initialize(new DHParameterSpec(P768, G2));
        KeyPair other = kpg2.generateKeyPair();
        KeyAgreement kaOrig = KeyAgreement.getInstance("DiffieHellman");
        kaOrig.init(priv);
        kaOrig.doPhase(other.getPublic(), true);
        byte[] secretOrig = kaOrig.generateSecret();
        KeyAgreement kaSpec = KeyAgreement.getInstance("DiffieHellman");
        kaSpec.init(back);
        kaSpec.doPhase(other.getPublic(), true);
        check(java.util.Arrays.equals(secretOrig, kaSpec.generateSecret()),
                "spec-reconstructed key produces identical shared secret");
        check(secretOrig.length == 96, "768-bit secret length 96");
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
