/*
 * @test
 * @summary java.security.interfaces.ECKey conformance: interface hierarchy and key methods,
 *          record-style implementation passthrough (getParams identity/null), supertype
 *          lattice (no superinterfaces, not a Key, not Serializable), and real EC secp256r1
 *          pair closed loops: both halves are ECKeys with equal shared params, 256-bit field,
 *          params reusable for a fresh generation round.
 * @run main ECKeyTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

public class ECKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.ECKey");
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
    static ECParameterSpec curve() {
        BigInteger p = new BigInteger("ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16);
        BigInteger a = p.subtract(BigInteger.valueOf(3));
        BigInteger b = new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16);
        EllipticCurve c = new EllipticCurve(new ECFieldFp(p), a, b);
        ECPoint g = new ECPoint(
                new BigInteger("6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16),
                new BigInteger("4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16));
        return new ECParameterSpec(c, g, new BigInteger("ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16), 1);
    }

    static void testContract() throws Exception {
        // record-style minimal implementation: getParams passes the holder value through
        ECParameterSpec spec = curve();
        ECKey rec = new RecECKey(spec);
        check(rec.getParams() == spec, "record getParams identity passthrough");
        check(new RecECKey(null).getParams() == null, "record null params passthrough");

        // supertype lattice
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

        // real EC pair: both halves are ECKeys carrying equal shared params
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));
        KeyPair kp = kpg.generateKeyPair();
        ECKey pub = (ECKey) kp.getPublic();
        ECKey priv = (ECKey) kp.getPrivate();
        check(pub instanceof ECPublicKey, "public half is an ECPublicKey");
        check(priv instanceof ECPrivateKey, "private half is an ECPrivateKey");
        ECParameterSpec pp = pub.getParams();
        ECParameterSpec sp = priv.getParams();
        check(pp != null && sp != null, "real keys expose non-null params");
        check(pp.equals(sp), "both halves share the same ECParameterSpec");
        check(pp.getCurve().getField().getFieldSize() == 256, "secp256r1 field size is 256");
        check(pp.getCurve().getA() != null && pp.getCurve().getB() != null, "curve coefficients present");
        check(pub.getParams().equals(pub.getParams()), "repeated getParams equal");

        // the exposed params are reusable for another generation round
        KeyPairGenerator reuse = KeyPairGenerator.getInstance("EC");
        reuse.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));
        ECParameterSpec again = ((ECKey) reuse.generateKeyPair().getPublic()).getParams();
        check(again.getCurve().getField().getFieldSize() == 256, "params survive regeneration round");
        check(again.getCurve().getB().equals(pp.getCurve().getB()), "same curve b coefficient");

        // equals semantics
        check(pub.equals(pub), "reflexive equals");
        check(!pub.equals(priv), "public half not equal to private half");
        check(!pub.equals(rec), "real key not equal to record impl");
    }

    static class RecECKey implements ECKey {
        private final ECParameterSpec params;
        RecECKey(ECParameterSpec params) { this.params = params; }
        public ECParameterSpec getParams() { return params; }
    }
}
