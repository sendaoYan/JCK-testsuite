/*
 * @test
 * @summary java.security.interfaces.ECPublicKey conformance: white-box structure + black-box behavior.
 * @run main ECPublicKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class ECPublicKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.ECPublicKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.PublicKey", "java.security.interfaces.ECKey"});
        checkMethods(c, new String[]{"getParams", "getW"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kp = kpg.generateKeyPair();
        ECPublicKey pub = (ECPublicKey) kp.getPublic();
        check(ECPublicKey.serialVersionUID == -3314988629879632826L, "serialVersionUID");
        check(pub instanceof PublicKey && pub instanceof ECKey, "supertypes");
        // W is an affine point strictly inside the field
        ECPoint w = pub.getW();
        java.math.BigInteger fieldPrime = ((java.security.spec.ECFieldFp) pub.getParams().getCurve().getField()).getP();
        check(w.getAffineX().signum() >= 0 && w.getAffineX().compareTo(fieldPrime) < 0, "x in [0, p)");
        check(w.getAffineY().signum() >= 0 && w.getAffineY().compareTo(fieldPrime) < 0, "y in [0, p)");
        check(pub.getFormat().equals("X.509"), "X.509 format");
        // KeyFactory round trip from W + params
        KeyFactory kf = KeyFactory.getInstance("EC");
        ECPublicKey back = (ECPublicKey) kf.generatePublic(new ECPublicKeySpec(w, pub.getParams()));
        check(back.getW().getAffineX().equals(w.getAffineX()) && back.getW().getAffineY().equals(w.getAffineY()),
            "round trip W");
        // the factory does not validate curve membership: an arbitrary point is accepted
        ECPublicKey weird = (ECPublicKey) kf.generatePublic(new ECPublicKeySpec(new ECPoint(BigInteger.ONE, BigInteger.ONE), pub.getParams()));
        check(weird != null, "off-curve point accepted by factory");
        // serialization preserves W
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(pub); }
        Object copy = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(((ECPublicKey) copy).getW().getAffineX().equals(w.getAffineX()), "serialized W preserved");

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
