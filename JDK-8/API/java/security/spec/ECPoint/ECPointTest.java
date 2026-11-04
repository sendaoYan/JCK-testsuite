/*
 * @test
 * @summary java.security.spec.ECPoint conformance: full contract coverage.
 *          Constructor NPE for null x or y, POINT_INFINITY constant (null affine
 *          coordinates, singleton equality), value equals/hashCode across instances,
 *          infinity-vs-finite asymmetry, getAffineX/Y round-trip, toString,
 *          non-Serializable contract.
 * @run main ECPointTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.spec.ECPoint;

public class ECPointTest {
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

    public static void main(String[] args) throws Exception {
        java.security.spec.ECPoint p = new java.security.spec.ECPoint(new java.math.BigInteger("3"), new java.math.BigInteger("10"));
        check(new java.math.BigInteger("3").equals(p.getAffineX()), "getAffineX");
        check(new java.math.BigInteger("10").equals(p.getAffineY()), "getAffineY");
        check(java.security.spec.ECPoint.POINT_INFINITY.equals(java.security.spec.ECPoint.POINT_INFINITY), "POINT_INFINITY");

        // ---- constructor null matrix (probed: NPE "affine coordinate x or y is null") ----
        checkThrows(NullPointerException.class,
                () -> new ECPoint(null, BigInteger.ONE), "ECPoint(null, y)");
        checkThrows(NullPointerException.class,
                () -> new ECPoint(BigInteger.ONE, null), "ECPoint(x, null)");

        // ---- POINT_INFINITY contract ----
        ECPoint inf = ECPoint.POINT_INFINITY;
        check(inf.getAffineX() == null, "INFINITY getAffineX null (probed)");
        check(inf.getAffineY() == null, "INFINITY getAffineY null (probed)");
        check(inf == ECPoint.POINT_INFINITY, "INFINITY is the same instance");
        check(inf.equals(ECPoint.POINT_INFINITY), "INFINITY equals itself");
        check(inf.hashCode() == ECPoint.POINT_INFINITY.hashCode(), "INFINITY hashCode stable");

        // ---- value equals / hashCode ----
        ECPoint a = new ECPoint(BigInteger.valueOf(3), BigInteger.valueOf(10));
        ECPoint b = new ECPoint(BigInteger.valueOf(3), BigInteger.valueOf(10));
        ECPoint c = new ECPoint(BigInteger.valueOf(3), BigInteger.valueOf(11));
        ECPoint d = new ECPoint(BigInteger.valueOf(4), BigInteger.valueOf(10));
        check(a.equals(b), "equals same coordinates");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal for equal points");
        check(!a.equals(c), "different y not equal");
        check(!a.equals(d), "different x not equal");
        check(!a.equals(null), "equals null false");
        check(!a.equals("point"), "equals wrong type false");
        check(a.equals(a), "equals reflexive");
        // distinct BigInteger instances with equal values are still equal points
        check(new ECPoint(new BigInteger("3"), new BigInteger("10")).equals(
                        new ECPoint(new BigInteger(new byte[]{3}), new BigInteger(new byte[]{10}))),
                "equals across distinct BigInteger instances");

        // ---- infinity <-> finite asymmetry ----
        check(!a.equals(inf), "finite != INFINITY");
        check(!inf.equals(a), "INFINITY != finite");

        // ---- getters round-trip with constructed values ----
        check(a.getAffineX().intValue() == 3 && a.getAffineY().intValue() == 10,
                "getAffineX/Y round-trip");
        check(a.getAffineX().equals(a.getAffineX()), "getAffineX stable");

        // ---- non-Serializable, toString ----
        check(!Serializable.class.isAssignableFrom(ECPoint.class), "not Serializable");
        check(a.toString().startsWith("java.security.spec.ECPoint@"),
                "toString default Object form: " + a.toString());

        System.out.println("PASS (" + passed + " assertions)");
    }
}
