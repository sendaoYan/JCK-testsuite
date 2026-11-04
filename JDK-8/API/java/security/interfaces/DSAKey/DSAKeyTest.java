/*
 * @test
 * @summary java.security.interfaces.DSAKey conformance: interface hierarchy and key methods,
 *          record-style implementation passthrough (getParams identity/null), supertype
 *          lattice (DSAKey is neither Key nor Serializable; both DSA key halves are DSAKeys),
 *          and real generated DSA-1024 pair closed loops: shared domain parameters across
 *          halves, params reusable for generation, equals semantics.
 * @run main DSAKeyTest
 */

import java.io.Serializable;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAParameterSpec;

public class DSAKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getParams"), "declares getParams");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — record-style impl + real-key closed loops ----
    static void testContract() throws Exception {
        // record-style minimal implementation: getParams must pass the holder value through
        DSAParams recParams = new RecParams();
        DSAKey rec = new RecDSAKey(recParams);
        check(rec.getParams() == recParams, "record getParams identity passthrough");
        check(new RecDSAKey(null).getParams() == null, "record null params passthrough");
        check(rec instanceof DSAKey, "record impl is a DSAKey");

        // supertype lattice: DSAKey is a bare interface, not a Key, not Serializable
        check(!Key.class.isAssignableFrom(DSAKey.class), "DSAKey is not a Key");
        check(!Serializable.class.isAssignableFrom(DSAKey.class), "DSAKey is not Serializable");
        check(DSAKey.class.getInterfaces().length == 0, "DSAKey has no superinterfaces");
        check(DSAKey.class.isAssignableFrom(DSAPublicKey.class), "DSAPublicKey is a DSAKey");
        check(DSAKey.class.isAssignableFrom(DSAPrivateKey.class), "DSAPrivateKey is a DSAKey");
        try {
            DSAKey.class.getDeclaredField("serialVersionUID");
            check(false, "DSAKey declares no serialVersionUID");
        } catch (NoSuchFieldException e) {
            check(true, "DSAKey declares no serialVersionUID");
        }

        // real generated pair: both halves are DSAKeys sharing domain parameters
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        DSAKey pub = (DSAKey) kp.getPublic();
        DSAKey priv = (DSAKey) kp.getPrivate();
        check(pub instanceof DSAPublicKey, "public half is a DSAPublicKey");
        check(priv instanceof DSAPrivateKey, "private half is a DSAPrivateKey");
        DSAParams pp = pub.getParams();
        DSAParams sp = priv.getParams();
        check(pp != null && sp != null, "real keys expose non-null DSAParams");
        check(pp instanceof DSAParams, "params concrete type implements DSAParams");
        check(pp.getP().equals(sp.getP()) && pp.getQ().equals(sp.getQ())
                && pp.getG().equals(sp.getG()), "both halves share domain parameters");
        check(pp.getP().bitLength() == 1024, "p is 1024 bits (as initialized)");
        check(pp.getQ().bitLength() == 160, "q is 160 bits");
        check(pub.getParams().equals(pub.getParams()), "repeated getParams equal");

        // the exposed params are reusable for another generation round
        KeyPairGenerator reuse = KeyPairGenerator.getInstance("DSA");
        reuse.initialize(new DSAParameterSpec(pp.getP(), pp.getQ(), pp.getG()));
        DSAParams again = ((DSAKey) reuse.generateKeyPair().getPublic()).getParams();
        check(again.getP().equals(pp.getP()) && again.getQ().equals(pp.getQ())
                && again.getG().equals(pp.getG()), "params reusable for generation");

        // equals semantics
        check(pub.equals(pub), "reflexive equals");
        check(!pub.equals(priv), "public half not equal to private half");
        check(!pub.equals(rec), "real key not equal to record impl");
    }

    static class RecParams implements DSAParams {
        public java.math.BigInteger getP() { return java.math.BigInteger.valueOf(23); }
        public java.math.BigInteger getQ() { return java.math.BigInteger.valueOf(11); }
        public java.math.BigInteger getG() { return java.math.BigInteger.valueOf(2); }
    }

    static class RecDSAKey implements DSAKey {
        private final DSAParams params;
        RecDSAKey(DSAParams params) { this.params = params; }
        public DSAParams getParams() { return params; }
    }
}
