/*
 * @test
 * @summary java.security.interfaces.DSAPrivateKey conformance: white-box structure + black-box behavior.
 * @run main DSAPrivateKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class DSAPrivateKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAPrivateKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.interfaces.DSAKey", "java.security.PrivateKey"});
        checkMethods(c, new String[]{"getParams", "getX"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        DSAPrivateKey priv = (DSAPrivateKey) kp.getPrivate();
        check(DSAPrivateKey.serialVersionUID == 7776497482533790279L, "serialVersionUID");
        check(priv instanceof PrivateKey && priv instanceof DSAKey, "supertypes");
        // x in [1, q)
        BigInteger x = priv.getX();
        DSAParams pp = priv.getParams();
        check(x.signum() > 0 && x.compareTo(pp.getQ()) < 0, "x in [1, q)");
        check(priv.getAlgorithm().equals("DSA"), "algorithm");
        check(priv.getFormat().equals("PKCS#8"), "PKCS#8 format");
        check(priv.getEncoded() != null && priv.getEncoded().length > 0, "encoded present");
        // sign with the interface fields, verify against the public half
        Signature sig = Signature.getInstance("SHA256withDSA");
        sig.initSign(priv);
        byte[] msg = "jck-dsa".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(kp.getPublic());
        sig.update(msg);
        check(sig.verify(s), "signature verifies");
        // KeyFactory round trip from x, p, q, g
        KeyFactory kf = KeyFactory.getInstance("DSA");
        DSAPrivateKey back = (DSAPrivateKey) kf.generatePrivate(
            new DSAPrivateKeySpec(x, pp.getP(), pp.getQ(), pp.getG()));
        check(back.getX().equals(x), "round trip x");
        check(back.getParams().getP().equals(pp.getP()), "round trip params");
        // serialization keeps x and params
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(priv); }
        Object copy = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(((DSAPrivateKey) copy).getX().equals(x), "serialized x preserved");

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
