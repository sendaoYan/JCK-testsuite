/*
 * @test
 * @summary java.security.interfaces.DSAKey conformance: white-box structure + black-box behavior.
 * @run main DSAKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class DSAKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getParams"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Both halves of a real generated DSA pair implement DSAKey
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        DSAKey pub = (DSAKey) kp.getPublic();
        DSAKey priv = (DSAKey) kp.getPrivate();
        check(pub != null && priv != null, "both halves implement DSAKey");
        DSAParams pp = pub.getParams();
        DSAParams sp = priv.getParams();
        check(pp != null && sp != null, "params present");
        // parameter set: 2048-bit p, 224-bit q, generator in range
        check(pp.getP().bitLength() == 2048, "p is 2048 bits");
        check(pp.getQ().bitLength() == 224, "q is 224 bits");
        check(pp.getG().signum() > 0 && pp.getG().compareTo(pp.getP()) < 0, "g in [1, p)");
        // public and private halves carry the same domain parameters
        check(pp.getP().equals(sp.getP()) && pp.getQ().equals(sp.getQ()) && pp.getG().equals(sp.getG()),
            "shared domain parameters");
        // params are themselves exposed for reuse in key generation
        KeyPairGenerator reuse = KeyPairGenerator.getInstance("DSA");
        reuse.initialize(new DSAParameterSpec(pp.getP(), pp.getQ(), pp.getG()));
        KeyPair kp2 = reuse.generateKeyPair();
        DSAParams again = ((DSAKey) kp2.getPublic()).getParams();
        check(again.getP().equals(pp.getP()) && again.getQ().equals(pp.getQ()) && again.getG().equals(pp.getG()),
            "params reusable for generation");

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
