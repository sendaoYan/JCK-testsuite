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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — record impl, lattice, primality, equals ----
    static void testContract() throws Exception {
        // record-style minimal implementation: getters pass fixed values through, null included
        DSAParams rec = new RecParams();
        check(rec.getP().equals(BigInteger.valueOf(23)), "record getP passthrough");
        check(rec.getQ().equals(BigInteger.valueOf(11)), "record getQ passthrough");
        check(rec.getG().equals(BigInteger.valueOf(2)), "record getG passthrough");
        check(new NullParams().getP() == null && new NullParams().getQ() == null
                && new NullParams().getG() == null, "record null passthrough");

        // lattice: extends AlgorithmParameterSpec (JDK-9+; JDK-8 had no superinterface),
        // not a Key, not Serializable, no constant
        check(AlgorithmParameterSpec.class.isAssignableFrom(DSAParams.class),
                "is an AlgorithmParameterSpec (JDK-9+ hierarchy)");
        check(DSAParams.class.getInterfaces().length == 1
                && DSAParams.class.getInterfaces()[0] == AlgorithmParameterSpec.class,
                "single direct superinterface AlgorithmParameterSpec");
        check(!Key.class.isAssignableFrom(DSAParams.class), "not a Key");
        check(!Serializable.class.isAssignableFrom(DSAParams.class), "not Serializable");
        try {
            DSAParams.class.getDeclaredField("serialVersionUID");
            check(false, "no serialVersionUID declared");
        } catch (NoSuchFieldException e) {
            check(true, "no serialVersionUID declared");
        }

        // primality of the domain integers and the subgroup order equation
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        DSAParams pp = ((DSAKey) kpg.generateKeyPair().getPublic()).getParams();
        check(pp.getP().isProbablePrime(64), "p is prime");
        check(pp.getQ().isProbablePrime(64), "q is prime");
        check(pp.getG().modPow(pp.getQ(), pp.getP()).equals(BigInteger.ONE),
                "g^q mod p == 1 (order of g divides q)");
        check(pp.getP().bitLength() == 1024 && pp.getQ().bitLength() == 160,
                "legacy 1024/160 set as initialized");

        // equals semantics on the record impl
        check(rec.equals(rec), "record reflexive equals");
        check(!rec.equals(pp), "record not equal to real params");
    }

    static class RecParams implements DSAParams {
        public BigInteger getP() { return BigInteger.valueOf(23); }
        public BigInteger getQ() { return BigInteger.valueOf(11); }
        public BigInteger getG() { return BigInteger.valueOf(2); }
    }

    static class NullParams implements DSAParams {
        public BigInteger getP() { return null; }
        public BigInteger getQ() { return null; }
        public BigInteger getG() { return null; }
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
