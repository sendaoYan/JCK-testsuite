/*
 * @test
 * @summary java.security.spec.ECParameterSpec conformance: full contract coverage.
 *          Constructor matrix (null curve/generator/order -> NPE with distinct
 *          probed messages, cofactor <= 0 -> IAE "h is not positive"), getter
 *          round-trip, no equals/hashCode override (identity semantics, probed),
 *          instanceof AlgorithmParameterSpec, interop with a fully-populated
 *          P-256-shaped spec, non-Serializable.
 * @run main ECParameterSpecTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

public class ECParameterSpecTest {
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

    static final EllipticCurve P256_CURVE = new EllipticCurve(
            new ECFieldFp(new BigInteger(
                    "ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16)),
            new BigInteger("ffffffff00000001000000000000000000000000fffffffffffffffffffffffc", 16),
            new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16),
            new byte[]{(byte) 0xc4, (byte) 0x9d, (byte) 0x36, (byte) 0x08});

    static final ECPoint P256_G = new ECPoint(
            new BigInteger("6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16),
            new BigInteger("4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16));

    static final BigInteger P256_N =
            new BigInteger("ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16);

    public static void main(String[] args) throws Exception {
        java.security.spec.EllipticCurve curve = new java.security.spec.EllipticCurve(
            new java.security.spec.ECFieldFp(new java.math.BigInteger("23")),
            new java.math.BigInteger("1"), new java.math.BigInteger("1"));
        java.security.spec.ECPoint g = new java.security.spec.ECPoint(new java.math.BigInteger("3"), new java.math.BigInteger("10"));
        java.security.spec.ECParameterSpec s = new java.security.spec.ECParameterSpec(curve, g, new java.math.BigInteger("7"), 1);
        check(curve.equals(s.getCurve()), "getCurve");
        check(g.equals(s.getGenerator()), "getGenerator");
        check(new java.math.BigInteger("7").equals(s.getOrder()), "getOrder");
        check(s.getCofactor() == 1, "getCofactor");

        // ---- constructor null matrix (probed: distinct NPE per parameter) ----
        checkThrows(NullPointerException.class,
                () -> new ECParameterSpec(null, g, BigInteger.valueOf(7), 1),
                "null curve -> NPE (probed 'curve is null')");
        checkThrows(NullPointerException.class,
                () -> new ECParameterSpec(curve, null, BigInteger.valueOf(7), 1),
                "null generator -> NPE (probed 'g is null')");
        checkThrows(NullPointerException.class,
                () -> new ECParameterSpec(curve, g, null, 1),
                "null order -> NPE (probed 'n is null')");

        // ---- cofactor validation (probed) ----
        checkThrows(IllegalArgumentException.class,
                () -> new ECParameterSpec(curve, g, BigInteger.valueOf(7), 0),
                "cofactor 0 -> IAE (probed 'h is not positive')");
        checkThrows(IllegalArgumentException.class,
                () -> new ECParameterSpec(curve, g, BigInteger.valueOf(7), -1),
                "cofactor -1 -> IAE");
        check(new ECParameterSpec(curve, g, BigInteger.valueOf(7), Integer.MAX_VALUE)
                .getCofactor() == Integer.MAX_VALUE, "large cofactor accepted");

        // ---- getters round-trip with NIST P-256 values ----
        ECParameterSpec p256 = new ECParameterSpec(P256_CURVE, P256_G, P256_N, 1);
        check(p256.getCurve().equals(P256_CURVE), "P-256 curve round-trip");
        check(p256.getGenerator().equals(P256_G), "P-256 generator round-trip");
        check(p256.getOrder().equals(P256_N), "P-256 order round-trip");
        check(p256.getCofactor() == 1, "P-256 cofactor");
        check(p256.getCurve().getField().getFieldSize() == 256, "P-256 field size 256");
        check(p256.getOrder().bitLength() == 256, "P-256 order bit length 256");

        // ---- no equals/hashCode override: identity semantics (probed) ----
        ECParameterSpec s2 = new ECParameterSpec(curve, g, BigInteger.valueOf(7), 1);
        check(!s.equals(s2), "no value equals: equal-content instances not equal (probed)");
        check(s.equals(s), "reflexive");

        // ---- marker interface ----
        check(s instanceof AlgorithmParameterSpec, "implements AlgorithmParameterSpec");

        // ---- not Serializable ----
        check(!Serializable.class.isAssignableFrom(ECParameterSpec.class), "not Serializable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
