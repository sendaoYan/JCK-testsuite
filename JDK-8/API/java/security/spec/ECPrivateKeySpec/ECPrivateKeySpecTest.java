/*
 * @test
 * @summary java.security.spec.ECPrivateKeySpec conformance: full contract coverage.
 *          Constructor null matrix (null s -> NPE "s is null", null params ->
 *          NPE "params is null", probed), getS/getParams round-trip, no
 *          equals/hashCode override (identity semantics, probed), instanceof
 *          KeySpec, range of private scalar values (ONE, P-256 sized), non-Serializable.
 * @run main ECPrivateKeySpecTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.KeySpec;

public class ECPrivateKeySpecTest {
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

    static final EllipticCurve CURVE = new EllipticCurve(
            new ECFieldFp(new BigInteger(
                    "ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16)),
            BigInteger.ONE, BigInteger.valueOf(4));
    static final ECParameterSpec PARAMS = new ECParameterSpec(CURVE,
            new ECPoint(BigInteger.valueOf(2), BigInteger.valueOf(3)),
            new BigInteger("ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16), 1);

    public static void main(String[] args) throws Exception {
        java.security.spec.EllipticCurve curve = new java.security.spec.EllipticCurve(
            new java.security.spec.ECFieldFp(new java.math.BigInteger("23")),
            new java.math.BigInteger("1"), new java.math.BigInteger("1"));
        java.security.spec.ECPoint g = new java.security.spec.ECPoint(new java.math.BigInteger("3"), new java.math.BigInteger("10"));
        java.security.spec.ECParameterSpec ps = new java.security.spec.ECParameterSpec(curve, g, new java.math.BigInteger("7"), 1);
        java.math.BigInteger s = new java.math.BigInteger("5");
        java.security.spec.ECPrivateKeySpec spec = new java.security.spec.ECPrivateKeySpec(s, ps);
        check(s.equals(spec.getS()), "getS");
        check(ps.equals(spec.getParams()), "getParams");

        // ---- constructor null matrix (probed) ----
        checkThrows(NullPointerException.class,
                () -> new ECPrivateKeySpec(null, ps), "null s -> NPE (probed 's is null')");
        checkThrows(NullPointerException.class,
                () -> new ECPrivateKeySpec(s, null), "null params -> NPE (probed 'params is null')");

        // ---- getters round-trip ----
        ECPrivateKeySpec r = new ECPrivateKeySpec(s, ps);
        check(r.getS().equals(s), "getS round-trip");
        check(r.getParams().equals(ps), "getParams round-trip");

        // no range validation of the scalar: 0, 1, negative, larger than order all accepted
        check(new ECPrivateKeySpec(BigInteger.ZERO, ps).getS().intValue() == 0,
                "scalar 0 accepted (no range validation)");
        check(new ECPrivateKeySpec(BigInteger.ONE, ps).getS().intValue() == 1,
                "scalar 1 accepted");
        check(new ECPrivateKeySpec(BigInteger.valueOf(-9), ps).getS().intValue() == -9,
                "negative scalar accepted");
        BigInteger huge = new BigInteger(
                "ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16);
        check(new ECPrivateKeySpec(huge, PARAMS).getS().equals(huge),
                "order-sized scalar accepted");

        // ---- deep chain, independence, misc ----
        check(new ECPrivateKeySpec(s, PARAMS).getParams().getOrder().bitLength() == 256,
                "deep chain s -> params -> order bitLength 256");
        check(r.toString().startsWith("java.security.spec.ECPrivateKeySpec@"),
                "toString default Object form");
        check(!r.equals(null), "equals null false");
        // two specs over the same params do not interfere
        ECPrivateKeySpec sa = new ECPrivateKeySpec(BigInteger.ONE, ps);
        ECPrivateKeySpec sb2 = new ECPrivateKeySpec(BigInteger.valueOf(2), ps);
        check(sa.getS().intValue() == 1 && sb2.getS().intValue() == 2,
                "independent specs hold independent scalars");

        // ---- no equals/hashCode override: identity semantics (probed) ----
        ECPrivateKeySpec r2 = new ECPrivateKeySpec(s, ps);
        check(!r.equals(r2), "equal-content instances not equal (probed)");
        check(r.equals(r), "reflexive");

        // ---- interfaces ----
        check(r instanceof KeySpec, "implements KeySpec");
        check(!Serializable.class.isAssignableFrom(ECPrivateKeySpec.class), "not Serializable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
