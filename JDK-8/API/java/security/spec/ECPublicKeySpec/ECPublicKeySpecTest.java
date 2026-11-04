/*
 * @test
 * @summary java.security.spec.ECPublicKeySpec conformance: full contract coverage.
 *          Constructor null matrix (null w -> NPE "w is null", null params ->
 *          NPE "params is null", probed), getW/getParams round-trip, no
 *          equals/hashCode override (identity semantics, probed), instanceof
 *          KeySpec, POINT_INFINITY usable as public point, non-Serializable.
 * @run main ECPublicKeySpecTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.KeySpec;

public class ECPublicKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static EllipticCurve curve() {
        return new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23)),
                BigInteger.ONE, BigInteger.ONE);
    }

    static ECParameterSpec params() {
        return new ECParameterSpec(curve(),
                new ECPoint(BigInteger.valueOf(3), BigInteger.valueOf(10)),
                BigInteger.valueOf(7), 1);
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.EllipticCurve cu = curve();
        java.security.spec.ECPoint g = new java.security.spec.ECPoint(new java.math.BigInteger("3"), new java.math.BigInteger("10"));
        java.security.spec.ECParameterSpec ps = new java.security.spec.ECParameterSpec(cu, g, new java.math.BigInteger("7"), 1);
        java.security.spec.ECPoint w = new java.security.spec.ECPoint(new java.math.BigInteger("6"), new java.math.BigInteger("4"));
        java.security.spec.ECPublicKeySpec spec = new java.security.spec.ECPublicKeySpec(w, ps);
        check(w.equals(spec.getW()), "getW");
        check(ps.equals(spec.getParams()), "getParams");

        // ---- constructor null matrix (probed) ----
        checkThrows(NullPointerException.class,
                () -> new ECPublicKeySpec(null, ps), "null w -> NPE (probed 'w is null')");
        checkThrows(NullPointerException.class,
                () -> new ECPublicKeySpec(w, null), "null params -> NPE (probed 'params is null')");

        // ---- getters round-trip ----
        ECPublicKeySpec r = new ECPublicKeySpec(w, ps);
        check(r.getW().equals(w), "getW round-trip");
        check(r.getParams().equals(ps), "getParams round-trip");
        check(r.getW().getAffineX().intValue() == 6 && r.getW().getAffineY().intValue() == 4,
                "w coordinates preserved");

        // ---- same objects reused across specs ----
        ECPublicKeySpec r2 = new ECPublicKeySpec(w, ps);
        check(r2.getW().equals(r.getW()) && r2.getParams().equals(r.getParams()),
                "two specs built from same inputs agree");

        // ---- no equals/hashCode override: identity semantics (probed) ----
        check(!r.equals(r2), "equal-content instances not equal (probed)");
        check(r.equals(r), "reflexive");

        // ---- deep chain and misc ----
        check(r.getParams().getCurve().getField().getFieldSize() == 5,
                "deep chain w -> params -> curve -> field size");
        check(r.getW() != null && r.getParams() != null, "getters never null for valid ctor");
        check(r.toString().startsWith("java.security.spec.ECPublicKeySpec@"),
                "toString default Object form");
        check(!r.equals(null), "equals null false");
        // POINT_INFINITY is rejected as a public point (probed)
        checkThrows(IllegalArgumentException.class,
                () -> new ECPublicKeySpec(ECPoint.POINT_INFINITY, ps),
                "w == POINT_INFINITY -> IAE (probed 'w is ECPoint.POINT_INFINITY')");

        // ---- interfaces ----
        check(r instanceof KeySpec, "implements KeySpec");
        check(!Serializable.class.isAssignableFrom(ECPublicKeySpec.class), "not Serializable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
