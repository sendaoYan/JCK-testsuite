/*
 * @test
 * @summary java.security.interfaces.DSAParams conformance: white-box structure + black-box behavior.
 * @run main DSAParamsTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class DSAParamsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAParams");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getG", "getP", "getQ"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(2048);
        DSAParams p = ((DSAKey) kpg.generateKeyPair().getPublic()).getParams();
        check(p instanceof AlgorithmParameterSpec, "is an AlgorithmParameterSpec");
        // structural sanity: q divides p-1 cannot be cheaply asserted, but sizes and ranges are fixed
        check(p.getP().bitLength() == 2048 && p.getQ().bitLength() == 224, "2048/224 parameter set");
        check(p.getP().signum() == 1 && p.getQ().signum() == 1 && p.getG().signum() == 1, "all positive");
        check(p.getG().compareTo(BigInteger.TWO) >= 0, "generator at least 2");
        // round trip through AlgorithmParameters
        AlgorithmParameters ap = AlgorithmParameters.getInstance("DSA");
        ap.init(new DSAParameterSpec(p.getP(), p.getQ(), p.getG()));
        DSAParameterSpec back = ap.getParameterSpec(DSAParameterSpec.class);
        check(back.getP().equals(p.getP()) && back.getQ().equals(p.getQ()) && back.getG().equals(p.getG()),
            "AlgorithmParameters round trip");
        // the default 2048 set is used consistently across separate generators
        DSAParams other = ((DSAKey) KeyPairGenerator.getInstance("DSA").generateKeyPair().getPublic()).getParams();
        check(other.getP().bitLength() == 2048 && other.getQ().bitLength() == 224, "same default set");
        // 1024-bit initialization uses the legacy 1024/160 set
        KeyPairGenerator legacy = KeyPairGenerator.getInstance("DSA");
        legacy.initialize(1024);
        DSAParams old = ((DSAKey) legacy.generateKeyPair().getPublic()).getParams();
        check(old.getP().bitLength() == 1024 && old.getQ().bitLength() == 160, "legacy 1024/160 set");

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
