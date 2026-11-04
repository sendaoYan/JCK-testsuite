/*
 * @test
 * @summary java.security.interfaces.ECKey conformance: white-box structure + black-box behavior.
 * @run main ECKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class ECKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.ECKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getParams"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kp = kpg.generateKeyPair();
        ECKey pub = (ECKey) kp.getPublic();
        ECKey priv = (ECKey) kp.getPrivate();
        check(pub != null && priv != null, "both halves implement ECKey");
        ECParameterSpec pp = pub.getParams();
        ECParameterSpec sp = priv.getParams();
        check(pp != null && sp != null, "params present");
        check(pp.equals(sp), "same named curve for both halves");
        check(pp.getCurve().getField().getFieldSize() == 256, "secp256r1 field size");
        check(pp.getOrder().bitLength() == 256, "256-bit order");
        check(pp.getCofactor() == 1, "cofactor 1");
        // generation is repeatable with the same params object
        KeyPairGenerator again = KeyPairGenerator.getInstance("EC");
        again.initialize(pp);
        ECParameterSpec pp2 = ((ECKey) again.generateKeyPair().getPublic()).getParams();
        check(pp2.getCurve().getField().getFieldSize() == 256, "params object reusable");
        // a different curve yields different params
        KeyPairGenerator p384 = KeyPairGenerator.getInstance("EC");
        p384.initialize(new ECGenParameterSpec("secp384r1"));
        check(((ECKey) p384.generateKeyPair().getPublic()).getParams().getCurve().getField().getFieldSize() == 384,
            "secp384r1 field size");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — record impl, lattice, equals semantics ----
    static void testContract() throws Exception {
        // record-style minimal implementation: getParams passes the holder value through
        ECParameterSpec spec = ((ECKey) KeyPairGenerator.getInstance("EC")
                .generateKeyPair().getPublic()).getParams();
        ECKey rec = new RecECKey(spec);
        check(rec.getParams() == spec, "record getParams identity passthrough");
        check(new RecECKey(null).getParams() == null, "record null params passthrough");

        // lattice: bare interface, not a Key, not Serializable, no constant
        check(ECKey.class.getInterfaces().length == 0, "no superinterfaces");
        check(!Key.class.isAssignableFrom(ECKey.class), "not a Key");
        check(!Serializable.class.isAssignableFrom(ECKey.class), "not Serializable");
        check(ECKey.class.isAssignableFrom(ECPublicKey.class), "ECPublicKey is an ECKey");
        check(ECKey.class.isAssignableFrom(ECPrivateKey.class), "ECPrivateKey is an ECKey");
        try {
            ECKey.class.getDeclaredField("serialVersionUID");
            check(false, "no serialVersionUID declared");
        } catch (NoSuchFieldException e) {
            check(true, "no serialVersionUID declared");
        }

        // equals semantics
        KeyPair kp = KeyPairGenerator.getInstance("EC").generateKeyPair();
        ECKey pub = (ECKey) kp.getPublic();
        check(pub.equals(pub), "reflexive equals");
        check(!pub.equals(kp.getPrivate()), "public half not equal to private half");
        check(!pub.equals(rec), "real key not equal to record impl");
        check(pub.hashCode() == pub.hashCode(), "hashCode stable");
    }

    static class RecECKey implements ECKey {
        private final ECParameterSpec params;
        RecECKey(ECParameterSpec params) { this.params = params; }
        public ECParameterSpec getParams() { return params; }
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
