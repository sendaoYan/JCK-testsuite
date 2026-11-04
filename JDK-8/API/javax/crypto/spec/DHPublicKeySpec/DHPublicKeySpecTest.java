/*
 * @test
 * @summary javax.crypto.spec.DHPublicKeySpec conformance: white-box structure + black-box behavior.
 * @run main DHPublicKeySpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

public class DHPublicKeySpecTest {
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
        Class<?> c = DHPublicKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()), "plain class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getY", "getP", "getG"});
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 3 && k.getParameterTypes()[0] == BigInteger.class
                    && k.getParameterTypes()[1] == BigInteger.class
                    && k.getParameterTypes()[2] == BigInteger.class) ctors++;
        }
        check(ctors == 1 && c.getConstructors().length == 1, "single public ctor (y,p,g)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (preserved) plain accessor behavior
        BigInteger y = BigInteger.valueOf(4);
        BigInteger p = BigInteger.valueOf(23);
        BigInteger g = BigInteger.valueOf(5);
        DHPublicKeySpec s = new DHPublicKeySpec(y, p, g);
        check(s.getY().equals(y) && s.getP().equals(p) && s.getG().equals(g), "accessors");
        check(s instanceof KeySpec, "instanceof KeySpec");
        check(new DHPublicKeySpec(y, p, g).getY().equals(y), "second instance same y");
        check(new DHPublicKeySpec(y, p, g).getP().equals(p), "second instance same p");

        // large fixed values
        BigInteger yBig = new BigInteger(
            "3AA1BCE743C2577F9E2B1EBC2C8C9F7D3A4F6E8D2B0C4A6E8D0F2B4C6A8E0D2F4", 16);
        DHPublicKeySpec big = new DHPublicKeySpec(yBig, P768, G2);
        check(big.getY().equals(yBig), "big y exact");
        check(big.getP().equals(P768), "big p exact");
        check(big.getG().equals(G2), "big g exact");

        // real chain (reverse direction): runtime DH key -> getKeySpec(DHPublicKeySpec.class)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
        kpg.initialize(new DHParameterSpec(P768, G2));
        KeyPair kp = kpg.generateKeyPair();
        DHPublicKey pub = (DHPublicKey) kp.getPublic();
        KeyFactory kf = KeyFactory.getInstance("DH");
        DHPublicKeySpec spec = kf.getKeySpec(pub, DHPublicKeySpec.class);
        check(spec instanceof KeySpec, "factory-produced spec instanceof KeySpec");
        check(spec.getY().equals(pub.getY()), "spec y equals key y");
        check(spec.getP().equals(pub.getParams().getP()), "spec p equals key p");
        check(spec.getG().equals(pub.getParams().getG()), "spec g equals key g");

        // forward direction: spec -> generatePublic -> usable public key
        PublicKey back = kf.generatePublic(spec);
        check(back instanceof DHPublicKey, "generatePublic returns DHPublicKey");
        check(((DHPublicKey) back).getY().equals(spec.getY()), "reconstructed y equals spec y");
        check(((DHPublicKey) back).getParams().getP().equals(P768), "reconstructed params p");

        // the reconstructed public key participates in a working agreement
        KeyPairGenerator kpg2 = KeyPairGenerator.getInstance("DiffieHellman");
        kpg2.initialize(new DHParameterSpec(P768, G2));
        KeyPair peer = kpg2.generateKeyPair();
        KeyAgreement kaWithSpec = KeyAgreement.getInstance("DiffieHellman");
        kaWithSpec.init(peer.getPrivate());
        kaWithSpec.doPhase(back, true);
        byte[] secretA = kaWithSpec.generateSecret();
        KeyAgreement kaOther = KeyAgreement.getInstance("DiffieHellman");
        kaOther.init(kp.getPrivate());
        kaOther.doPhase(peer.getPublic(), true);
        byte[] secretB = kaOther.generateSecret();
        check(java.util.Arrays.equals(secretA, secretB),
                "spec-reconstructed public key yields symmetric shared secret");
        check(secretA.length == 96, "768-bit secret length 96");
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
