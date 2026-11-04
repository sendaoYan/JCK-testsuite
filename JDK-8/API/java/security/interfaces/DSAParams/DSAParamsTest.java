/*
 * @test
 * @summary java.security.interfaces.DSAParams conformance: interface hierarchy and key methods,
 *          record-style implementation passthrough (p/q/g identity + null), supertype lattice
 *          (no superinterfaces, not a Key, not Serializable), and real DSA-1024 domain
 *          parameter contracts: p 1024-bit prime, q 160-bit prime, g in (1,p) with
 *          g^q mod p == 1, shared across key halves, reusable for generation.
 * @run main DSAParamsTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAKey;
import java.security.spec.DSAParameterSpec;

public class DSAParamsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAParams");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getG"), "declares getG");
        check(ms.contains("getP"), "declares getP");
        check(ms.contains("getQ"), "declares getQ");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — record-style impl + real domain parameter math ----
    static void testContract() throws Exception {
        // record-style minimal implementation: getters pass the fixed values through
        DSAParams rec = new RecParams();
        check(rec.getP().equals(BigInteger.valueOf(23)), "record getP passthrough");
        check(rec.getQ().equals(BigInteger.valueOf(11)), "record getQ passthrough");
        check(rec.getG().equals(BigInteger.valueOf(2)), "record getG passthrough");
        check(new NullParams().getP() == null && new NullParams().getQ() == null
                && new NullParams().getG() == null, "record null passthrough");

        // supertype lattice
        check(DSAParams.class.getInterfaces().length == 0, "no superinterfaces");
        check(!Key.class.isAssignableFrom(DSAParams.class), "not a Key");
        check(!Serializable.class.isAssignableFrom(DSAParams.class), "not Serializable");
        try {
            DSAParams.class.getDeclaredField("serialVersionUID");
            check(false, "no serialVersionUID declared");
        } catch (NoSuchFieldException e) {
            check(true, "no serialVersionUID declared");
        }

        // real DSA-1024 parameters satisfy the documented domain math
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        DSAParams pp = ((DSAKey) kp.getPublic()).getParams();
        DSAParams sp = ((DSAKey) kp.getPrivate()).getParams();
        check(pp.getP().bitLength() == 1024, "p is 1024 bits");
        check(pp.getQ().bitLength() == 160, "q is 160 bits");
        check(pp.getP().signum() > 0 && pp.getQ().signum() > 0, "p, q positive");
        check(pp.getP().isProbablePrime(64), "p is prime");
        check(pp.getQ().isProbablePrime(64), "q is prime");
        check(pp.getG().signum() > 0 && pp.getG().compareTo(pp.getP()) < 0, "g in (0, p)");
        check(pp.getG().modPow(pp.getQ(), pp.getP()).equals(BigInteger.ONE),
                "g^q mod p == 1 (order of g divides q)");
        check(pp.getP().equals(sp.getP()) && pp.getQ().equals(sp.getQ())
                && pp.getG().equals(sp.getG()), "identical params on both halves");

        // params feed a DSAParameterSpec and come back unchanged
        DSAParameterSpec spec = new DSAParameterSpec(pp.getP(), pp.getQ(), pp.getG());
        check(spec.getP().equals(pp.getP()) && spec.getQ().equals(pp.getQ())
                && spec.getG().equals(pp.getG()), "DSAParameterSpec carries same values");
        KeyPairGenerator reuse = KeyPairGenerator.getInstance("DSA");
        reuse.initialize(spec);
        DSAParams again = ((DSAKey) reuse.generateKeyPair().getPublic()).getParams();
        check(again.getP().equals(pp.getP()) && again.getQ().equals(pp.getQ())
                && again.getG().equals(pp.getG()), "params survive regeneration round");

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
}
