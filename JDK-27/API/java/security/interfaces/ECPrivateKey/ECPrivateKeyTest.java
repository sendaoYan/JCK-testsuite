/*
 * @test
 * @summary java.security.interfaces.ECPrivateKey conformance: white-box structure + black-box behavior.
 * @run main ECPrivateKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class ECPrivateKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.ECPrivateKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.PrivateKey", "java.security.interfaces.ECKey"});
        checkMethods(c, new String[]{"getParams", "getS"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kp = kpg.generateKeyPair();
        ECPrivateKey priv = (ECPrivateKey) kp.getPrivate();
        check(ECPrivateKey.serialVersionUID == -7896394956925609184L, "serialVersionUID");
        check(priv instanceof PrivateKey && priv instanceof ECKey, "supertypes");
        // s in [1, n)
        BigInteger s = priv.getS();
        ECParameterSpec pp = priv.getParams();
        check(s.signum() > 0 && s.compareTo(pp.getOrder()) < 0, "s in [1, n)");
        check(priv.getAlgorithm().equals("EC"), "algorithm");
        check(priv.getFormat().equals("PKCS#8"), "PKCS#8 format");
        // sign with SHA256withECDSA and verify with the public half
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initSign(priv);
        byte[] msg = "jck-ec".getBytes("UTF-8");
        sig.update(msg);
        byte[] s1 = sig.sign();
        sig.initVerify(kp.getPublic());
        sig.update(msg);
        check(sig.verify(s1), "signature verifies");
        // KeyFactory round trip from s + params
        KeyFactory kf = KeyFactory.getInstance("EC");
        ECPrivateKey back = (ECPrivateKey) kf.generatePrivate(new ECPrivateKeySpec(s, pp));
        check(back.getS().equals(s), "round trip s");
        check(back.getParams().getOrder().equals(pp.getOrder()), "round trip order");
        // serialization preserves s
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(priv); }
        Object copy = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(((ECPrivateKey) copy).getS().equals(s), "serialized s preserved");

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
