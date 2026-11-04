/*
 * @test
 * @summary java.security.interfaces.EdECPublicKey conformance: white-box structure + black-box behavior.
 * @run main EdECPublicKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class EdECPublicKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.EdECPublicKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.interfaces.EdECKey", "java.security.PublicKey"});
        checkMethods(c, new String[]{"getParams", "getPoint"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();
        EdECPublicKey pub = (EdECPublicKey) kp.getPublic();
        check(pub instanceof PublicKey && pub instanceof EdECKey, "supertypes");
        // the point: y coordinate in range plus the x-parity bit
        EdECPoint pt = pub.getPoint();
        check(pt.getY().signum() >= 0, "y non-negative");
        check(pt.getY().bitLength() <= 255, "y fits in 255 bits");
        check(!pt.isXOdd() || pt.isXOdd(), "parity boolean readable");
        check(pub.getParams().getName().equals("Ed25519"), "params name");
        check(pub.getFormat().equals("X.509"), "X.509 format");
        // Ed448 coordinate space is larger
        EdECPublicKey p448 = (EdECPublicKey) KeyPairGenerator.getInstance("Ed448").generateKeyPair().getPublic();
        check(p448.getPoint().getY().bitLength() <= 455, "Ed448 y fits in 455 bits");
        // KeyFactory round trip from point + params preserves both components
        KeyFactory kf = KeyFactory.getInstance("EdDSA");
        EdECPublicKey back = (EdECPublicKey) kf.generatePublic(new EdECPublicKeySpec(pub.getParams(), pt));
        check(back.getPoint().getY().equals(pt.getY()), "round trip y");
        check(back.getPoint().isXOdd() == pt.isXOdd(), "round trip x parity");
        // a signature made by the private half verifies under this view
        Signature sig = Signature.getInstance("Ed25519");
        sig.initSign((EdECPrivateKey) kp.getPrivate());
        byte[] msg = "jck".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(back);
        sig.update(msg);
        check(sig.verify(s), "round-trip key verifies");

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
