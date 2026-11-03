/*
 * @test
 * @summary java.security.interfaces.XECPublicKey conformance: white-box structure + black-box behavior.
 * @run main XECPublicKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class XECPublicKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.XECPublicKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.interfaces.XECKey", "java.security.PublicKey"});
        checkMethods(c, new String[]{"getParams", "getU"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("XDH");
        kpg.initialize(NamedParameterSpec.X25519);
        KeyPair kp = kpg.generateKeyPair();
        XECPublicKey pub = (XECPublicKey) kp.getPublic();
        check(pub instanceof PublicKey && pub instanceof XECKey, "supertypes");
        // u in [0, 2^255)
        BigInteger u = pub.getU();
        check(u.signum() >= 0, "u non-negative");
        check(u.bitLength() <= 255, "u fits in 255 bits");
        check(pub.getParams() instanceof NamedParameterSpec, "params present");
        check(pub.getFormat().equals("X.509"), "X.509 format");
        // X448 coordinate space is larger
        KeyPairGenerator g448 = KeyPairGenerator.getInstance("XDH");
        g448.initialize(NamedParameterSpec.X448);
        XECPublicKey k448 = (XECPublicKey) g448.generateKeyPair().getPublic();
        check(k448.getU().bitLength() <= 448, "X448 u fits in 448 bits");
        // KeyFactory round trip preserves u and params
        KeyFactory kf = KeyFactory.getInstance("XDH");
        XECPublicKey back = (XECPublicKey) kf.generatePublic(new XECPublicKeySpec(pub.getParams(), u));
        check(back.getU().equals(u), "round trip u");
        check(((NamedParameterSpec) back.getParams()).getName().equals("X25519"), "round trip params");
        // key agreement with the reconstructed public key yields the same secret
        KeyAgreement ka1 = KeyAgreement.getInstance("XDH");
        ka1.init(kp.getPrivate()); ka1.doPhase(pub, true);
        byte[] s1 = ka1.generateSecret();
        KeyAgreement ka2 = KeyAgreement.getInstance("XDH");
        ka2.init(kp.getPrivate()); ka2.doPhase(back, true);
        check(java.util.Arrays.equals(s1, ka2.generateSecret()), "round-trip key agrees identically");

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
